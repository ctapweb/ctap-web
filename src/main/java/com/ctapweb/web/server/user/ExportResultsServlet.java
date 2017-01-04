package com.ctapweb.web.server.user;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ctapweb.feature.logging.LogMarker;
import com.ctapweb.web.server.DBConnectionManager;
import com.ctapweb.web.server.analysis.AnalysisUtils;
import com.ctapweb.web.shared.AnalysisStatus;
import com.ctapweb.web.shared.Tag;
import com.ctapweb.web.shared.exception.AccessToResourceDeniedException;
import com.ctapweb.web.shared.exception.DatabaseException;
import com.ctapweb.web.shared.exception.UserNotLoggedInException;

public class ExportResultsServlet extends HttpServlet {

	private AnalysisGeneratorServiceImpl analysisGeneratorServiceImpl = 
			new AnalysisGeneratorServiceImpl();

	Connection dbConnection = DBConnectionManager.getDbConnection();
	private static final Logger logger = LogManager.getLogger();
	long analysisID;
	ServletOutputStream respStream;
	String tableType = "wide"; //default table type 

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse resp) 
			throws ServletException, IOException {
		String serviceName = "exportResultsServlet";

		//long or wide table
		if(!request.getParameter("tableType").isEmpty()) {
			tableType = request.getParameter("tableType"); 
		}

		analysisID = Long.parseLong(request.getParameter("analysisID"));

		try {
			long userID = ServiceUtils.logServiceStartAndGetUserID(request, logger, serviceName);

			//check if user owner of analysis
			if(!AnalysisUtils.isUserAnalysisOwner(userID, analysisID)) {
				throw logger.throwing(new ServletException(new AccessToResourceDeniedException()));
			}

			//check if analysis is finished
			AnalysisStatus analysisStatus = AnalysisUtils.getAnalysisStatus(analysisID);
			if(!analysisStatus.getStatus().equals(AnalysisStatus.Status.FINISHED) ||
					analysisStatus.getProgress() != 1.0 ) {
				throw logger.throwing(new ServletException("Analysis " + 
						analysisStatus.getAnalysisID() + " hasn't finished."));
			}

			//all check pass
			byte[] buffer = new byte[1024];

			resp.setContentType( "application/octet-stream" );
			resp.setHeader( "Content-Disposition:", "attachment;filename=" + "\"" 
					+ "Results_" + analysisID +".csv" + "\"" );
			respStream = resp.getOutputStream();
			//		resp.setContentLength((int)outputFile.toFile().length());
			resp.setBufferSize(buffer.length);

			if("long".equals(tableType)) {
				getLongTable();
			} else if ("wide".equals(tableType)) {
				getWideTable();
			}

		}catch (DatabaseException | UserNotLoggedInException | SQLException e) {
			throw logger.throwing(new ServletException(e));
		}

	}

	//gets the results as a long table
	private void getLongTable() throws SQLException, IOException {
		String queryStr = ""
				+ "SELECT text.id, text.title, result.feature_id, analysis_engine.name, result.value " 
				+ "FROM result, text, analysis_engine " 
				+ "WHERE result.text_id=text.id " 
				+ "     AND result.feature_id=analysis_engine.id "
				+ "     AND result.analysis_id=?";
		PreparedStatement ps;
		ps = dbConnection.prepareStatement(queryStr);
		ps.setLong(1, analysisID);
		ResultSet rs = ps.executeQuery();

		respStream.println("Text_id\tTags\tText_Title\tFeature_id\tFeature_Name\tValue");
		while(rs.next()) {
			long textID = rs.getLong("id");

			//get all tags assigned to the text
			String tagSetStr = "";
			for(Tag tag: CorpusManagerServiceUtils.getTextTags(textID)) {
				tagSetStr += "[" + tag.getName() + "] ";
			}

			respStream.print(textID + "\t");
			respStream.print(tagSetStr + "\t");
			respStream.print(rs.getString("title") + "\t");
			respStream.print(rs.getLong("feature_id") + "\t");
			respStream.print(rs.getString("name") + "\t");
			respStream.print(rs.getDouble("value") + "\n");
		} 
	}

	//gets the results as a wide table
	private void getWideTable() throws SQLException, IOException {
		//get feature names
		List<String> featureNames = new ArrayList<>();

		String queryStr = "SELECT DISTINCT result.feature_id, ae.name "
				+ "FROM result, analysis_engine as ae "
				+ "WHERE result.analysis_id=? AND result.feature_id=ae.id ORDER BY 1;";
		PreparedStatement ps;
		ps = dbConnection.prepareStatement(queryStr);
		ps.setLong(1, analysisID);
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
			featureNames.add(rs.getString("name"));
		}

		//query to get the cross table (wide format)
		queryStr = ""
				+ "SELECT * FROM crosstab("
				+ "		'SELECT result.text_id, text.title, result.feature_id, result.value  "
				+ "		 FROM result, text "
				+ "		 WHERE result.analysis_id=" + analysisID + " AND result.text_id=text.id "
				+ "		 ORDER BY 1;', " //source sql

				+ "		'SELECT DISTINCT result.feature_id "
				+ "		 FROM result "
				+ "		 WHERE result.analysis_id=" + analysisID 
				+ "		 ORDER BY 1;'" //category sql
				+ "		) AS ct(text_id BIGINT, text_title text, "; //give column names
		//				+ "feature1 NUMERIC, feature2 NUMERIC, feature3 NUMERIC, feature4 NUMERIC, feature5 NUMERIC, feature6 NUMERIC, feature7 NUMERIC, feature8 NUMERIC, feature9 NUMERIC, feature10 NUMERIC); ";
		for(int i = 0; i < featureNames.size(); i++) {
			queryStr += "feature" + (i+1) + " NUMERIC,";
		}
		queryStr = queryStr.substring(0, queryStr.length() - 1); //get rid of the last comman
		queryStr += ")";

		ps = dbConnection.prepareStatement(queryStr);
		rs = ps.executeQuery();

		//header line
		String header = "Text_id\tTags\tText_title\t";
		for(String featureName: featureNames) {
			header += featureName + "\t";
		}
		header = header.substring(0, header.length() - 1);
		respStream.println(header);

		while(rs.next()) {
			long textID = rs.getLong("text_id");

			//get all tags assigned to the text
			String tagSetStr = "";
			for(Tag tag: CorpusManagerServiceUtils.getTextTags(textID)) {
				tagSetStr += "[" + tag.getName() + "] ";
			}

			respStream.print(textID + "\t");
			respStream.print(tagSetStr + "\t");
			respStream.print(rs.getString("text_title") + "\t");

			//			//get value columns
			String featureValuesStr = "";
			for(int i = 0; i < featureNames.size(); i++) {
				featureValuesStr += rs.getDouble(i + 3) + "\t";
			}
			respStream.print(
					featureValuesStr.substring(0, featureValuesStr.length() - 1) + "\n");
		}
	}
}

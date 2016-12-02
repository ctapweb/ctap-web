package com.ctapweb.web.server.user;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse resp) 
			throws ServletException, IOException {
		String serviceName = "exportResultsServlet";
		long analysisID = Long.parseLong(request.getParameter("analysisID"));

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
			ServletOutputStream respStream = resp.getOutputStream();
			//		resp.setContentLength((int)outputFile.toFile().length());
			resp.setBufferSize(buffer.length);

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
		}catch (DatabaseException | UserNotLoggedInException | SQLException e) {
			throw logger.throwing(new ServletException(e));
		}

	}
}

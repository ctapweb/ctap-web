package com.ctapweb.web.server.user;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ctapweb.web.server.DBConnectionManager;
import com.ctapweb.web.server.logging.LogMarker;
import com.ctapweb.web.server.logging.ServiceRequestCompletedMessage;
import com.ctapweb.web.server.logging.ServiceRequestReceivedMessage;
import com.ctapweb.web.server.logging.VerifyingUserCookiesMessage;
import com.ctapweb.web.shared.CorpusText;
import com.ctapweb.web.shared.Tag;
import com.ctapweb.web.shared.UserInfo;
import com.ctapweb.web.shared.exception.DatabaseException;
import com.ctapweb.web.shared.exception.UserNotLoggedInException;

public class ExportCorpusServlet extends HttpServlet {

	private HttpServletRequest request;
	private UserServiceImpl userServiceImpl = new UserServiceImpl();
	Connection dbConnection = DBConnectionManager.getDbConnection();

	private static final Logger logger = LogManager.getLogger();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
			throws ServletException, IOException {
		this.request = req;
		long corpusID = Long.parseLong(req.getParameter("corpusID"));
		boolean inOneFile = Boolean.parseBoolean(req.getParameter("inOneFile"));
		String serviceName = "exportCorpusServlet";
		List<CorpusText> textList;

		//Obtain corpus texts
		try {
			long userID = logServiceStartAndGetUserID(serviceName);
			textList = getTextList(userID, corpusID);

		} catch (Exception e) {
			throw logger.throwing(new ServletException(e.getMessage()));
		}

		//create a tmp directory

		//		Path tmpPath = Paths.get("tmp/");
		Path tmpPath = Paths.get(getServletContext().getRealPath("/") + "/tmp/");
		Path tmpDir = Files.createTempDirectory(tmpPath, "exportedCorpus"+corpusID);
		Path outputFile = null;
		byte[] buffer = new byte[1024];

		if(inOneFile) {
			//write to one file
			outputFile = tmpDir.resolve("Exported_corpus_" + corpusID+ ".txt");
			PrintWriter printWriter = new PrintWriter(outputFile.toFile());
			for(CorpusText text: textList) {
				printWriter.println(text.getTitle());
				printWriter.println("Tags: " + text.getTagString());
				printWriter.write(text.getContent());

				//new lines to separate texts
				printWriter.println();
				printWriter.println();
			}
			printWriter.flush();

			//close print writer
			if(printWriter != null) {
				printWriter.close();
			}
		} else {
			//write to multiple files
			outputFile = tmpDir.resolve("Exported_corpus_" + corpusID + ".zip");
			ZipOutputStream zipOutputStream = new ZipOutputStream(
					new FileOutputStream(outputFile.toFile()));

			for(CorpusText text: textList) {
				//package the file
				ZipEntry zipEntry = new ZipEntry(text.getId() + "_" + text.getTitle());
				zipOutputStream.putNextEntry(zipEntry);

				InputStream ins = new ByteArrayInputStream(
						(text.getTagString() + "\n" +
						text.getContent()).
						getBytes(StandardCharsets.UTF_8));
				int len;
				while((len=ins.read(buffer)) > 0) {
					zipOutputStream.write(buffer, 0, len);
				}

				zipOutputStream.closeEntry();
			}
			zipOutputStream.close();
		}

		if(outputFile != null) {
			FileInputStream fin = new FileInputStream(outputFile.toFile());

			resp.setContentType( "application/octet-stream" );
			resp.setHeader( "Content-Disposition:", "attachment;filename=" + "\"" 
					+ outputFile.getFileName() + "\"" );
			ServletOutputStream respStream = resp.getOutputStream();
			resp.setContentLength((int)outputFile.toFile().length());
			resp.setBufferSize(buffer.length);

			int len;
			while((len = fin.read(buffer)) > 0) {
				respStream.write(buffer, 0, len);
			}

			//close stream
			if(fin != null) {
				fin.close();
			}
			//delete the tmp file and folder
			Files.delete(outputFile);
			Files.delete(tmpDir);
		}

		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
				new ServiceRequestCompletedMessage(serviceName));
	}

	private long logServiceStartAndGetUserID(String serviceName) 
			throws DatabaseException, UserNotLoggedInException {
		long userID = 0;
		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
				new ServiceRequestReceivedMessage(serviceName));

		//verify user cookies and get user id
		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
				new VerifyingUserCookiesMessage(serviceName));
		UserInfo userInfo = userServiceImpl.verifyUserCookies(request);
		if(userInfo != null) {
			userID = userInfo.getId();
		} else {
			throw logger.throwing(new UserNotLoggedInException());
		}
		return userID;
	}

	private List<CorpusText> getTextList(long userID, long corpusID) 
			throws DatabaseException {
		List<CorpusText> textList = new ArrayList<CorpusText>();

		//get text list from database
		String queryStr = ""
				+ "SELECT text.id, text.title, text.content, text.create_timestamp "
				+ "FROM text "
				+ "JOIN corpus ON (corpus.id = text.corpus_id) "
				+ "WHERE corpus_id=? "
				+ "     AND corpus.owner_id=? "
				+ "ORDER BY text.id DESC ";
		try {
			PreparedStatement ps = dbConnection.prepareStatement(queryStr);
			ps.setLong(1, corpusID);
			ps.setLong(2, userID);

			ResultSet rs = ps.executeQuery();

			// get infomation of all text
			while(rs.next()) {
				CorpusText text = new CorpusText();
				text.setId(rs.getLong("id"));
				text.setCorpusID(corpusID);
				text.setTitle(rs.getString("title"));
				text.setTagSet(getTextTags(text.getId()));
				text.setContent(rs.getString("content"));
				text.setCreateDate(rs.getDate("create_timestamp"));

				textList.add(text);
			}

		} catch (SQLException e) {
			throw logger.throwing(new DatabaseException(e.getMessage()));
		}

		return textList;
	}

	private Set<Tag> getTextTags(long textID) throws SQLException {
		Set<Tag> tags = new HashSet<>();

		//get tags for the text
		String queryStr = "SELECT tag.id, tag.name "
				+ "FROM tag, ta_te "
				+ "WHERE ta_te.ta_id = tag.id"
				+ "     AND ta_te.te_id =?";
		PreparedStatement ps = dbConnection.prepareStatement(queryStr);
		ps.setLong(1, textID);
		ResultSet tagRS = ps.executeQuery();
		while(tagRS.next()) {
			Tag tag = new Tag();
			tag.setId(tagRS.getLong("id"));
			tag.setName(tagRS.getString("name"));
			tags.add(tag);
		}

		return tags;
	}
}

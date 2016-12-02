package com.ctapweb.web.server.user;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
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

public class ImportCorpusServlet extends HttpServlet implements Servlet {

	private HttpServletRequest request;
	private long FILE_SIZE_LIMIT = 100 * 1024 * 1024; // 100 MiB
	private static final Logger logger = LogManager.getLogger();
	private long corpusID = 0;
	private UserServiceImpl userServiceImpl = new UserServiceImpl();
	Connection dbConnection = DBConnectionManager.getDbConnection();

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
			throws ServletException, IOException {
		String serviceName = "importCorpusServlet";
		this.request = req;

		try {
			long userID = logServiceStartAndGetUserID(serviceName);

			DiskFileItemFactory fileItemFactory = new DiskFileItemFactory();
			ServletFileUpload fileUpload = new ServletFileUpload(fileItemFactory);
			fileUpload.setSizeMax(FILE_SIZE_LIMIT);

			List<FileItem> items = fileUpload.parseRequest(req);

			//for storing selected tags
			Set<Tag> tags = new HashSet<>();

			//get the form fields first
			for (FileItem item : items) {
				if (item.isFormField()) {
					if(item.getFieldName().equals("corpusID")) {
						corpusID = Long.parseLong(item.getString());
					}
					if(item.getFieldName().startsWith("tag")) {
						Tag tag = new Tag();
						tag.setId(Long.parseLong(item.getString()));
						tags.add(tag);
					}
				}
			}

			if(corpusID == 0 || userID==0) {
				logger.warn("No user cookie or corpus ID detected or user not logged in. Possible intrusion!");
				resp.sendError(HttpServletResponse.SC_FORBIDDEN,
						"User not logged in or invalid corpus ID provided.");
				return;
			}

			//get the file and save it in corpus db
			for (FileItem item : items) {
				if (!item.isFormField()) {
					//check size
					if (item.getSize() > FILE_SIZE_LIMIT) {
						resp.sendError(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE,
								"File size exceeds limit.");
						return;
					}

					//add text file to db
					logger.info("Adding uploaded file to database: " + item.getName());
//					logger.info("The file type is: " + item.getContentType());
					CorpusText corpusText = new CorpusText();
					corpusText.setCorpusID(corpusID);
					corpusText.setTitle(item.getName());
					corpusText.setContent(item.getString());
					corpusText.setTagSet(tags);
					
					writeToDatabase(corpusText);

					if (!item.isInMemory()) {
						item.delete();
					}
				}

			}
		} catch (Exception e) {
			throw logger.throwing(new ServletException(e.getMessage()));
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
	
	private void writeToDatabase(CorpusText corpusText) throws DatabaseException {
		String insertStr = ""
				+ "INSERT INTO text (corpus_id, title, content, create_timestamp) "
				+ "VALUES (?, ?, ?, CURRENT_TIMESTAMP) RETURNING id";
		try {
			PreparedStatement ps = dbConnection.prepareStatement(insertStr);
			ps.setLong(1, corpusText.getCorpusID());
			ps.setString(2, corpusText.getTitle());
			ps.setString(3, corpusText.getContent());
			ResultSet rs = ps.executeQuery();

			//get the last inserted id
			long insertedID = 0;
			if(rs.next()) {
				insertedID = rs.getLong("id");
			}

			//add tags to text
			for(Tag tag : corpusText.getTagSet()) {
				insertStr = ""
						+ "INSERT INTO ta_te (ta_id, te_id) "
						+ "VALUES (?, ?)";
				ps = dbConnection.prepareStatement(insertStr);
				ps.setLong(1, tag.getId());
				ps.setLong(2, insertedID);
				ps.executeUpdate();
			}

		} catch (SQLException e) {
			throw logger.throwing(new DatabaseException(e.getMessage()));
		}
	}

}













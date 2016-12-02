package com.ctapweb.web.server.user;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ctapweb.web.client.service.CorpusManagerService;
import com.ctapweb.web.server.DBConnectionManager;
import com.ctapweb.web.server.logging.LogMarker;
import com.ctapweb.web.server.logging.ServiceRequestCompletedMessage;
import com.ctapweb.web.server.logging.ServiceRequestReceivedMessage;
import com.ctapweb.web.server.logging.VerifyingUserCookiesMessage;
import com.ctapweb.web.shared.Corpus;
import com.ctapweb.web.shared.CorpusFolder;
import com.ctapweb.web.shared.CorpusText;
import com.ctapweb.web.shared.Tag;
import com.ctapweb.web.shared.UserInfo;
import com.ctapweb.web.shared.exception.AccessToResourceDeniedException;
import com.ctapweb.web.shared.exception.DatabaseException;
import com.ctapweb.web.shared.exception.EmptyInfoException;
import com.ctapweb.web.shared.exception.ResourceAlreadyExistsException;
import com.ctapweb.web.shared.exception.UserNotLoggedInException;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class CorpusManagerServiceImpl extends RemoteServiceServlet implements CorpusManagerService {

	private static final Logger logger = LogManager.getLogger();
	Connection dbConnection = DBConnectionManager.getDbConnection();
	UserServiceImpl userServiceImpl = new UserServiceImpl();

	@Override
	public List<Corpus> getCorpusList(int offset, int limit) 
			throws UserNotLoggedInException, DatabaseException {
		String serviceName = "getCorpusList";
		long userID = logServiceStartAndGetUserID(serviceName); 

		ArrayList<Corpus> corpusList = new ArrayList<Corpus>(limit); 

		//get data from database
		String queryStr = ""
				+ "SELECT c.id, c.owner_id, c.folder_id, f.name AS folder_name, c.name, c.description, c.create_timestamp "
				+ "FROM corpus AS c "
				+ "LEFT JOIN corpus_folder AS f ON c.folder_id=f.id "
				+ "WHERE c.owner_id=? "
				+ "ORDER BY c.id DESC "
				+ "LIMIT ? OFFSET ?";
		PreparedStatement ps;
		try {
			ps = dbConnection.prepareStatement(queryStr);
			ps.setLong(1, userID);
			ps.setLong(2, limit);
			ps.setLong(3, offset);

			ResultSet rs = ps.executeQuery();

			// get infomation of all corpus
			while(rs.next()) {
				Corpus corpus = new Corpus();
				corpus.setId(rs.getLong("id"));
				corpus.setOwnerID(rs.getLong("owner_id"));
				corpus.setFolderID(rs.getLong("folder_id"));
				corpus.setFolderName(rs.getString("folder_name"));
				corpus.setName(rs.getString("name"));
				corpus.setDescription(rs.getString("description"));
				corpus.setCreateDate(rs.getDate("create_timestamp"));

				corpusList.add(corpus);
			}

		} catch (SQLException e) {
			throw logger.throwing(new DatabaseException(e.getMessage()));
		}

		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
				new ServiceRequestCompletedMessage(serviceName));
		return corpusList;
	}

	@Override
	public Integer getCorpusCount() 
			throws UserNotLoggedInException, DatabaseException {
		String serviceName = "getCorpusCount";
		long userID = logServiceStartAndGetUserID(serviceName); 

		int corpusCount = 0;
		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
				"Querying database for corpus count...");
		String queryStr = "SELECT COUNT(id) FROM corpus "
				+ "WHERE owner_id=? ";
		try {
			PreparedStatement ps = dbConnection.prepareStatement(queryStr);
			ps.setLong(1, userID);

			ResultSet rs =ps.executeQuery(); 

			if(rs.next()) {
				corpusCount = rs.getInt("count");
			} 

			logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
					"Found {} corpora for user {}.", corpusCount, userID);

		} catch (SQLException e) {
			throw new DatabaseException(e.getMessage());
		}

		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
				new ServiceRequestCompletedMessage(serviceName));
		return corpusCount;
	}

	@Override
	public Void addCorpus(Corpus corpus)
			throws UserNotLoggedInException, EmptyInfoException, DatabaseException {
		String serviceName = "addCorpus";
		long userID = logServiceStartAndGetUserID(serviceName); 

		long folderID = corpus.getFolderID();

		//check if corpus info is empty
		if(corpus.getName().isEmpty()) {
			throw new EmptyInfoException();
		}

		String insertStr = "INSERT INTO corpus (owner_id, folder_id, name, description, create_timestamp) "
				+ "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";
		try {
			PreparedStatement ps = dbConnection.prepareStatement(insertStr);
			ps.setLong(1, userID);

			if(folderID == 0) {
				ps.setNull(2, Types.BIGINT);
			} else {
				ps.setLong(2, folderID);
			}

			ps.setString(3, corpus.getName());
			ps.setString(4, corpus.getDescription());

			ps.executeUpdate();

		} catch (SQLException e) {
			throw logger.throwing(new DatabaseException(e.getMessage())); 
		}

		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
				new ServiceRequestCompletedMessage(serviceName));
		return null;
	}

	@Override
	public Void deleteCorpus(Corpus corpus)
			throws UserNotLoggedInException, AccessToResourceDeniedException, DatabaseException {
		String serviceName = "deleteCorpus";
		long userID = logServiceStartAndGetUserID(serviceName); 

		//check if user owner of the corpus
		if(!isUserCorpusOwner(userID, corpus.getId())) {
			throw logger.throwing(new AccessToResourceDeniedException());
		}

		//check passed, delete corpus
		String queryStr = "DELETE FROM corpus "
				+ "WHERE id=? AND owner_id=?";
		try {
			PreparedStatement ps = dbConnection.prepareStatement(queryStr);
			ps.setLong(1, corpus.getId());
			ps.setLong(2, userID);
			ps.executeUpdate();
		} catch (SQLException e) {
			throw logger.throwing(new DatabaseException(e.getMessage()));
		}

		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
				new ServiceRequestCompletedMessage(serviceName));
		return null;
	}

	@Override
	public Void updateCorpus(Corpus corpus)
			throws EmptyInfoException, UserNotLoggedInException,  
			AccessToResourceDeniedException, DatabaseException {
		String serviceName = "updateCorpus";
		long userID = logServiceStartAndGetUserID(serviceName); 

		long corpusID = corpus.getId();
		String corpusName = corpus.getName();
		long folderID = corpus.getFolderID();
		String corpusDesc = corpus.getDescription();

		//check if info is empty
		if(corpusID == 0 || corpus.getName().isEmpty()) {
			throw logger.throwing(new EmptyInfoException());
		}

		//check if user owner of the corpus and folder
		if(!isUserCorpusOwner(userID, corpusID) ||
				!isUserFolderOwner(userID, folderID)) {
			throw logger.throwing(new AccessToResourceDeniedException());
		}

		//check passed, update corpus
		String queryStr = "UPDATE corpus SET "
				+ "name=?, description=?, folder_id=? "
				+ "WHERE id = ? AND owner_id = ? ";
		try {
			PreparedStatement ps = dbConnection.prepareStatement(queryStr);
			ps.setString(1, corpusName);
			ps.setString(2, corpusDesc);

			if(folderID == 0) {
				ps.setNull(3, Types.BIGINT);
			} else {
				ps.setLong(3, folderID);
			}

			ps.setLong(4, corpusID);
			ps.setLong(5, userID);
			ps.executeUpdate();
		} catch (SQLException e) {
			throw logger.throwing(new DatabaseException(e.getMessage()));
		}

		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
				new ServiceRequestCompletedMessage(serviceName));
		return null;
	}

	@Override
	public Integer getTextCount(long corpusID)
			throws UserNotLoggedInException, AccessToResourceDeniedException, DatabaseException {
		String serviceName = "getTextCount";
		long userID = logServiceStartAndGetUserID(serviceName); 

		int textCount = 0;

		//check if user owner of the corpus
		if(!isUserCorpusOwner(userID, corpusID)) {
			throw new AccessToResourceDeniedException();
		}

		//check passed, get text count
		String queryStr = "SELECT COUNT(text.id) FROM text " +
				"JOIN corpus ON (text.corpus_id = corpus.id) " +
				"WHERE text.corpus_id=? " + 
				"AND corpus.owner_id=? ";

		try {
			PreparedStatement ps = dbConnection.prepareStatement(queryStr);
			ps.setLong(1, corpusID);
			ps.setLong(2, userID);
			ResultSet rs = ps.executeQuery();

			if(rs.next()) {
				textCount = rs.getInt("count");
			}

		} catch (SQLException e) {
			throw logger.throwing(new DatabaseException(e.getMessage()));
		}

		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
				new ServiceRequestCompletedMessage(serviceName));
		return textCount;
	}

	@Override
	public List<CorpusText> getTextList(long corpusID, int offset, int limit)
			throws UserNotLoggedInException, AccessToResourceDeniedException, DatabaseException {
		String serviceName = "getTextList";
		long userID = logServiceStartAndGetUserID(serviceName); 

		ArrayList<CorpusText> textList = new ArrayList<CorpusText>(limit); 

		//check if user owner of the corpus
		if(!isUserCorpusOwner(userID, corpusID)) {
			throw logger.throwing(new AccessToResourceDeniedException());
		}

		//check passed, get data from database
		String queryStr = ""
				+ "SELECT text.id, text.title, text.content, text.create_timestamp "
				+ "FROM text "
				+ "JOIN corpus ON (corpus.id = text.corpus_id) "
				+ "WHERE corpus_id=? "
				+ "     AND corpus.owner_id=? "
				+ "ORDER BY text.id DESC "
				+ "LIMIT ? OFFSET ?";
		try {
			PreparedStatement ps = dbConnection.prepareStatement(queryStr);
			ps.setLong(1, corpusID);
			ps.setLong(2, userID);
			ps.setInt(3, limit);
			ps.setInt(4, offset);

			ResultSet rs = ps.executeQuery();

			// get infomation of all text
			while(rs.next()) {
				CorpusText text = new CorpusText();
				text.setId(rs.getLong("id"));
				text.setCorpusID(corpusID);
				text.setTitle(rs.getString("title"));
				text.setTagSet(CorpusManagerServiceUtils.getTextTags(text.getId()));
				text.setContent(rs.getString("content"));
				text.setCreateDate(rs.getDate("create_timestamp"));

				textList.add(text);
			}

		} catch (SQLException e) {
			throw logger.throwing(new DatabaseException(e.getMessage()));
		}

		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
				new ServiceRequestCompletedMessage(serviceName));
		return textList;
	}

//	private Set<Tag> getTextTags(long textID) throws SQLException {
//		Set<Tag> tags = new HashSet<>();
//
//		//get tags for the text
//		String queryStr = "SELECT tag.id, tag.name "
//				+ "FROM tag, ta_te "
//				+ "WHERE ta_te.ta_id = tag.id"
//				+ "     AND ta_te.te_id =?";
//		PreparedStatement ps = dbConnection.prepareStatement(queryStr);
//		ps.setLong(1, textID);
//		ResultSet tagRS = ps.executeQuery();
//		while(tagRS.next()) {
//			Tag tag = new Tag();
//			tag.setId(tagRS.getLong("id"));
//			tag.setName(tagRS.getString("name"));
//			tags.add(tag);
//		}
//
//		return tags;
//	}

	@Override
	public Void addTextToCorpus(CorpusText corpusText)
			throws UserNotLoggedInException, AccessToResourceDeniedException,
			EmptyInfoException, DatabaseException {
		String serviceName = "addTextToCorpus";
		long userID = logServiceStartAndGetUserID(serviceName); 

		//check if text info is empty
		if(corpusText.getTitle().isEmpty()) {
			throw logger.throwing(new EmptyInfoException());
		}

		//check if user owner of the corpus
		if(!isUserCorpusOwner(userID, corpusText.getCorpusID())) {
			throw logger.throwing(new AccessToResourceDeniedException());
		}

		//check if text tags owned by user
		for(Tag tag: corpusText.getTagSet()) {
			if(!isUserTagOwner(userID, tag.getId())) {
				throw logger.throwing(new AccessToResourceDeniedException());
			}
		}

		//check passed
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

		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
				new ServiceRequestCompletedMessage(serviceName));
		return null;
	}

	@Override
	public Void updateText(CorpusText corpusText)
			throws UserNotLoggedInException, EmptyInfoException, 
			AccessToResourceDeniedException, DatabaseException {
		String serviceName = "updateText";
		long userID = logServiceStartAndGetUserID(serviceName); 

		//check if info is empty
		if(corpusText.getId() == 0 || corpusText.getTitle().isEmpty()) {
			throw logger.throwing(new EmptyInfoException());
		}

		//check if user owner of the corpus
		if(!isUserCorpusOwner(userID, corpusText.getCorpusID())) {
			throw logger.throwing(new AccessToResourceDeniedException());
		}

		//check if text tags owned by user
		for(Tag tag: corpusText.getTagSet()) {
			if(!isUserTagOwner(userID, tag.getId())) {
				throw logger.throwing(new AccessToResourceDeniedException());
			}
		}

		//check passed
		String updateStr = ""
				+ "UPDATE text SET title=?, content=? "
				+ "WHERE id=?";
		long textID = corpusText.getId();
		try {
			PreparedStatement ps = dbConnection.prepareStatement(updateStr);
			ps.setString(1, corpusText.getTitle());
			ps.setString(2, corpusText.getContent());
			ps.setLong(3, textID);

			ps.executeUpdate();

			//clear original tags for text
			String deleteStr = ""
					+ "DELETE FROM ta_te "
					+ "WHERE te_id = ?";
			ps = dbConnection.prepareStatement(deleteStr);
			ps.setLong(1, textID); 
			ps.executeUpdate();

			//add tags to text
			String insertStr;
			for(Tag tag : corpusText.getTagSet()) {
				insertStr = ""
						+ "INSERT INTO ta_te (ta_id, te_id) "
						+ "VALUES (?, ?)";
				ps = dbConnection.prepareStatement(insertStr);
				ps.setLong(1, tag.getId());
				ps.setLong(2, textID);
				ps.executeUpdate();
			}

		} catch (SQLException e) {
			throw logger.throwing(new DatabaseException(e.getMessage()));
		}

		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
				new ServiceRequestCompletedMessage(serviceName));
		return null;
	}

	@Override
	public Void deleteText(CorpusText corpusText)
			throws UserNotLoggedInException, AccessToResourceDeniedException, DatabaseException {
		String serviceName = "deleteText";
		long userID = logServiceStartAndGetUserID(serviceName); 

		//check if user owner of the corpus
		if(!isUserCorpusOwner(userID, corpusText.getCorpusID())) {
			throw new AccessToResourceDeniedException();
		}	

		PreparedStatement ps;
		ResultSet rs;
		String queryStr;

		try {

			//check passed, delete corpus text
			queryStr = "DELETE FROM text "
					+ "WHERE id=?";
			ps = dbConnection.prepareStatement(queryStr);
			ps.setLong(1, corpusText.getId());
			ps.executeUpdate();
		} catch (SQLException e) {
			logger.warn("Error occured while deleting text... " + e.getMessage());
			throw new DatabaseException(e.getMessage());
		}

		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
				new ServiceRequestCompletedMessage(serviceName));
		return null;
	}

	@Override
	public String getCorpusName(long corpusID)
			throws UserNotLoggedInException, AccessToResourceDeniedException, DatabaseException {
		String serviceName = "getCorpusName";
		long userID = logServiceStartAndGetUserID(serviceName); 

		String corpusName = null;

		//check if user owner of the corpus
		if(!isUserCorpusOwner(userID, corpusID)) {
			throw new AccessToResourceDeniedException();
		}

		logger.info("Getting corpus name for corpus id " + corpusID + "...");
		//get data from database
		String queryStr = ""
				+ "SELECT name, owner_id "
				+ "FROM corpus "
				+ "WHERE id=? AND owner_id=? ";
		PreparedStatement ps;
		try {
			ps = dbConnection.prepareStatement(queryStr);
			ps.setLong(1, corpusID);
			ps.setLong(2, userID);

			ResultSet rs = ps.executeQuery();

			if(rs.next()) {
				corpusName = rs.getString("name");
			} else {
				throw new AccessToResourceDeniedException();
			}

		} catch (SQLException e) {
			logger.warn("Database error: " + e.getMessage());
			throw new DatabaseException(e.getMessage());
		}

		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
				new ServiceRequestCompletedMessage(serviceName));
		return corpusName;
	}

	public boolean isUserCorpusOwner(long userID, long corpusID) 
			throws DatabaseException {
		boolean isUserOwner = false;

		//check if the logged in user is the same as the feature set owner
		String queryStr =  "SELECT id FROM corpus "
				+ "WHERE id=? AND owner_id=?";
		PreparedStatement ps;
		try {
			ps = dbConnection.prepareStatement(queryStr);
			ps.setLong(1, corpusID);
			ps.setLong(2, userID);
			ResultSet rs = ps.executeQuery();
			if(rs.isBeforeFirst()) {
				isUserOwner = true;
			} else {
				logger.warn("User " + userID + " trying to access corpus " 
						+ corpusID+", which is not owned by the user. Operation forbidden.");
			}
		} catch (SQLException e) {
			throw logger.throwing(new DatabaseException(e.getMessage()));
		}

		return isUserOwner;
	}

	private boolean isUserFolderOwner(long userID, long folderID) 
			throws DatabaseException {
		boolean isUserOwner = false;

		//check if the logged in user is the same as the folder owner
		String queryStr =  "SELECT id FROM corpus_folder "
				+ "WHERE id=? AND owner_id=?";
		PreparedStatement ps;
		try {
			ps = dbConnection.prepareStatement(queryStr);
			ps.setLong(1, folderID);
			ps.setLong(2, userID);
			ResultSet rs = ps.executeQuery();
			if(rs.isBeforeFirst()) {
				isUserOwner = true;
			} else {
				logger.warn("User " + userID + " trying to access corpus " 
						+ folderID+", which is not owned by the user. Operation forbidden.");
			}
		} catch (SQLException e) {
			throw logger.throwing(new DatabaseException(e.getMessage()));
		}

		return isUserOwner;
	}

	private boolean isUserTagOwner(long userID, long tagID) 
			throws DatabaseException {
		boolean isUserOwner = false;

		//check if the logged in user is the same as the tag owner
		String queryStr =  "SELECT id FROM tag "
				+ "WHERE id=? AND owner_id=?";
		PreparedStatement ps;
		try {
			ps = dbConnection.prepareStatement(queryStr);
			ps.setLong(1, tagID);
			ps.setLong(2, userID);
			ResultSet rs = ps.executeQuery();
			if(rs.isBeforeFirst()) {
				isUserOwner = true;
			} else {
				logger.warn("User " + userID + " trying to access tag " 
						+ tagID+", which is not owned by the user. Operation forbidden.");
			}
		} catch (SQLException e) {
			throw new DatabaseException(e.getMessage());
		}

		return isUserOwner;
	}

	//check if folder name already exists
	private boolean isFolderNameExists(long userID, String folderName) 
			throws DatabaseException {
		boolean isExist  = false;

		String queryStr = "SELECT id FROM corpus_folder "
				+ "WHERE owner_id=? "
				+ "     AND name=?";
		PreparedStatement ps;
		try {
			ps = dbConnection.prepareStatement(queryStr);
			ps.setLong(1, userID);
			ps.setString(2, folderName);
			ResultSet rs = ps.executeQuery();
			if(rs.isBeforeFirst()) {
				//folder name already exists
				isExist = true;
			}
		} catch (SQLException e) {
			throw logger.throwing(new DatabaseException(e.getMessage()));
		}

		return isExist;
	}

	//check if tag name already exists
	private boolean isTagNameExists(long userID, String tagName) 
			throws DatabaseException {
		boolean isExist  = false;

		String queryStr = "SELECT id FROM tag "
				+ "WHERE owner_id=? "
				+ "     AND name=?";
		PreparedStatement ps;
		try {
			ps = dbConnection.prepareStatement(queryStr);
			ps.setLong(1, userID);
			ps.setString(2, tagName);
			ResultSet rs = ps.executeQuery();
			if(rs.isBeforeFirst()) {
				//folder name already exists
				isExist = true;
			}
		} catch (SQLException e) {
			throw logger.throwing(new DatabaseException(e.getMessage()));
		}

		return isExist;
	}
	@Override
	public Void addCorpusFolder(String folderName)
			throws UserNotLoggedInException, EmptyInfoException, 
			ResourceAlreadyExistsException, DatabaseException {
		String serviceName = "addCorpusFolder";
		long userID = logServiceStartAndGetUserID(serviceName); 

		//check if folder name is empty
		if(folderName.isEmpty()) {
			throw logger.throwing(new EmptyInfoException());
		}

		//check if folder name exists
		if(isFolderNameExists(userID, folderName)) {
			throw logger.throwing(new ResourceAlreadyExistsException());
		}

		try {
			String insertStr = "INSERT INTO corpus_folder (owner_id, name, create_timestamp) "
					+ "VALUES (?, ?, CURRENT_TIMESTAMP)";
			PreparedStatement ps= dbConnection.prepareStatement(insertStr);
			ps.setLong(1, userID);
			ps.setString(2, folderName);

			ps.executeUpdate();

		} catch (SQLException e) {
			throw logger.throwing(new DatabaseException(e.getMessage()));
		}

		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
				new ServiceRequestCompletedMessage(serviceName));
		return null;
	}

	private long logServiceStartAndGetUserID(String serviceName) 
			throws DatabaseException, UserNotLoggedInException {
		long userID = 0;
		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
				new ServiceRequestReceivedMessage(serviceName));

		//verify user cookies and get user id
		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
				new VerifyingUserCookiesMessage(serviceName));
		UserInfo userInfo = userServiceImpl.verifyUserCookies(getThreadLocalRequest());
		if(userInfo != null) {
			userID = userInfo.getId();
		} else {
			throw logger.throwing(new UserNotLoggedInException());
		}
		return userID;
	}

	@Override
	public Integer getCorpusFolderCount() 
			throws UserNotLoggedInException, DatabaseException {
		String serviceName = "getCorpusFolderCount";
		long userID = logServiceStartAndGetUserID(serviceName); 

		int corpusFolderCount = 0;

		logger.info("Getting corpus folder count for user (id) " + userID + "...");
		String queryStr = "SELECT COUNT(id) FROM corpus_folder "
				+ "WHERE owner_id=? ";
		try {
			PreparedStatement ps = dbConnection.prepareStatement(queryStr);
			ps.setLong(1, userID);

			ResultSet rs =ps.executeQuery(); 

			if(rs.next()) {
				corpusFolderCount = rs.getInt("count");

				logger.info("Found " + corpusFolderCount + " corpus folders for user (id) " + userID + ".");
			} else {
				logger.info("DB returns not result.");
			}


		} catch (SQLException e) {
			throw new DatabaseException(e.getMessage());
		}

		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
				new ServiceRequestCompletedMessage(serviceName));
		return corpusFolderCount;
	}

	@Override
	public List<CorpusFolder> getCorpusFolderList(int offset, int limit)
			throws UserNotLoggedInException, DatabaseException {
		String serviceName = "getCorpusFolderList";
		long userID = logServiceStartAndGetUserID(serviceName); 

		ArrayList<CorpusFolder> corpusFolderList = new ArrayList<CorpusFolder>(limit); 
		String queryStr = ""
				+ "SELECT f.id, f.owner_id, f.name,COUNT(c.id) AS num_corpora "
				+ "FROM corpus_folder AS f "
				+ "LEFT JOIN corpus AS c on f.id=c.folder_id "
				+ "WHERE f.owner_id=? "
				+ "GROUP BY f.id "
				+ "ORDER BY f.id DESC "
				+ "LIMIT ? OFFSET ?";
		PreparedStatement ps;
		try {
			ps = dbConnection.prepareStatement(queryStr);
			ps.setLong(1, userID);
			ps.setLong(2, limit);
			ps.setLong(3, offset);

			ResultSet rs = ps.executeQuery();

			// get infomation of all corpus folders
			while(rs.next()) {
				CorpusFolder corpusFolder = new CorpusFolder();
				corpusFolder.setId(rs.getLong("id"));
				corpusFolder.setOwnerID(rs.getLong("owner_id"));
				corpusFolder.setName(rs.getString("name"));
				corpusFolder.setNumCorpora(rs.getInt("num_corpora"));

				corpusFolderList.add(corpusFolder);
			}


		} catch (SQLException e) {
			throw logger.throwing(new DatabaseException(e.getMessage()));
		}

		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
				new ServiceRequestCompletedMessage(serviceName));
		return corpusFolderList;
	}

	@Override
	public Void deleteCorpusFolder(CorpusFolder corpusFolder)
			throws UserNotLoggedInException, AccessToResourceDeniedException, DatabaseException {
		String serviceName = "deleteCorpusFolder";
		long userID = logServiceStartAndGetUserID(serviceName); 

		//check if user owner of the folder
		if(!isUserFolderOwner(userID, corpusFolder.getId())) {
			throw logger.throwing(new AccessToResourceDeniedException());
		}


		//check passed, delete corpus
		String queryStr = "DELETE FROM corpus_folder "
				+ "WHERE id=? AND owner_id=?";
		try {
			PreparedStatement ps = dbConnection.prepareStatement(queryStr);
			ps.setLong(1, corpusFolder.getId());
			ps.setLong(2, userID);
			ps.executeUpdate();
		} catch (SQLException e) {
			throw logger.throwing(new DatabaseException(e.getMessage()));
		}

		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
				new ServiceRequestCompletedMessage(serviceName));
		return null;
	}

	@Override
	public Integer getCorpusCount(long folderID)
			throws UserNotLoggedInException, DatabaseException {
		String serviceName = "getCorpusCount in Folder";
		long userID = logServiceStartAndGetUserID(serviceName); 

		int corpusCount = 0;

		logger.info("Getting corpus count in folder " + folderID + " for user (id) " + userID + "...");
		String queryStr = "SELECT COUNT(id) FROM corpus "
				+ "WHERE owner_id=? "
				+ "     AND folder_id=?";
		try {
			PreparedStatement ps = dbConnection.prepareStatement(queryStr);
			ps.setLong(1, userID);
			ps.setLong(2, folderID);

			ResultSet rs =ps.executeQuery(); 

			if(rs.next()) {
				corpusCount = rs.getInt("count");
			} 

		} catch (SQLException e) {
			throw logger.throwing(new DatabaseException(e.getMessage()));
		}

		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
				new ServiceRequestCompletedMessage(serviceName));
		return corpusCount;
	}

	@Override
	public List<Corpus> getCorpusList(long folderID, int offset, int limit)
			throws UserNotLoggedInException, DatabaseException {
		String serviceName = "getCorpusList";
		long userID = logServiceStartAndGetUserID(serviceName); 

		ArrayList<Corpus> corpusList = new ArrayList<Corpus>(limit); 

		//get data from database
		//		String queryStr = ""
		//				+ "SELECT id, owner_id, folder_id, name, description, create_timestamp "
		//				+ "FROM corpus "
		//				+ "WHERE owner_id=? AND folder_id=?"
		//				+ "ORDER BY id DESC "
		//				+ "LIMIT ? OFFSET ?";
		String queryStr = ""
				+ "SELECT c.id, c.owner_id, c.folder_id, f.name AS folder_name, c.name, c.description, c.create_timestamp "
				+ "FROM corpus AS c "
				+ "LEFT JOIN corpus_folder AS f ON c.folder_id=f.id "
				+ "WHERE c.owner_id=? AND c.folder_id=? "
				+ "ORDER BY c.id DESC "
				+ "LIMIT ? OFFSET ?";
		PreparedStatement ps;
		try {
			ps = dbConnection.prepareStatement(queryStr);
			ps.setLong(1, userID);
			ps.setLong(2, folderID);
			ps.setLong(3, limit);
			ps.setLong(4, offset);

			ResultSet rs = ps.executeQuery();

			// get infomation of all corpus
			while(rs.next()) {
				Corpus corpus = new Corpus();
				corpus.setId(rs.getLong("id"));
				corpus.setOwnerID(rs.getLong("owner_id"));
				corpus.setFolderID(rs.getLong("folder_id"));
				corpus.setFolderName(rs.getString("folder_name"));
				corpus.setName(rs.getString("name"));
				corpus.setDescription(rs.getString("description"));
				corpus.setCreateDate(rs.getDate("create_timestamp"));

				corpusList.add(corpus);
			}


		} catch (SQLException e) {
			throw logger.throwing(new DatabaseException(e.getMessage()));
		}

		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
				new ServiceRequestCompletedMessage(serviceName));
		return corpusList;
	}

	@Override
	public Void updateCorpusFolder(CorpusFolder corpusFolder)
			throws EmptyInfoException, UserNotLoggedInException, 
			AccessToResourceDeniedException, ResourceAlreadyExistsException, 
			DatabaseException {
		String serviceName = "updateCorpusFolder";
		long userID = logServiceStartAndGetUserID(serviceName); 

		String folderName = corpusFolder.getName();
		long folderID = corpusFolder.getId();

		//check if info is empty
		if(folderID == 0 || folderName.isEmpty()) {
			throw logger.throwing(new EmptyInfoException());
		}

		//check if user owner of the corpus folder
		if(!isUserFolderOwner(userID, folderID)) {
			throw logger.throwing(new AccessToResourceDeniedException());
		}

		if(isFolderNameExists(userID, folderName)) {
			throw logger.throwing(new ResourceAlreadyExistsException());
		}

		String queryStr = "UPDATE corpus_folder SET "
				+ "name=? "
				+ "WHERE id = ? AND owner_id = ? ";
		try {
			//check passed, update corpus
			PreparedStatement ps = dbConnection.prepareStatement(queryStr);
			ps.setString(1, folderName); 
			ps.setLong(2, folderID);
			ps.setLong(3, userID);
			ps.executeUpdate();
		} catch (SQLException e) {
			throw logger.throwing(new DatabaseException(e.getMessage()));
		}

		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
				new ServiceRequestCompletedMessage(serviceName));
		return null;
	}

	@Override
	public Integer getTagCount() 
			throws UserNotLoggedInException, DatabaseException {
		String serviceName = "getTagCount";
		long userID = logServiceStartAndGetUserID(serviceName); 

		int tagCount = 0;

		String queryStr = "SELECT COUNT(id) FROM tag "
				+ "WHERE owner_id=? ";
		try {
			PreparedStatement ps = dbConnection.prepareStatement(queryStr);
			ps.setLong(1, userID);

			ResultSet rs =ps.executeQuery(); 

			if(rs.next()) {
				tagCount = rs.getInt("count");
			}
		} catch (SQLException e) {
			throw logger.throwing(new DatabaseException(e.getMessage()));
		}

		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
				new ServiceRequestCompletedMessage(serviceName));
		return tagCount;
	}

	/**
	 * Gets all the tags belong to user. For each tag, also count the number of texts tagged with it.
	 * @throws AccessToResourceDeniedException 
	 */
	@Override
	public List<Tag> getTagList(long corpusID, int offset, int limit)
			throws UserNotLoggedInException, DatabaseException, AccessToResourceDeniedException {
		String serviceName = "getTagList";
		long userID = logServiceStartAndGetUserID(serviceName); 

		List<Tag> tagList = new ArrayList<>(limit); 

		//get all tags that belong to the user from database
		String queryStr = ""
				+ "SELECT id, owner_id, name FROM tag "
				+ "WHERE owner_id=? "
				+ "LIMIT ? OFFSET ?";
		PreparedStatement ps;
		try {
			ps = dbConnection.prepareStatement(queryStr);
			ps.setLong(1, userID);
			ps.setLong(2, limit);
			ps.setLong(3, offset);

			ResultSet rs = ps.executeQuery();

			// get infomation of all tags
			while(rs.next()) {
				Tag tag = new Tag();
				tag.setId(rs.getLong("id"));
				tag.setOwnerID(rs.getLong("owner_id"));
				tag.setName(rs.getString("name"));
				tag.setNumText(getTextCount(corpusID, tag.getId()));

				tagList.add(tag);
			}
		} catch (SQLException e) {
			throw logger.throwing(new DatabaseException(e.getMessage()));
		}

		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
				new ServiceRequestCompletedMessage(serviceName));
		return tagList;
	}

	@Override
	public Void addTag(String tagName)
			throws UserNotLoggedInException, EmptyInfoException, 
			ResourceAlreadyExistsException, DatabaseException {
		String serviceName = "addTag";
		long userID = logServiceStartAndGetUserID(serviceName); 

		//check if tag name is empty
		if(tagName.isEmpty()) {
			throw logger.throwing(new EmptyInfoException());
		}

		//check if tag name exists
		if(isTagNameExists(userID, tagName)) {
			throw logger.throwing(new ResourceAlreadyExistsException());
		}

		String insertStr = "INSERT INTO tag (owner_id, name, create_timestamp) "
				+ "VALUES (?, ?, CURRENT_TIMESTAMP)";
		try {
			PreparedStatement ps = dbConnection.prepareStatement(insertStr);
			ps.setLong(1, userID);
			ps.setString(2, tagName);

			ps.executeUpdate();

		} catch (SQLException e) {
			throw logger.throwing(new DatabaseException(e.getMessage())); 
		}

		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
				new ServiceRequestCompletedMessage(serviceName));
		return null;
	}

	@Override
	public Void updateTag(Tag tag)
			throws EmptyInfoException, UserNotLoggedInException, 
			AccessToResourceDeniedException, ResourceAlreadyExistsException,
			DatabaseException {
		String serviceName = "updateTag";
		long userID = logServiceStartAndGetUserID(serviceName); 

		String tagName = tag.getName();
		long tagID = tag.getId();

		//check if info is empty
		if(tagID == 0 || tagName.isEmpty()) {
			throw logger.throwing(new EmptyInfoException());
		}

		//check if user owner of the tag
		if(!isUserTagOwner(userID, tagID)) {
			throw logger.throwing(new AccessToResourceDeniedException());
		}

		//check if tag name already exists 
		if(isTagNameExists(userID, tagName)) {
			throw logger.throwing(new ResourceAlreadyExistsException());
		}

		//check passed, update tag
		String queryStr = "UPDATE tag SET "
				+ "name=? "
				+ "WHERE id = ? AND owner_id = ? ";
		try {
			PreparedStatement ps = dbConnection.prepareStatement(queryStr);
			ps.setString(1, tagName); 
			ps.setLong(2, tagID);
			ps.setLong(3, userID);

			ps.executeUpdate();
		} catch (SQLException e) {
			throw logger.throwing(new DatabaseException(e.getMessage()));
		}

		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
				new ServiceRequestCompletedMessage(serviceName));
		return null;
	}

	@Override
	public Void deleteTag(Tag tag)
			throws UserNotLoggedInException, AccessToResourceDeniedException, DatabaseException {
		String serviceName = "deleteTag";
		long userID = logServiceStartAndGetUserID(serviceName); 

		long tagID = tag.getId();

		//check if user owner of the tag
		if(!isUserTagOwner(userID, tagID)) {
			throw new AccessToResourceDeniedException();
		}

		//check passed, delete tag
		String queryStr = "DELETE FROM tag "
				+ "WHERE id=? AND owner_id=?";
		try {
			PreparedStatement ps = dbConnection.prepareStatement(queryStr);
			ps.setLong(1, tag.getId());
			ps.setLong(2, userID);
			ps.executeUpdate();
		} catch (SQLException e) {
			throw logger.throwing(new DatabaseException(e.getMessage()));
		}

		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
				new ServiceRequestCompletedMessage(serviceName));
		return null;
	}

	@Override
	public Integer getTextCount(long corpusID, long tagID)
			throws UserNotLoggedInException, AccessToResourceDeniedException, DatabaseException {
		String serviceName = "getTextCount";
		long userID = logServiceStartAndGetUserID(serviceName); 

		int textCount = 0;

		//check if user owner of the corpus and tag
		if(!isUserCorpusOwner(userID, corpusID) || !isUserTagOwner(userID, tagID)) {
			throw new AccessToResourceDeniedException();
		}

		//check passed, get text count
		String queryStr = "SELECT COUNT(text.id) "
				+ "FROM text, corpus, tag, ta_te "
				+ "WHERE text.corpus_id = corpus.id"
				+ "     AND ta_te.te_id = text.id"
				+ "     AND tag.id = ta_te.ta_id "
				+ "     AND tag.id = ?"
				+ "     AND tag.owner_id=?"
				+ "     AND corpus.id=?";

		try {
			PreparedStatement ps = dbConnection.prepareStatement(queryStr);
			ps.setLong(1, tagID);
			ps.setLong(2, userID);
			ps.setLong(3, corpusID);
			ResultSet rs = ps.executeQuery();
			if(rs.next()) {
				textCount = rs.getInt("count");
			}

		} catch (SQLException e) {
			throw logger.throwing(new DatabaseException(e.getMessage()));
		}

		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
				new ServiceRequestCompletedMessage(serviceName));
		return textCount;
	}

	@Override
	public List<CorpusText> getTextList(long corpusID, long tagID, int offset, int limit)
			throws UserNotLoggedInException, AccessToResourceDeniedException, DatabaseException {
		String serviceName = "getTextList";
		long userID = logServiceStartAndGetUserID(serviceName); 

		ArrayList<CorpusText> textList = new ArrayList<CorpusText>(limit); 

		//check if user owner of corpus and tag
		if(!isUserCorpusOwner(userID, corpusID) || !isUserTagOwner(userID, tagID)) {
			throw logger.throwing(new AccessToResourceDeniedException());
		}

		//check passed, get data from database
		String queryStr = ""
				+ "SELECT text.id, text.title, text.content, text.create_timestamp "
				+ "FROM text, corpus, ta_te, tag "
				+ "WHERE text.corpus_id = corpus.id "
				+ "     AND text.id = ta_te.te_id "
				+ "     AND ta_te.ta_id = tag.id "
				+ "     AND corpus.id = ? "
				+ "     AND tag.id = ?"
				+ "     AND corpus.owner_id = ?"
				+ "ORDER BY text.id DESC "
				+ "LIMIT ? OFFSET ?";
		try {
			PreparedStatement ps = dbConnection.prepareStatement(queryStr);
			ps.setLong(1, corpusID);
			ps.setLong(2, tagID);
			ps.setLong(3, userID);
			ps.setInt(4, limit);
			ps.setInt(5, offset);

			ResultSet rs = ps.executeQuery();

			// get infomation of all text
			while(rs.next()) {
				CorpusText text = new CorpusText();
				text.setId(rs.getLong("id"));
				text.setCorpusID(corpusID);
				text.setTitle(rs.getString("title"));
				text.setTagSet(CorpusManagerServiceUtils.getTextTags(text.getId()));
				text.setContent(rs.getString("content"));
				text.setCreateDate(rs.getDate("create_timestamp"));
				textList.add(text);
			}

		} catch (SQLException e) {
			throw logger.throwing(new DatabaseException(e.getMessage()));
		}

		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
				new ServiceRequestCompletedMessage(serviceName));
		return textList;
	}

	@Override
	public Void tagText(CorpusText text, Tag tag)
			throws UserNotLoggedInException, DatabaseException, AccessToResourceDeniedException {
		String serviceName = "tagText";
		long userID = logServiceStartAndGetUserID(serviceName); 

		Long textID = text.getId();
		Long tagID = tag.getId();

		//check if user owner of text and tag
		if(!isUserCorpusOwner(userID, text.getCorpusID()) || !isUserTagOwner(userID, tagID)) {
			throw new AccessToResourceDeniedException();
		}

		String insertStr;
		PreparedStatement ps;
		ResultSet rs;
		try {
			String queryStr = "SELECT * FROM ta_te "
					+ "WHERE ta_id=? AND te_id=?";
			ps=dbConnection.prepareCall(queryStr);
			ps.setLong(1, tagID);
			ps.setLong(2, textID);
			rs = ps.executeQuery();

			if(rs.isBeforeFirst()) {
				//already tagged, does nothing
				return null;
			}

			//check passed, add tag to text 
			insertStr = ""
					+ "INSERT INTO ta_te (ta_id, te_id) "
					+ "VALUES (?, ?) ";
			ps = dbConnection.prepareStatement(insertStr);
			ps.setLong(1, tag.getId());
			ps.setLong(2, text.getId());

			ps.executeUpdate();

		} catch (SQLException e) {
			throw new DatabaseException(e.getMessage());
		}

		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
				new ServiceRequestCompletedMessage(serviceName));
		return null;
	}
}

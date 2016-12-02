package com.ctapweb.web.server.user;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ctapweb.web.client.service.FeatureSelectorService;
import com.ctapweb.web.server.DBConnectionManager;
import com.ctapweb.web.server.logging.LogMarker;
import com.ctapweb.web.server.logging.ServiceRequestCompletedMessage;
import com.ctapweb.web.server.logging.ServiceRequestReceivedMessage;
import com.ctapweb.web.server.logging.VerifyingUserCookiesMessage;
import com.ctapweb.web.shared.AnalysisEngine;
import com.ctapweb.web.shared.FeatureSet;
import com.ctapweb.web.shared.UserInfo;
import com.ctapweb.web.shared.AnalysisEngine.AEType;
import com.ctapweb.web.shared.exception.AccessToResourceDeniedException;
import com.ctapweb.web.shared.exception.DatabaseException;
import com.ctapweb.web.shared.exception.EmptyInfoException;
import com.ctapweb.web.shared.exception.UserNotLoggedInException;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class FeatureSelectorServiceImpl extends RemoteServiceServlet 
implements FeatureSelectorService {

	Connection dbConnection = DBConnectionManager.getDbConnection();

	private static final Logger logger = LogManager.getLogger();

	UserServiceImpl userServiceImpl = new UserServiceImpl();

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
	public Integer getFeatureSetCount() 
			throws UserNotLoggedInException, DatabaseException {
		String serviceName = "getFeatureSetCount";
		long userID = logServiceStartAndGetUserID(serviceName); 

		Integer featureSetCount = 0;

		String queryStr = "SELECT COUNT(id) FROM feature_set "
				+ "WHERE owner_id=? ";
		try {
			PreparedStatement ps = dbConnection.prepareStatement(queryStr);
			ps.setLong(1, userID);

			ResultSet rs =ps.executeQuery(); 

			if(rs.isBeforeFirst()) {
				rs.next();
				featureSetCount = rs.getInt("count");

				logger.info("Found " + featureSetCount + " feature sets for user (id) " + userID + ".");
			} else {
				logger.info("DB returns not result.");
			}

		} catch (SQLException e) {
			throw new DatabaseException(e.getMessage());
		}

		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
				new ServiceRequestCompletedMessage(serviceName));
		return featureSetCount;
	}

	@Override
	public List<FeatureSet> getFeatureSetList(int offset, int limit)
			throws UserNotLoggedInException, DatabaseException {
		String serviceName = "getFeatureSetList";
		long userID = logServiceStartAndGetUserID(serviceName); 

		ArrayList<FeatureSet> featureSetList = new ArrayList<FeatureSet>(limit); 

		//get data from database
		String queryStr = ""
				+ "SELECT id, owner_id, name, description, create_timestamp "
				+ "FROM feature_set "
				+ "WHERE owner_id=? "
				+ "ORDER BY id DESC "
				+ "LIMIT ? OFFSET ?";
		PreparedStatement ps;
		try {
			ps = dbConnection.prepareStatement(queryStr);
			ps.setLong(1, userID);
			ps.setInt(2, limit);
			ps.setInt(3, offset);

			ResultSet rs = ps.executeQuery();

			// get infomation of all feature sets
			while(rs.next()) {
				FeatureSet featureSet = new FeatureSet();
				featureSet.setId(rs.getLong("id"));
				featureSet.setOwnerId(rs.getLong("owner_id"));
				featureSet.setName(rs.getString("name"));
				featureSet.setDescription(rs.getString("description"));
				featureSet.setCreateDate(rs.getDate("create_timestamp"));
				featureSetList.add(featureSet);
			}
		} catch (SQLException e) {
			logger.warn("Database error: " + e.getMessage());
			throw new DatabaseException(e.getMessage());
		}

		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
				new ServiceRequestCompletedMessage(serviceName));
		return featureSetList;
	}

	@Override
	public Void addFeatureSet(FeatureSet featureSet) 
			throws EmptyInfoException, UserNotLoggedInException, DatabaseException {
		String serviceName = "addFeatureSet";
		long userID = logServiceStartAndGetUserID(serviceName); 

		//check if feature set info is empty
		if(featureSet.getName().isEmpty()) {
			throw new EmptyInfoException();
		}
		String insertStr = "INSERT INTO feature_set (owner_id, name, description, create_timestamp) "
				+ "VALUES (?, ?, ?, CURRENT_TIMESTAMP)";
		try {
			PreparedStatement ps = dbConnection.prepareStatement(insertStr);
			ps.setLong(1, userID);
			ps.setString(2, featureSet.getName());
			ps.setString(3, featureSet.getDescription());

			ps.executeUpdate();

		} catch (SQLException e) {
			logger.warn("DB error occured while adding new feature set: " + e.getMessage());
			throw new DatabaseException(e.getMessage()); 
		}

		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
				new ServiceRequestCompletedMessage(serviceName));
		return null;
	}

	@Override
	public Void deleteFeatureSet(FeatureSet featureSet)
			throws UserNotLoggedInException, AccessToResourceDeniedException, DatabaseException {
		String serviceName = "deleteFeatureSet";
		long userID = logServiceStartAndGetUserID(serviceName); 

		//check if user owner of the feature set
		if(!isUserFSOwner(userID, featureSet.getId())) {
			throw new AccessToResourceDeniedException();
		}

		logger.info("Deleting feature set " + featureSet.getId() + "...");
		PreparedStatement ps;
		ResultSet rs;
		String queryStr;
		try {
			queryStr = "DELETE FROM feature_set "
					+ "WHERE id=? AND owner_id=?";
			ps = dbConnection.prepareStatement(queryStr);
			ps.setLong(1, featureSet.getId());
			ps.setLong(2, userID);
			ps.executeUpdate();
		} catch (SQLException e) {
			logger.warn("Error occured while deleting feature set record... " + e.getMessage());
			throw new DatabaseException(e.getMessage());
		}

		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
				new ServiceRequestCompletedMessage(serviceName));
		return null;
	}

	@Override
	public Void updateFeatureSet(FeatureSet featureSet)
			throws EmptyInfoException, UserNotLoggedInException, 
			AccessToResourceDeniedException, DatabaseException {
		String serviceName = "updateFeatureSet";
		long userID = logServiceStartAndGetUserID(serviceName); 

		//check if user owner of the feature set
		if(!isUserFSOwner(userID, featureSet.getId())) {
			throw new AccessToResourceDeniedException();
		}

		//check if info is empty
		if(featureSet.getId() == 0 || featureSet.getName().isEmpty()) {
			throw new EmptyInfoException();
		}

		PreparedStatement ps;

		try {
			//check passed, update corpus
			String updateStr = ""
					+ "UPDATE feature_set SET "
					+ "name=?, description=? "
					+ "WHERE id = ? AND owner_id = ? ";
			ps = dbConnection.prepareStatement(updateStr);
			ps.setString(1, featureSet.getName());
			ps.setString(2, featureSet.getDescription());
			ps.setLong(3, featureSet.getId());
			ps.setLong(4, userID);
			ps.executeUpdate();

		} catch (SQLException e) {
			logger.warn("Error occured while updating feature set info... " + e.getMessage());
			throw new DatabaseException(e.getMessage());
		}

		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
				new ServiceRequestCompletedMessage(serviceName));
		return null;
	}

	@Override
	public Integer getFeatureCount(long featureSetID)
			throws UserNotLoggedInException, AccessToResourceDeniedException, DatabaseException {
		String serviceName = "getFeatureCount(featureSetID)";
		long userID = logServiceStartAndGetUserID(serviceName); 

		int featureCount = 0;

		//check if user owner of the feature set
		logger.trace(LogMarker.CTAP_SERVER_MARKER, "Checking if user owner of feature set...");
		if(!isUserFSOwner(userID, featureSetID)) {
			throw logger.throwing(new AccessToResourceDeniedException());
		}

		String queryStr = ""
				+ "SELECT COUNT(id) FROM fs_cf "
				+ "WHERE fs_id=?";
		try {

			PreparedStatement ps = dbConnection.prepareStatement(queryStr);
			ps.setLong(1, featureSetID);
			ResultSet rs = ps.executeQuery();
			if(rs.next()) {
				featureCount = rs.getInt("count");
			}

		} catch (SQLException e) {
			throw logger.throwing(new DatabaseException(e.getMessage()));
		}

		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
				new ServiceRequestCompletedMessage(serviceName));
		return featureCount;
	}

	@Override
	public List<AnalysisEngine> getFeatureList(long featureSetID, int offset, int limit)
			throws UserNotLoggedInException, AccessToResourceDeniedException, DatabaseException {
		String serviceName = "getFeatureList(featureSetID)";
		long userID = logServiceStartAndGetUserID(serviceName); 

		List<AnalysisEngine> fsFeatureList = new ArrayList<>(limit); 

		//check if user owner of the feature set
		logger.trace(LogMarker.CTAP_SERVER_MARKER, "Checking if user owner of feature set...");
		if(!isUserFSOwner(userID, featureSetID)) {
			throw logger.throwing(new AccessToResourceDeniedException());
		}

		//get data from database
		String queryStr = "SELECT ae.id, ae.name, ae.version, ae.vendor, ae.description, ae.create_timestamp " 
				+ "FROM analysis_engine AS ae, "
				+ "     fs_cf, feature_set AS fs "
				+ "WHERE ae.id = fs_cf.cf_id "
				+ "     AND fs.id = fs_cf.fs_id "
				+ "     AND fs_cf.fs_id=? "
				+ "ORDER BY id DESC "
				+ "LIMIT ? OFFSET ?";
		PreparedStatement ps;
		try {
			ps = dbConnection.prepareStatement(queryStr);
			ps.setLong(1, featureSetID);
			ps.setInt(2, limit);
			ps.setInt(3, offset);

			ResultSet rs = ps.executeQuery();

			// get infomation of all features included in this feature set
			while(rs.next()) {
				AnalysisEngine ae = new AnalysisEngine();
				ae.setId(rs.getLong("id"));
				ae.setName(rs.getString("name"));
				ae.setType(AEType.FEATURE_EXTRACTOR);
				ae.setVersion(rs.getString("version"));
				ae.setVendor(rs.getString("vendor"));
				ae.setDescription(rs.getString("description"));
				ae.setCreateDate(rs.getDate("create_timestamp"));

				fsFeatureList.add(ae);
			}
		} catch (SQLException e) {
			throw logger.throwing(new DatabaseException(e.getMessage()));
		}

		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
				new ServiceRequestCompletedMessage(serviceName));
		return fsFeatureList;
	}

	@Override
	public Void removeFeatureFromFS(long featureSetID, long featureID)
			throws UserNotLoggedInException,AccessToResourceDeniedException, DatabaseException {
		String serviceName = "removeFeatureFromFS";
		long userID = logServiceStartAndGetUserID(serviceName); 

		//check if user owner of the feature set
		if(!isUserFSOwner(userID, featureSetID)) {
			throw logger.throwing(new AccessToResourceDeniedException());
		}

		String removeStr = "DELETE FROM fs_cf "
				+ "WHERE fs_id=? AND cf_id=?";

		try {
			PreparedStatement ps = dbConnection.prepareStatement(removeStr);
			ps.setLong(1, featureSetID);
			ps.setLong(2, featureID);
			ps.executeUpdate();
		} catch (SQLException e) {
			throw logger.throwing(new DatabaseException(e.getMessage()));
		}

		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
				new ServiceRequestCompletedMessage(serviceName));
		return null;
	}

	@Override
	public Integer getFeatureCount()
			throws UserNotLoggedInException, DatabaseException {
		String serviceName = "getFeatureCount()";
		long userID = logServiceStartAndGetUserID(serviceName); 

		int featureCount = 0;

		String queryStr = ""
				+ "SELECT COUNT(id) "
				+ "FROM analysis_engine "
				+ "WHERE type=?";
		try {
			PreparedStatement ps = dbConnection.prepareStatement(queryStr);
			ps.setString(1, AEType.FEATURE_EXTRACTOR.toString());
			ResultSet rs = ps.executeQuery();
			if(rs.next()) {
				featureCount = rs.getInt("count");
			}

		} catch (SQLException e) {
			throw logger.throwing(new DatabaseException(e.getMessage()));
		}

		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
				new ServiceRequestCompletedMessage(serviceName));
		return featureCount;
	}

	//TODO refactor the functions to make use of FeatureSetUtils
	@Override
	public List<AnalysisEngine> getFeatureList(int offset, int limit)
			throws UserNotLoggedInException, DatabaseException {
		String serviceName = "getFeatureList()";
		long userID = logServiceStartAndGetUserID(serviceName); 

		List<AnalysisEngine> availableFeatureList = new ArrayList<>(limit); 

		//get data from database
		String queryStr = ""
				+ "SELECT id, name, version, vendor, description, create_timestamp " 
				+ "FROM analysis_engine "
				+ "WHERE type = ?"
				+ "ORDER BY id DESC "
				+ "LIMIT ? OFFSET ?";
		try {
			PreparedStatement ps = dbConnection.prepareStatement(queryStr);
			ps.setString(1, AEType.FEATURE_EXTRACTOR.toString());
			ps.setInt(2, limit);
			ps.setInt(3, offset);

			ResultSet rs = ps.executeQuery();

			// get infomation of all features in the DB
			while(rs.next()) {
				AnalysisEngine ae = new AnalysisEngine();
				ae.setId(rs.getLong("id"));
				ae.setName(rs.getString("name"));
				ae.setType(AEType.FEATURE_EXTRACTOR);
				ae.setVersion(rs.getString("version"));
				ae.setVendor(rs.getString("vendor"));
				ae.setDescription(rs.getString("description"));
				ae.setCreateDate(rs.getDate("create_timestamp"));

				availableFeatureList.add(ae);
			}
		} catch (SQLException e) {
			throw logger.throwing(new DatabaseException(e.getMessage()));
		}

		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
				new ServiceRequestCompletedMessage(serviceName));
		return availableFeatureList;
	}

	@Override
	public Void addToFeatureSet(long featureID, long featureSetID)
			throws UserNotLoggedInException, AccessToResourceDeniedException, DatabaseException {
		String serviceName = "addToFeatureSet";
		long userID = logServiceStartAndGetUserID(serviceName); 

		//check if user owner of the feature set
		if(!isUserFSOwner(userID, featureSetID)) {
			throw logger.throwing(new AccessToResourceDeniedException());
		}

		try {
			//check if feature already in feature set
			String queryStr = ""
					+ "SELECT id " 
					+ "FROM fs_cf "
					+ "WHERE fs_id=? AND cf_id=?";
			PreparedStatement ps = dbConnection.prepareStatement(queryStr);
			ps.setLong(1, featureSetID);
			ps.setLong(2, featureID);

			ResultSet rs = ps.executeQuery();
			if(rs.isBeforeFirst()) {
				return null; //feature already in feature set, does nothing
			}

			//add feature to feature set
			String insertStr = ""
					+ "INSERT INTO fs_cf (fs_id, cf_id) "
					+ "VALUES(?, ?)";
			ps = dbConnection.prepareStatement(insertStr);
			ps.setLong(1, featureSetID);
			ps.setLong(2, featureID);
			ps.executeUpdate();
		} catch (SQLException e) {
			throw logger.throwing(new DatabaseException(e.getMessage()));
		}

		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
				new ServiceRequestCompletedMessage(serviceName));
		return null;
	}

	@Override
	public String getFeatureSetName(long featureSetID)
			throws UserNotLoggedInException, AccessToResourceDeniedException, DatabaseException {
		String serviceName = "getFeatureSetName";
		long userID = logServiceStartAndGetUserID(serviceName); 

		String featureSetName = null;

		//check if user owner of the feature set
		if(!isUserFSOwner(userID, featureSetID)) {
			throw new AccessToResourceDeniedException();
		}

		String queryStr;
		PreparedStatement ps;
		ResultSet rs;

		try {

			//get data from database
			logger.info("Getting feature set name for feature set id " + featureSetID + "...");
			queryStr = ""
					+ "SELECT name, owner_id "
					+ "FROM feature_set "
					+ "WHERE id=? AND "
					+ "owner_id=?";
			ps = dbConnection.prepareStatement(queryStr);
			ps.setLong(1, featureSetID);
			ps.setLong(2, userID);

			rs = ps.executeQuery();

			if(rs.next()) {
				featureSetName = rs.getString("name");
			} 

		} catch (SQLException e) {
			logger.warn("Database error: " + e.getMessage());
			throw new DatabaseException(e.getMessage());
		}

		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
				new ServiceRequestCompletedMessage(serviceName));
		return featureSetName;
	}

	public boolean isUserFSOwner(long userID, long featureSetID) 
			throws DatabaseException {
		boolean isUserOwner = false;

		//check if the logged in user is the same as the feature set owner
		String queryStr =  "SELECT id FROM feature_set "
				+ "WHERE id=? AND owner_id=? ";
		PreparedStatement ps;
		try {
			ps = dbConnection.prepareStatement(queryStr);
			ps.setLong(1, featureSetID);
			ps.setLong(2, userID);
			ResultSet rs = ps.executeQuery();
			if(rs.isBeforeFirst()) {
				isUserOwner = true;
			} 
		} catch (SQLException e) {
			throw logger.throwing(new DatabaseException(e.getMessage()));
		}

		return isUserOwner;
	}

	@Override
	public Integer getFeatureCount(String keyword) 
			throws UserNotLoggedInException, DatabaseException {
		String serviceName = "getFeatureCount(keyword)";
		long userID = logServiceStartAndGetUserID(serviceName); 

		int featureCount = 0;

		//		//check if user owner of the feature set
		//		logger.trace(LogMarker.CTAP_SERVER_MARKER, "Checking if user owner of feature set...");
		//		if(!isUserFSOwner(userID, featureSetID)) {
		//			throw logger.throwing(new AccessToResourceDeniedException());
		//		}

		//		String queryStr = ""
		//				+ "SELECT COUNT(fs_cf.cf_id) "
		//				+ "FROM fs_cf, analysis_engine AS ae "
		//				+ "WHERE fs_cf.cf_id = ae.id "
		//				+ "     AND fs_cf.fs_id=?"
		//				+ "     AND ae.name ILIKE '%sd%' ";
		String queryStr = "SELECT COUNT(id) "
				+ "FROM analysis_engine "
				+ "WHERE name ILIKE ? ";
		try {
			PreparedStatement ps = dbConnection.prepareStatement(queryStr);
			ps.setString(1, "%" + keyword + "%");
			ResultSet rs = ps.executeQuery();
			if(rs.next()) {
				featureCount = rs.getInt("count");
			}
		} catch (SQLException e) {
			throw logger.throwing(new DatabaseException(e.getMessage()));
		}

		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
				new ServiceRequestCompletedMessage(serviceName));
		return featureCount;
	}

	@Override
	public List<AnalysisEngine> getFeatureList(String keyword, int offset, int limit)
			throws UserNotLoggedInException, DatabaseException {
		String serviceName = "getFeatureList(keyword)";
		long userID = logServiceStartAndGetUserID(serviceName); 

		List<AnalysisEngine> featureList = new ArrayList<>(limit); 

		//		//check if user owner of the feature set
		//		logger.trace(LogMarker.CTAP_SERVER_MARKER, "Checking if user owner of feature set...");
		//		if(!isUserFSOwner(userID, featureSetID)) {
		//			throw logger.throwing(new AccessToResourceDeniedException());
		//		}

		//get data from database
		//		String queryStr = "SELECT ae.id, ae.name, ae.version, ae.vendor, ae.description, ae.create_timestamp " 
		//				+ "FROM analysis_engine AS ae, fs_cf "
		//				+ "WHERE ae.id = fs_cf.cf_id "
		//				+ "     AND fs_cf.fs_id=? "
		//				+ "     AND ae.name ILIKE '%sd%' "
		//				+ "ORDER BY id DESC "
		//				+ "LIMIT ? OFFSET ?";
		String queryStr = "SELECT id, name, version, vendor, description, create_timestamp "
				+ "FROM analysis_engine "
				+ "WHERE name ILIKE ? "
				+ "ORDER BY id DESC "
				+ "LIMIT ? OFFSET ?";
		PreparedStatement ps;
		try {
			ps = dbConnection.prepareStatement(queryStr);
			ps.setString(1, "%" + keyword + "%");
			ps.setInt(2, limit);
			ps.setInt(3, offset);

			ResultSet rs = ps.executeQuery();

			// get infomation of all features included in this feature set
			while(rs.next()) {
				AnalysisEngine ae = new AnalysisEngine();
				ae.setId(rs.getLong("id"));
				ae.setName(rs.getString("name"));
				ae.setType(AEType.FEATURE_EXTRACTOR);
				ae.setVersion(rs.getString("version"));
				ae.setVendor(rs.getString("vendor"));
				ae.setDescription(rs.getString("description"));
				ae.setCreateDate(rs.getDate("create_timestamp"));

				featureList.add(ae);
			}
		} catch (SQLException e) {
			throw logger.throwing(new DatabaseException(e.getMessage()));
		}

		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
				new ServiceRequestCompletedMessage(serviceName));
		return featureList;
	}

	@Override
	public Integer getFeatureCount(long featureSetID, String keyword)
			throws UserNotLoggedInException, AccessToResourceDeniedException, DatabaseException {
		String serviceName = "getFeatureCount(featureSetID, keyword)";
		long userID = logServiceStartAndGetUserID(serviceName); 

		int featureCount = 0;

		//check if user owner of the feature set
		logger.trace(LogMarker.CTAP_SERVER_MARKER, "Checking if user owner of feature set...");
		if(!isUserFSOwner(userID, featureSetID)) {
			throw logger.throwing(new AccessToResourceDeniedException());
		}

		String queryStr = "SELECT COUNT(fs_cf.cf_id) "
				+ "FROM fs_cf, analysis_engine AS ae "
				+ "WHERE fs_cf.cf_id = ae.id "
				+ "     AND fs_cf.fs_id=?"
				+ "     AND ae.name ILIKE ? ";
		//		String queryStr = "SELECT COUNT(id) "
		//				+ "FROM analysis_engine "
		//				+ "WHERE name ILIKE ? ";
		try {
			PreparedStatement ps = dbConnection.prepareStatement(queryStr);
			ps.setLong(1, featureSetID);
			ps.setString(2, "%" + keyword + "%");
			ResultSet rs = ps.executeQuery();
			if(rs.next()) {
				featureCount = rs.getInt("count");
			}
		} catch (SQLException e) {
			throw logger.throwing(new DatabaseException(e.getMessage()));
		}

		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
				new ServiceRequestCompletedMessage(serviceName));
		return featureCount;
	}

	@Override
	public List<AnalysisEngine> getFeatureList(long featureSetID, String keyword, int offset, int limit)
			throws UserNotLoggedInException, AccessToResourceDeniedException, DatabaseException {
		String serviceName = "getFeatureList(featureSetID, keyword)";
		long userID = logServiceStartAndGetUserID(serviceName); 

		List<AnalysisEngine> featureList = new ArrayList<>(limit); 

		//check if user owner of the feature set
		logger.trace(LogMarker.CTAP_SERVER_MARKER, "Checking if user owner of feature set...");
		if(!isUserFSOwner(userID, featureSetID)) {
			throw logger.throwing(new AccessToResourceDeniedException());
		}

		//get data from database
		String queryStr = "SELECT ae.id, ae.name, ae.version, ae.vendor, ae.description, ae.create_timestamp " 
				+ "FROM analysis_engine AS ae, fs_cf "
				+ "WHERE ae.id = fs_cf.cf_id "
				+ "     AND fs_cf.fs_id=? "
				+ "     AND ae.name ILIKE ? "
				+ "ORDER BY id DESC "
				+ "LIMIT ? OFFSET ?";
		//		String queryStr = "SELECT id, name, version, vendor, description, create_timestamp "
		//				+ "FROM analysis_engine "
		//				+ "WHERE name ILIKE ? "
		//				+ "ORDER BY id DESC "
		//				+ "LIMIT ? OFFSET ?";
		PreparedStatement ps;
		try {
			ps = dbConnection.prepareStatement(queryStr);
			ps.setLong(1, featureSetID);
			ps.setString(2, "%" + keyword + "%");
			ps.setInt(3, limit);
			ps.setInt(4, offset);

			ResultSet rs = ps.executeQuery();

			// get infomation of all features included in this feature set
			while(rs.next()) {
				AnalysisEngine ae = new AnalysisEngine();
				ae.setId(rs.getLong("id"));
				ae.setName(rs.getString("name"));
				ae.setType(AEType.FEATURE_EXTRACTOR);
				ae.setVersion(rs.getString("version"));
				ae.setVendor(rs.getString("vendor"));
				ae.setDescription(rs.getString("description"));
				ae.setCreateDate(rs.getDate("create_timestamp"));

				featureList.add(ae);
			}
		} catch (SQLException e) {
			throw logger.throwing(new DatabaseException(e.getMessage()));
		}

		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
				new ServiceRequestCompletedMessage(serviceName));
		return featureList;
	}
}

package com.ctapweb.web.server.admin;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ctapweb.web.client.service.AdminService;
import com.ctapweb.web.server.DBConnectionManager;
import com.ctapweb.web.server.ServerProperties;
import com.ctapweb.web.server.analysis.AEUtils;
import com.ctapweb.web.server.logging.LogMarker;
import com.ctapweb.web.server.logging.ServiceRequestCompletedMessage;
import com.ctapweb.web.server.logging.ServiceRequestReceivedMessage;
import com.ctapweb.web.server.logging.VerifyingUserCookiesMessage;
import com.ctapweb.web.server.user.UserServiceImpl;
import com.ctapweb.web.shared.AnalysisEngine;
import com.ctapweb.web.shared.ComplexityFeature;
import com.ctapweb.web.shared.UserInfo;
import com.ctapweb.web.shared.exception.AEDependencyException;
import com.ctapweb.web.shared.exception.AdminNotLoggedInException;
import com.ctapweb.web.shared.exception.DatabaseException;
import com.ctapweb.web.shared.exception.EmptyInfoException;
import com.ctapweb.web.shared.exception.ServerIOException;
import com.ctapweb.web.shared.exception.UIMAException;
import com.ctapweb.web.shared.exception.UserNotLoggedInException;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class AdminServiceImpl extends RemoteServiceServlet implements AdminService {

	UserServiceImpl userServiceImpl = new UserServiceImpl();
	private Connection dbConnection  = DBConnectionManager.getDbConnection();
	Logger logger = LogManager.getLogger();

	@Override
	public void initDB(String adminPasswd) 
			throws AdminNotLoggedInException, DatabaseException {

		//check if adminPasswd correct
		if(!ServerProperties.INITDBPASSWD.equals(adminPasswd)) {
			logger.error(LogMarker.CTAP_SERVER_MARKER, "Wrong passwd for initializing DB.");
			throw new AdminNotLoggedInException();
		}

		//link to DB server and create tables 
		createUserAccountTable();

		createCorpusFolderTable();

		createCorpusTable();

		createTextTable();

		createAnalysisEngineTable();
		
		createAE_DependencyTable();

//		createComplexityFeatureTable();

//		createCF_AETable();

		createFeatureSetTable();

		//the table linking the feature set table and the complexity feature table
		createFS_CFTable();

		createAnalysisTable();

		createAnalysisStatusTable();

		createResultTable();

		createFeedbackTable();

		createGroupingTable();

		createGR_TETable();

		createTagTable();

		createTA_TETable();

	}

	private void createUserAccountTable() throws DatabaseException {
		// the user_account table
		//		String createTableStr = ""
		//				+ "CREATE TABLE IF NOT EXISTS user_account("
		//				+ "id BIGSERIAL PRIMARY KEY NOT NULL ,"
		//				+ "title VARCHAR(10), "
		//				+ "first_name VARCHAR(50), "
		//				+ "last_name VARCHAR(50), "
		//				+ "institution VARCHAR(100), "
		//				+ "email TEXT NOT NULL UNIQUE, "
		//				+ "passwd TEXT NOT NULL, "
		//				+ "session_token TEXT, "
		//				+ "last_login TIMESTAMP "
		//				+ ");"
		//				+ "";
		String createTableStr = ""
				+ "CREATE TABLE IF NOT EXISTS user_account("
				+ "id BIGSERIAL PRIMARY KEY NOT NULL ,"
				+ "email TEXT NOT NULL UNIQUE, "
				+ "passwd TEXT NOT NULL, "
				+ "session_token TEXT, "
				+ "last_login TIMESTAMP "
				+ ");"
				+ "";
		try (
				PreparedStatement ps = dbConnection.prepareStatement(createTableStr)
				){
			ps.execute();
		} catch (SQLException e) {
			throw new DatabaseException(e.getMessage());
		}

	}

	private void createFeedbackTable() throws DatabaseException {
		// the feedback table
		String createTableStr = ""
				+ "CREATE TABLE IF NOT EXISTS feedback("
				+ "id BIGSERIAL PRIMARY KEY NOT NULL ,"
				+ "user_id BIGINT NOT NULL REFERENCES user_account(id) ON DELETE CASCADE, " 
				+ "subject TEXT NOT NULL, "
				+ "content TEXT NOT NULL, "
				+ "sent_date TIMESTAMP NOT NULL "
				+ ");"
				+ "";
		try (
				PreparedStatement ps = dbConnection.prepareStatement(createTableStr)
				){
			ps.execute();
		} catch (SQLException e) {
			throw new DatabaseException(e.getMessage());
		}
	}

	private void createCorpusFolderTable() throws DatabaseException {
		String createTableStr = ""
				+ "CREATE TABLE IF NOT EXISTS corpus_folder("
				+ "id BIGSERIAL PRIMARY KEY NOT NULL ,"
				+ "owner_id BIGINT NOT NULL REFERENCES user_account(id) ON DELETE CASCADE,"
				+ "name TEXT NOT NULL,"
				+ "create_timestamp TIMESTAMP NOT NULL,"
				+ "UNIQUE(owner_id, name)" 
				+ ")";
		try (
				PreparedStatement ps = dbConnection.prepareStatement(createTableStr)
				){
			ps.execute();
		} catch (SQLException e) {
			throw new DatabaseException(e.getMessage());
		}
	}

	private void createTagTable() throws DatabaseException {
		String createTableStr = ""
				+ "CREATE TABLE IF NOT EXISTS tag("
				+ "id BIGSERIAL PRIMARY KEY NOT NULL ,"
				+ "owner_id BIGINT NOT NULL REFERENCES user_account(id) ON DELETE CASCADE,"
				+ "name TEXT NOT NULL,"
				+ "create_timestamp TIMESTAMP NOT NULL,"
				+ "UNIQUE(owner_id, name)" 
				+ ")";
		try (
				PreparedStatement ps = dbConnection.prepareStatement(createTableStr)
				){
			ps.execute();
		} catch (SQLException e) {
			throw new DatabaseException(e.getMessage());
		}
	}

	private void createCorpusTable() throws DatabaseException {
		String createTableStr = ""
				+ "CREATE TABLE IF NOT EXISTS corpus("
				+ "id BIGSERIAL PRIMARY KEY NOT NULL ,"
				+ "owner_id BIGINT NOT NULL REFERENCES user_account(id) ON DELETE CASCADE,"
				+ "name TEXT NOT NULL,"
				+ "folder_id BIGINT REFERENCES corpus_folder(id) ON DELETE CASCADE, "
				+ "description TEXT,"
				+ "create_timestamp TIMESTAMP NOT NULL" + ");"
				+ "";
		try (
				PreparedStatement ps = dbConnection.prepareStatement(createTableStr)
				){
			ps.execute();
		} catch (SQLException e) {
			throw new DatabaseException(e.getMessage());
		}
	}

	private void createTextTable() throws DatabaseException {
		String createTableStr = ""
				+ "CREATE TABLE IF NOT EXISTS text("
				+ "id BIGSERIAL PRIMARY KEY NOT NULL ,"
				+ "corpus_id BIGINT NOT NULL REFERENCES corpus(id) ON DELETE CASCADE,"
				+ "title TEXT,"
				+ "content TEXT,"
				+ "create_timestamp TIMESTAMP NOT NULL" + ");"
				+ "";
		try (
				PreparedStatement ps = dbConnection.prepareStatement(createTableStr)
				){
			ps.execute();
		} catch (SQLException e) {
			throw new DatabaseException(e.getMessage());
		}
	}

	private void createAnalysisStatusTable() throws DatabaseException {
		String createTableStr = ""
				+ "CREATE TABLE IF NOT EXISTS analysis_status("
				+ "id BIGSERIAL PRIMARY KEY NOT NULL ,"
				+ "analysis_id BIGINT UNIQUE NOT NULL REFERENCES analysis(id) ON DELETE CASCADE,"
				+ "progress REAL NOT NULL,"
				+ "status VARCHAR(20) NOT NULL, "
				+ "last_update TIMESTAMP NOT NULL"
				+ ");";
		try (
				PreparedStatement ps = dbConnection.prepareStatement(createTableStr)
				){
			ps.execute();
		} catch (SQLException e) {
			throw new DatabaseException(e.getMessage());
		}
	}

	private void createAnalysisEngineTable() throws DatabaseException {
		String createTableStr = ""
				+ "CREATE TABLE IF NOT EXISTS analysis_engine("
				+ "id BIGSERIAL PRIMARY KEY NOT NULL ,"
				+ "name TEXT NOT NULL, "
				+ "type TEXT NOT NULL, "
				+ "version TEXT, "
				+ "vendor TEXT, "
				+ "description TEXT, "
				+ "descriptor_file_name TEXT NOT NULL UNIQUE,"
				+ "descriptor_file_content TEXT NOT NULL,"
				+ "create_timestamp TIMESTAMP NOT NULL"
				+ ")"
				+ "";
		try (
				PreparedStatement ps = dbConnection.prepareStatement(createTableStr)
				){
			ps.execute();
		} catch (SQLException e) {
			throw new DatabaseException(e.getMessage());
		}
	}

	private void createComplexityFeatureTable() throws DatabaseException {
//		String createTableStr = ""
//				+ "CREATE TABLE IF NOT EXISTS complexity_feature("
//				+ "id BIGSERIAL PRIMARY KEY NOT NULL ,"
//				+ "name TEXT NOT NULL,"
//				+ "description TEXT, "
//				+ "create_timestamp TIMESTAMP NOT NULL)"
//				+ "";
//		try (
//				PreparedStatement ps = dbConnection.prepareStatement(createTableStr)
//				){
//			ps.execute();
//		} catch (SQLException e) {
//			throw new DatabaseException(e.getMessage());
//		}	
	}

	private void createCF_AETable() throws DatabaseException {
//		String createTableStr = ""
//				+ "CREATE TABLE IF NOT EXISTS cf_ae("
//				+ "id BIGSERIAL PRIMARY KEY NOT NULL ,"
//				+ "cf_id BIGINT NOT NULL REFERENCES complexity_feature(id) ON DELETE CASCADE,"
//				+ "ae_id BIGINT NOT NULL REFERENCES analysis_engine(id) ON DELETE RESTRICT"
//				+ ")"
//				+ "";
//		try (
//				PreparedStatement ps = dbConnection.prepareStatement(createTableStr)
//				){
//			ps.execute();
//		} catch (SQLException e) {
//			throw new DatabaseException(e.getMessage());
//		}
	}

	private void createAE_DependencyTable() throws DatabaseException {
		String createTableStr = ""
				+ "CREATE TABLE IF NOT EXISTS ae_dependency("
				+ "id BIGSERIAL PRIMARY KEY NOT NULL ,"
				+ "ae_id BIGINT NOT NULL REFERENCES analysis_engine(id) ON DELETE CASCADE,"
				+ "dep_ae_id BIGINT NOT NULL REFERENCES analysis_engine(id) ON DELETE CASCADE, "
				+ "UNIQUE (ae_id, dep_ae_id)"
				+ ")"
				+ "";
		try (
				PreparedStatement ps = dbConnection.prepareStatement(createTableStr)
				){
			ps.execute();
		} catch (SQLException e) {
			throw new DatabaseException(e.getMessage());
		}
	}

	@Override
	public long getAECount()
			throws UserNotLoggedInException, 
			AdminNotLoggedInException,DatabaseException {
		String serviceName = "getAECount";
		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
				new ServiceRequestReceivedMessage(serviceName));

		if(!isUserAdmin()) {
			throw logger.throwing(new AdminNotLoggedInException());
		}
		
		long aeCount = 0;

		//query database
		String queryStr = "SELECT COUNT(id) "
				+ "FROM analysis_engine ";
		PreparedStatement ps;
		try {
			ps = dbConnection.prepareStatement(queryStr);
			ResultSet rs = ps.executeQuery();
			if(rs.isBeforeFirst()) {
				rs.next();
				aeCount = rs.getLong("count");
			}
		} catch (SQLException e) {
			throw logger.throwing(new DatabaseException(e.getMessage()));
		}

		return aeCount;
	}

	@Override
	public List<AnalysisEngine> getAEList(int offset, int limit) 
			throws UserNotLoggedInException, AdminNotLoggedInException, DatabaseException {
		String serviceName = "getAEList";
		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
				new ServiceRequestReceivedMessage(serviceName));

		if(!isUserAdmin()) {
			throw logger.throwing(new AdminNotLoggedInException());
		}

		List<AnalysisEngine> aeList;
		try {
			aeList = AEUtils.getAEList(offset, limit);
		} catch (SQLException e) {
			throw logger.throwing(new DatabaseException(e.getMessage()));
		}

		return aeList;
	}

	@Override
	public Void updateAE(String userCookieValue, AnalysisEngine ae)
			throws EmptyInfoException, UserNotLoggedInException, 
			AdminNotLoggedInException, DatabaseException {

		//check if admin is logged in
		if(!isUserAdmin()) {
			throw new AdminNotLoggedInException();
		}

		//check if ae info is empty
		if(ae.getName().isEmpty() || ae.getDescriptorFileName().isEmpty()) {
			throw new EmptyInfoException();
		}

		logger.info("Updating AE info..."); 
		String updateStr = ""
				+ "UPDATE analysis_engine SET "
				+ "name=?, description=?, descriptor_file_name =? "
				+ "WHERE id = ?"; 
		try {
			PreparedStatement ps = dbConnection.prepareStatement(updateStr);
			ps.setString(1, ae.getName());
			ps.setString(2, ae.getDescription());
			ps.setString(3, ae.getDescriptorFileName());
			ps.setLong(4, ae.getId());
			ps.executeUpdate();

		} catch (SQLException e) {
			logger.warn("Error occured while updating AE: " + e.getMessage());
			throw new DatabaseException(e.getMessage());
		}

		logger.info("AE info updated.");
		return null;
	}

	@Override
	public Void deleteAE(AnalysisEngine ae) 
			throws UserNotLoggedInException, AdminNotLoggedInException, DatabaseException {
		String serviceName = "deleteAE";
		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
				new ServiceRequestReceivedMessage(serviceName));

		//check if admin is logged in
		if(!isUserAdmin()) {
			throw logger.throwing(new AdminNotLoggedInException());
		}

		String deleteStr = ""
				+ "DELETE FROM analysis_engine "
				+ "WHERE id=?";
		try {
			PreparedStatement ps = dbConnection.prepareStatement(deleteStr);
			ps.setLong(1, ae.getId());
			ps.executeUpdate();
		} catch (SQLException e) {
			throw logger.throwing(new DatabaseException(e.getMessage()));
		}

		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
				new ServiceRequestCompletedMessage(serviceName));
		return null;
	}

//	@Override
//	public Void addAE(String userCookieValue, AnalysisEngine ae) 
//			throws EmptyInfoException, UserNotLoggedInException, 
//			AdminNotLoggedInException, DatabaseException {
//
//		//check if admin is logged in
//		if(!isUserAdmin()) {
//			throw new AdminNotLoggedInException();
//		}
//
//		//check if ae info is empty
//		if(ae.getName().isEmpty() || ae.getDescriptorFileName().isEmpty()) {
//			throw new EmptyInfoException();
//		}
//
//		logger.info("Adding new AE...");
//
//		String insertStr = "INSERT INTO "
//				+ "analysis_engine (name, description, descriptor_file_name, create_timestamp) "
//				+ "VALUES (?, ?, ?, CURRENT_TIMESTAMP)";
//		try {
//			PreparedStatement ps = dbConnection.prepareStatement(insertStr);
//			ps.setString(1, ae.getName());
//			ps.setString(2, ae.getDescription());
//			ps.setString(3, ae.getDescriptorFileName());
//
//			ps.executeUpdate();
//
//		} catch (SQLException e) {
//			logger.warn("DB error occured while adding new AE: " + e.getMessage());
//			throw new DatabaseException(e.getMessage());
//		}
//
//		logger.info("New AE added to DB.");
//		return null;
//	}

	@Override
	public long getCFCount(String userCookieValue) 
			throws UserNotLoggedInException, AdminNotLoggedInException, DatabaseException {
		long cfCount = 0;

		//check if admin is logged in
		if(!isUserAdmin()) {
			throw new AdminNotLoggedInException();
		}

		logger.info("Getting CF count...");

		//query database
		String queryStr = ""
				+ "SELECT COUNT(id) "
				+ "FROM complexity_feature ";

		PreparedStatement ps;
		try {
			ps = dbConnection.prepareStatement(queryStr);
			ResultSet rs = ps.executeQuery();
			if(rs.isBeforeFirst()) {
				rs.next();
				cfCount = rs.getLong("count");
				logger.info("Found " + cfCount + " CF. Returning to the client...");
			} else {
				logger.warn("DB returns no results.");
			}
		} catch (SQLException e) {
			logger.warn("DB error: " + e.getMessage());
			throw new DatabaseException(e.getMessage());
		}

		return cfCount;
	}

	@Override
	public List<ComplexityFeature> getCFList(String userCookieValue, int offset, int limit)
			throws UserNotLoggedInException, AdminNotLoggedInException, DatabaseException {

		//check if admin is logged in
		if(!isUserAdmin()) {
			throw new AdminNotLoggedInException();
		}

		ArrayList<ComplexityFeature> cfList = new ArrayList<>(limit);

		//get data from database;
		logger.info("Getting CF list from DB...");
		String queryStr = ""
				+ "SELECT id, name, description, create_timestamp "
				+ "FROM complexity_feature "
				+ "ORDER BY id DESC "
				+ "LIMIT ? OFFSET ?";
		PreparedStatement ps;
		try {
			ps = dbConnection.prepareStatement(queryStr);
			ps.setInt(1, limit);
			ps.setInt(2, offset);

			ResultSet rs = ps.executeQuery();

			// get infomation of all complexity feature

			while(rs.next()) {
				ComplexityFeature cf = new ComplexityFeature();
				cf.setId(rs.getLong("id"));
				cf.setName(rs.getString("name"));
				cf.setDescription(rs.getString("description"));
				cf.setCreateDate(rs.getDate("create_timestamp"));
				cf.setAeList(new ArrayList<AnalysisEngine>());

				//get the analysis engines for this CF
				queryStr = ""
						+ "SELECT analysis_engine.id, analysis_engine.name "
						+ "FROM cf_ae "
						+ "JOIN analysis_engine ON cf_ae.ae_id=analysis_engine.id "
						+ "WHERE cf_ae.cf_id=?";
				ps = dbConnection.prepareStatement(queryStr);
				ps.setLong(1, cf.getId());
				ResultSet aeRS = ps.executeQuery();
				while(aeRS.next()) {
					AnalysisEngine ae = new AnalysisEngine();
					ae.setId(aeRS.getLong("id"));
					ae.setName(aeRS.getString("name"));
					cf.getAeList().add(ae);
				}

				cfList.add(cf);
			}


		} catch (SQLException e) {
			logger.warn("Database error: " + e.getMessage());
			throw new DatabaseException(e.getMessage());
		}

		logger.info("CF list obtained, returning to client...");
		return cfList;
	}

	@Override
	public Void updateCF(String userCookieValue, ComplexityFeature cf)
			throws EmptyInfoException, UserNotLoggedInException, 
			AdminNotLoggedInException, DatabaseException {

		//check if admin is logged in
		if(!isUserAdmin()) {
			throw new AdminNotLoggedInException();
		}

		//check if cf info is empty
		if(cf.getName().isEmpty()) {
			throw new EmptyInfoException();
		}

		//update CF table
		logger.info("Updating CF info..."); 
		String updateStr = ""
				+ "UPDATE complexity_feature "
				+ "SET name=?, description=? "
				+ "WHERE id=?";
		try {
			PreparedStatement ps = dbConnection.prepareStatement(updateStr);
			ps.setString(1, cf.getName());
			ps.setString(2, cf.getDescription());
			ps.setLong(3, cf.getId());
			ps.executeUpdate();
		} catch (SQLException e) {
			logger.warn("Error occured while updating CF: " + e.getMessage());
			throw new DatabaseException(e.getMessage());
		}
		logger.info("CF info updated.");

		//update cf_ae table
		//clear cf_ae entries related to the CF
		logger.info("Updating cf_ae table...");
		String deleteStr = ""
				+ "DELETE FROM cf_ae "
				+ "WHERE cf_id=?";
		try {
			PreparedStatement ps = dbConnection.prepareStatement(deleteStr);
			ps.setLong(1, cf.getId());
			ps.executeUpdate();
		} catch (SQLException e) {
			logger.warn("DB error occurred: " + e.getMessage());
			throw new DatabaseException(e.getMessage());
		}

		String insertStr = ""
				+ "INSERT INTO cf_ae (cf_id, ae_id) "
				+ "VALUES (?, ?)";
		for(AnalysisEngine ae: cf.getAeList()) {
			try {
				PreparedStatement ps = dbConnection.prepareStatement(insertStr);
				ps.setLong(1, cf.getId());
				ps.setLong(2, ae.getId());
				ps.executeUpdate();
			} catch (SQLException e) {
				logger.warn("DB error occurred: " + e.getMessage());
				throw new DatabaseException(e.getMessage());
			}
		}
		logger.info("cf_ae table updated. CF updated successfully!");

		return null;
	}

	@Override
	public Void deleteCF(String userCookieValue, ComplexityFeature cf)
			throws UserNotLoggedInException, AdminNotLoggedInException, DatabaseException {
		//check if admin is logged in
		if(!isUserAdmin()) {
			throw new AdminNotLoggedInException();
		}

		logger.info("Deleting CF..."); 
		String deleteStr = ""
				+ "DELETE FROM complexity_feature "
				+ "WHERE id=?";
		try {
			PreparedStatement ps = dbConnection.prepareStatement(deleteStr);
			ps.setLong(1, cf.getId());
			ps.executeUpdate();
		} catch (SQLException e) {
			logger.warn("Error occured while deleting CF record... " + e.getMessage());
			throw new DatabaseException(e.getMessage());
		}

		logger.info("CF deleted.");
		return null;
	}

	@Override
	public Void addCF(String userCookieValue, ComplexityFeature cf)
			throws EmptyInfoException, UserNotLoggedInException, 
			AdminNotLoggedInException, DatabaseException {
		//check if admin is logged in
		if(!isUserAdmin()) {
			throw new AdminNotLoggedInException();
		}

		//check if cf info is empty
		if(cf.getName().isEmpty()) {
			throw new EmptyInfoException();
		}

		logger.info("Adding new CF...");	

		String insertStr;
		PreparedStatement ps;
		try {
			//add to complexity_feature table, gets the inserted id 
			//(**this is cool and only postgresql supports it)
			insertStr = "INSERT INTO complexity_feature "
					+ "(name, description, create_timestamp) "
					+ "VALUES (?, ?, CURRENT_TIMESTAMP) "
					+ "RETURNING id";
			ps = dbConnection.prepareStatement(insertStr);
			ps.setString(1, cf.getName());
			ps.setString(2, cf.getDescription());
			ResultSet rs = ps.executeQuery();

			//get the newly inserted 
			if(rs.next()) {
				int cfID = rs.getInt("id");

				//now add to cf_ae table
				insertStr = "INSERT INTO cf_ae (cf_id, ae_id) "
						+ "VALUES (?, ?)";
				ps = dbConnection.prepareStatement(insertStr);
				for(AnalysisEngine ae: cf.getAeList()) {
					ps.setLong(1, cfID);
					ps.setLong(2, ae.getId());
					ps.executeUpdate();
				}
			}

		} catch (SQLException e) {
			logger.warn("DB error occured while adding new CF: " + e.getMessage());
			throw new DatabaseException(e.getMessage());
		}

		logger.info("New CF added to DB.");

		return null;
	}

	private void createFeatureSetTable() throws DatabaseException {
		String createTableStr = ""
				+ "CREATE TABLE IF NOT EXISTS feature_set("
				+ "id BIGSERIAL PRIMARY KEY NOT NULL ,"
				+ "owner_id BIGINT NOT NULL REFERENCES user_account(id) ON DELETE CASCADE,"
				+ "name TEXT NOT NULL,"
				+ "description TEXT,"
				+ "create_timestamp TIMESTAMP NOT NULL" + ");"
				+ "";
		try (
				PreparedStatement ps = dbConnection.prepareStatement(createTableStr)
				){
			ps.execute();
		} catch (SQLException e) {
			throw new DatabaseException(e.getMessage());
		}
	}

	private void createFS_CFTable() throws DatabaseException {
		String createTableStr = ""
				+ "CREATE TABLE IF NOT EXISTS fs_cf("
				+ "id BIGSERIAL PRIMARY KEY NOT NULL ,"
				+ "fs_id BIGINT NOT NULL REFERENCES feature_set(id) ON DELETE CASCADE,"
				+ "cf_id BIGINT NOT NULL REFERENCES analysis_engine(id) ON DELETE CASCADE"
				+ ")"
				+ "";
		try (
				PreparedStatement ps = dbConnection.prepareStatement(createTableStr)
				){
			ps.execute();
		} catch (SQLException e) {
			throw new DatabaseException(e.getMessage());
		}
	}

	private void createGR_TETable() throws DatabaseException {
		String createTableStr = ""
				+ "CREATE TABLE IF NOT EXISTS gr_te("
				+ "id BIGSERIAL PRIMARY KEY NOT NULL ,"
				+ "gr_id BIGINT NOT NULL REFERENCES grouping(id) ON DELETE CASCADE,"
				+ "te_id BIGINT NOT NULL REFERENCES text(id) ON DELETE CASCADE"
				+ ")"
				+ "";
		try (
				PreparedStatement ps = dbConnection.prepareStatement(createTableStr)
				){
			ps.execute();
		} catch (SQLException e) {
			throw new DatabaseException(e.getMessage());
		}
	}

	private void createTA_TETable() throws DatabaseException {
		String createTableStr = ""
				+ "CREATE TABLE IF NOT EXISTS ta_te("
				+ "ta_id BIGINT NOT NULL REFERENCES tag(id) ON DELETE CASCADE,"
				+ "te_id BIGINT NOT NULL REFERENCES text(id) ON DELETE CASCADE,"
				+ "UNIQUE(ta_id, te_id)"
				+ ")"
				+ "";
		try (
				PreparedStatement ps = dbConnection.prepareStatement(createTableStr)
				){
			ps.execute();
		} catch (SQLException e) {
			throw new DatabaseException(e.getMessage());
		}
	}

	private void createAnalysisTable() throws DatabaseException {
		String createTableStr = ""
				+ "CREATE TABLE IF NOT EXISTS analysis("
				+ "id BIGSERIAL PRIMARY KEY NOT NULL ,"
				+ "owner_id BIGINT NOT NULL REFERENCES user_account(id) ON DELETE CASCADE,"
				+ "name TEXT NOT NULL,"
				+ "description TEXT,"
				+ "corpus_id BIGINT NOT NULL REFERENCES corpus(id) ON DELETE CASCADE,"
				+ "tag_filter_logic VARCHAR(50),"
				+ "tag_filter_keyword TEXT,"
				+ "featureset_id BIGINT NOT NULL REFERENCES feature_set(id) ON DELETE CASCADE,"
				+ "create_timestamp TIMESTAMP NOT NULL" + ");"
				+ "";
		try (
				PreparedStatement ps = dbConnection.prepareStatement(createTableStr)
				){
			ps.execute();
		} catch (SQLException e) {
			throw new DatabaseException(e.getMessage());
		}
	}

	private void createResultTable() throws DatabaseException {
		String createTableStr = ""
				+ "CREATE TABLE IF NOT EXISTS result("
				+ "id BIGSERIAL PRIMARY KEY NOT NULL ,"
				+ "analysis_id BIGINT NOT NULL REFERENCES analysis(id) ON DELETE CASCADE,"
				+ "text_id BIGINT NOT NULL REFERENCES text(id) ON DELETE CASCADE,"
				+ "feature_id BIGINT NOT NULL REFERENCES analysis_engine(id) ON DELETE CASCADE,"
				+ "value NUMERIC NOT NULL"
				+ ")";
		try (
				PreparedStatement ps = dbConnection.prepareStatement(createTableStr)
				){
			ps.execute();
		} catch (SQLException e) {
			throw new DatabaseException(e.getMessage());
		}
	}

	private void createGroupingTable() throws DatabaseException {
		String createTableStr = ""
				+ "CREATE TABLE IF NOT EXISTS grouping("
				+ "id BIGSERIAL PRIMARY KEY NOT NULL ,"
				+ "analysis_id BIGINT NOT NULL REFERENCES analysis(id) ON DELETE CASCADE,"
				+ "name TEXT NOT NULL,"
				+ "description TEXT,"
				+ "create_timestamp TIMESTAMP NOT NULL" + ");"
				+ "";
		try (
				PreparedStatement ps = dbConnection.prepareStatement(createTableStr)
				){
			ps.execute();
		} catch (SQLException e) {
			throw new DatabaseException(e.getMessage());
		}
	}


	private void clearGroupingTable() throws DatabaseException {
		logger.info("Clearing grouping table...");
		String getDeletedIDStr = ""
				+ "SELECT id FROM grouping "
				+ "WHERE is_deleted IS TRUE";
		PreparedStatement ps;
		try {
			ps=dbConnection.prepareStatement(getDeletedIDStr);
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				int deletedID = rs.getInt("id");

				String delDependencyRecords = ""
						+ "DELETE FROM gr_te "
						+ "WHERE gr_id=?";
				PreparedStatement psDelDepenRecords = 
						dbConnection.prepareStatement(delDependencyRecords);
				psDelDepenRecords.setInt(1, deletedID);
				psDelDepenRecords.execute();
			}
			logger.info("Done!");
		} catch (SQLException e) {
			throw new DatabaseException(e.getMessage());
		}
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
	public boolean isUserAdmin()
			throws UserNotLoggedInException, DatabaseException {
		return isUserAdmin(getThreadLocalRequest());
	}
	
	public boolean isUserAdmin(HttpServletRequest request)
			throws UserNotLoggedInException, DatabaseException {
		String serviceName = "isUserAdmin";
		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
				new ServiceRequestReceivedMessage(serviceName));

		//verify user cookies and get user id
		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
				new VerifyingUserCookiesMessage(serviceName));
		UserInfo userInfo = userServiceImpl.verifyUserCookies(request);

		boolean isAdmin = false;

		if(userInfo != null && userInfo.getEmail().equals(ServerProperties.ADMINEMAIL)) {
			//the user is admin
			isAdmin = true;
		}

		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
				new ServiceRequestCompletedMessage(serviceName));
		return isAdmin;
	}
	
	@Override
	public void importAE()
			throws UserNotLoggedInException, AdminNotLoggedInException, 
			DatabaseException, ServerIOException, UIMAException, AEDependencyException {
		String serviceName = "importAE";
		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
				new ServiceRequestReceivedMessage(serviceName));

		if(!isUserAdmin()) {
			throw logger.throwing(new AdminNotLoggedInException());
		}
		
		//import the AE descriptor files
		try {
			ImportAE importAE = new ImportAE();
			importAE.importFeatureUIMADescriptors();
		} catch (IOException e) {
			throw logger.throwing(new ServerIOException());
		} catch (SQLException e) {
			throw logger.throwing(new DatabaseException());
		}

		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
				new ServiceRequestCompletedMessage(serviceName));
	}
}

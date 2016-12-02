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

import com.ctapweb.web.client.service.ResultVisualizerService;
import com.ctapweb.web.server.DBConnectionManager;
import com.ctapweb.web.server.analysis.AnalysisUtils;
import com.ctapweb.web.server.logging.LogMarker;
import com.ctapweb.web.server.logging.ServiceRequestCompletedMessage;
import com.ctapweb.web.shared.Analysis;
import com.ctapweb.web.shared.AnalysisEngine;
import com.ctapweb.web.shared.PlotData;
import com.ctapweb.web.shared.exception.AccessToResourceDeniedException;
import com.ctapweb.web.shared.exception.DatabaseException;
import com.ctapweb.web.shared.exception.UserNotLoggedInException;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class ResultVisualizerServiceImpl extends RemoteServiceServlet 
implements ResultVisualizerService {

	private static final Logger logger = LogManager.getLogger();
	Connection dbConnection = DBConnectionManager.getDbConnection();
	UserServiceImpl userServiceImpl = new UserServiceImpl();
	AnalysisGeneratorServiceImpl analysisGeneratorServiceImpl = new AnalysisGeneratorServiceImpl();

	private boolean isUserGroupOwner(long userID, long groupID) 
			throws DatabaseException {
		boolean isUserOwner = false;

		//check if the logged in user is the same as the group set owner
		String queryStr =  "SELECT grouping.id "
				+ "FROM grouping, analysis "
				+ "WHERE grouping.analysis_id=analysis.id "
				+ "     AND grouping.id=? AND analysis.owner_id=? ";
		PreparedStatement ps;
		try {
			ps = dbConnection.prepareStatement(queryStr);
			ps.setLong(1, groupID);
			ps.setLong(2, userID);
			ResultSet rs = ps.executeQuery();
			if(rs.isBeforeFirst()) {
				isUserOwner = true;
			} else {
				logger.warn("User " + userID + " trying to access group " 
						+ groupID+", which is not owned by the user. Operation forbidden.");
			}
		} catch (SQLException e) {
			throw new DatabaseException(e.getMessage());
		}

		return isUserOwner;
	}

	private boolean isUserTextOwner(long userID, long textID) 
			throws DatabaseException {
		boolean isUserOwner = false;

		//check if the logged in user is the same as the text owner
		String queryStr =  ""
				+ "SELECT text.id "
				+ "FROM text, corpus "
				+ "WHERE text.corpus_id=corpus.id "
				+ "     AND corpus.owner_id=? "
				+ "     AND text.id=?";
		PreparedStatement ps;
		try {
			ps = dbConnection.prepareStatement(queryStr);
			ps.setLong(1, userID);
			ps.setLong(2, textID);
			ResultSet rs = ps.executeQuery();
			if(rs.isBeforeFirst()) {
				isUserOwner = true;
			} else {
				logger.warn("User " + userID + " trying to access text " 
						+ textID+", which is not owned by the user. Operation forbidden.");
			}
		} catch (SQLException e) {
			throw new DatabaseException(e.getMessage());
		}

		return isUserOwner;
	}

	//	@Override
	//	public Integer getGroupCount(long analysisID)
	//			throws UserNotLoggedInException, AccessToResourceDeniedException, DatabaseException {
	//		String serviceName = "getGroupCount";
	//		long userID = logServiceStartAndGetUserID(serviceName); 
	//
	//		Integer groupCount = 0;
	//
	//		try {
	//			if(!AnalysisUtils.isUserAnalysisOwner(userID, analysisID)) {
	//				throw new AccessToResourceDeniedException();
	//			}
	//			
	//			logger.info("Getting group  count for user (id) " + userID + " on analysis " + analysisID + "...");
	//			String queryStr = "SELECT COUNT(id) FROM grouping "
	//					+ "WHERE analysis_id=? ";
	//			PreparedStatement ps = dbConnection.prepareStatement(queryStr);
	//			ps.setLong(1, analysisID);
	//
	//			ResultSet rs =ps.executeQuery(); 
	//
	//			if(rs.next()) {
	//				groupCount = rs.getInt("count");
	//
	//				logger.info("Found " + groupCount + " group sets for user (id) " + userID + ".");
	//			} else {
	//				logger.info("DB returns not result.");
	//			}
	//
	//		} catch (SQLException e) {
	//			throw new DatabaseException(e.getMessage());
	//		}
	//
	//		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
	//				new ServiceRequestCompletedMessage(serviceName));
	//		return groupCount;
	//	}

	//	@Override
	//	public List<Group> getGroupList(long analysisID, int offset, int limit)
	//			throws UserNotLoggedInException, AccessToResourceDeniedException, DatabaseException {
	//		String serviceName = "getGroupList";
	//		long userID = logServiceStartAndGetUserID(serviceName); 
	//
	//		ArrayList<Group> groupList = new ArrayList<Group>(limit); 
	//
	//		try {
	//			if(!AnalysisUtils.isUserAnalysisOwner(userID, analysisID)) {
	//				throw new AccessToResourceDeniedException();
	//			}
	//			
	//			//get data from database
	//			String queryStr = ""
	//					+ "SELECT id, analysis_id, name, description, create_timestamp "
	//					+ "FROM grouping "
	//					+ "WHERE analysis_id=? "
	//					+ "ORDER BY id DESC "
	//					+ "LIMIT ? OFFSET ?";
	//			PreparedStatement ps;
	//			ps = dbConnection.prepareStatement(queryStr);
	//			ps.setLong(1, analysisID);
	//			ps.setInt(2, limit);
	//			ps.setInt(3, offset);
	//
	//			ResultSet rs = ps.executeQuery();
	//
	//			// get infomation of all group sets
	//			while(rs.next()) {
	//				Group groupSet = new Group();
	//				groupSet.setId(rs.getLong("id"));
	//				groupSet.setAnalysisID(rs.getLong("analysis_id"));
	//				groupSet.setName(rs.getString("name"));
	//				groupSet.setDescription(rs.getString("description"));
	//				groupSet.setCreateDate(rs.getDate("create_timestamp"));
	//				groupList.add(groupSet);
	//			}
	//
	//
	//		} catch (SQLException e) {
	//			logger.warn("Database error: " + e.getMessage());
	//			throw new DatabaseException(e.getMessage());
	//		}
	//
	//		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
	//				new ServiceRequestCompletedMessage(serviceName));
	//		return groupList;
	//	}
	//


	//	@Override
	//	public Void addGroup(Group group)
	//			throws EmptyInfoException, UserNotLoggedInException, AccessToResourceDeniedException, DatabaseException {
	//		String serviceName = "addGroup";
	//		long userID = logServiceStartAndGetUserID(serviceName); 
	//
	//		//check if group set info is empty
	//		if(group.getName().isEmpty() || 
	//				group.getAnalysisID() == 0) {
	//			throw new EmptyInfoException();
	//		}
	//
	//		String insertStr = "INSERT INTO grouping (analysis_id, name, description, create_timestamp) "
	//				+ "VALUES (?, ?, ?, CURRENT_TIMESTAMP)";
	//		try {
	//			PreparedStatement ps = dbConnection.prepareStatement(insertStr);
	//			ps.setLong(1, group.getAnalysisID());
	//			ps.setString(2, group.getName());
	//			ps.setString(3, group.getDescription());
	//
	//			ps.executeUpdate();
	//
	//		} catch (SQLException e) {
	//			logger.warn("DB error occured while adding new group set: " + e.getMessage());
	//			throw new DatabaseException(e.getMessage()); 
	//		}
	//
	//		logger.info("New group set added to DB.");
	//		return null;
	//	}

	//	@Override
	//	public Void updateGroup(Group group)
	//			throws EmptyInfoException, UserNotLoggedInException, AccessToResourceDeniedException, DatabaseException {
	//		String serviceName = "updateGroup";
	//		long userID = logServiceStartAndGetUserID(serviceName); 
	//
	//		//check if info complete
	//		if(group.getId() == 0 || group.getName().isEmpty() ||
	//				group.getAnalysisID() == 0) {
	//			throw new EmptyInfoException();
	//		}
	//
	//		//check if user owner of the group 
	//		if(!isUserGroupOwner(userID, group.getId())) {
	//			throw new AccessToResourceDeniedException();
	//		}
	//
	//		PreparedStatement ps;
	//
	//		try {
	//			//check passed, update corpus
	//			String updateStr = ""
	//					+ "UPDATE grouping SET "
	//					+ "name=?, description=? "
	//					+ "WHERE id = ? AND analysis_id = ? ";
	//			ps = dbConnection.prepareStatement(updateStr);
	//			ps.setString(1, group.getName());
	//			ps.setString(2, group.getDescription());
	//			ps.setLong(3, group.getId());
	//			ps.setLong(4, group.getAnalysisID());
	//			ps.executeUpdate();
	//
	//		} catch (SQLException e) {
	//			logger.warn("Error occured while updating group info... " + e.getMessage());
	//			throw new DatabaseException(e.getMessage());
	//		}
	//
	//		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
	//				new ServiceRequestCompletedMessage(serviceName));
	//		return null;
	//	}

	//	@Override
	//	public Void deleteGroup(Group group)
	//			throws UserNotLoggedInException, AccessToResourceDeniedException, DatabaseException {
	//		String serviceName = "deleteGroup";
	//		long userID = logServiceStartAndGetUserID(serviceName); 
	//
	//		//check if user owner of the feature set
	//		if(!isUserGroupOwner(userID, group.getId())) {
	//			throw new AccessToResourceDeniedException();
	//		}
	//
	//		logger.info("Deleting group " + group.getId() + "...");
	//		PreparedStatement ps;
	//		String queryStr;
	//		try {
	//			queryStr = "DELETE FROM grouping "
	//					+ "WHERE id=? AND analysis_id=?";
	//			ps = dbConnection.prepareStatement(queryStr);
	//			ps.setLong(1, group.getId());
	//			ps.setLong(2, group.getAnalysisID());
	//			ps.executeUpdate();
	//		} catch (SQLException e) {
	//			logger.warn("Error occured while deleting group set record... " + e.getMessage());
	//			throw new DatabaseException(e.getMessage());
	//		}
	//
	//		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
	//				new ServiceRequestCompletedMessage(serviceName));
	//		return null;
	//	}

	//	@Override
	//	public Group getGroup(long groupID)
	//			throws UserNotLoggedInException, AccessToResourceDeniedException, DatabaseException {
	//		String serviceName = "getGroup";
	//		long userID = logServiceStartAndGetUserID(serviceName); 
	//
	//		Group group = new Group();
	//
	//		if(!isUserGroupOwner(userID, groupID)) {
	//			throw new AccessToResourceDeniedException();
	//		}
	//
	//		logger.info("Getting group info  for group id " + groupID + "...");
	//		String queryStr = "SELECT id, analysis_id, name, description FROM grouping "
	//				+ "WHERE id=? ";
	//		try {
	//			PreparedStatement ps = dbConnection.prepareStatement(queryStr);
	//			ps.setLong(1, groupID);
	//
	//			ResultSet rs =ps.executeQuery(); 
	//
	//			if(rs.next()) {
	//				group.setId(rs.getLong("id"));
	//				group.setAnalysisID(rs.getLong("analysis_id"));
	//				group.setName(rs.getString("name"));
	//				group.setDescription(rs.getString("description"));
	//
	//				logger.info("Obtained group info  for " + groupID + ".");
	//			} else {
	//				logger.info("DB returns no result.");
	//			}
	//
	//		} catch (SQLException e) {
	//			throw new DatabaseException(e.getMessage());
	//		}
	//
	//		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
	//				new ServiceRequestCompletedMessage(serviceName));
	//		return group;
	//	}

	//	@Override
	//	public Integer getGroupTextCount(long groupID)
	//			throws UserNotLoggedInException, AccessToResourceDeniedException, DatabaseException {
	//		String serviceName = "getGroupTextCount";
	//		long userID = logServiceStartAndGetUserID(serviceName); 
	//
	//		int groupTextCount = 0;
	//
	//		//check if user owner of the group
	//		if(!isUserGroupOwner(userID, groupID)) {
	//			throw new AccessToResourceDeniedException();
	//		}
	//
	//		String queryStr = ""
	//				+ "SELECT COUNT(id) FROM gr_te "
	//				+ "WHERE gr_id=?";
	//		try {
	//
	//			PreparedStatement ps = dbConnection.prepareStatement(queryStr);
	//			ps.setLong(1, groupID);
	//			ResultSet rs = ps.executeQuery();
	//			if(rs.next()) {
	//				groupTextCount = rs.getInt("count");
	//			}
	//
	//		} catch (SQLException e) {
	//			throw new DatabaseException(e.getMessage());
	//		}
	//
	//		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
	//				new ServiceRequestCompletedMessage(serviceName));
	//		return groupTextCount;
	//	}

	//	@Override
	//	public Integer getAllAnalyzedTextCount(long groupID)
	//			throws UserNotLoggedInException, AccessToResourceDeniedException, DatabaseException {
	//		String serviceName = "getAllAnalyzedTextCount";
	//		long userID = logServiceStartAndGetUserID(serviceName); 
	//
	//		int allAnalyzedTextCount = 0;
	//		long analysisID = getGroup(groupID).getAnalysisID();
	//
	//		try {
	//			if(!AnalysisUtils.isUserAnalysisOwner(userID, analysisID)) {
	//				throw new AccessToResourceDeniedException();
	//			}
	//
	//			logger.info("Getting all analyzed text count for analysis (id) " + analysisID + "...");
	//			String queryStr = ""
	//					+ "SELECT COUNT(text.id) "
	//					+ "FROM analysis, text "
	//					+ "WHERE analysis.corpus_id=text.corpus_id "
	//					+ "     AND analysis.id=? ";
	//			PreparedStatement ps = dbConnection.prepareStatement(queryStr);
	//			ps.setLong(1, analysisID);
	//			ResultSet rs = ps.executeQuery();
	//			if(rs.next()) {
	//				allAnalyzedTextCount = rs.getInt("count");
	//			}
	//
	//		} catch (SQLException e) {
	//			throw new DatabaseException(e.getMessage());
	//		}
	//
	//		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
	//				new ServiceRequestCompletedMessage(serviceName));
	//		return allAnalyzedTextCount;
	//	}

	//	@Override
	//	public List<CorpusText> getGroupTextList(long groupID, int offset, int limit)
	//			throws UserNotLoggedInException, AccessToResourceDeniedException, DatabaseException {
	//		String serviceName = "getGroupTextList";
	//		long userID = logServiceStartAndGetUserID(serviceName); 
	//
	//		ArrayList<CorpusText> groupTextList = new ArrayList<CorpusText>(limit); 
	//
	//		//check if user owner of the group
	//		if(!isUserGroupOwner(userID, groupID)) {
	//			throw new AccessToResourceDeniedException();
	//		}
	//
	//		//get data from database
	//		String queryStr = ""
	//				+ "SELECT text.id, text.title "
	//				+ "FROM gr_te, text "
	//				+ "WHERE gr_te.te_id=text.id "
	//				+ "     AND gr_te.gr_id=?"
	//				+ "ORDER BY text.id DESC "
	//				+ "LIMIT ? OFFSET ?";
	//		PreparedStatement ps;
	//		try {
	//			ps = dbConnection.prepareStatement(queryStr);
	//			ps.setLong(1, groupID);
	//			ps.setInt(2, limit);
	//			ps.setInt(3, offset);
	//
	//			ResultSet rs = ps.executeQuery();
	//
	//			// get infomation of all texts included in this group
	//			while(rs.next()) {
	//				CorpusText text = new CorpusText();
	//				text.setId(rs.getLong("id"));
	//				text.setTitle(rs.getString("title"));
	//				groupTextList.add(text);
	//			}
	//		} catch (SQLException e) {
	//			throw new DatabaseException(e.getMessage());
	//		}
	//
	//		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
	//				new ServiceRequestCompletedMessage(serviceName));
	//		return groupTextList;
	//	}

	//	@Override
	//	public List<CorpusText> getAllAnalyzedTextList(long groupID, int offset, int limit)
	//			throws UserNotLoggedInException, AccessToResourceDeniedException, DatabaseException {
	//		String serviceName = "getAllAnalyzedTextList";
	//		long userID = logServiceStartAndGetUserID(serviceName); 
	//
	//		ArrayList<CorpusText> allAnalyzedTextList = new ArrayList<CorpusText>(limit); 
	//		long analysisID = getGroup(groupID).getAnalysisID();
	//
	//		try {
	//			//check if user owner of the group
	//			if(!AnalysisUtils.isUserAnalysisOwner(userID, analysisID)) {
	//				throw new AccessToResourceDeniedException();
	//			}
	//
	//			//get data from database
	//			String queryStr = ""
	//					+ "SELECT text.id, text.title "
	//					+ "FROM analysis, text "
	//					+ "WHERE analysis.id=? "
	//					+ "     AND analysis.corpus_id=text.corpus_id "
	//					+ "ORDER BY text.id DESC "
	//					+ "LIMIT ? OFFSET ?";
	//			PreparedStatement ps;
	//			ps = dbConnection.prepareStatement(queryStr);
	//			ps.setLong(1, analysisID);
	//			ps.setInt(2, limit);
	//			ps.setInt(3, offset);
	//
	//			ResultSet rs = ps.executeQuery();
	//
	//			// get infomation of all texts included in this group
	//			while(rs.next()) {
	//				CorpusText text = new CorpusText();
	//				text.setId(rs.getLong("id"));
	//				text.setTitle(rs.getString("title"));
	//				allAnalyzedTextList.add(text);
	//			}
	//		} catch (SQLException e) {
	//			throw new DatabaseException(e.getMessage());
	//		}
	//
	//		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
	//				new ServiceRequestCompletedMessage(serviceName));
	//		return allAnalyzedTextList;
	//	}

	//	@Override
	//	public Void addTextToGroup(long textID, long groupID)
	//			throws UserNotLoggedInException, AccessToResourceDeniedException, DatabaseException {
	//		String serviceName = "addTextToGroup";
	//		long userID = logServiceStartAndGetUserID(serviceName); 
	//
	//		//check if user owner of the group
	//		if(!isUserGroupOwner(userID, groupID) || 
	//				!isUserTextOwner(userID, textID)) {
	//			throw new AccessToResourceDeniedException();
	//		}
	//
	//		try {
	//			//check if text already in group
	//			String queryStr = ""
	//					+ "SELECT id " 
	//					+ "FROM gr_te "
	//					+ "WHERE gr_id=? AND te_id=?";
	//			PreparedStatement ps = dbConnection.prepareStatement(queryStr);
	//			ps.setLong(1, groupID);
	//			ps.setLong(2, textID);
	//
	//			ResultSet rs = ps.executeQuery();
	//			if(rs.isBeforeFirst()) {
	//				return null; //text already in group, does nothing
	//			}
	//
	//			//add feature to feature set
	//			String insertStr = ""
	//					+ "INSERT INTO gr_te (gr_id, te_id) "
	//					+ "VALUES(?, ?)";
	//			ps = dbConnection.prepareStatement(insertStr);
	//			ps.setLong(1, groupID);
	//			ps.setLong(2, textID);
	//			ps.executeUpdate();
	//		} catch (SQLException e) {
	//			logger.warn("DB error: " + e.getMessage());
	//			throw new DatabaseException(e.getMessage());
	//		}
	//
	//		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
	//				new ServiceRequestCompletedMessage(serviceName));
	//		return null;
	//	}

	//	@Override
	//	public Void removeTextFromGroup(long textID, long groupID)
	//			throws UserNotLoggedInException, AccessToResourceDeniedException, DatabaseException {
	//		String serviceName = "removeTextFromGroup";
	//		long userID = logServiceStartAndGetUserID(serviceName); 
	//
	//		//check if user owner of the resources
	//		if(!isUserGroupOwner(userID, groupID) || 
	//				!isUserTextOwner(userID, textID)) {
	//			throw new AccessToResourceDeniedException();
	//		}
	//		String removeStr = ""
	//				+ "DELETE FROM gr_te "
	//				+ "WHERE gr_id=? AND te_id=?";
	//		try {
	//			PreparedStatement ps = dbConnection.prepareStatement(removeStr);
	//			ps.setLong(1, groupID);
	//			ps.setLong(2, textID);
	//			ps.executeUpdate();
	//		} catch (SQLException e) {
	//			throw new DatabaseException(e.getMessage());
	//		}
	//
	//		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
	//				new ServiceRequestCompletedMessage(serviceName));
	//		return null;
	//	}

	@Override
	public List<PlotData> getPlotData(long analysisID, long featureID, String statistics)
			throws UserNotLoggedInException, AccessToResourceDeniedException, DatabaseException {
		String serviceName = "getPlotData";
		List<PlotData> plotDataList = new ArrayList<>();

		try {
			long userID = ServiceUtils.logServiceStartAndGetUserID(getThreadLocalRequest(), logger, serviceName); 

			//check if user owner of the resources
			if(!AnalysisUtils.isUserAnalysisOwner(userID, analysisID)) {
				throw logger.throwing(new AccessToResourceDeniedException());
			}

			String queryStr = "SELECT tag.name, " + statistics + "(result.value) AS value "
					+ "FROM result, text, ta_te, tag "
					+ "WHERE result.text_id=text.id "
					+ "     AND text.id=ta_te.te_id "
					+ "     AND ta_te.ta_id=tag.id "
					+ "     AND analysis_id=? "
					+ "     AND  feature_id=? "
					+ "     AND tag.name ilike ? "
					+ "GROUP BY tag.name, result.feature_id "
					+ "ORDER BY name";

			PreparedStatement ps = dbConnection.prepareStatement(queryStr);
			ps.setLong(1, analysisID);
			ps.setLong(2, featureID);
			ps.setString(3, AnalysisUtils.getTagFilterString(analysisID));
			ResultSet rs = ps.executeQuery();

			while(rs.next()) {
				PlotData plotData = new PlotData();
				plotData.setCategoryName(rs.getString("name"));
				plotData.setValue(rs.getDouble("value"));
				plotDataList.add(plotData);
			}
		} catch (SQLException e) {
			throw logger.throwing(new DatabaseException(e));
		}

		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
				new ServiceRequestCompletedMessage(serviceName));
		
		return plotDataList;
	}

	@Override
	public List<AnalysisEngine> getResultFeatureList(long analysisID)
			throws UserNotLoggedInException, AccessToResourceDeniedException, DatabaseException {
		String serviceName = "getResultFeatureList";
		long userID = ServiceUtils.logServiceStartAndGetUserID(getThreadLocalRequest(), logger, serviceName); 

		List<AnalysisEngine> featureList = new ArrayList<>();
		
		try {
			//check if user owner of analysis
			if(!AnalysisUtils.isUserAnalysisOwner(userID, analysisID)) {
				throw logger.throwing(new AccessToResourceDeniedException());
			}
			
			String queryStr = "SELECT DISTINCT result.feature_id, ae.name "
					+ "FROM result, analysis_engine AS ae "
					+ "WHERE result.feature_id=ae.id "
					+ "     AND result.analysis_id=?";
			PreparedStatement ps = dbConnection.prepareStatement(queryStr);
			ps.setLong(1, analysisID);
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				AnalysisEngine feature = new AnalysisEngine();
				feature.setId(rs.getLong("feature_id"));
				feature.setName(rs.getString("name"));
				
				featureList.add(feature);
			}

		} catch (SQLException e) {
			throw logger.throwing(new DatabaseException(e));
		}

		return featureList;
	}

	@Override
	public List<Analysis> getAnalysisList()
			throws UserNotLoggedInException, AccessToResourceDeniedException, DatabaseException {
		String serviceName = "getAnalysisList";
		long userID = ServiceUtils.logServiceStartAndGetUserID(getThreadLocalRequest(), logger, serviceName); 

		List<Analysis> analysisList = new ArrayList<>();
		
		try {
			String queryStr = "SELECT DISTINCT a.id, a.owner_id, a.name, a.description, a.corpus_id,"
					+ "a.tag_filter_logic, a.tag_filter_keyword, a.featureset_id, a.create_timestamp "
					+ "FROM result, analysis AS a "
					+ "WHERE result.analysis_id=a.id "
					+ "     AND a.owner_id=?";
			PreparedStatement ps = dbConnection.prepareStatement(queryStr);
			ps.setLong(1, userID);
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				Analysis analysis = new Analysis();
				analysis.setId(rs.getLong("id"));
				analysis.setOwner_id(rs.getLong("owner_id"));
				analysis.setName(rs.getString("name")); 
				analysis.setDescription(rs.getString("description")); 
				analysis.setCorpusID(rs.getLong("corpus_id"));
				analysis.setTagFilterLogic(rs.getString("tag_filter_logic"));
				analysis.setTagKeyword(rs.getString("tag_filter_keyword"));
				analysis.setFeatureSetID(rs.getLong("featureset_id"));
				analysis.setCreateDate(rs.getDate("create_timestamp"));

				analysisList.add(analysis);
			}
		} catch (SQLException e) {
			throw logger.throwing(new DatabaseException(e));
		}

		return analysisList;
	}

	//	@Override
	//	public Set<Tag> getResultCategories(Analysis analysis)
	//	 		throws UserNotLoggedInException, AccessToResourceDeniedException, DatabaseException {
	//		String serviceName = "getResultTagSet";
	//		long userID = ServiceUtils.logServiceStartAndGetUserID(
	//				getThreadLocalRequest(), logger, serviceName);
	//		
	//		Set<Tag> tagSet = new HashSet<>();
	//		long analysisID = analysis.getId();
	//		
	//		try {
	//			//check if user analysis owner
	//			if(!AnalysisUtils.isUserAnalysisOwner(userID, analysisID)) {
	//				throw logger.throwing(new AccessToResourceDeniedException());
	//			}
	//
	//			String queryStr = "SELECT DISTINCT tag.id, tag.name "
	//					+ "FROM result, text, ta_te, tag "
	//					+ "WHERE result.text_id=text.id "
	//					+ "     AND text.id=ta_te.te_id "
	//					+ "	  AND ta_te.ta_id=tag.id "
	//					+ "     AND tag.name ILIKE ? "
	//					+ "	  AND result.analysis_id=?";
	//			PreparedStatement ps = dbConnection.prepareStatement(queryStr);
	//			ps.setString(1, AnalysisUtils.getTagFilterString(analysis));
	//			ps.setLong(2, analysisID);
	//			ResultSet rs = ps.executeQuery();
	//			while(rs.next()) {
	//				Tag tag = new Tag();
	//				tag.setId(rs.getLong("id"));
	//				tag.setName(rs.getString("name"));
	//
	//				tagSet.add(tag);
	//			}
	//			
	//		} catch (SQLException e) {
	//			throw logger.throwing(new DatabaseException(e));
	//		}
	//
	//		return tagSet;
	//	}
}
package com.ctapweb.web.server.user;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.uima.collection.metadata.CpeDescriptorException;
import org.apache.uima.resource.ResourceInitializationException;

import com.ctapweb.web.client.service.AnalysisGeneratorService;
import com.ctapweb.web.server.DBConnectionManager;
import com.ctapweb.web.server.analysis.AnalysisUtils;
import com.ctapweb.web.server.analysis.RunAnalysis;
import com.ctapweb.web.server.logging.LogMarker;
import com.ctapweb.web.server.logging.ServiceRequestCompletedMessage;
import com.ctapweb.web.server.logging.ServiceRequestReceivedMessage;
import com.ctapweb.web.server.logging.VerifyingUserCookiesMessage;
import com.ctapweb.web.shared.Analysis;
import com.ctapweb.web.shared.AnalysisStatus;
import com.ctapweb.web.shared.UserInfo;
import com.ctapweb.web.shared.exception.AccessToResourceDeniedException;
import com.ctapweb.web.shared.exception.DatabaseException;
import com.ctapweb.web.shared.exception.EmptyInfoException;
import com.ctapweb.web.shared.exception.UIMAException;
import com.ctapweb.web.shared.exception.UserNotLoggedInException;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class AnalysisGeneratorServiceImpl extends RemoteServiceServlet 
implements AnalysisGeneratorService {

	private static final Logger logger = LogManager.getLogger();

	Connection dbConnection = DBConnectionManager.getDbConnection();
	UserServiceImpl userServiceImpl = new UserServiceImpl();
	FeatureSelectorServiceImpl featureSelectorServiceImpl = new FeatureSelectorServiceImpl();
	CorpusManagerServiceImpl corpusManagerServiceImpl = new CorpusManagerServiceImpl();
	

	@Override
	public Integer getAnalysisCount() 
			throws UserNotLoggedInException, DatabaseException {
		String serviceName = "getAnalysisCount";
		long userID = logServiceStartAndGetUserID(serviceName); 

		Integer analysisCount = 0;

		String queryStr = "SELECT COUNT(id) FROM analysis "
				+ "WHERE owner_id=? ";
		try {
			PreparedStatement ps = dbConnection.prepareStatement(queryStr);
			ps.setLong(1, userID);

			ResultSet rs =ps.executeQuery(); 
			if(	rs.next()) {
				analysisCount = rs.getInt("count");
			} 
		} catch (SQLException e) {
			throw logger.throwing(new DatabaseException(e.getMessage()));
		}

		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
				new ServiceRequestCompletedMessage(serviceName));
		return analysisCount;
	}

	@Override
	public List<Analysis> getAnalysisList(int offset, int limit)
			throws UserNotLoggedInException, DatabaseException {
		String serviceName = "getAnalysisList";
		long userID = logServiceStartAndGetUserID(serviceName); 

		ArrayList<Analysis> analysisList = new ArrayList<Analysis>(limit); 

		//get data from database
		String queryStr = ""
				+ "SELECT analysis.id, analysis.owner_id, analysis.name, "
				+ "analysis.description, "
				+ "analysis.corpus_id, corpus.name AS corpus_name, "
				+ "analysis.tag_filter_logic, tag_filter_keyword, "
				+ "analysis.featureset_id, feature_set.name AS featureset_name, "
				+ "analysis.create_timestamp "
				+ "FROM analysis "
				+ "JOIN corpus ON (analysis.corpus_id=corpus.id) "
				+ "JOIN feature_set ON (analysis.featureset_id=feature_set.id) "
				+ "WHERE analysis.owner_id=?"
				+ "ORDER BY analysis.id DESC "
				+ "LIMIT ? OFFSET ?";
		PreparedStatement ps;
		try {
			ps = dbConnection.prepareStatement(queryStr);
			ps.setLong(1, userID);
			ps.setInt(2, limit);
			ps.setInt(3, offset);

			ResultSet rs = ps.executeQuery();

			// get infomation of all analysis of this user 
			while(rs.next()) {
				Analysis analysis = new Analysis();
				analysis.setId(rs.getLong("id"));
				analysis.setOwner_id(rs.getLong("owner_id"));
				analysis.setName(rs.getString("name")); 
				analysis.setDescription(rs.getString("description")); 
				analysis.setCorpusID(rs.getLong("corpus_id"));
				analysis.setCorpusName(rs.getString("corpus_name"));
				analysis.setTagFilterLogic(rs.getString("tag_filter_logic"));
				analysis.setTagKeyword(rs.getString("tag_filter_keyword"));
				analysis.setCreateDate(rs.getDate("create_timestamp"));
				analysis.setFeatureSetID(rs.getLong("featureset_id"));
				analysis.setFeatureSetName(rs.getString("featureset_name"));

				analysisList.add(analysis);
			}
		} catch (SQLException e) {
			throw logger.throwing(new DatabaseException(e.getMessage()));
		}

		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
				new ServiceRequestCompletedMessage(serviceName));
		return analysisList;
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
	public void addAnalysis(Analysis analysis)
			throws EmptyInfoException, UserNotLoggedInException, 
			AccessToResourceDeniedException, DatabaseException {
		String serviceName = "addAnalysis";
		long userID = logServiceStartAndGetUserID(serviceName); 

		String analysisName = analysis.getName();
		long corpusID = analysis.getCorpusID();
		long featureSetID = analysis.getFeatureSetID();
		
		//check if analysis info is empty
		if(analysisName.isEmpty() || corpusID == 0 || featureSetID ==0 ) {
			throw logger.throwing(new EmptyInfoException());
		}
		
		//check if user resource owner
		if(!corpusManagerServiceImpl.isUserCorpusOwner(userID, corpusID) ||
				!featureSelectorServiceImpl.isUserFSOwner(userID, featureSetID)) {
			throw logger.throwing(new AccessToResourceDeniedException());
		}

		String insertStr = "INSERT INTO "
				+ "analysis (owner_id, name, description, corpus_id, tag_filter_logic, tag_filter_keyword, "
				+ "featureset_id, create_timestamp) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
		try {
			PreparedStatement ps = dbConnection.prepareStatement(insertStr);
			ps.setLong(1, userID);
			ps.setString(2, analysisName);
			ps.setString(3, analysis.getDescription());
			ps.setLong(4, corpusID);
			ps.setString(5, analysis.getTagFilterLogic());
			ps.setString(6, analysis.getTagKeyword());
			ps.setLong(7, analysis.getFeatureSetID());

			ps.executeUpdate();

		} catch (SQLException e) {
			throw logger.throwing(new DatabaseException(e.getMessage())); 
		}
		
		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
				new ServiceRequestCompletedMessage(serviceName));
	}

	@Override
	public Void deleteAnalysis(long analysisID)
			throws UserNotLoggedInException,AccessToResourceDeniedException, DatabaseException {
		String serviceName = "deleteAnalysis";
		long userID = logServiceStartAndGetUserID(serviceName); 

		//check if user owner of the analysis
		try {
			if(!AnalysisUtils.isUserAnalysisOwner(userID, analysisID)) {
				throw logger.throwing(new AccessToResourceDeniedException());
			}
			
			//delete record
			String deleteStr = ""
					+ "DELETE FROM analysis "
					+ "WHERE id=? AND owner_id=?";
			PreparedStatement ps = dbConnection.prepareStatement(deleteStr);
			ps.setLong(1, analysisID);
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
	public Void updateAnalysis(Analysis analysis)
			throws  EmptyInfoException, UserNotLoggedInException, 
			AccessToResourceDeniedException, DatabaseException {
		String serviceName = "updateAnalysis";
		long userID = logServiceStartAndGetUserID(serviceName); 

		String analysisName = analysis.getName();
		long analysisID = analysis.getId();
		long corpusID = analysis.getCorpusID();
		long featureSetID = analysis.getFeatureSetID();
		
		//check if analysis info is empty
		if(analysisID == 0  || analysisName.isEmpty() || corpusID == 0 || featureSetID ==0 ) {
			throw logger.throwing(new EmptyInfoException());
		}
		
		//check if user resource owner
		try {
			if(!AnalysisUtils.isUserAnalysisOwner(userID, analysisID) ||
					!corpusManagerServiceImpl.isUserCorpusOwner(userID, corpusID) ||
					!featureSelectorServiceImpl.isUserFSOwner(userID, featureSetID)) {
				throw logger.throwing(new AccessToResourceDeniedException());
			}
			
			String updateStr = "UPDATE analysis SET name=?, description=?, corpus_id=?, "
					+ "tag_filter_logic = ?, tag_filter_keyword=?, "
					+ "featureset_id=? "
					+ "WHERE id = ? AND owner_id = ?";
			PreparedStatement ps = dbConnection.prepareStatement(updateStr);
			ps.setString(1, analysisName);
			ps.setString(2, analysis.getDescription());
			ps.setLong(3, corpusID);
			ps.setString(4, analysis.getTagFilterLogic());
			ps.setString(5, analysis.getTagKeyword());
			ps.setLong(6, featureSetID);
			ps.setLong(7, analysisID);
			ps.setLong(8, userID);
			ps.executeUpdate();

		} catch (SQLException e) {
			throw logger.throwing(new DatabaseException(e.getMessage()));
		}
		
		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
				new ServiceRequestCompletedMessage(serviceName));
		return null;
	}

	@Override
	public Void runAnalysis(Analysis analysis)
			throws UserNotLoggedInException, AccessToResourceDeniedException, 
			DatabaseException, UIMAException {
		String serviceName = "runAnalysis";
		long userID = logServiceStartAndGetUserID(serviceName); 


		//run the analysis
//		logger.warn("Web container working directory: " + getServletContext().getRealPath("/"));
//		String workDir = getServletContext().getRealPath("/");
//		RunAnalysis runAnalysis = new RunAnalysis(analysis, workDir);  
		try {
			//check if user owner of the analysis
			if(!AnalysisUtils.isUserAnalysisOwner(userID, analysis.getId())) {
				throw logger.throwing(new AccessToResourceDeniedException());
			}

			RunAnalysis runAnalysis = new RunAnalysis(analysis);
			runAnalysis.run();

		} catch (ResourceInitializationException | CpeDescriptorException | IOException e) {
			throw logger.throwing(new UIMAException(e));
		} catch (SQLException e) {
			throw logger.throwing(new DatabaseException(e));
		}  
		
		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
				new ServiceRequestCompletedMessage(serviceName));
		return null;
	}

	@Override
	public String getAnalysisName(long analysisID)
			throws UserNotLoggedInException, AccessToResourceDeniedException, DatabaseException {
		String serviceName = "getAnalysisName";
		long userID = logServiceStartAndGetUserID(serviceName); 

		String analysisName = null;

		try {
			//check if user the owner
			if(!AnalysisUtils.isUserAnalysisOwner(userID, analysisID)) {
				logger.warn("User " + userID + " trying to access analysis " + 
						analysisID +" which is not owned by the user. Access denied!");
				throw new AccessToResourceDeniedException();
			}
			
			//get data from database
			String queryStr = ""
					+ "SELECT name "
					+ "FROM analysis "
					+ "WHERE id=? AND owner_id = ?";
			PreparedStatement ps = dbConnection.prepareStatement(queryStr);
			ps.setLong(1, analysisID);
			ps.setLong(2, userID);

			ResultSet rs = ps.executeQuery();

			if(rs.next()) {
				analysisName = rs.getString("name");
			}
		} catch (SQLException e) {
			logger.warn("Database error: " + e.getMessage());
			throw new DatabaseException(e.getMessage());
		}

		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
				new ServiceRequestCompletedMessage(serviceName));
		return analysisName;
	}

	@Override
	public AnalysisStatus getAnalysisStatus(Analysis analysis)
			throws UserNotLoggedInException, AccessToResourceDeniedException, DatabaseException {
		String serviceName = "getAnalysisStatus";
		long userID = logServiceStartAndGetUserID(serviceName); 

		AnalysisStatus analysisStatus = null;


		try {
			//check if user owner of the analysis
			if(!AnalysisUtils.isUserAnalysisOwner(userID, analysis.getId())) {
				throw logger.throwing(new AccessToResourceDeniedException());
			}	
			analysisStatus = AnalysisUtils.getAnalysisStatus(analysis.getId());
		} catch (SQLException e) {
			throw logger.throwing(new DatabaseException(e.getMessage()));
		}

		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
				new ServiceRequestCompletedMessage(serviceName));
		return analysisStatus;
	}

	@Override
	public Void updateAnalysisStatus(AnalysisStatus analysisStatus)
			throws UserNotLoggedInException, AccessToResourceDeniedException, DatabaseException {
		String serviceName = "updateAnalysisStatus";
		long userID = logServiceStartAndGetUserID(serviceName); 

		try {
			//check if user owner of the analysis
			if(!AnalysisUtils.isUserAnalysisOwner(userID, analysisStatus.getAnalysisID())) {
				throw logger.throwing(new AccessToResourceDeniedException());
			}

			AnalysisUtils.updateAnalysisStatus(analysisStatus);
		} catch (SQLException e) {
			throw logger.throwing(new DatabaseException(e));
		}

		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
				new ServiceRequestCompletedMessage(serviceName));
		return null;
	}
}

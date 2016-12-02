/**
 * 
 */
package com.ctapweb.web.client.service;

import java.util.List;

import com.ctapweb.web.shared.AnalysisEngine;
import com.ctapweb.web.shared.FeatureSet;
import com.ctapweb.web.shared.exception.AccessToResourceDeniedException;
import com.ctapweb.web.shared.exception.DatabaseException;
import com.ctapweb.web.shared.exception.EmptyInfoException;
import com.ctapweb.web.shared.exception.UserNotLoggedInException;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/** Provides services for the corpus manager.
 * @author xiaobin
 *
 */
@RemoteServiceRelativePath("featureSelector")
public interface FeatureSelectorService extends RemoteService {

	/**
	 * Gets the number of feature sets the user owns.
	 * @param cookieSessionValue
	 * @return
	 * @throws UserNotLoggedInException
	 * @throws DatabaseException
	 */
	Integer getFeatureSetCount() 
			throws UserNotLoggedInException, DatabaseException;
	/**
	 * Gets the list of feature sets the user owns.
	 * @param cookieSessionValue
	 * @param offset
	 * @param limit
	 * @return
	 * @throws UserNotLoggedInException
	 * @throws DatabaseException
	 */
	List<FeatureSet> getFeatureSetList(int offset, int limit) 
			throws UserNotLoggedInException, DatabaseException;

	/**
	 * Adds a new feature set.
	 * @param cookieSessionValue
	 * @param featureSet
	 * @return
	 * @throws EmptyInfoException
	 * @throws UserNotLoggedInException
	 * @throws DatabaseException
	 */
	Void addFeatureSet(FeatureSet featureSet) 
			throws EmptyInfoException, UserNotLoggedInException, DatabaseException;

	/**
	 * Deletes a feature set. 
	 * @param cookieSessionValue
	 * @param featureSet
	 * @return
	 * @throws UserNotLoggedInException
	 * @throws AccessToResourceDeniedException
	 * @throws DatabaseException
	 */
	Void deleteFeatureSet(FeatureSet featureSet) 
			throws UserNotLoggedInException, AccessToResourceDeniedException, DatabaseException;

	/**
	 * Updates feature set information.
	 * @param cookieSessionValue
	 * @param featureSet
	 * @return
	 * @throws EmptyInfoException
	 * @throws UserNotLoggedInException
	 * @throws AccessToResourceDeniedException
	 * @throws DatabaseException
	 */
	Void updateFeatureSet(FeatureSet featureSet)
			throws EmptyInfoException, UserNotLoggedInException, 
			AccessToResourceDeniedException, DatabaseException;

	/**
	 * Gets the number of features in a festure set.
	 * @param cookieSessionValue
	 * @param featureSetID
	 * @return
	 * @throws UserNotLoggedInException
	 * @throws AccessToResourceDeniedException
	 * @throws DatabaseException
	 */
	Integer getFeatureCount(long featureSetID)
			throws UserNotLoggedInException, AccessToResourceDeniedException, DatabaseException;

	/**
	 * Gets a list of features selected into a feature set.
	 * @param cookieSessionValue
	 * @param fsID
	 * @param offset
	 * @param limit
	 * @return
	 * @throws UserNotLoggedInException
	 * @throws AccessToResourceDeniedException
	 * @throws DatabaseException
	 */
	List<AnalysisEngine> getFeatureList(long featureSetID, int offset, int limit) 
			throws UserNotLoggedInException, AccessToResourceDeniedException, DatabaseException;

	/**
	 * Removes feature from feature set.
	 * @param cookieSessionValue
	 * @param fsID
	 * @param cfID
	 * @return
	 * @throws UserNotLoggedInException
	 * @throws AccessToResourceDeniedException
	 * @throws DatabaseException
	 */
	Void removeFeatureFromFS(long featureSetID, long featureID) 
			throws UserNotLoggedInException, AccessToResourceDeniedException, DatabaseException;

	/**
	 * Gets the number of all available features.
	 * @param cookieSessionValue
	 * @return
	 * @throws UserNotLoggedInException
	 * @throws DatabaseException
	 */
	Integer getFeatureCount()
			throws UserNotLoggedInException, DatabaseException;

	/**
	 * Gets the number of all available features whose name contains the keyword. 
	 * @param keyword
	 * @return
	 * @throws UserNotLoggedInException
	 * @throws AccessToResourceDeniedException
	 * @throws DatabaseException
	 */
	Integer getFeatureCount(String keyword)
			throws UserNotLoggedInException, DatabaseException;

	/**
	 * Gets the number of features selected into the feature set whose name contains the keyword.
	 * @param featureSetID
	 * @param keyword
	 * @return
	 * @throws UserNotLoggedInException
	 * @throws AccessToResourceDeniedException
	 * @throws DatabaseException
	 */
	Integer getFeatureCount(long featureSetID, String keyword)
			throws UserNotLoggedInException, AccessToResourceDeniedException, DatabaseException;

	/**
	 * Gets a list of all the features provided by the system. 
	 * @param cookieSessionValue
	 * @param offset
	 * @param limit
	 * @return
	 * @throws UserNotLoggedInException
	 * @throws DatabaseException
	 */
	List<AnalysisEngine> getFeatureList(int offset, int limit) 
			throws UserNotLoggedInException, DatabaseException;

	/**
	 * Gets a list of features the system provides whose names contain the keyword.
	 * @param featureSetID
	 * @param keyword
	 * @param offset
	 * @param limit
	 * @return
	 * @throws UserNotLoggedInException
	 * @throws AccessToResourceDeniedException
	 * @throws DatabaseException
	 */
	List<AnalysisEngine> getFeatureList(String keyword, int offset, int limit) 
			throws UserNotLoggedInException, DatabaseException;
	/**
	 * Gets a list of feautures selected into the feature set whose names contain the keyword.
	 * @param featureSetID
	 * @param keyword
	 * @param offset
	 * @param limit
	 * @return
	 * @throws UserNotLoggedInException
	 * @throws DatabaseException
	 */
	List<AnalysisEngine> getFeatureList(long featureSetID, String keyword, int offset, int limit) 
			throws UserNotLoggedInException, AccessToResourceDeniedException, DatabaseException;

	/** 
	 * Adds a feature to feature set. 
	 * @param cookieSessionValue
	 * @param featureID
	 * @param featureSetID
	 * @return
	 * @throws UserNotLoggedInException
	 * @throws AccessToResourceDeniedException
	 * @throws DatabaseException
	 */
	Void addToFeatureSet(long featureID, long featureSetID)
			throws UserNotLoggedInException, AccessToResourceDeniedException, DatabaseException;

	/**
	 * Gets the name of a feature set.
	 * @param userCookieValue
	 * @param featureSetID
	 * @return
	 * @throws UserNotLoggedInException
	 * @throws AccessToResourceDeniedException
	 * @throws DatabaseException
	 */
	String getFeatureSetName(long featureSetID)
			throws UserNotLoggedInException, AccessToResourceDeniedException, DatabaseException;

}

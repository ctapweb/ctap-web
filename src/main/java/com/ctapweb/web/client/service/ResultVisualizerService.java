/**
 * 
 */
package com.ctapweb.web.client.service;

import java.util.List;

import com.ctapweb.web.shared.Analysis;
import com.ctapweb.web.shared.AnalysisEngine;
import com.ctapweb.web.shared.PlotData;
import com.ctapweb.web.shared.exception.AccessToResourceDeniedException;
import com.ctapweb.web.shared.exception.DatabaseException;
import com.ctapweb.web.shared.exception.UserNotLoggedInException;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/** Provides services for the corpus manager.
 * @author xiaobin
 *
 */
@RemoteServiceRelativePath("resultVisualizer")
public interface ResultVisualizerService extends RemoteService {

	/**
	 * Gets a set a tags specified by an analysis.
	 * @param analysisID
	 * @return
	 * @throws UserNotLoggedInException
	 * @throws AccessToResourceDeniedException
	 * @throws DatabaseException
	 */
//	public Set<Tag> getResultCategories(Analysis analysis) 
//			throws UserNotLoggedInException, AccessToResourceDeniedException, DatabaseException;
//
	
	/**
	 * Gets the number of groups the texts in an anlysis are divided into.
	 * @param userCookieValue
	 * @param analysisID
	 * @return
	 * @throws UserNotLoggedInException
	 * @throws AccessToResourceDeniedException
	 * @throws DatabaseException
	 */
	//	public Integer getGroupCount(long analysisID) 
	//			throws UserNotLoggedInException, AccessToResourceDeniedException, DatabaseException;

	/**
	 * Gets a list of all groups to assign the texts in an analysis.
	 * @param userCookieValue
	 * @param analysisID
	 * @param offset
	 * @param limit
	 * @return
	 * @throws UserNotLoggedInException
	 * @throws AccessToResourceDeniedException
	 * @throws DatabaseException
	 */
	//	public List<Group> getGroupList(long analysisID, int offset, int limit)
	//			throws UserNotLoggedInException, AccessToResourceDeniedException, DatabaseException;

	/**
	 * Adds a group for result visualization.
	 * @param userCookieValue
	 * @param group
	 * @return
	 * @throws EmptyInfoException
	 * @throws UserNotLoggedInException
	 * @throws AccessToResourceDeniedException
	 * @throws DatabaseException
	 */
	//	public Void addGroup(Group group) 
	//			throws EmptyInfoException, UserNotLoggedInException, 
	//			AccessToResourceDeniedException,DatabaseException;

	/**
	 * Updates group information.
	 * @param userCookieValue
	 * @param group
	 * @return
	 * @throws EmptyInfoException
	 * @throws UserNotLoggedInException
	 * @throws AccessToResourceDeniedException
	 * @throws DatabaseException
	 */
	//	public Void updateGroup(Group group)
	//			throws EmptyInfoException, UserNotLoggedInException, 
	//			AccessToResourceDeniedException, DatabaseException;

	/**
	 * Deletes a group.
	 * @param userCookieValue
	 * @param group
	 * @return
	 * @throws UserNotLoggedInException
	 * @throws AccessToResourceDeniedException
	 * @throws DatabaseException
	 */
	//	public Void deleteGroup(Group group)
	//			throws UserNotLoggedInException, AccessToResourceDeniedException, DatabaseException; 

	/**
	 * Gets information for a specific group.
	 * @param userCookieValue
	 * @param groupID
	 * @return
	 * @throws UserNotLoggedInException
	 * @throws AccessToResourceDeniedException
	 * @throws DatabaseException
	 */
	//	public Group getGroup(long groupID) 
	//			throws UserNotLoggedInException, AccessToResourceDeniedException, DatabaseException; 

	/**
	 * Gets the number of texts in a certain group.
	 * @param userCookieValue
	 * @param groupID
	 * @return
	 * @throws UserNotLoggedInException
	 * @throws AccessToResourceDeniedException
	 * @throws DatabaseException
	 */
	//	public Integer getGroupTextCount(long groupID)
	//			throws UserNotLoggedInException, AccessToResourceDeniedException, DatabaseException;

	/**
	 * Gets a list of all the texts in a certain group.
	 * @param userCookieValue
	 * @param groupID
	 * @param offset
	 * @param limit
	 * @return
	 * @throws UserNotLoggedInException
	 * @throws AccessToResourceDeniedException
	 * @throws DatabaseException
	 */
	//	public List<CorpusText> getGroupTextList(long groupID, int offset, int limit)
	//			throws UserNotLoggedInException, AccessToResourceDeniedException, DatabaseException; 

	/**
	 * Gets the number of texts analyzed by a specific analysis.
	 * @param userCookieValue
	 * @param analysisID
	 * @return
	 * @throws UserNotLoggedInException
	 * @throws AccessToResourceDeniedException
	 * @throws DatabaseException
	 */
//	public Integer getAllAnalyzedTextCount(long analysisID)
//			throws UserNotLoggedInException, AccessToResourceDeniedException, DatabaseException;

	/**
	 * Gets a list of all the texts analyzed by a specific analysis.
	 * @param userCookieValue
	 * @param groupID
	 * @param offset
	 * @param limit
	 * @return
	 * @throws UserNotLoggedInException
	 * @throws AccessToResourceDeniedException
	 * @throws DatabaseException
	 */
//	public List<CorpusText> getAllAnalyzedTextList(long groupID, int offset, int limit)
//			throws UserNotLoggedInException, AccessToResourceDeniedException, DatabaseException; 

	/**
	 * Adds a text to group.
	 * @param userCookieValue
	 * @param textID
	 * @param groupID
	 * @return
	 * @throws UserNotLoggedInException
	 * @throws AccessToResourceDeniedException
	 * @throws DatabaseException
	 */
	//	public Void addTextToGroup(long textID, long groupID)
	//			throws UserNotLoggedInException, AccessToResourceDeniedException, DatabaseException;

	/**
	 * Removes a text from the specified group.
	 * @param userCookieValue
	 * @param textID
	 * @param groupID
	 * @return
	 * @throws UserNotLoggedInException
	 * @throws AccessToResourceDeniedException
	 * @throws DatabaseException
	 */
	//	public Void removeTextFromGroup(long textID, long groupID)
	//			throws UserNotLoggedInException,AccessToResourceDeniedException, DatabaseException;

	/**
	 * Gets average feature value from the result of an analysis. 
	 * The results are categoried by the tag filter specification set in the analysis. 
	 * @param userCookieValue
	 * @param analysisID
	 * @param featureID
	 * @param statistics
	 * @return
	 * @throws UserNotLoggedInException
	 * @throws AccessToResourceDeniedException
	 * @throws DatabaseException
	 */
	public List<PlotData> getPlotData(long analysisID, long featureID, String statistics)
			throws UserNotLoggedInException,AccessToResourceDeniedException, DatabaseException;

	/**
	 * Gets a list of features an analysis produces.
	 * @param analysisID
	 * @return
	 * @throws UserNotLoggedInException
	 * @throws AccessToResourceDeniedException
	 * @throws DatabaseException
	 */
	public List<AnalysisEngine> getResultFeatureList(long analysisID)
			throws UserNotLoggedInException,AccessToResourceDeniedException, DatabaseException;

	/**
	 * Gets a list of analysis that has been run and has results.
	 * @param analysisID
	 * @return
	 * @throws UserNotLoggedInException
	 * @throws AccessToResourceDeniedException
	 * @throws DatabaseException
	 */
	public List<Analysis> getAnalysisList()
			throws UserNotLoggedInException,AccessToResourceDeniedException, DatabaseException;

}

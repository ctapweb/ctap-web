/**
 * 
 */
package com.ctapweb.web.client.service;

import java.util.List;

import com.ctapweb.web.shared.Analysis;
import com.ctapweb.web.shared.AnalysisStatus;
import com.ctapweb.web.shared.exception.AccessToResourceDeniedException;
import com.ctapweb.web.shared.exception.DatabaseException;
import com.ctapweb.web.shared.exception.EmptyInfoException;
import com.ctapweb.web.shared.exception.UIMAException;
import com.ctapweb.web.shared.exception.UserNotLoggedInException;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/** Provides services for the corpus manager.
 * @author xiaobin
 *
 */
@RemoteServiceRelativePath("analysisGenerator")
public interface AnalysisGeneratorService extends RemoteService {
	
	/**
	 * Gets the number of analysis owned by the user.
	 * @param cookieSessionValue
	 * @return
	 * @throws UserNotLoggedInException
	 * @throws DatabaseException
	 */
	Integer getAnalysisCount() 
			throws UserNotLoggedInException, DatabaseException;
	
	/**
	 * Gets a list of all anlysis owned by user.
	 * @param cookieSessionValue
	 * @param offset
	 * @param limit
	 * @return
	 * @throws UserNotLoggedInException
	 * @throws DatabaseException
	 */
	List<Analysis> getAnalysisList(int offset, int limit) 
			throws UserNotLoggedInException, DatabaseException;

	void addAnalysis(Analysis newAnalysis) 
			throws EmptyInfoException, AccessToResourceDeniedException, 
			UserNotLoggedInException, DatabaseException;
	
	/**
	 * Deletes an anlysis from the database.
	 * @param cookieSessionValue
	 * @param analysisID
	 * @return
	 * @throws UserNotLoggedInException
	 * @throws AccessToResourceDeniedException
	 * @throws DatabaseException
	 */
	Void deleteAnalysis(long analysisID) 
		throws UserNotLoggedInException, AccessToResourceDeniedException, DatabaseException;
	
	/**
	 * Updates the analysis details.
	 * @param cookieSessionValue
	 * @param analysis
	 * @return
	 * @throws EmptyInfoException
	 * @throws UserNotLoggedInException
	 * @throws AccessToResourceDeniedException
	 * @throws DatabaseException
	 */
	Void updateAnalysis(Analysis analysis) 
		throws EmptyInfoException, UserNotLoggedInException, 
		AccessToResourceDeniedException, DatabaseException;
	
	/**
	 * Runs an analysis when the user clicks the 'run' button on the client page.
	 * @param cookieSessionValue
	 * @param analysis
	 * @return
	 * @throws UserNotLoggedInException
	 * @throws AccessToResourceDeniedException
	 * @throws DatabaseException
	 * @throws UIMAException
	 */
	Void runAnalysis(Analysis analysis)
		throws UserNotLoggedInException, AccessToResourceDeniedException, DatabaseException, UIMAException;
	
	/**
	 * Gets the status of an anlysis. See if it is running, paused, stopped, or finished.
	 * @param cookieSessionValue
	 * @param analysis
	 * @return
	 * @throws UserNotLoggedInException
	 * @throws AccessToResourceDeniedException
	 * @throws DatabaseException
	 */
	AnalysisStatus getAnalysisStatus(Analysis analysis)
		throws UserNotLoggedInException, AccessToResourceDeniedException, DatabaseException;

	/**
	 * Updates the status of the analysis.
	 * @param userCookieValue
	 * @param analysisStatus
	 * @return
	 * @throws UserNotLoggedInException
	 * @throws AccessToResourceDeniedException
	 * @throws DatabaseException
	 */
	Void updateAnalysisStatus(AnalysisStatus analysisStatus) 
		throws UserNotLoggedInException, AccessToResourceDeniedException, DatabaseException;

	/**
	 * Gets the anlysis' name.
	 * @param userCookieValue
	 * @param analysisID
	 * @return
	 * @throws UserNotLoggedInException
	 * @throws AccessToResourceDeniedException
	 * @throws DatabaseException
	 */
	String getAnalysisName(long analysisID)
		throws UserNotLoggedInException, AccessToResourceDeniedException, DatabaseException;
}

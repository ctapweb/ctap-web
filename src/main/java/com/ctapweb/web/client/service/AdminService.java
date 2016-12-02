package com.ctapweb.web.client.service;

import java.util.List;

import com.ctapweb.web.shared.AnalysisEngine;
import com.ctapweb.web.shared.ComplexityFeature;
import com.ctapweb.web.shared.exception.AEDependencyException;
import com.ctapweb.web.shared.exception.AdminNotLoggedInException;
import com.ctapweb.web.shared.exception.DatabaseException;
import com.ctapweb.web.shared.exception.EmptyInfoException;
import com.ctapweb.web.shared.exception.ServerIOException;
import com.ctapweb.web.shared.exception.UIMAException;
import com.ctapweb.web.shared.exception.UserNotLoggedInException;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * Provides admin services.
 * @author xiaobin
 *
 */
@RemoteServiceRelativePath("admin")
public interface AdminService extends RemoteService {
	
	/** 
	 * Initializes the database, creating necessary DB tables.
	 * @param adminPasswd
	 * @throws DatabaseException
	 */
	public void initDB(String adminPasswd) 
			throws AdminNotLoggedInException, DatabaseException;

	//AE management
	/**
	 * Gets the number of analysis engines.
	 * @param userCookieValue
	 * @return
	 * @throws UserNotLoggedInException
	 * @throws AdminNotLoggedInException
	 * @throws DatabaseException
	 */
	public long getAECount() 
			throws UserNotLoggedInException, AdminNotLoggedInException, DatabaseException;
	
	/**
	 * Gets a list of all the analysis engines added to the system.
	 * @param userCookieValue
	 * @param offset
	 * @param limit
	 * @return
	 * @throws UserNotLoggedInException
	 * @throws AdminNotLoggedInException
	 * @throws DatabaseException
	 */
	public List<AnalysisEngine> getAEList(int offset, int limit) 
			throws UserNotLoggedInException, AdminNotLoggedInException, DatabaseException;
	
	/**
	 * Updates analysis engine information.
	 * @param userCookieValue
	 * @param ae
	 * @return
	 * @throws EmptyInfoException
	 * @throws UserNotLoggedInException
	 * @throws AdminNotLoggedInException
	 * @throws DatabaseException
	 */
	public Void updateAE(String userCookieValue, AnalysisEngine ae)
			throws EmptyInfoException, UserNotLoggedInException, 
			AdminNotLoggedInException, DatabaseException;
	
	/**
	 * Deletes an analysis engine.
	 * @param userCookieValue
	 * @param ae
	 * @return
	 * @throws UserNotLoggedInException
	 * @throws AdminNotLoggedInException
	 * @throws DatabaseException
	 */
	public Void deleteAE(AnalysisEngine ae)
			throws UserNotLoggedInException, AdminNotLoggedInException, DatabaseException;
	
	/**
	 * Adds an analysis engine.
	 * @param userCookieValue
	 * @param ae
	 * @return
	 * @throws EmptyInfoException
	 * @throws UserNotLoggedInException
	 * @throws AdminNotLoggedInException
	 * @throws DatabaseException
	 */
//	public Void addAE(String userCookieValue, AnalysisEngine ae) 
//			throws EmptyInfoException, UserNotLoggedInException, 
//			AdminNotLoggedInException, DatabaseException;
//	
	//CF management
	/**
	 * Gets the number of complexity features the system provides.
	 * @param userCookieValue
	 * @return
	 * @throws UserNotLoggedInException
	 * @throws AdminNotLoggedInException
	 * @throws DatabaseException
	 */
	public long getCFCount(String userCookieValue)
			throws UserNotLoggedInException, AdminNotLoggedInException, DatabaseException;
	
	/**
	 * Gets a list of all complexity features provided by the system.
	 * @param adminToken
	 * @param offset
	 * @param limit
	 * @return
	 * @throws UserNotLoggedInException
	 * @throws AdminNotLoggedInException
	 * @throws DatabaseException
	 */
	public List<ComplexityFeature> getCFList(String adminToken, int offset, int limit) 
			throws UserNotLoggedInException, AdminNotLoggedInException, DatabaseException;
	
	/**
	 * Updates complexity feature information.
	 * @param userCookieValue
	 * @param cf
	 * @return
	 * @throws EmptyInfoException
	 * @throws UserNotLoggedInException
	 * @throws AdminNotLoggedInException
	 * @throws DatabaseException
	 */
	public Void updateCF(String userCookieValue, ComplexityFeature cf)
			throws EmptyInfoException, UserNotLoggedInException, 
			AdminNotLoggedInException, DatabaseException;
	
	/**
	 * Deletes a complexity feature.
	 * @param userCookieValue
	 * @param cf
	 * @return
	 * @throws UserNotLoggedInException
	 * @throws AdminNotLoggedInException
	 * @throws DatabaseException
	 */
	public Void deleteCF(String userCookieValue, ComplexityFeature cf) 
			throws UserNotLoggedInException, AdminNotLoggedInException, DatabaseException;
	
	/**
	 * Adds a complexity feature.
	 * @param userCookieValue
	 * @param cf
	 * @return
	 * @throws EmptyInfoException
	 * @throws UserNotLoggedInException
	 * @throws AdminNotLoggedInException
	 * @throws DatabaseException
	 */
	public Void addCF(String userCookieValue, ComplexityFeature cf) 
			throws EmptyInfoException, UserNotLoggedInException, 
			AdminNotLoggedInException, DatabaseException;
	
	/**
	 * Check is user is logged in as administrator.
	 * @param userCookieValue
	 * @return
	 * @throws UserNotLoggedInException
	 * @throws AdminNotLoggedInException
	 * @throws DatabaseException
	 */
	public boolean isUserAdmin() 
			throws UserNotLoggedInException, AdminNotLoggedInException, DatabaseException;
	
	public void importAE() 
			throws UserNotLoggedInException, AdminNotLoggedInException, 
			DatabaseException, ServerIOException, UIMAException, AEDependencyException;
	
}

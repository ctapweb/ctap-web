/**
 * 
 */
package com.ctapweb.web.client.service;

import com.google.gwt.core.client.GWT;

/**
 * @author xiaobin
 * Provides service singletons to avoid repeated service request. 
 */
public class Services {

	private static AdminServiceAsync adminService = null;
	private static UserServiceAsync userService = null;
	private static CorpusManagerServiceAsync corpusManagerService = null;
	private static FeatureSelectorServiceAsync featureSelectorService = null;
	private static AnalysisGeneratorServiceAsync analysisGeneratorService= null;
	private static ResultVisualizerServiceAsync resultVisualizerService = null;
	private Services() {}
	
	/**
	 * Gets admin service
	 * @return
	 */
	public static AdminServiceAsync getAdminService() {
		if(adminService == null) {
			adminService = GWT.create(AdminService.class);
		} 
		
		return adminService;
	}
	
	/** Gets user service
	 * 
	 */
	public static UserServiceAsync getUserService() {
		if(userService == null) {
			userService = GWT.create(UserService.class);
		}
		return userService;
	}

	/** Gets corpus manager service
	 * 
	 */
	public static CorpusManagerServiceAsync getCorpusManagerService() {
		if(corpusManagerService == null) {
			corpusManagerService = GWT.create(CorpusManagerService.class);
		}
		return corpusManagerService;
	}
	
	/** Gets feature selector service
	 * 
	 */
	public static FeatureSelectorServiceAsync getFeatureSelectorService() {
		if(featureSelectorService == null) {
			featureSelectorService = GWT.create(FeatureSelectorService.class);
		}
		return featureSelectorService;
	}
	
	/** Gets analysis generator service.
	 * 
	 */
	public static AnalysisGeneratorServiceAsync getAnalysisGeneratorService() {
		if(analysisGeneratorService == null) {
			analysisGeneratorService = GWT.create(AnalysisGeneratorService.class);
		}
		return analysisGeneratorService;
	}
	
	public static ResultVisualizerServiceAsync getResultVisualizerService() {
		if(resultVisualizerService == null) {
			resultVisualizerService= GWT.create(ResultVisualizerService.class);
		}
		return resultVisualizerService;
	}
}

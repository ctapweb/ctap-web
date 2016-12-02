/**
 * 
 */
package com.ctapweb.web.client;

/**
 * @author xiaobin
 * 
 * Stores the history tokens used in this application. 
 */
public class HistoryToken {
//	public static final String home = "home";
//	public static final String admin = "admin";
	public static final String signup = "signup";
	public static final String terms = "terms";
	public static final String privacy = "privacy";
	public static final String signin = "signin";
	public static final String recoverPass = "recoverPass";
	public static final String error="error";
	public static final String userhome = "userhome";
	public static final String dashboard = "dashboard";
	public static final String corpusmanager= "corpusmanager";
	public static final String textmanager= "textmanager";
//
	public static final String featureselector= "featureselector";
	public static final String featuresetmanager= "featuresetmanager";
//
	public static final String analysisgenerator = "analysisgenerator";
	public static final String resultvisualizer = "resultvisualizer";
	public static final String groupsetmanager = "groupsetmanager";
	public static final String groupmanager= "groupmanager";

	
	public static final String editprofile= "editprofile";
	public static final String sendfeedback = "sendfeedback";
	public static final String documentation = "documentation";
	
	//admin page tokens
	public static final String initDB = "initdb";
	public static final String adminDB = "admindb";
	public static final String adminUser = "adminuser";
	public static final String adminAE = "adminae";
	public static final String adminCF = "admincf";
	

	//errorMsg is used by the error page to show custom error message to the user.
	private static String errorMsg;
	
	public static final void setErrorMsg(String errorMessage) {
		errorMsg = errorMessage;
	}
	
	public static final String getErrorMsg() {
		return errorMsg;
	}

}

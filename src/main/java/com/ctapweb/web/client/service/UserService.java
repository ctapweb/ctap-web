/**
 * 
 */
package com.ctapweb.web.client.service;

import com.ctapweb.web.shared.UserInfo;
import com.ctapweb.web.shared.exception.CaptchaIncorrectException;
import com.ctapweb.web.shared.exception.DatabaseException;
import com.ctapweb.web.shared.exception.EmailAddressNotLegalException;
import com.ctapweb.web.shared.exception.EmptyInfoException;
import com.ctapweb.web.shared.exception.UserAlreadyExistsException;
import com.ctapweb.web.shared.exception.UserNotFoundException;
import com.ctapweb.web.shared.exception.UserNotLoggedInException;
import com.ctapweb.web.shared.exception.WrongPasswordException;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/** Provides user services, including user sign up, update user profile, etc.
 * @author xiaobin
 *
 */

@RemoteServiceRelativePath("user")
public interface UserService extends RemoteService {

	/**
	 * Signs up a new user.
	 * @param userInfo
	 * @return
	 * @throws EmailAddressNotLegalException
	 * @throws UserAlreadyExistsException
	 * @throws DatabaseException
	 * @throws EmptyInfoException
	 */
	void signup(UserInfo userInfo, String captcha)
			throws EmailAddressNotLegalException, 
			UserAlreadyExistsException,
			DatabaseException, EmptyInfoException, CaptchaIncorrectException;

	/**
	 * Signs the user in.
	 * @param userInfo
	 * @return
	 * @throws EmptyInfoException
	 * @throws UserNotFoundException
	 * @throws WrongPasswordException
	 * @throws DatabaseException
	 * @throws EmailAddressNotLegalException
	 */
	void signin(UserInfo userInfo, boolean rememberUser) 
			throws EmptyInfoException, UserNotFoundException, 
			WrongPasswordException, DatabaseException, EmailAddressNotLegalException;

	/**
	 * 	Signs the user in from the server. This is used when a user has already logged in in an earlier session. 
	 * A session token has been saved in the server database and the user's browser cookies. When the user
	 * comes back, the application reads the user cookies and submit it to the server to check if the session token
	 * is the one stored in the database. If they match, log the user in.
	 * @param userCookieValue
	 * @return
	 * @throws UserNotLoggedInException
	 * @throws DatabaseException
	 */
//	UserInfo signinFromServer(String userCookieValue) 
//			throws UserNotLoggedInException, DatabaseException;

	/**
	 * Verify user cookie values against the database record.
	 * @return null if user cookies match database record
	 * @throws DatabaseException
	 */
	UserInfo verifyUserCookies()
		throws DatabaseException;
	/**
	 * Signs the user out by deleting the session token records in the user database. 
	 * @param userCookieValue
	 * @throws DatabaseException
	 */
	void signout() throws UserNotLoggedInException, DatabaseException;

	/** Updates user profile.
	 * 
	 * @param email
	 * @param passwd
	 * @param fullName
	 * @return
	 */

	void updateProfile(UserInfo userInfo) 	
			throws UserNotLoggedInException,
			DatabaseException, EmptyInfoException;

	/**
	 * Saves user feedback in database.
	 * @param userCookieValue
	 * @param subject
	 * @param content
	 * @throws UserNotLoggedInException
	 * @throws DatabaseException
	 * @throws EmptyInfoException
	 */
	void sendFeedback(String subject, String content) 
			throws UserNotLoggedInException, DatabaseException, EmptyInfoException;

}

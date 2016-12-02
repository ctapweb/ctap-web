package com.ctapweb.web.server.user;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ctapweb.web.client.service.UserService;
import com.ctapweb.web.server.BCrypt;
import com.ctapweb.web.server.DBConnectionManager;
import com.ctapweb.web.server.Utils;
import com.ctapweb.web.server.logging.LogMarker;
import com.ctapweb.web.server.logging.ServiceRequestCompletedMessage;
import com.ctapweb.web.server.logging.ServiceRequestReceivedMessage;
import com.ctapweb.web.server.logging.VerifyingUserCookiesMessage;
import com.ctapweb.web.shared.SharedProperties;
import com.ctapweb.web.shared.UserInfo;
import com.ctapweb.web.shared.exception.CaptchaIncorrectException;
import com.ctapweb.web.shared.exception.DatabaseException;
import com.ctapweb.web.shared.exception.EmailAddressNotLegalException;
import com.ctapweb.web.shared.exception.EmptyInfoException;
import com.ctapweb.web.shared.exception.UserAlreadyExistsException;
import com.ctapweb.web.shared.exception.UserNotFoundException;
import com.ctapweb.web.shared.exception.UserNotLoggedInException;
import com.ctapweb.web.shared.exception.WrongPasswordException;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import nl.captcha.Captcha;

public class UserServiceImpl extends RemoteServiceServlet implements UserService {

	Connection dbConnection = DBConnectionManager.getDbConnection();
	private static final Logger logger = LogManager.getLogger();

	@Override
	public void signup(UserInfo userInfo, String captchaStr)
			throws EmailAddressNotLegalException, UserAlreadyExistsException, 
			DatabaseException, EmptyInfoException, CaptchaIncorrectException {
		String serviceName = "signup";
		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
				new ServiceRequestReceivedMessage(serviceName, userInfo));

		String email = userInfo.getEmail();
		String passwd = userInfo.getPasswd();

		// empty info exception
		if (email.isEmpty() || userInfo.getPasswd().isEmpty() 
				|| email.length() < 6 || captchaStr.isEmpty()) {
			throw logger.throwing(new EmptyInfoException());
		}

		//check captcha
		HttpSession session = getThreadLocalRequest().getSession();
		Captcha captcha = (Captcha) session.getAttribute(Captcha.NAME);
		if(!captcha.isCorrect(captchaStr)) {
			throw logger.throwing(new CaptchaIncorrectException());
		}
		
		// validate the email
		if (!com.ctapweb.web.shared.Utils.isEmailValid(email)) {
			throw logger.throwing(new EmailAddressNotLegalException());
		}

		// check if email already exists
		if (isEmailExist(email)) {
			throw logger.throwing(new UserAlreadyExistsException());
		}

		// encrypt the password
		String hashedPasswd = BCrypt.hashpw(passwd, BCrypt.gensalt());

		// get unique session token
		String sessionToken = getUniqueSessionToken(email);
		userInfo.setSessionToken(sessionToken);

		// link to DB server and create the user
		// also log the user in
		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER,
				"User info verified. Creating user account entry in database...");
		try {
			PreparedStatement ps = dbConnection.prepareStatement(
					"INSERT INTO user_account(email, passwd, session_token, last_login) " + 
					"VALUES (?, ?, ?, CURRENT_TIMESTAMP) RETURNING id");
			ps.setString(1, email);
			ps.setString(2, hashedPasswd);
			ps.setString(3, sessionToken);

			ResultSet rs = ps.executeQuery();
			//get user id
			if(rs.next()) {
				userInfo.setId(rs.getLong("id"));
			}

			logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
					"User info registered in database. Setting response cookies...");
			//set response cookies
			Utils.setUserCookies(getThreadLocalResponse(), userInfo, false);

			logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
					new ServiceRequestCompletedMessage("Signup", userInfo));

		} catch (SQLException e) {
			throw logger.throwing(new DatabaseException(e.getMessage()));
		}

		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
				new ServiceRequestCompletedMessage(serviceName));
	}

	@Override
	public void signin(UserInfo userInfo, boolean rememberUser) 
			throws EmptyInfoException, EmailAddressNotLegalException,
			UserNotFoundException, WrongPasswordException, DatabaseException {
		String serviceName = "signin";
		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
				new ServiceRequestReceivedMessage(serviceName, userInfo));

		String email = userInfo.getEmail();
		String passwd = userInfo.getPasswd();

		if (email.isEmpty() || passwd.isEmpty()) {
			throw logger.throwing(new EmptyInfoException());
		}

		if (!com.ctapweb.web.shared.Utils.isEmailValid(email)) {
			throw logger.throwing(new EmailAddressNotLegalException());
		}

		// query user_account table and see if user name and password match
		String queryStr = "SELECT * FROM user_account WHERE email=?";
		PreparedStatement ps;
		try {
			ps = dbConnection.prepareStatement(queryStr);
			ps.setString(1, email);

			ResultSet rs = ps.executeQuery();

			if (!rs.isBeforeFirst()) {
				// no such user
				throw logger.throwing(new UserNotFoundException(email));
			} else {
				// user exists, see if passwd match
				rs.next();
				String hashedPw = rs.getString("passwd");

				if (!BCrypt.checkpw(passwd, hashedPw)) {
					throw logger.throwing(new WrongPasswordException(email));
				}


				//				// set user info object, which will be returned to client
				//				userInfo.setPasswd(""); //remove password
				userInfo.setId(rs.getLong("id"));

				// The session token is from the combination of a session id and
				// the user's email.
				// It is highly unlikely for this token to have duplications.
				String uniqueToken = getUniqueSessionToken(email);
				userInfo.setSessionToken(uniqueToken);

				// save the session token into database
				String updateStr = "" + "UPDATE user_account " + "SET session_token=?, last_login=CURRENT_TIMESTAMP "
						+ "WHERE email=?";
				ps = dbConnection.prepareStatement(updateStr);
				ps.setString(1, uniqueToken);
				ps.setString(2, email);
				ps.executeUpdate();

				logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
						"User info verified with database entry. User logged in successfully. Setting response cookies...");
				//set response cookies
				Utils.setUserCookies(getThreadLocalResponse(), userInfo, rememberUser);
			}
		} catch (SQLException e) {
			throw logger.throwing(new DatabaseException(e.getMessage()));
		}

		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
				new ServiceRequestCompletedMessage(serviceName));
	}

	/**
	 * Checks if email already exists in the database.
	 * 
	 * @param email
	 * @return
	 * @throws DatabaseException
	 */
	private boolean isEmailExist(String email) throws DatabaseException {

		boolean isExist = false;

		String queryStr = "SELECT id FROM user_account WHERE email=?";
		PreparedStatement ps;
		ResultSet rs;
		try {
			ps = dbConnection.prepareStatement(queryStr);
			ps.setString(1, email);
			rs = ps.executeQuery();
			if (rs.isBeforeFirst()) {
				isExist = true;
			}
		} catch (SQLException e) {
			throw new DatabaseException(e.getMessage());
		}
		return isExist;
	}

	//	@Override
	//	public UserInfo signinFromServer(String userCookieValue) 
	//			throws UserNotLoggedInException, DatabaseException {
	//		//		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER,
	//		//				new ServiceRequestReceivedMessage("SigninFromServer", ));
	//
	//		UserInfo userInfo = new UserInfo();
	//		String queryStr = "" + "SELECT * " + "FROM user_account " + "WHERE session_token = ?";
	//
	//		try {
	//			PreparedStatement ps = dbConnection.prepareStatement(queryStr);
	//			ps.setString(1, userCookieValue);
	//
	//			ResultSet rs = ps.executeQuery();
	//
	//			// user has a log in record in db
	//			if (rs.isBeforeFirst()) {
	//				// get the record
	//				rs.next();
	//				userInfo.setId(rs.getLong("id"));
	//				//				userInfo.setTitle(rs.getString("title"));
	//				//				userInfo.setFirstName(rs.getString("first_name"));
	//				//				userInfo.setLastName(rs.getString("last_name"));
	//				//				userInfo.setInstitution(rs.getString("institution"));
	//				userInfo.setEmail(rs.getString("email"));
	//				logger.info("User signed in from server confirmed. Returning user info...");
	//			} else {
	//				throw new UserNotLoggedInException();
	//			}
	//
	//		} catch (SQLException e) {
	//			throw new DatabaseException(e.getMessage());
	//		}
	//
	//		return userInfo;
	//	}

	@Override
	public void signout() 
			throws UserNotLoggedInException, DatabaseException {
		String serviceName = "signout";
		long userID = logServiceStartAndGetUserID(serviceName); 

		// delete user login record from user_account table
		String deleteStr = 
				"UPDATE user_account " + "SET session_token ='' " + "WHERE id=?";
		PreparedStatement ps;
		try {
			ps = dbConnection.prepareStatement(deleteStr);
			ps.setLong(1, userID);
			ps.executeUpdate();
		} catch (SQLException e) {
			throw logger.throwing(new DatabaseException(e.getMessage()));
		}

		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
				new ServiceRequestCompletedMessage(serviceName));
	}

	/**
	 * Gets an unique token for identifying the user. The token is constructed
	 * by hashing the sessionID + userEmail, which makes it very unlikely to be
	 * repeated.
	 * 
	 * @param email
	 *            an email adress as a string
	 * @return a unique token
	 */
	private String getUniqueSessionToken(String email) {
		String str = this.getThreadLocalRequest().getSession().getId() + email;
		return BCrypt.hashpw(str, BCrypt.gensalt());
	}

	/**
	 * Updates user profile.
	 */
	@Override
	public void updateProfile(UserInfo userInfo)
			throws UserNotLoggedInException, DatabaseException, EmptyInfoException {
		String serviceName = "updateProfile";
		long userID = logServiceStartAndGetUserID(serviceName); 

		String passwd = userInfo.getPasswd();

		// empty info exception
		if (userInfo.getPasswd().length() < 6) {
			throw new EmptyInfoException();
		}

		// encrypt the password
		String hashedPasswd = BCrypt.hashpw(userInfo.getPasswd(), BCrypt.gensalt());

		// link to DB server and update user info
		String updateStr = "UPDATE user_account " 
				+ "SET passwd=? "
				+ "WHERE id=?";
		try {
			PreparedStatement ps = dbConnection.prepareStatement(updateStr);
			ps.setString(1, hashedPasswd);
			ps.setLong(2, userID);

			ps.executeUpdate();

		} catch (SQLException e) {
			throw logger.throwing(new DatabaseException(e.getMessage()));
		}

		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
				new ServiceRequestCompletedMessage(serviceName));
	}

	private long logServiceStartAndGetUserID(String serviceName) 
			throws DatabaseException, UserNotLoggedInException {
		long userID = 0;
		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
				new ServiceRequestReceivedMessage(serviceName));

		//verify user cookies and get user id
		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
				new VerifyingUserCookiesMessage(serviceName));
		UserInfo userInfo = verifyUserCookies(getThreadLocalRequest());
		if(userInfo != null) {
			userID = userInfo.getId();
		} else {
			throw logger.throwing(new UserNotLoggedInException());
		}
		return userID;
	}

	@Override
	public void sendFeedback(String subject, String content)
			throws UserNotLoggedInException, DatabaseException, EmptyInfoException {
		String serviceName = "sendFeedback";
		long userID = logServiceStartAndGetUserID(serviceName); 

		if (subject.isEmpty() || content.isEmpty()) {
			throw new EmptyInfoException();
		}

		String insertStr = "INSERT INTO feedback (user_id, subject, content, sent_date) "
				+ "VALUES (?, ?, ?, CURRENT_TIMESTAMP)";
		PreparedStatement ps;
		try {
			ps = dbConnection.prepareStatement(insertStr);
			ps.setLong(1, userID);
			ps.setString(2, subject);
			ps.setString(3, content);

			ps.executeUpdate();

		} catch (SQLException e) {
			throw logger.throwing(new DatabaseException(e.getMessage()));
		}

		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
				new ServiceRequestCompletedMessage(serviceName));
	}

	/**
	 * This version is used by server side programs.
	 * @param request
	 * @return
	 * @throws DatabaseException
	 */
	public UserInfo verifyUserCookies(HttpServletRequest request) 
			throws DatabaseException {
		String serviceName = "verifyUserCookies";
		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER,
				"Received request for service verifyUserCookies. Verifying user cookies...");

		UserInfo userInfo = null;
		String email = null;
		long id = 0;
		String sessionToken = null;

		//get request cookies
		for(Cookie cookie : request.getCookies()) {
			String cookieName = cookie.getName();
			String cookieValue = cookie.getValue();
			if(SharedProperties.COOKIES_USER_EMAIL.equals(cookieName)) {
				email = cookieValue;
			} else if(SharedProperties.COOKIES_USER_ID.equals(cookieName)) {
				id = Long.parseLong(cookieValue);
			} else if(SharedProperties.COOKIES_USER_SESSIONTOKEN.equals(cookieName)) {
				sessionToken = cookieValue;
			}
		}

		if(email == null || id == 0 || sessionToken == null) {
			logger.log(Level.WARN, LogMarker.CTAP_SERVER_MARKER,
					"No user cookies or cookies incomplete. Verify user failed.");
			//cookies incomplete, return
			return null;
		}

		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER,
				"Found user cookies: id = " + id + "; email = " + email + "; sessionToken = " + sessionToken + ". "
						+ "Querying database to verify cookie information...");
		String queryStr = ""
				+ "SELECT * FROM user_account "
				+ "WHERE id = ? "
				+ "     AND email = ? "
				+ "     AND session_token = ?";
		try {
			PreparedStatement ps = dbConnection.prepareStatement(queryStr);
			ps.setLong(1, id);
			ps.setString(2, email);
			ps.setString(3, sessionToken);

			ResultSet rs = ps.executeQuery();
			if(rs.isBeforeFirst()) {
				//verified
				userInfo = new UserInfo();
				userInfo.setId(id);
				userInfo.setEmail(email);
				userInfo.setSessionToken(sessionToken);

				logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER,
						"User cookies verified. Returning to client...");
			} else {
				logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER,
						"User cookies could not be verified. Returning to client...");
			}

		} catch (SQLException e) {
			throw logger.throwing(new DatabaseException(e.getMessage()));
		}

		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
				new ServiceRequestCompletedMessage(serviceName));
		return userInfo;	
	}

	@Override
	public UserInfo verifyUserCookies() throws DatabaseException {
		UserInfo userInfo = verifyUserCookies(getThreadLocalRequest());
		return userInfo;
	}



}

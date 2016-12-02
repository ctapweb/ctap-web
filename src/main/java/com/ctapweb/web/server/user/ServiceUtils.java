package com.ctapweb.web.server.user;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

import com.ctapweb.web.server.logging.LogMarker;
import com.ctapweb.web.server.logging.ServiceRequestReceivedMessage;
import com.ctapweb.web.server.logging.VerifyingUserCookiesMessage;
import com.ctapweb.web.shared.UserInfo;
import com.ctapweb.web.shared.exception.DatabaseException;
import com.ctapweb.web.shared.exception.UserNotLoggedInException;

public class ServiceUtils {
	private static UserServiceImpl userServiceImpl = new UserServiceImpl();

	public static long logServiceStartAndGetUserID(HttpServletRequest request, 
			Logger logger, String serviceName) throws DatabaseException, UserNotLoggedInException {
		long userID = 0;
		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
				new ServiceRequestReceivedMessage(serviceName));

		//verify user cookies and get user id
		logger.log(Level.TRACE, LogMarker.CTAP_SERVER_MARKER, 
				new VerifyingUserCookiesMessage(serviceName));
		UserInfo userInfo = userServiceImpl.verifyUserCookies(request);
		if(userInfo != null) {
			userID = userInfo.getId();
		} else {
			throw logger.throwing(new UserNotLoggedInException());
		}
		return userID;
	}

}

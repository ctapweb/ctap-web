package com.ctapweb.web.server;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import com.ctapweb.web.shared.SharedProperties;
import com.ctapweb.web.shared.UserInfo;

/**Provides utility functions for server classes.
 * 
 * @author xiaobin
 *
 */
public class Utils {

	public static void setUserCookies(HttpServletResponse response, 
			UserInfo userInfo, boolean rememberUser) {
		int cookieAge; 
		if(rememberUser) {
			// age of cookie
			cookieAge = 60 * 60 * 365; // one year 
		} else {
			cookieAge = -1; // minus age mean it will expire when the browser window is closed
		}

		Cookie emailCookie = new Cookie(SharedProperties.COOKIES_USER_EMAIL, userInfo.getEmail());
		Cookie idCookie = new Cookie(SharedProperties.COOKIES_USER_ID, userInfo.getId() + "");
		Cookie sessionTokenCookie = new Cookie(SharedProperties.COOKIES_USER_SESSIONTOKEN, 
				userInfo.getSessionToken());
		emailCookie.setMaxAge(cookieAge);
		idCookie.setMaxAge(cookieAge);
		sessionTokenCookie.setMaxAge(cookieAge);
		
		response.addCookie(emailCookie);
		response.addCookie(idCookie);
		response.addCookie(sessionTokenCookie);
	
}
	
}

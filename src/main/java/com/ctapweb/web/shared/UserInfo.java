package com.ctapweb.web.shared;

import java.io.Serializable;
/**
 * Stores the user information: id and sessionID
 * @author xiaobin
 *
 */
public class UserInfo implements Serializable {

	private long id=0;
	private String email ="";
	private String passwd ="";
	private String sessionToken = "";

	public UserInfo() {
	}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}


	public String getSessionToken() {
		return sessionToken;
	}

	public void setSessionToken(String sessionToken) {
		this.sessionToken = sessionToken;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPasswd() {
		return passwd;
	}

	public void setPasswd(String passwd) {
		this.passwd = passwd;
	}
	
	@Override
	public String toString() {
		return "id: " + id + "; email: " + email +"; sessionToken: " + sessionToken;
	}

}

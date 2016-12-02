package com.ctapweb.web.shared.exception;

import java.io.Serializable;

public class UserNotLoggedInException extends Exception implements Serializable {

	private static final String message = "User not logged in.";

	public UserNotLoggedInException() {
		super(message);
	}

	public UserNotLoggedInException(String message) {
		super(message);
	}

}

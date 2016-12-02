package com.ctapweb.web.shared.exception;

import java.io.Serializable;

public class UserOwnerNotMatchException extends Exception implements Serializable {

	private static final String message = "The logged in user and the resource owner do not match.";

	public UserOwnerNotMatchException() {
		super(message);
	}

	public UserOwnerNotMatchException(String message) {
		super(message);
	}

}

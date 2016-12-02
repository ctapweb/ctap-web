package com.ctapweb.web.shared.exception;

import java.io.Serializable;

public class AdminNotLoggedInException extends Exception implements Serializable {

	private static final String message = "User not logged in as admin.";

	public AdminNotLoggedInException() {
		super(message);
	}

	public AdminNotLoggedInException(String message) {
		super(message);
	}

}

package com.ctapweb.web.shared.exception;

import java.io.Serializable;

public class UserNotFoundException extends Exception implements Serializable {

	private static final String message = "User does not exist in the database."; 

	public UserNotFoundException() {
		super(message);
	}

	public UserNotFoundException(String userEmail) {
		super("User " + userEmail + " does not exist in the database.");
	}

}

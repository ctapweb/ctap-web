package com.ctapweb.web.shared.exception;

import java.io.Serializable;

public class WrongPasswordException extends Exception implements Serializable {

	private static final String message = "Password incorrect.";
	
	public WrongPasswordException() {
		super(message);
	}

	public WrongPasswordException(String userEmail) {
		super("Incorrect password attempted for user " + userEmail +".");
	}

}

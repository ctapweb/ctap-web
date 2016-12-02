package com.ctapweb.web.shared.exception;

import java.io.Serializable;

public class DatabaseException extends Exception implements Serializable {

	private static final String message = "A database error occurred.";

	public DatabaseException() {
		super(message);
	}

	public DatabaseException(String msg) {
		super(msg);
	}
	
	public DatabaseException(Throwable e) {
		super(e);
	}


	
	
}

package com.ctapweb.web.shared.exception;

import java.io.Serializable;

public class EmptyInfoException extends Exception implements Serializable {

	private static final String message = "Some required information is not provided.";

	public EmptyInfoException() {
		super(message);
	}

	public EmptyInfoException(String msg) {
		super(msg);
	}

}

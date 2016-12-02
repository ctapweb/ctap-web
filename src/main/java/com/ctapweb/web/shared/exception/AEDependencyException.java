package com.ctapweb.web.shared.exception;

import java.io.Serializable;

public class AEDependencyException extends Exception implements Serializable {
	
	private static final String message = "An AE dependency exception occured.";

	public AEDependencyException() {
		super(message);
	}

	public AEDependencyException(String errorMessage) {
		super(errorMessage);
	}

}

package com.ctapweb.web.shared.exception;

import java.io.Serializable;

public class UIMAException extends Exception implements Serializable {
	
	private static final String message = "An UIMA error occured, please contact the administrator "
			+ "for assistance to solve the problem.";

	public UIMAException() {
		super(message);
	}

	public UIMAException(String errorMessage) {
		super(errorMessage);
	}
	
	public UIMAException(Throwable cause) {
		super(cause);
	}

}

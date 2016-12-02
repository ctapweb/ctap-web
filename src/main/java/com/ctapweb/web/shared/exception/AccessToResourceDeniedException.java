package com.ctapweb.web.shared.exception;

import java.io.Serializable;

public class AccessToResourceDeniedException extends Exception implements Serializable {
	
	private static final String message = "You are not the owner of this "
			+ "resource or the resource does not exist.";

	public AccessToResourceDeniedException() {
		super(message);
	}

	public AccessToResourceDeniedException(String errorMessage) {
		super(message);
	}

}

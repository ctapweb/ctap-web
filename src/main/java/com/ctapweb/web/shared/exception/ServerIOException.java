package com.ctapweb.web.shared.exception;

import java.io.Serializable;

public class ServerIOException extends Exception implements Serializable {

	private static final String message = "An IO error occurred on the server.";

	public ServerIOException() {
		super(message);
	}

	public ServerIOException(String msg) {
		super(msg);
	}


	
	
}

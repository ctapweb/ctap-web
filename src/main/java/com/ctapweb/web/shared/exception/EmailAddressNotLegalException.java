package com.ctapweb.web.shared.exception;

import java.io.Serializable;


public class EmailAddressNotLegalException extends Exception implements Serializable {

	private static final String message = "The Email adress is not legal.";

	public EmailAddressNotLegalException() {
		super(message);
	}

	public EmailAddressNotLegalException(String message) {
		super(message);
	}


	
	
}

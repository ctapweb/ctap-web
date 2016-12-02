/**
 * 
 */
package com.ctapweb.web.shared.exception;

import java.io.Serializable;

/**
 * @author xiaobin
 *
 */
public class UserAlreadyExistsException extends Exception implements Serializable {

	private static final String message = "The user already exists.";

	public UserAlreadyExistsException() {
		super(message);
	}

	public UserAlreadyExistsException(String message) {
		super(message);
	}

}

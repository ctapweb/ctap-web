/**
 * 
 */
package com.ctapweb.web.shared.exception;

import java.io.Serializable;

/**
 * @author xiaobin
 *
 */
public class ResourceAlreadyExistsException extends Exception implements Serializable {

	private static final String message = "A resource with the same name already exists.";

	public ResourceAlreadyExistsException() {
		super(message);
	}

	public ResourceAlreadyExistsException(String message) {
		super(message);
	}

}

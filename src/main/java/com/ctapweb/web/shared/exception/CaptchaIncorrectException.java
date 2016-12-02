package com.ctapweb.web.shared.exception;

import java.io.Serializable;

public class CaptchaIncorrectException extends Exception implements Serializable {

	private static final String message = "The captcha characters entered are not correct.";

	public CaptchaIncorrectException() {
		super(message);
	}

	public CaptchaIncorrectException(String msg) {
		super(msg);
	}

}

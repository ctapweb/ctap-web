package com.ctapweb.web.server.logging;

import org.apache.logging.log4j.message.ParameterizedMessage;

/**
 * A message for logging AE initialization event.
 * @author xiaobin
 *
 */
public class VerifyingUserCookiesMessage extends ParameterizedMessage {

	public VerifyingUserCookiesMessage(String serviceName) {
		super("Verifying user cookies for service {}...", serviceName);
	}
	
}

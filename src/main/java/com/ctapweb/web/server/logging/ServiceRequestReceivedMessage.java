package com.ctapweb.web.server.logging;

import org.apache.logging.log4j.message.ParameterizedMessage;

import com.ctapweb.web.shared.UserInfo;

/**
 * A message for logging AE initialization event.
 * @author xiaobin
 *
 */
public class ServiceRequestReceivedMessage extends ParameterizedMessage {


	public ServiceRequestReceivedMessage(String serviceName, UserInfo userInfo) {
		super("Received request for service {} from <{}>.", new Object[] {serviceName, userInfo});
	}

	public ServiceRequestReceivedMessage(String serviceName) {
		super("Received request for service {}.", serviceName);
	}
	
	
}

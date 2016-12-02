package com.ctapweb.web.server.logging;

import org.apache.logging.log4j.message.ParameterizedMessage;

import com.ctapweb.web.shared.UserInfo;

/**
 * A message for logging AE initialization event.
 * @author xiaobin
 *
 */
public class ServiceRequestCompletedMessage extends ParameterizedMessage {


	public ServiceRequestCompletedMessage(String serviceName, UserInfo userInfo) {
		super("Completed request for service {} from <{}>. Returning to client...", new Object[] {serviceName, userInfo});
	}
	
	public ServiceRequestCompletedMessage(String serviceName) {
		super("Completed request for service {}. Returning to client...", serviceName);
	}
	
}

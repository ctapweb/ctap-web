package com.ctapweb.web.server.admin;

import java.util.logging.LogRecord;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.jul.DefaultLevelConverter;
import org.apache.logging.log4j.jul.LevelConverter;

import com.ctapweb.web.server.logging.LogMarker;
import com.google.gwt.logging.shared.RemoteLoggingService;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * Implements RemoteLoggingService, which is used by GWT to send client logs to
 * the server. On the CTAP server side, we used the log4j framework. So this
 * class is used as a bridge between Java's native logging framework, which is
 * used by GWT client side, and the log4j.
 * 
 * @author xiaobin
 *
 */
public class CTAPRemoteLoggingServiceImpl extends RemoteServiceServlet implements RemoteLoggingService {

	// Logger JULRootLogger = Logger.getLogger("");
	private LevelConverter levelConverter = new DefaultLevelConverter();

	/**
	 * A few steps are taken to bridge between Java logging and log4j. 1. Map
	 * JUL levels to log4j levels 2.
	 */
	@Override
	public String logOnServer(LogRecord record) {

		String loggerName = record.getLoggerName();
		Level logLevel = levelConverter.toLevel(record.getLevel());
		String logMessage = record.getMessage();

		Logger logger = LogManager.getLogger(loggerName);
		logger.log(logLevel, LogMarker.CTAP_CLIENT_MARKER, logMessage);

		return null;
	}

}

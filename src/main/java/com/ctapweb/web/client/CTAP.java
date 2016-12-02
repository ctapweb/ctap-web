package com.ctapweb.web.client;

import java.util.logging.Logger;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.History;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class CTAP implements EntryPoint {
	Logger logger = Logger.getLogger(CTAP.class.getName());

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.gwt.core.client.EntryPoint#onModuleLoad()
	 */
	@Override
	public void onModuleLoad() {

		logger.finer("System started.");

		// add history management
		logger.finer("Adding history mechanism, which is used to navigate the system components...");
		History.addValueChangeHandler(new HistoryValueChangeHandler());

		// first time the page is opened
		if (History.getToken().isEmpty()) {
			logger.finer("No history token detected. Seting history token and redirecting to the signin page...");
			History.newItem(HistoryToken.signin);
		} else {
			History.fireCurrentHistoryState();
		}

		// //add history management
		// logger.finer("Adding history mechanism, which is used to navigate the
		// system components...");
		// History.addValueChangeHandler(new HistoryValueChangeHandler());

		// History.fireCurrentHistoryState();

	}

}

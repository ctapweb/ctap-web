/**
 * 
 */
package com.ctapweb.web.client.component;

import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author xiaobin
 *
 */
public class Error extends Composite {

	private static ErrorUiBinder uiBinder = GWT.create(ErrorUiBinder.class);

	interface ErrorUiBinder extends UiBinder<Widget, Error> {
	}

	@UiField Label errorMsg;
	
	Logger logger = Logger.getLogger(Error.class.getName());
	
	public Error() {
		initWidget(uiBinder.createAndBindUi(this));
	}
	
	public Error(String msg) {
		initWidget(uiBinder.createAndBindUi(this));
		
		errorMsg.setText(msg);
		
		// log the message
		logger.severe(msg);

	}

}

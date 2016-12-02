package com.ctapweb.web.client;

import java.util.logging.Logger;

import com.ctapweb.web.client.component.Error;
import com.ctapweb.web.client.component.PrivacyPolicy;
import com.ctapweb.web.client.component.RecoverPassword;
import com.ctapweb.web.client.component.Signin;
import com.ctapweb.web.client.component.Signup;
import com.ctapweb.web.client.component.Terms;
import com.ctapweb.web.client.component.UserHome;
import com.ctapweb.web.client.component.admin.InitDB;
import com.ctapweb.web.client.service.Services;
import com.ctapweb.web.shared.SharedProperties;
import com.ctapweb.web.shared.UserInfo;
import com.ctapweb.web.shared.Utils;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

public class HistoryValueChangeHandler implements ValueChangeHandler<String> {

	Logger logger = Logger.getLogger(HistoryValueChangeHandler.class.getName());

	/**
	 * Handles history token. Based on the token value, displays different pages.
	 * This is also used to navigate the application.
	 */
	@Override
	public void onValueChange(ValueChangeEvent<String> event) {

		final String userCookieValue = Cookies.getCookie(SharedProperties.USERCOOKIENAME);

		String historyToken = History.getToken();
		String[] tokenSplit =  historyToken.split("\\?");
		String pageToken = tokenSplit[0];

		logger.finer("History token [" + historyToken + "] detected, opening the page... ");
				
		//decide which page to show
		switch(pageToken) {
		case HistoryToken.signup:
			clearAndAddToRootPanel(new Signup());
			break;
		case HistoryToken.terms:
			clearAndAddToRootPanel(new Terms());
			break;
		case HistoryToken.privacy:
			clearAndAddToRootPanel(new PrivacyPolicy());
			break;
		case HistoryToken.recoverPass:
			clearAndAddToRootPanel(new RecoverPassword());
			break;

		case HistoryToken.userhome:
		case HistoryToken.dashboard:
		case HistoryToken.corpusmanager:
		case HistoryToken.textmanager:
		case HistoryToken.featureselector:
		case HistoryToken.featuresetmanager:
		case HistoryToken.analysisgenerator:
		case HistoryToken.resultvisualizer:
		case HistoryToken.groupsetmanager:
		case HistoryToken.groupmanager:
		case HistoryToken.editprofile:
		case HistoryToken.sendfeedback:
		case HistoryToken.documentation:
		case HistoryToken.adminDB:
		case HistoryToken.adminUser:
		case HistoryToken.adminAE:
		case HistoryToken.adminCF:
			logger.finer("Page viewable only to logged in user. Requesting verifyUserCookies service...");
			Services.getUserService().verifyUserCookies(new AsyncCallback<UserInfo>() {
				@Override
				public void onFailure(Throwable caught) {
					logger.finer("Caught server exception " + caught);
					Utils.showErrorPage(caught.getMessage());
				}
				@Override
				public void onSuccess(UserInfo userInfo) {
					if(userInfo != null) {
						logger.finer("User cookies verified, opening User Home...");
						clearAndAddToRootPanel(new UserHome(userInfo)); 
					} else {
						logger.finer("User cookies could not be verified, opening Signin page...");
						History.newItem(HistoryToken.signin);
					}
				}
			});
			break;

		case HistoryToken.error:
			clearAndAddToRootPanel(new Error(HistoryToken.getErrorMsg()));
			break;
		case HistoryToken.initDB:
			clearAndAddToRootPanel(new InitDB());
			break;
		case HistoryToken.signin: 
		default:
			//show the home page, which is the signin page
			clearAndAddToRootPanel(new Signin());
		}
		
	}

	private void clearAndAddToRootPanel(Widget widget) {
		RootPanel.get().clear();
		RootPanel.get().add(widget);
	}
}

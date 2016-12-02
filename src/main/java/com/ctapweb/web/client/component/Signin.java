/**
 * 
 */
package com.ctapweb.web.client.component;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.ctapweb.web.client.HistoryToken;
import com.ctapweb.web.client.service.Services;
import com.ctapweb.web.shared.UserInfo;
import com.ctapweb.web.shared.Utils;
import com.ctapweb.web.shared.exception.DatabaseException;
import com.ctapweb.web.shared.exception.EmptyInfoException;
import com.ctapweb.web.shared.exception.UserNotFoundException;
import com.ctapweb.web.shared.exception.WrongPasswordException;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * The sign in page of the system.
 * 
 * @author xiaobin
 *
 */
public class Signin extends Composite {

	private static SigninUiBinder uiBinder = GWT.create(SigninUiBinder.class);

	interface SigninUiBinder extends UiBinder<Widget, Signin> {
	}

	@UiField HTMLPanel emailFormGroup;
	@UiField TextBox email;
	@UiField Label emailError;

	@UiField HTMLPanel passwdFormGroup;
	@UiField PasswordTextBox passwd;
	@UiField Label passwdError;

	@UiField CheckBox rememberMe;

	@UiField Anchor forgotPassword;
	@UiField Anchor newUser;

	@UiField Button login;

	Logger logger = Logger.getLogger(Signin.class.getName());

	public Signin() {
		logger.log(Level.FINER, "Opening Signin page...");

		initWidget(uiBinder.createAndBindUi(this));

		initWidgetStyles();
	}

	private void initWidgetStyles() {
		// set placeholder
		String formGroupNormalStyle = "form-group has-feedback";
		emailFormGroup.setStyleName(formGroupNormalStyle);
		passwdFormGroup.setStyleName(formGroupNormalStyle);
		email.getElement().setAttribute("placeholder", "Email");
		passwd.getElement().setAttribute("placeholder", "Password");
		forgotPassword.getElement().getStyle().setCursor(Cursor.POINTER);
		newUser.getElement().getStyle().setCursor(Cursor.POINTER);
		emailError.setVisible(false);
		passwdError.setVisible(false);

	}

	@UiHandler("login")
	void onLoginClick(ClickEvent e) {
		logger.finer("Login button clicked, doing login action...");
		doLogin();
	}

	void doLogin() {

		initWidgetStyles();

		if (isInputValid()) {
			logger.finer("Input verified, sending user info to server...");
			// log the user in
			UserInfo userInfo = new UserInfo();
			userInfo.setEmail(email.getText());
			userInfo.setPasswd(passwd.getText());

			logger.finer("Requesting login service for user <" + userInfo +  ">...");
			Services.getUserService().signin(userInfo, rememberMe.getValue(),
					new AsyncCallback<Void>() {

				@Override
				public void onSuccess(Void result) {
					// log in succeeded, set cookie
					logger.finer("Received successful server reply. Redirecting to UserHome...");

					//TODO delete this line
//					Cookies.setCookie(SharedProperties.USERCOOKIENAME, userInfo.getSessionToken());

					// show user home
					History.newItem(HistoryToken.userhome);
				}

				@Override
				public void onFailure(Throwable caught) {
					logger.log(Level.WARNING, "Caught server exception " + caught);
					// show error msg
					if (caught instanceof UserNotFoundException) {
						showEmailError("Email does not exist in database.");
					} else if (caught instanceof WrongPasswordException) {
						showPasswdError("Password incorrect.");
					} else if (caught instanceof EmptyInfoException || 
							caught instanceof DatabaseException) {
						// serious error, show the error panel
						logger.log(Level.SEVERE, "Caught severe server exception, redirecting to error page..." );
						Utils.showErrorPage(caught.getMessage());
					}
				}
			});
		}

	}

	private boolean isInputValid() {
		logger.finer("Checking if input is valid...");
		boolean isValid = true;

		// validate email input
		String emailAdd = email.getText();
		if (!Utils.isEmailValid(emailAdd)) {
			// not valid, show the error message
			logger.finer("Email address " + emailAdd + " is not valid.");
			showEmailError("Empty or wrong email address.");
			isValid = false;
		}

		// validate password
		String passwdStr = passwd.getText();
		if (passwdStr.isEmpty()) {
			logger.finer("Pass word is not valid: empty pass word.");
			showPasswdError("Please enter your password.");
			isValid = false;
		}

		return isValid;
	}

	private void showEmailError(String errorMsg) {
		emailFormGroup.addStyleName("has-error");
		emailError.setText(errorMsg);
		emailError.setVisible(true);
	}

	private void showPasswdError(String errorMsg) {
		passwdFormGroup.addStyleName("has-error");
		passwdError.setText(errorMsg);
		passwdError.setVisible(true);
	}

	@UiHandler("newUser")
	void onNewUserClick(ClickEvent e) {
		logger.finer("User clicked New User button. Redirecting to Signup page...");
		History.newItem(HistoryToken.signup);
	}

	@UiHandler("passwd")
	void onPasswdKeyDown(KeyDownEvent e) {
		if (e.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
			doLogin();
		}
	}

	@UiHandler("forgotPassword")
	void onForgotPasswordClick(ClickEvent e) {
		logger.finer("User clicked Forgot Password button. Redirecting to Recover Pass page...");
		History.newItem(HistoryToken.recoverPass);
	}
}

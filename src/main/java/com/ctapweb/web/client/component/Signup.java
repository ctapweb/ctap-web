package com.ctapweb.web.client.component;

import java.util.logging.Logger;

import com.ctapweb.web.client.HistoryToken;
import com.ctapweb.web.client.service.Services;
import com.ctapweb.web.shared.UserInfo;
import com.ctapweb.web.shared.Utils;
import com.ctapweb.web.shared.exception.CaptchaIncorrectException;
import com.ctapweb.web.shared.exception.DatabaseException;
import com.ctapweb.web.shared.exception.EmailAddressNotLegalException;
import com.ctapweb.web.shared.exception.EmptyInfoException;
import com.ctapweb.web.shared.exception.UserAlreadyExistsException;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.thirdparty.guava.common.util.concurrent.CycleDetectingLockFactory.Policies;
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

public class Signup extends Composite {

	private static SignupUiBinder uiBinder = GWT.create(SignupUiBinder.class);

	interface SignupUiBinder extends UiBinder<Widget, Signup> {
	}

	@UiField HTMLPanel emailFormGroup;
	@UiField TextBox email;
	@UiField Label emailError;

	@UiField HTMLPanel captchaFormGroup;
	@UiField TextBox captcha;
	@UiField Label captchaError;

	@UiField HTMLPanel passwdFormGroup1;
	@UiField PasswordTextBox passwd1;
	@UiField Label passwdError1;

	@UiField HTMLPanel passwdFormGroup2;
	@UiField PasswordTextBox passwd2;
	@UiField Label passwdError2;

	@UiField HTMLPanel termsFormGroup;
	@UiField CheckBox agree;
	@UiField Anchor terms;
	@UiField Anchor privacy;
	@UiField Label termsError;

	@UiField Button signup;
	@UiField Button signin;

	Logger logger = Logger.getLogger(Signup.class.getName());

	public Signup() {
		logger.finer("Opening Signup page...");
		initWidget(uiBinder.createAndBindUi(this));

		initWidgetStyles();
	}

	private void initWidgetStyles() {
		// set placeholder
		String formGroupNormalStyle = "form-group has-feedback";
		emailFormGroup.setStyleName(formGroupNormalStyle);
		captchaFormGroup.setStyleName(formGroupNormalStyle);
		passwdFormGroup1.setStyleName(formGroupNormalStyle);
		passwdFormGroup2.setStyleName(formGroupNormalStyle);
		termsFormGroup.setStyleName(formGroupNormalStyle);
		email.getElement().setAttribute("placeholder", "Email");
		passwd1.getElement().setAttribute("placeholder", "Password");
		passwd2.getElement().setAttribute("placeholder", "Retype Password");
		captcha.getElement().setAttribute("placeholder", "Enter the Characters Shown Above");
		emailError.setVisible(false);
		passwdError1.setVisible(false);
		passwdError2.setVisible(false);
		captchaError.setVisible(false);
		termsError.setVisible(false);
		terms.getElement().getStyle().setCursor(Cursor.POINTER);
		privacy.getElement().getStyle().setCursor(Cursor.POINTER);

	}

	@UiHandler("signup")
	void onSignUpClick(ClickEvent e) {

		logger.finer("User clicked Sign up button. Doing sign up action...");
		initWidgetStyles();

		if (isInputValid()) {
			logger.finer("User input verified. Creating user info object...");
			// sign up the user;
			UserInfo userInfo = new UserInfo();
			userInfo.setEmail(email.getText());
			userInfo.setPasswd(passwd1.getText());
			String captchaStr = captcha.getText();

			logger.info("Requesting sign up service for user <" + userInfo +">...");
			Services.getUserService().signup(userInfo, captchaStr, new AsyncCallback<Void>() {

				@Override
				public void onFailure(Throwable caught) {
					logger.warning("Received server exception " + caught);
					// show error msg
					if (caught instanceof EmailAddressNotLegalException
							|| caught instanceof UserAlreadyExistsException) {
						showEmailError(caught.getMessage());
					} else if (caught instanceof EmptyInfoException || caught instanceof DatabaseException) {
						// serious error, show the error panel
						HistoryToken.setErrorMsg(caught.getMessage());
						History.newItem(HistoryToken.error);
					} if (caught instanceof CaptchaIncorrectException) {
						showCaptchaError(caught.getMessage());
						
					}
				}

				@Override
				public void onSuccess(Void result) {
					logger.finer("Received successful server reply. Redirecting to User Home...");
					History.newItem(HistoryToken.userhome);
				}
			});
		}

	}

	private boolean isInputValid() {
		logger.finer("Validating user input...");
		boolean isValid = true;
		// validate email input
		if (!Utils.isEmailValid(email.getText())) {
			// not valid, show the error message
			showEmailError("Empty or wrong email address format.");
			isValid = false;
		}

		// validate password
		if (passwd1.getText().length() < 6 || 
				passwd2.getText().length() < 6) {
			showPasswdError("Password should be at least 6 characters.");
			isValid = false;
		}
		
		if(!passwd1.getText().equals(passwd2.getText())) {
			showPasswdError("Passwords do not match.");
			isValid = false;
		}
		
		//validate captcha is entered
		if(captcha.getText().isEmpty()) {
			showCaptchaError("Please enter the characters shown above.");
			isValid = false;
		}
		
		if(!agree.getValue()) {
			showTermsError("You must agree to the terms to use our service.");
			isValid = false;
		}

		return isValid;
	}

	private void showEmailError(String errorMsg) {
		logger.finer("Email input error: " + errorMsg + ".");
		emailFormGroup.addStyleName("has-error");
		emailError.setText(errorMsg);
		emailError.setVisible(true);
	}

	private void showCaptchaError(String errorMsg) {
		logger.finer("Captcha input error: " + errorMsg + ".");
		captchaFormGroup.addStyleName("has-error");
		captchaError.setText(errorMsg);
		captchaError.setVisible(true);
	}
	
	private void showPasswdError(String errorMsg) {
		logger.finer("Password input error: " + errorMsg + ".");
		passwdFormGroup1.addStyleName("has-error");
		passwdError1.setText(errorMsg);
		passwdError1.setVisible(true);

		passwdFormGroup2.addStyleName("has-error");
		passwdError2.setText(errorMsg);
		passwdError2.setVisible(true);
	}

	private void showTermsError(String errorMsg) {
		logger.finer("Terms input error: " + errorMsg + ".");
		termsFormGroup.addStyleName("has-error");
		termsError.setText(errorMsg);
		termsError.setVisible(true);
	}

	@UiHandler("signin")
	void onSigninClick(ClickEvent e) {
		logger.finer("User clicked Sign in button. Redirecting to Sign in page...");
		History.newItem(HistoryToken.signin);
	}
	
	@UiHandler("terms")
	void onTermsClick(ClickEvent e) {
		logger.finer("User clicked terms button. Redirecting to terms page...");
		History.newItem(HistoryToken.terms);
	}
	@UiHandler("privacy")
	void onPrivacyClick(ClickEvent e) {
		logger.finer("User clicked privacy policy button. Redirecting to privacy policy page...");
		History.newItem(HistoryToken.privacy);
		
	}
}

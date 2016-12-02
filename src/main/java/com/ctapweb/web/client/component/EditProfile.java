package com.ctapweb.web.client.component;

import java.util.logging.Logger;

import com.ctapweb.web.client.service.Services;
import com.ctapweb.web.shared.UserInfo;
import com.ctapweb.web.shared.Utils;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class EditProfile extends Composite {

	private static EditProfileUiBinder uiBinder = GWT.create(EditProfileUiBinder.class);

	interface EditProfileUiBinder extends UiBinder<Widget, EditProfile> {
	}

	@UiField HTMLPanel emailFormGroup;
	@UiField TextBox email;
//	@UiField Label emailError;

	@UiField HTMLPanel passwdFormGroup1;
	@UiField PasswordTextBox passwd1;
	@UiField Label passwdError1;
	@UiField HTMLPanel passwdFormGroup2;
	@UiField PasswordTextBox passwd2;
	@UiField Label passwdError2;

	@UiField Label feedbackLabel;
	@UiField Button update;

	Logger logger = Logger.getLogger(CorpusManager.class.getName());

	public EditProfile() {
		initWidget(uiBinder.createAndBindUi(this));

		initWidgetStyles();

		//get user email from cookie
		logger.finer("Requesting service verifyUserCookies...");
		Services.getUserService().verifyUserCookies(new AsyncCallback<UserInfo>() {
			@Override
			public void onFailure(Throwable caught) {
				logger.severe("Caught service exception: " + caught);
				Utils.showErrorPage(caught.getMessage());
			}

			@Override
			public void onSuccess(UserInfo userInfo) {
				logger.finer("verifyUserCookies returned successfully.");
				email.setText(userInfo.getEmail());
			}
		});
	}

	private void initWidgetStyles() {
		// set placeholder
		String formGroupNormalStyle = "form-group";
		emailFormGroup.setStyleName(formGroupNormalStyle);
		passwdFormGroup1.setStyleName(formGroupNormalStyle);
		passwdFormGroup2.setStyleName(formGroupNormalStyle);
//		emailError.setVisible(false);
		passwdError1.setVisible(false);
		passwdError2.setVisible(false);
		feedbackLabel.setVisible(false);

	}

	@UiHandler("update")
	void onUpdateClick(ClickEvent e) {
		logger.finer("User clicked update button.");

		initWidgetStyles();

		if (isInputValid()) {
			// update user info;
			UserInfo userInfo = new UserInfo();
			userInfo.setPasswd(passwd1.getText());

			logger.finer("Requesting service updateProfile...");
			Services.getUserService().updateProfile(userInfo, new AsyncCallback<Void>() {

				@Override
				public void onFailure(Throwable caught) {
					// serious error, show the error panel
					logger.severe("Caught service exception " + caught);
					Utils.showErrorPage(caught.getMessage());
				}

				@Override
				public void onSuccess(Void result) {
					logger.finer("updateProfile returned successfully.");
					feedbackLabel.setVisible(true);
				}
			});
		}

	}

	private boolean isInputValid() {
		boolean isValid = true;

		String password1 = passwd1.getText();
		String password2 = passwd2.getText();
		// validate password
		if (password1.length() < 6 || password2.length() < 6 || !password1.equals(password2) ) {
			showPasswdError("Please make sure your password is no less than 6 characters "
					+ "and the two passwords match. ");
			isValid = false;
		}
		return isValid;
	}

	private void showPasswdError(String errorMsg) {
		passwdFormGroup1.addStyleName("has-error");
		passwdError1.setText(errorMsg);
		passwdError1.setVisible(true);
		passwdFormGroup2.addStyleName("has-error");
		passwdError2.setText(errorMsg);
		passwdError2.setVisible(true);
	}
}

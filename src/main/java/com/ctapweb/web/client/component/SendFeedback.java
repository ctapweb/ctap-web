package com.ctapweb.web.client.component;

import java.util.logging.Logger;

import com.ctapweb.web.client.service.Services;
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
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class SendFeedback extends Composite {

	private static SendFeedbackUiBinder uiBinder = GWT.create(SendFeedbackUiBinder.class);

	interface SendFeedbackUiBinder extends UiBinder<Widget, SendFeedback> {
	}

	@UiField HTMLPanel subjectFormGroup;
	@UiField TextBox subject;
	@UiField Label subjectError;

	@UiField HTMLPanel contentFormGroup;
	@UiField TextArea content;
	@UiField Label contentError;

	@UiField Label feedbackLabel;
	@UiField Button send;

	Logger logger = Logger.getLogger(CorpusManager.class.getName());

	public SendFeedback() {
		initWidget(uiBinder.createAndBindUi(this));

		initWidgetStyles();
	}

	private void initWidgetStyles() {
		// set placeholder
		String formGroupNormalStyle = "form-group";
		subjectFormGroup.setStyleName(formGroupNormalStyle);
		contentFormGroup.setStyleName(formGroupNormalStyle);
		subjectError.setVisible(false);
		contentError.setVisible(false);
		feedbackLabel.setVisible(false);
	}

	@UiHandler("send")
	void onSendClick(ClickEvent e) {
		logger.finer("User clicked send button.");

		initWidgetStyles();

		if (isInputValid()) {
			// send feedback to server
			logger.finer("Requesting service sendFeedback...");
			Services.getUserService().sendFeedback(subject.getText(), content.getText(),
					new AsyncCallback<Void>() {

				@Override
				public void onFailure(Throwable caught) {
					logger.severe("Caught service exception " + caught);
					Utils.showErrorPage(caught.getMessage());
				}

				@Override
				public void onSuccess(Void result) {
					logger.finer("sendFeedback service returned successfully.");
					feedbackLabel.setVisible(true);
					subject.setText("");
					content.setText("");
				}
			});
		}

	}

	private boolean isInputValid() {
		boolean isValid = true;
		// validate subject
		if (subject.getText().isEmpty()) {
			showSubjectError("Please enter a subject for your feedback.");
			isValid = false;
		}

		// validate content
		if (content.getText().isEmpty()) {
			showContentError("Please enter your feedback content.");
			isValid = false;
		}
		return isValid;
	}

	private void showContentError(String errorMsg) {
		contentFormGroup.addStyleName("has-error");
		contentError.setText(errorMsg);
		contentError.setVisible(true);
	}

	private void showSubjectError(String errorMsg) {
		subjectFormGroup.addStyleName("has-error");
		subjectError.setText(errorMsg);
		subjectError.setVisible(true);
	}
}
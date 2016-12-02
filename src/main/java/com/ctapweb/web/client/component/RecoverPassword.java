package com.ctapweb.web.client.component;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class RecoverPassword extends Composite {

	private static RecoverPasswordUiBinder uiBinder = GWT.create(RecoverPasswordUiBinder.class);

	interface RecoverPasswordUiBinder extends UiBinder<Widget, RecoverPassword> {
	}

	public RecoverPassword() {
		initWidget(uiBinder.createAndBindUi(this));
	}

}

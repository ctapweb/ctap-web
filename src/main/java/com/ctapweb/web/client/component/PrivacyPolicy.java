package com.ctapweb.web.client.component;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class PrivacyPolicy extends Composite {

	private static PrivacyPolicyUiBinder uiBinder = GWT.create(PrivacyPolicyUiBinder.class);

	interface PrivacyPolicyUiBinder extends UiBinder<Widget, PrivacyPolicy> {
	}

	public PrivacyPolicy() {
		initWidget(uiBinder.createAndBindUi(this));
	}

}

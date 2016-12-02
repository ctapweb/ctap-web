package com.ctapweb.web.client.component;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class Terms extends Composite {

	private static TermsUiBinder uiBinder = GWT.create(TermsUiBinder.class);

	interface TermsUiBinder extends UiBinder<Widget, Terms> {
	}

	public Terms() {
		initWidget(uiBinder.createAndBindUi(this));
	}

}

package com.ctapweb.web.client.component;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class Documentation extends Composite {

	private static DocumentationUiBinder uiBinder = GWT.create(DocumentationUiBinder.class);

	interface DocumentationUiBinder extends UiBinder<Widget, Documentation> {
	}

	public Documentation() {
		initWidget(uiBinder.createAndBindUi(this));
	}

}

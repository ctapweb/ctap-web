package com.ctapweb.web.client.component.widget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;

public class SweetAlert extends Composite {

	private static SweetAlertUiBinder uiBinder = GWT.create(SweetAlertUiBinder.class);

	interface SweetAlertUiBinder extends UiBinder<Widget, SweetAlert> {
	}

	@UiField HTMLPanel sweetAlertPanel;
	@UiField Button closeBtn;
	public SweetAlert() {
		initWidget(uiBinder.createAndBindUi(this));
	}
	
	@UiHandler("closeBtn")
	void onCloseBtnClick(ClickEvent e) {
		sweetAlertPanel.setVisible(false);
	}

}

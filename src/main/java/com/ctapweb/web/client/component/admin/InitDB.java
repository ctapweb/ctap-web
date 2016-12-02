package com.ctapweb.web.client.component.admin;

import com.ctapweb.web.client.service.Services;
import com.ctapweb.web.shared.Utils;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.Widget;

public class InitDB extends Composite {

	private static InitDBUiBinder uiBinder = GWT.create(InitDBUiBinder.class);

	interface InitDBUiBinder extends UiBinder<Widget, InitDB> {
	}

	@UiField PasswordTextBox passwd;
	@UiField Button initDBbtn;
	
	public InitDB() {
		initWidget(uiBinder.createAndBindUi(this));
	}
	
	@UiHandler("initDBbtn")
	void onInitDBbtnClick(ClickEvent e) {
		Services.getAdminService().initDB(passwd.getText(), new AsyncCallback<Void>() {
			
			@Override
			public void onSuccess(Void result) {
				Window.alert("Databased initialized successfully!");
			}
			
			@Override
			public void onFailure(Throwable caught) {
				Utils.showErrorPage(caught.getMessage());
			}
		});
	}

}

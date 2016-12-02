package com.ctapweb.web.client.component.admin;

import com.ctapweb.web.client.service.Services;
import com.ctapweb.web.shared.SharedProperties;
import com.ctapweb.web.shared.Utils;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class DBManager extends Composite {

	private static DBManagerUiBinder uiBinder = GWT.create(DBManagerUiBinder.class);

	interface DBManagerUiBinder extends UiBinder<Widget, DBManager> {
	}

	@UiField
	Button initDB;

	public DBManager() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@UiHandler("initDB")
	void onInitDBClick(ClickEvent e) {
		String userCookieValue = Cookies.getCookie(SharedProperties.USERCOOKIENAME);

		if (userCookieValue == null) {
			Window.alert("Please log in as admin first!");
			return;
		}

		Services.getAdminService().initDB(userCookieValue, new AsyncCallback<Void>() {

			@Override
			public void onFailure(Throwable caught) {
				Utils.showErrorPage(caught.getMessage());
			}

			@Override
			public void onSuccess(Void result) {
				Window.alert("Database initialized!");
			}
		});
	}

}

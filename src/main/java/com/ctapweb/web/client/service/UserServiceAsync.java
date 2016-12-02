package com.ctapweb.web.client.service;

import com.ctapweb.web.shared.UserInfo;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface UserServiceAsync {

	void updateProfile(UserInfo userInfo, AsyncCallback<Void> callback);

	void signin(UserInfo userInfo, boolean rememberUser, AsyncCallback<Void> callback);

	void signup(UserInfo userInfo, String captcha, AsyncCallback<Void> callback);

	void signout(AsyncCallback<Void> callback);

	void sendFeedback(String subject, String content, AsyncCallback<Void> callback);

	void verifyUserCookies(AsyncCallback<UserInfo> callback);


}

package com.ctapweb.web.client.service;

import java.util.List;

import com.ctapweb.web.shared.AnalysisEngine;
import com.ctapweb.web.shared.ComplexityFeature;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface AdminServiceAsync {

	void initDB(String userCookieValue, AsyncCallback<Void> callback);

	void getAECount(AsyncCallback<Long> callback);

	void getAEList(int offset, int limit, AsyncCallback<List<AnalysisEngine>> callback);

	void updateAE(String userCookieValue, AnalysisEngine ae, AsyncCallback<Void> callback);

	void deleteAE(AnalysisEngine ae, AsyncCallback<Void> callback);

	void getCFCount(String userCookieValue, AsyncCallback<Long> callback);

	void getCFList(String userCookieValue, int offset, int limit, AsyncCallback<List<ComplexityFeature>> callback);

	void updateCF(String userCookieValue, ComplexityFeature cf, AsyncCallback<Void> callback);

	void deleteCF(String userCookieValue, ComplexityFeature cf, AsyncCallback<Void> callback);

	void addCF(String userCookieValue, ComplexityFeature cf, AsyncCallback<Void> callback);

	void isUserAdmin(AsyncCallback<Boolean> callback);

	void importAE(AsyncCallback<Void> callback);

//	void getAllAEList(String userCookieValue, AsyncCallback<List<AnalysisEngine>> callback);

}

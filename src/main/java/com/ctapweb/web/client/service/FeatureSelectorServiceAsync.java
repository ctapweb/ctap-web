package com.ctapweb.web.client.service;

import java.util.List;

import com.ctapweb.web.shared.AnalysisEngine;
import com.ctapweb.web.shared.FeatureSet;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface FeatureSelectorServiceAsync {

	void getFeatureSetCount(AsyncCallback<Integer> callback);

	void getFeatureSetList(int offset, int limit, AsyncCallback<List<FeatureSet>> callback);

	void addFeatureSet(FeatureSet featureSet, AsyncCallback<Void> callback);

	void deleteFeatureSet(FeatureSet featureSet, AsyncCallback<Void> callback);

	void updateFeatureSet(FeatureSet featureSet, AsyncCallback<Void> callback);

	void addToFeatureSet(long featureID, long featureSetID, AsyncCallback<Void> callback);

	void getFeatureSetName(long featureSetID, AsyncCallback<String> callback);

	void getFeatureList(int offset, int limit, AsyncCallback<List<AnalysisEngine>> callback);

	void getFeatureList(long featureSetID, int offset, int limit, AsyncCallback<List<AnalysisEngine>> callback);

	void getFeatureCount(AsyncCallback<Integer> callback);

	void getFeatureCount(long featureSetID, AsyncCallback<Integer> callback);

	void removeFeatureFromFS(long featureSetID, long featureID, AsyncCallback<Void> callback);


	void getFeatureCount(String keyword, AsyncCallback<Integer> callback);

	void getFeatureList(String keyword, int offset, int limit, AsyncCallback<List<AnalysisEngine>> callback);

	void getFeatureCount(long featureSetID, String keyword, AsyncCallback<Integer> callback);

	void getFeatureList(long featureSetID, String keyword, int offset, int limit,
			AsyncCallback<List<AnalysisEngine>> callback);


}

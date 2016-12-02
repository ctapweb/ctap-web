package com.ctapweb.web.client.service;

import java.util.List;

import com.ctapweb.web.shared.Analysis;
import com.ctapweb.web.shared.AnalysisStatus;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface AnalysisGeneratorServiceAsync {

	void addAnalysis(Analysis newAnalysis, AsyncCallback<Void> callback);

	void deleteAnalysis(long analysisID, AsyncCallback<Void> callback);

	void getAnalysisCount(AsyncCallback<Integer> callback);

	void getAnalysisList(int offset, int limit, AsyncCallback<List<Analysis>> callback);

	void updateAnalysis(Analysis analysis, AsyncCallback<Void> callback);

	void runAnalysis(Analysis analysis, AsyncCallback<Void> callback);

	void getAnalysisName(long analysisID, AsyncCallback<String> callback);

	void getAnalysisStatus(Analysis analysis, AsyncCallback<AnalysisStatus> callback);

	void updateAnalysisStatus(AnalysisStatus analysisStatus, AsyncCallback<Void> callback);

}

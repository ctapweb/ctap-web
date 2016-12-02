package com.ctapweb.web.client.service;

import java.util.List;

import com.ctapweb.web.shared.Analysis;
import com.ctapweb.web.shared.AnalysisEngine;
import com.ctapweb.web.shared.PlotData;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ResultVisualizerServiceAsync {

	void getResultFeatureList(long analysisID, AsyncCallback<List<AnalysisEngine>> callback);

	void getAnalysisList(AsyncCallback<List<Analysis>> callback);

	void getPlotData(long analysisID, long featureID, String statistics, AsyncCallback<List<PlotData>> callback);


}

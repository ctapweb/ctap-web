package com.ctapweb.web.client.component;

import java.util.List;
import java.util.logging.Logger;

import org.moxieapps.gwt.highcharts.client.AxisTitle;
import org.moxieapps.gwt.highcharts.client.BaseChart;
import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.Legend;
import org.moxieapps.gwt.highcharts.client.Legend.Align;
import org.moxieapps.gwt.highcharts.client.Series;
import org.moxieapps.gwt.highcharts.client.Series.Type;
import org.moxieapps.gwt.highcharts.client.Style;
import org.moxieapps.gwt.highcharts.client.ToolTip;
import org.moxieapps.gwt.highcharts.client.ToolTipData;
import org.moxieapps.gwt.highcharts.client.ToolTipFormatter;
import org.moxieapps.gwt.highcharts.client.labels.AxisLabelsData;
import org.moxieapps.gwt.highcharts.client.labels.AxisLabelsFormatter;
import org.moxieapps.gwt.highcharts.client.labels.YAxisLabels;
import org.moxieapps.gwt.highcharts.client.plotOptions.ColumnPlotOptions;

import com.ctapweb.web.client.service.Services;
import com.ctapweb.web.shared.Analysis;
import com.ctapweb.web.shared.AnalysisEngine;
import com.ctapweb.web.shared.AnalysisStatus;
import com.ctapweb.web.shared.PlotData;
import com.ctapweb.web.shared.Utils;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class ResultVisualizer extends Composite {

	private static ResultVisualizerUiBinder uiBinder = GWT.create(ResultVisualizerUiBinder.class);

	interface ResultVisualizerUiBinder extends UiBinder<Widget, ResultVisualizer> {
	}

	@UiField HTMLPanel chartPanel;
	@UiField ListBox analysisListBox;
	@UiField Button downloadResults;
	@UiField ListBox featureList1;
	@UiField ListBox plotTypeList1;
	@UiField ListBox featureList2;
	@UiField ListBox plotTypeList2;
	@UiField ListBox featureList3;
	@UiField ListBox plotTypeList3;
	@UiField ListBox statisticsList1;
	@UiField ListBox statisticsList2;
	@UiField ListBox statisticsList3;
	@UiField TextBox plotTitle;
	@UiField TextBox plotSubtitle;
	@UiField ListBox legendList;
	@UiField TextBox legendPosX;
	@UiField TextBox legendPosY;
	@UiField Button generateNewPlot;

	Logger logger = Logger.getLogger(CorpusManager.class.getName());

	// for storing analysis info
	private List<Analysis> resultAnalysisList; 
	private List<AnalysisEngine> resultFeatureList;
	//	private Analysis selectedAnalysis;
	private long selectedAnalysisID;

	// final Chart chart = new Chart();
	//	Chart chart;

	public ResultVisualizer() {
		logger.finer("Opening Result Visualizer page...");
		initWidget(uiBinder.createAndBindUi(this));

		logger.finer("Initializing form controls...");
		initFormControls();

	}

	private Type getPlotType(ListBox plotTypeList) {
		Type plotType;
		switch(plotTypeList.getSelectedValue()) {
		case "COLUMNS": 
			plotType = Series.Type.COLUMN; break;
		case "LINE":
			plotType = Series.Type.LINE; break;
		case "SPLINE": 
			plotType = Series.Type.SPLINE; break;
		case "POINTS":
		default:
			plotType = Series.Type.SCATTER; break;
		}
		return plotType;
	}
	
	private Legend createLegend() {
		boolean legendShow;
		int x;
		int y; 
		if(legendList.getSelectedValue().equals("SHOW")) {
			legendShow = true;
		} else {
			legendShow = false;
		}
		
		x = Integer.parseInt(legendPosX.getText());
		y = Integer.parseInt(legendPosY.getText());
		
		Legend legend = new Legend()  
		.setLayout(Legend.Layout.VERTICAL)  
		.setAlign(Legend.Align.RIGHT)  
		.setVerticalAlign(Legend.VerticalAlign.TOP)  
		.setX(x)  
		.setY(y)  
		.setFloating(true)  
		.setBorderWidth(1)  
		.setBackgroundColor("#FFFFFF")  
		.setShadow(true)
		.setVerticalAlign(org.moxieapps.gwt.highcharts.client.Legend.VerticalAlign.TOP)
		.setAlign(Align.RIGHT)
		.setEnabled(legendShow);
		
		return legend;
	}
	@UiHandler("generateNewPlot")
	void onGenerateNewPlotClick(ClickEvent e) {
		//creates a chart object
		final long featureID1 = Long.parseLong(featureList1.getSelectedValue());
		final long featureID2 = Long.parseLong(featureList2.getSelectedValue());
		final long featureID3 = Long.parseLong(featureList3.getSelectedValue());
		final String featureName1 = featureList1.getSelectedItemText();
		final String featureName2 = featureList2.getSelectedItemText();
		final String featureName3 = featureList3.getSelectedItemText();
		final Type plotType1 = getPlotType(plotTypeList1);
		final Type plotType2 = getPlotType(plotTypeList2);
		final Type plotType3 = getPlotType(plotTypeList3);
		final String statisticsName1 = statisticsList1.getSelectedItemText();
		final String statisticsName2 = statisticsList2.getSelectedItemText();
		final String statisticsName3 = statisticsList3.getSelectedItemText();
		final String statisticsFunction1 = statisticsList1.getSelectedValue();
		final String statisticsFunction2 = statisticsList2.getSelectedValue();
		final String statisticsFunction3 = statisticsList3.getSelectedValue();

		final String title = plotTitle.getText();
		final String subtitle = plotSubtitle.getText();
		final long analysisID = Long.parseLong(analysisListBox.getSelectedValue());


		final Chart chart = new Chart()
				.setChartTitleText(title)
				.setChartSubtitleText(subtitle)
				.setZoomType(BaseChart.ZoomType.X_AND_Y)
				.setLegend(createLegend())  
				.setToolTip(new ToolTip().setFormatter(new ToolTipFormatter() {
					@Override
					public String format(ToolTipData toolTipData) {
						return toolTipData.getSeriesName() + ": " + 
								toolTipData.getYAsDouble(); 
					}
				}));

		//plot the first feature
		Services.getResultVisualizerService().getPlotData(analysisID, featureID1, 
				statisticsFunction1, new AsyncCallback<List<PlotData>>() {

			@Override
			public void onFailure(Throwable caught) {
				Utils.showErrorPage(caught.getMessage());
			}

			@Override
			public void onSuccess(List<PlotData> plotDataList) {
				//set X axis categories
				String [] categories = getPlotDataCategories(plotDataList);
				chart.getXAxis().setCategories(categories);

				//add the first Y axis
				addYAxis(chart, plotDataList, plotType1, 0, statisticsName1 + " of " + featureName1, 
						"#89A54E", false);

				if(featureID2 == 0 ) {
					// adds the chart object to panel
					refreshChartPanel(chart);
					return;
				}

				///////////////////////////// second y axis
				Services.getResultVisualizerService().getPlotData(analysisID, featureID2,
						statisticsFunction2, new AsyncCallback<List<PlotData>>() {
					@Override
					public void onFailure(Throwable caught) {
						Utils.showErrorPage(caught.getMessage());
					}
					@Override
					public void onSuccess(List<PlotData> result) {
						//add the second Y axis
						addYAxis(chart, result, plotType2, 1, statisticsName2 + " of " + featureName2, 
								"#4572A7", true);

						if(featureID3 == 0 ) {
							// adds the chart object to panel
							refreshChartPanel(chart);
							return;
						}

						////////////////////////// third y axis
						Services.getResultVisualizerService().getPlotData(analysisID, featureID3, statisticsFunction3,
								new AsyncCallback<List<PlotData>>() {
							@Override
							public void onFailure(Throwable caught) {
								Utils.showErrorPage(caught.getMessage());
							}

							@Override
							public void onSuccess(List<PlotData> result) {
								//add the third Y axis
								addYAxis(chart, result, plotType3, 2, statisticsName3 + " of " + featureName3, 
										"#AA4643", true);
								refreshChartPanel(chart);
							}

						});

					}
				} );
				////////////////////////////
			}
		})	;
	}

	private void addYAxis(Chart chart, List<PlotData> plotDataList, Type plotType, int axisNumber, String axisName, String color, boolean opposite) {
		Number [] numbers = getPlotDataNumbers(plotDataList);
		chart.getYAxis(axisNumber)  
		.setLabels(new YAxisLabels()  
				.setStyle(new Style()  
						.setColor(color)  
						)  
				.setFormatter(new AxisLabelsFormatter() {  
					@Override
					public String format(AxisLabelsData axisLabelsData) {  
						return axisLabelsData.getValueAsDouble() + "";  
					}  
				})  
				)
		.setAxisTitle(new AxisTitle()  
				.setText(axisName)  
				.setStyle(new Style()  
						.setColor(color)  
						)  
				)
		.setOpposite(opposite);

		chart.addSeries(chart.createSeries()  
				.setName(axisName)  
				.setType(plotType)  
				.setYAxis(axisNumber)
				.setPlotOptions(new ColumnPlotOptions()  
						.setColor(color)  
						)  
				.setPoints(numbers)  
				); 
	}

	private void refreshChartPanel(Chart chart) {
		chartPanel.clear();
		chartPanel.add(chart);
	}



	private String [] getPlotDataCategories(List<PlotData> plotDataList) {
		int size = plotDataList.size();
		String [] categories = new String[size];

		for(int i =0; i < size; i++) {
			categories[i] = plotDataList.get(i).getCategoryName();
		}

		return categories;
	}

	private Number [] getPlotDataNumbers(List<PlotData> plotDataList) {
		int size = plotDataList.size();
		Number [] numbers = new Number[size];

		for(int i =0; i < size; i++) {
			numbers[i] = plotDataList.get(i).getValue();
		}

		return numbers;
	}

	private void initFormControls() {
		// clear form control
		analysisListBox.clear();
		analysisListBox.addItem("Select an anlysis... ", "0");

		// get result analysis list 
		Services.getResultVisualizerService().getAnalysisList(new AsyncCallback<List<Analysis>>() {

			@Override
			public void onFailure(Throwable caught) {
				Utils.showErrorPage(caught.getMessage());
			}

			@Override
			public void onSuccess(List<Analysis> result) {
				resultAnalysisList = result;
				for(Analysis analysis: result) {
					analysisListBox.addItem(analysis.getName(), analysis.getId() + "");
				}
			}
		});
	}

	@UiHandler("featureList1")
	void onFeatureList1SelectionChange(ChangeEvent e) {
		if (featureList1.getSelectedValue() != "0") {
			generateNewPlot.setEnabled(true);
		} else {
			generateNewPlot.setEnabled(false);
		}

	}

	@UiHandler("analysisListBox")
	void onAnalysisListSelectionChange(ChangeEvent e) {
		this.selectedAnalysisID = Long.parseLong(analysisListBox.getSelectedValue());

		if (selectedAnalysisID == 0) {
			disableControls();
			return;
		}

		//get result feature list
		logger.finer("Requesting serivce getResultFeatureList...");
		Services.getResultVisualizerService().getResultFeatureList(selectedAnalysisID, 
				new AsyncCallback<List<AnalysisEngine>>() {

			@Override
			public void onFailure(Throwable caught) {
				Utils.showErrorPage(caught.getMessage());
			}

			@Override
			public void onSuccess(List<AnalysisEngine> result) {
				resultFeatureList = result;
				enableControls();
				refreshFeatureList(result);
			}
		});
	}

	private void refreshFeatureList(List<AnalysisEngine> features) {
		featureList1.clear();
		featureList2.clear();
		featureList3.clear();
		featureList1.addItem("Select a feature...", "0");
		featureList2.addItem("Select a feature...", "0");
		featureList3.addItem("Select a feature...", "0");

		for(AnalysisEngine feature: features) {
			String name = feature.getName();
			String id = feature.getId()  + "";
			featureList1.addItem(name, id);
			featureList2.addItem(name, id);
			featureList3.addItem(name, id);
		}
	}

	private void disableControls() {
		//		manageGroups.setEnabled(false);
		downloadResults.setEnabled(false);
		plotTypeList1.setEnabled(false);
		plotTypeList1.setSelectedIndex(0);
		plotTypeList2.setEnabled(false);
		plotTypeList2.setSelectedIndex(0);
		plotTypeList3.setEnabled(false);
		plotTypeList3.setSelectedIndex(0);
		featureList1.setEnabled(false);
		featureList1.clear();
		featureList2.setEnabled(false);
		featureList2.clear();
		featureList3.setEnabled(false);
		featureList3.clear();
		statisticsList1.setEnabled(false);
		statisticsList1.setSelectedIndex(0);
		statisticsList2.setEnabled(false);
		statisticsList2.setSelectedIndex(0);
		statisticsList3.setEnabled(false);
		statisticsList3.setSelectedIndex(0);
		plotTitle.setEnabled(false);
		plotSubtitle.setEnabled(false);
		generateNewPlot.setEnabled(false);
		legendList.setEnabled(false);
		legendPosX.setEnabled(false);
		legendPosY.setEnabled(false);
	}

	private void enableControls() {
		//		manageGroups.setEnabled(true);
		downloadResults.setEnabled(true);
		plotTypeList1.setEnabled(true);
		plotTypeList2.setEnabled(true);
		plotTypeList3.setEnabled(true);
		featureList1.setEnabled(true);
		featureList2.setEnabled(true);
		featureList3.setEnabled(true);
		statisticsList1.setEnabled(true);
		statisticsList2.setEnabled(true);
		statisticsList3.setEnabled(true);
		plotTitle.setEnabled(true);
		plotSubtitle.setEnabled(true);
		legendList.setEnabled(true);
		legendPosX.setEnabled(true);
		legendPosY.setEnabled(true);
	}

	@UiHandler("downloadResults")
	void onDownloadResultsClick(ClickEvent e) {
		final Analysis analysis = new Analysis();
		analysis.setId(selectedAnalysisID);

		Services.getAnalysisGeneratorService().getAnalysisStatus(analysis,
				new AsyncCallback<AnalysisStatus>() {

			@Override
			public void onFailure(Throwable caught) {
				logger.severe("Caught service exception: " + caught);
				Utils.showErrorPage(caught.getMessage());
			}

			@Override
			public void onSuccess(AnalysisStatus analysisStatus) {
				logger.finer("Service getAnalysisStatus returned successfully...");
				if (!analysisStatus.getStatus().equals(AnalysisStatus.Status.FINISHED)
						|| analysisStatus.getProgress() != 1.0) {
					Window.alert("Analysis has not been finished!" + " id: "
							+ analysisStatus.getAnalysisID() + " progress: "
							+ analysisStatus.getProgress() + " status: " + analysisStatus.getStatus()
							+ ". Results are downloadable only when the analysis is finished successfully.");
				} else {
					// export results for download
					String url = GWT.getModuleBaseURL() + 
							"exportResultsServlet?analysisID=" + analysis.getId() +
							"&tableType=wide";
					Window.open(url, "_self", "status=0, toolbar=0, menubar=0, location=0");
				}
			}
		});
	}

}

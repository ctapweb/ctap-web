package com.ctapweb.web.client.component;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import com.ctapweb.web.client.service.Services;
import com.ctapweb.web.shared.Analysis;
import com.ctapweb.web.shared.AnalysisStatus;
import com.ctapweb.web.shared.Corpus;
import com.ctapweb.web.shared.FeatureSet;
import com.ctapweb.web.shared.Utils;
import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.Range;
import com.google.gwt.widgetideas.client.ProgressBar;

public class AnalysisGenerator extends Composite {

	private static AnalysisGeneratorUiBinder uiBinder = GWT.create(AnalysisGeneratorUiBinder.class);

	interface AnalysisGeneratorUiBinder extends UiBinder<Widget, AnalysisGenerator> {
	}

	// for the icon button in the cell table
	public interface IconButtonTemplate extends SafeHtmlTemplates {
		// @Template("<span class=\"{3}\">{0}: <a href=\"{1}\">{2}</a></span>")
		@Template("<em class=\"fa {0}\" ></em>")
		SafeHtml iconButtonInCellTable(String icon);

		// for buttons with icon labels
		@Template("<span class=\"btn-label\"><i class=\"fa {0}\"></i></span>{1}")
		SafeHtml labeledButton(String label, String text);

	}

	private static final IconButtonTemplate ICONBUTTONTEMPLATE = GWT.create(IconButtonTemplate.class);

	@UiField Button newAnalysis;
	@UiField Anchor selectAll;
	@UiField Anchor selectClear;
	@UiField Anchor selectReverse;
	@UiField Button deleteSelected;

	@UiField HTMLPanel progressBarPanel;

	@UiField ListBox nRecords;
	@UiField ListBox resultTableType;
	@UiField HTMLPanel analysisListPanel;

	@UiField HTMLPanel analysisDetailPanel;
	@UiField TextBox createdOn;
	@UiField TextBox analysisName;
	@UiField TextArea analysisDescription;
	@UiField ListBox tagFilterLogicListBox;
	@UiField TextBox tagFilterKeyword;
	@UiField Hidden analysisIdHidden;
	@UiField Hidden corpusIdHidden;
	@UiField Hidden featureSetIdHidden;
	@UiField ListBox selectCorpus;
	@UiField ListBox selectFeatureSet;
	@UiField Button saveDetail;
	@UiField Button closeDetail;

	@UiField HTMLPanel feedbackPanel;
	@UiField InlineLabel feedbackLabel;
	@UiField Button closeFeedbackPanel;

	Logger logger = Logger.getLogger(AnalysisGenerator.class.getName());

	// selection model
	static final MultiSelectionModel<Analysis> analysisListSelectionModel = new MultiSelectionModel<>(
			Analysis.KEY_PROVIDER);

	//
	AsyncDataProvider<Analysis> analysisDataProvider;

	final CellTable<Analysis> analysisList = new CellTable<Analysis>(Analysis.KEY_PROVIDER);
	SimplePager pager;

	public AnalysisGenerator() {
		initWidget(uiBinder.createAndBindUi(this));
		// inialize some widgets
		newAnalysis.setHTML(ICONBUTTONTEMPLATE.labeledButton("fa-file", "New Analysis"));
		deleteSelected.setHTML(ICONBUTTONTEMPLATE.labeledButton("fa-trash", "Delete Selected"));
		selectAll.getElement().getStyle().setCursor(Cursor.POINTER);
		selectClear.getElement().getStyle().setCursor(Cursor.POINTER);
		selectReverse.getElement().getStyle().setCursor(Cursor.POINTER);

		// create a cell table
		analysisList.setWidth("100%");

		// set the selection model, this multiselection model selects a record
		// whenever a checkbox in the row is selected
		analysisList.setSelectionModel(analysisListSelectionModel,
				DefaultSelectionEventManager.<Analysis> createCheckboxManager());

		// initialize columns to the cell table
		initializeColumns(analysisList);

		// RPC request to get corpus count
		Services.getAnalysisGeneratorService().getAnalysisCount(new AsyncCallback<Integer>() {
			@Override
			public void onFailure(Throwable caught) {
				// serious error, show error page
				logger.warning("Can't get analysis count, user not logged in or database error.");
				Utils.showErrorPage(caught.getMessage());
			}

			@Override
			public void onSuccess(Integer result) {
				analysisList.setRowCount(result, true);
			}
		});

		// list 10 items per page
		analysisList.setVisibleRange(0, 10);

		// data provider
		// AsyncDataProvider<Corpus> corpusDataProvider = new
		// AsyncDataProvider<Corpus>() {
		analysisDataProvider = new AsyncDataProvider<Analysis>() {
			@Override
			protected void onRangeChanged(HasData<Analysis> display) {
				final Range range = display.getVisibleRange();

				final int rangeStart = range.getStart();
				final int rangeLength = range.getLength();

				// an RPC request to get the analysis data
				Services.getAnalysisGeneratorService().getAnalysisList(rangeStart, rangeLength,
						new AsyncCallback<List<Analysis>>() {

					@Override
					public void onSuccess(List<Analysis> result) {
						updateRowData(rangeStart, result);
					}

					@Override
					public void onFailure(Throwable caught) {
						Utils.showErrorPage(caught.getMessage());
					}
				});

			}
		};

		// connect the list to the data provider
		analysisDataProvider.addDataDisplay(analysisList);

		// Create paging controls.
		pager = new SimplePager();
		pager.setDisplay(analysisList);

		analysisListPanel.add(analysisList);
		analysisListPanel.add(pager);
	}

	private ProgressBar addProgressBar(final Analysis analysis) {
		// check if progressbar already exists
		boolean isExist = false;
		final ProgressBar progressBar;
		int i;
		for (i = 0; i < progressBarPanel.getWidgetCount(); i++) {
			if (progressBarPanel.getWidget(i).getTitle().equals(analysis.getName())) {
				isExist = true;
				break;
			}
		}

		if (isExist) {
			progressBar = (ProgressBar) progressBarPanel.getWidget(i);
		} else {
			// add new progress bar
			progressBar = new ProgressBar(0.0, 1.0, 0.0, new ProgressBar.TextFormatter() {
				@Override
				protected String getText(ProgressBar bar, double curProgress) {
					return (analysis.getName() + ": " + (int) (100 * bar.getPercent())) + "%";
				}
			});
			progressBar.setTitle(analysis.getName()); // title of the progress bar is used to identify it
			progressBar.setHeight("25px");
			progressBar.setWidth("100%");
			progressBarPanel.add(progressBar);
		}
		
		return progressBar;
	}

	private void runAnalysis(final Analysis analysis) {
		//add progress bar or reuse existing progress bar
		final ProgressBar progressBar = addProgressBar(analysis);

		// a timer to check running status regularly
		final Timer timer = new Timer() {
			@Override
			public void run() {
				logger.finer("Requesting service getAnalysisStatus from within timer...");
				Services.getAnalysisGeneratorService().getAnalysisStatus(analysis,
						new AsyncCallback<AnalysisStatus>() {

					@Override
					public void onFailure(Throwable caught) {
						logger.severe("Caught service exception " + caught);
						Utils.showErrorPage(caught.getMessage());
					}

					@Override
					public void onSuccess(AnalysisStatus analysisStatus) {
						logger.finer("Service get AnalysisStatus from within timer returned successfully.");
						String status = analysisStatus.getStatus();
						progressBar.setProgress(analysisStatus.getProgress());

						if (status == null || !status.equals(AnalysisStatus.Status.RUNNING)) {
							cancel();
						}
					}
				});
			}
		};

		// check if analysis is already running
		logger.finer("Requesting service getAnalysisStatus...");
		Services.getAnalysisGeneratorService().getAnalysisStatus(analysis,
				new AsyncCallback<AnalysisStatus>() {
			@Override
			public void onFailure(Throwable caught) {
				logger.severe("Caught service exception " + caught);
				Utils.showErrorPage(caught.getMessage());
			}

			@Override
			public void onSuccess(AnalysisStatus analysisStatus) {
				logger.finer("Service getAnalysisStatus returned successfully.");
				String status = analysisStatus.getStatus();
				
				//not running:
				// analysis is stopped or analysis hasn't been initiated, initiate a new analysis
				if (status == null || status.equals(AnalysisStatus.Status.STOPPED)
						|| status.equals(AnalysisStatus.Status.FINISHED)) {
					logger.info("Analysis has no status, stopped, or finished. Initiating the analysis...");
					logger.finer("Requesting service runAnalysis...");
					

					Services.getAnalysisGeneratorService().runAnalysis(analysis, new AsyncCallback<Void>() {
						@Override
						public void onFailure(Throwable caught) {
							logger.severe("Caught service exception " + caught);
							Utils.showErrorPage(caught.getMessage());
						}

						@Override
						public void onSuccess(Void result) {
							logger.finer("Service runAnalysis returned successfully. "
									+ "Run analysis initiated. Setting timer to check running status regularly...");
							progressBar.setProgress(0);
							timer.scheduleRepeating(1000);
						}
					});
				} else if (status.equals(AnalysisStatus.Status.RUNNING)) {
					showFeedbackPanel("alert-warning", "The analysis is already running!");
				} else if (status.equals(AnalysisStatus.Status.PAUSED)) {
					// the analysis has been paused, try to resume it.
					analysisStatus.setStatus(AnalysisStatus.Status.RUNNING);

					Services.getAnalysisGeneratorService().updateAnalysisStatus(
							analysisStatus, new AsyncCallback<Void>() {

								@Override
								public void onFailure(Throwable caught) {
									Utils.showErrorPage(caught.getMessage());
								}

								@Override
								public void onSuccess(Void result) {
									timer.scheduleRepeating(1000);
									showFeedbackPanel("alert-success",
											"The analysis has been resumed.");
								}
							});
				}
			}
		});
	}

	private void initializeColumns(CellTable<Analysis> analysisList) {
		// the checkbox column
		Column<Analysis, Boolean> checkColumn = new Column<Analysis, Boolean>(new CheckboxCell(true, false)) {
			@Override
			public Boolean getValue(Analysis object) {
				// Get the value from the selection model.
				return analysisListSelectionModel.isSelected(object);
			}
		};
		analysisList.addColumn(checkColumn, "");
		analysisList.setColumnWidth(checkColumn, "3%");

		// the id column
		TextColumn<Analysis> idColumn = new TextColumn<Analysis>() {
			@Override
			public String getValue(Analysis analysis) {
				return analysis.getId() + "";
			}
		};
		analysisList.addColumn(idColumn, "ID");
		analysisList.setColumnWidth(idColumn, "3%");

		// the name column
		TextColumn<Analysis> nameColumn = new TextColumn<Analysis>() {
			@Override
			public String getValue(Analysis analysis) {
				return analysis.getName();
			}
		};
		analysisList.addColumn(nameColumn, "Analysis Name");

		// the corpus column
		TextColumn<Analysis> corpusColumn = new TextColumn<Analysis>() {
			@Override
			public String getValue(Analysis analysis) {
				return analysis.getCorpusName();
			}
		};
		analysisList.addColumn(corpusColumn, "Corpus");

		// the feature set column
		TextColumn<Analysis> featureSetColumn = new TextColumn<Analysis>() {
			@Override
			public String getValue(Analysis analysis) {
				return analysis.getFeatureSetName();
			}
		};
		analysisList.addColumn(featureSetColumn, "Feature Set");

		// the created date column
		TextColumn<Analysis> createdColumn = new TextColumn<Analysis>() {
			@Override
			public String getValue(Analysis analysis) {
				return DateTimeFormat.getFormat("dd/MM/yy").format(analysis.getCreateDate());
			}
		};
		analysisList.addColumn(createdColumn, "Created On");
		analysisList.setColumnWidth(createdColumn, "6%");

		// the run button column
		SafeHtml runIcon = ICONBUTTONTEMPLATE.iconButtonInCellTable("fa-play");
		ActionCell<Analysis> runCell = new ActionCell<>(runIcon, new ActionCell.Delegate<Analysis>() {
			@Override
			public void execute(final Analysis analysis) {
				logger.finer("User clicked run analysis button.");
				hideFeedbackPanel();
				runAnalysis(analysis);
			}
		});
		Column<Analysis, Analysis> runColumn = new Column<Analysis, Analysis>(runCell) {
			@Override
			public Analysis getValue(Analysis analysis) {
				return analysis;
			}
		};
		analysisList.addColumn(runColumn, "Run");
		analysisList.setColumnWidth(runColumn, "3%");

		// the pause button column
		SafeHtml pauseIcon = ICONBUTTONTEMPLATE.iconButtonInCellTable("fa-pause");
		ActionCell<Analysis> pauseCell = new ActionCell<>(pauseIcon, new ActionCell.Delegate<Analysis>() {
			@Override
			public void execute(final Analysis analysis) {
				logger.finer("User clicked pause analysis for analysis " + analysis.getId() + ".");
				hideFeedbackPanel();
				pauseAnalysis(analysis);
			}
		});
		Column<Analysis, Analysis> pauseColumn = new Column<Analysis, Analysis>(pauseCell) {

			@Override
			public Analysis getValue(Analysis analysis) {
				return analysis;
			}
		};
		analysisList.addColumn(pauseColumn, "Pause");
		analysisList.setColumnWidth(pauseColumn, "3%");

		// the stop button column
		SafeHtml stopIcon = ICONBUTTONTEMPLATE.iconButtonInCellTable("fa-stop");
		ActionCell<Analysis> stopCell = new ActionCell<>(stopIcon, new ActionCell.Delegate<Analysis>() {
			@Override
			public void execute(final Analysis analysis) {
				hideFeedbackPanel();
				stopAnalysis(analysis);
			}
		});
		Column<Analysis, Analysis> stopColumn = new Column<Analysis, Analysis>(stopCell) {

			@Override
			public Analysis getValue(Analysis analysis) {
				return analysis;
			}
		};
		analysisList.addColumn(stopColumn, "Stop");
		analysisList.setColumnWidth(stopColumn, "3%");

		// the export button column
		SafeHtml exportIcon = ICONBUTTONTEMPLATE.iconButtonInCellTable("fa-share-square-o");
		ActionCell<Analysis> exportCell = new ActionCell<>(exportIcon, new ActionCell.Delegate<Analysis>() {
			@Override
			public void execute(final Analysis analysis) {
				logger.finer("User clicked results button. Exporting results for analysis " + analysis.getId() + "...");
				hideFeedbackPanel();
				exportResults(analysis);
			}
		});
		Column<Analysis, Analysis> exportColumn = new Column<Analysis, Analysis>(exportCell) {
			@Override
			public Analysis getValue(Analysis analysis) {
				return analysis;
			}
		};
		analysisList.addColumn(exportColumn, "Results");
		analysisList.setColumnWidth(exportColumn, "3%");

		// the edit button column
		SafeHtml editIcon = ICONBUTTONTEMPLATE.iconButtonInCellTable("fa-pencil-square-o");
		ActionCell<Analysis> editCell = new ActionCell<>(editIcon, new ActionCell.Delegate<Analysis>() {
			@Override
			public void execute(Analysis analysis) {
				showDetailPanel(analysis);
			}
		});
		Column<Analysis, Analysis> editColumn = new Column<Analysis, Analysis>(editCell) {
			@Override
			public Analysis getValue(Analysis analysis) {
				return analysis;
			}
		};
		analysisList.addColumn(editColumn, "Edit");
		analysisList.setColumnWidth(editColumn, "3%");

		// the delete button column
		SafeHtml deleteIcon = ICONBUTTONTEMPLATE.iconButtonInCellTable("fa-trash-o");
		ActionCell<Analysis> deleteCell = new ActionCell<>(deleteIcon, new ActionCell.Delegate<Analysis>() {
			@Override
			public void execute(Analysis analysis) {
				if (!Window.confirm("Do you really want to delete " + analysis.getName())) {
					return;
				}
				logger.finer("User clicked delete analysis button.");
				deleteAnalysis(analysis);
			}
		});
		Column<Analysis, Analysis> deleteColumn = new Column<Analysis, Analysis>(deleteCell) {

			@Override
			public Analysis getValue(Analysis analysis) {
				return analysis;
			}
		};
		analysisList.addColumn(deleteColumn, "Delete");
		analysisList.setColumnWidth(deleteColumn, "3%");
	}

	private void exportResults(final Analysis analysis) {
		// check analysis status
		logger.finer("Requesting service getAnalysisStatus...");
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
					showFeedbackPanel("alert-warning", "Analysis has not been finished!" + " id: "
							+ analysisStatus.getAnalysisID() + " progress: "
							+ analysisStatus.getProgress() + " status: " + analysisStatus.getStatus()
							+ ". Results are downloadable only when the analysis is finished successfully.");
				} else {
					// export results for download
					String url = GWT.getModuleBaseURL() + 
							"exportResultsServlet?analysisID=" + analysis.getId() +
							"&tableType=" + resultTableType.getSelectedValue();
					Window.open(url, "_self", "status=0, toolbar=0, menubar=0, location=0");
				}
			}
		});
	}

	private void stopAnalysis(final Analysis analysis) {
		// check if analysis is running
		Services.getAnalysisGeneratorService().getAnalysisStatus(analysis,
				new AsyncCallback<AnalysisStatus>() {

			@Override
			public void onFailure(Throwable caught) {
				Utils.showErrorPage(caught.getMessage());
			}

			@Override
			public void onSuccess(AnalysisStatus analysisStatus) {
				String status = analysisStatus.getStatus();
				if (status.equals(AnalysisStatus.Status.RUNNING)|| 
						status.equals(AnalysisStatus.Status.PAUSED)) {
					// analysis is running or paused, stop it
					analysisStatus.setStatus(AnalysisStatus.Status.STOPPED);

					Services.getAnalysisGeneratorService().updateAnalysisStatus(
							analysisStatus, new AsyncCallback<Void>() {

								@Override
								public void onFailure(Throwable caught) {
									Utils.showErrorPage(caught.getMessage());
								}

								@Override
								public void onSuccess(Void result) {
									// remove the progress bar
									// check if progressbar already exists
//									boolean isExist = false;
//									final ProgressBar progressBar;
									int i;
									for (i = 0; i < progressBarPanel.getWidgetCount(); i++) {
										if (progressBarPanel.getWidget(i).getTitle()
												.equals(analysis.getName())) {
											progressBarPanel.remove(i);
											break;
										}
									}
									showFeedbackPanel("alert-danger",
											"Stopped analysis " + analysis.getName() + ".");
								}
							});
				} else {
					showFeedbackPanel("alert-warning",
							"Analysis " + analysis.getName() + " is not running!");
				}
			}
		});
	}
	private void pauseAnalysis(final Analysis analysis) {
		// check if analysis is running or paused
		Services.getAnalysisGeneratorService().getAnalysisStatus(analysis,
				new AsyncCallback<AnalysisStatus>() {

			@Override
			public void onFailure(Throwable caught) {
				Utils.showErrorPage(caught.getMessage());
			}

			@Override
			public void onSuccess(AnalysisStatus analysisStatus) {
				String status = analysisStatus.getStatus();
				if (status.equals(AnalysisStatus.Status.RUNNING)) {
					// analysis is running, pause it
					analysisStatus.setStatus(AnalysisStatus.Status.PAUSED);

					Services.getAnalysisGeneratorService().updateAnalysisStatus(
							analysisStatus, new AsyncCallback<Void>() {

								@Override
								public void onFailure(Throwable caught) {
									Utils.showErrorPage(caught.getMessage());
								}

								@Override
								public void onSuccess(Void result) {
									showFeedbackPanel("alert-warning",
											"Paused analysis " + analysis.getName() + ".");
								}
							});
				} else {
					showFeedbackPanel("alert-warning",
							"Analysis " + analysis.getName() + " is not running!");
				}
			}
		});
	}
	private void showDetailPanel(Analysis analysis) {
		logger.finer("Showing details panel...");
		selectCorpus.clear();
		selectFeatureSet.clear();
		feedbackPanel.setVisible(false);

		createdOn.setText(analysis.getCreateDate().toString());
		analysisName.setText(analysis.getName());
		analysisDescription.setText(analysis.getDescription());
		setTagFilterLogicListBox(analysis.getTagFilterLogic());
		tagFilterKeyword.setText(analysis.getTagKeyword());
		analysisIdHidden.setValue(analysis.getId() + "");
		corpusIdHidden.setValue(analysis.getCorpusID() + "");
		featureSetIdHidden.setValue(analysis.getFeatureSetID() + "");

		// get lists
		getDetailPanelCorpusList();

		getDetailPanelFeatureSetList();

		analysisListPanel.setStyleName("col-lg-8");
		analysisDetailPanel.setVisible(true);
	}

	private void getDetailPanelFeatureSetList() {
		// get feature set list
		logger.finer("Requesting service getFeatureSetCount...");
		Services.getFeatureSelectorService().getFeatureSetCount(new AsyncCallback<Integer>() {
			@Override
			public void onFailure(Throwable caught) {
				logger.severe("Caught service exception " + caught);
				Utils.showErrorPage(caught.getMessage());
			}

			@Override
			public void onSuccess(Integer result) {
				logger.finer("Service getFeatureSetCount returned successfully. Requesting service getFeatureSetList...");
				Services.getFeatureSelectorService().getFeatureSetList(0, result,
						new AsyncCallback<List<FeatureSet>>() {

					@Override
					public void onFailure(Throwable caught) {
						logger.severe("Caught service exception " + caught);
						Utils.showErrorPage(caught.getMessage());
					}

					@Override
					public void onSuccess(List<FeatureSet> featureSetList) {
						logger.finer("Service getFeatureSetList returned successfully. Adding feature set list to list box..."); 
						// add feature set list to the list box
						for (FeatureSet featureSet : featureSetList) {
							selectFeatureSet.addItem(featureSet.getName(), featureSet.getId() + "");
						}
						for (int i = 0; i < selectFeatureSet.getItemCount(); i++) {
							if (Integer.parseInt(featureSetIdHidden.getValue()) == Integer
									.parseInt(selectFeatureSet.getValue(i))) {
								selectFeatureSet.setSelectedIndex(i);
								break;
							}
						}
					}
				});

			}
		});
	}

	private void getDetailPanelCorpusList() {
		logger.finer("Requesting service getCorpusCount...");
		Services.getCorpusManagerService().getCorpusCount(new AsyncCallback<Integer>() {
			@Override
			public void onFailure(Throwable caught) {
				logger.severe("Caught service exception " + caught);
				Utils.showErrorPage(caught.getMessage());
			}

			@Override
			public void onSuccess(Integer result) {
				logger.finer("Service getCorpusCount returned successfully. Requesting service getCorpusList...");
				Services.getCorpusManagerService().getCorpusList(0, result,
						new AsyncCallback<List<Corpus>>() {
					@Override
					public void onFailure(Throwable caught) {
						logger.severe("Caught service exception " + caught);
						Utils.showErrorPage(caught.getMessage());
					}

					@Override
					public void onSuccess(List<Corpus> corpusList) {
						logger.finer("Service getCorpusList returned successfully. Adding corpus list to corpus listbox...");
						// add corpus list to the list box
						for (Corpus corpus : corpusList) {
							selectCorpus.addItem(corpus.getName(), corpus.getId() + "");
						}
						//set corpus selected item
						for (int i = 0; i < selectCorpus.getItemCount(); i++) {
							if (Integer.parseInt(corpusIdHidden.getValue()) == Integer
									.parseInt(selectCorpus.getValue(i))) {
								selectCorpus.setSelectedIndex(i);
								break;
							}
						}
					}
				});
			}
		});
	}

	//Sets the filter logic list box's selected item
	private void setTagFilterLogicListBox(String tagFilterLogic) {
		for(int i = 0; i < tagFilterLogicListBox.getItemCount(); i++) {
			if(tagFilterLogic.equals(tagFilterLogicListBox.getValue(i))) {
				tagFilterLogicListBox.setSelectedIndex(i);
			}
		}
	}


	private void hideDetailPanel() {
		analysisDetailPanel.setVisible(false);
		analysisListPanel.setStyleName("col-lg-12");
	}

	@UiHandler("closeDetail")
	void onCloseDetailClick(ClickEvent e) {
		hideDetailPanel();
	}

	@UiHandler("saveDetail")
	void onSaveDetailClick(ClickEvent e) {
		logger.finer("User clicked save analysis button.");
		// check if analysis info is empty
		if (analysisName.getText().isEmpty() || selectCorpus.getSelectedIndex() == -1
				|| selectFeatureSet.getSelectedIndex() == -1) {
			showFeedbackPanel("alert-danger",
					"You need to enter a name and select a corpus and a feature set for your analysis.");
			return;
		}

		//construct the analysis object
		Analysis analysis = new Analysis();
		analysis.setId(Integer.parseInt(analysisIdHidden.getValue()));
		analysis.setName(analysisName.getText());
		analysis.setDescription(analysisDescription.getText());
		analysis.setTagKeyword(tagFilterKeyword.getText());
		analysis.setTagFilterLogic(tagFilterLogicListBox.getSelectedValue());
		analysis.setCorpusID(Integer.parseInt(selectCorpus.getSelectedValue()));
		analysis.setFeatureSetID(Integer.parseInt(selectFeatureSet.getSelectedValue()));

		if (analysis.getId() == -1) {
			// new analysis
			logger.finer("Requesting service addAnalysis...");
			Services.getAnalysisGeneratorService().addAnalysis(analysis, new AsyncCallback<Void>() {
				@Override
				public void onFailure(Throwable caught) {
					logger.severe("Caught service exception " + caught);
					Utils.showErrorPage(caught.getMessage());
				}
				@Override
				public void onSuccess(Void result) {
					logger.finer("Service addAnalysis returned successfully.");
					hideDetailPanel();
					refreshAnalysisList();
					showFeedbackPanel("alert-info", "New analysis created!");
				}
			});
		} else {
			// update analysis info
			logger.finer("Requesting service updateAnalysis...");
			Services.getAnalysisGeneratorService().updateAnalysis(analysis, new AsyncCallback<Void>() {
				@Override
				public void onFailure(Throwable caught) {
					logger.severe("Caught service exception " + caught);
					Utils.showErrorPage(caught.getMessage());
				}
				@Override
				public void onSuccess(Void result) {
					logger.finer("Service updateAnalysis returned successfully.");
					hideDetailPanel();
					refreshAnalysisList();
					showFeedbackPanel("alert-info", "Analysis details updated!");
				}
			});
		}
	}

	//	private Analysis.TagFilterLogic getTagFilterLogic() {
	//		Analysis.TagFilterLogic tagFilterLogic = Analysis.TagFilterLogic.NOFILTER;
	//
	//		String tagFilterLogicStr = tagFilterLogicListBox.getSelectedValue();
	//		if(Analysis.TagFilterLogic.NOFILTER.toString().equals(tagFilterLogicStr)) {
	//			tagFilterLogic = Analysis.TagFilterLogic.NOFILTER;
	//		} else if(Analysis.TagFilterLogic.EQUALS.toString().equals(tagFilterLogicStr)) {
	//			tagFilterLogic = Analysis.TagFilterLogic.EQUALS;
	//		} else if(Analysis.TagFilterLogic.STARTSWITH.toString().equals(tagFilterLogicStr)) {
	//			tagFilterLogic = Analysis.TagFilterLogic.STARTSWITH;
	//		} else if(Analysis.TagFilterLogic.CONTAINS.toString().equals(tagFilterLogicStr)) {
	//			tagFilterLogic = Analysis.TagFilterLogic.CONTAINS;
	//		} else if(Analysis.TagFilterLogic.ENDSWITH.toString().equals(tagFilterLogicStr)) {
	//			tagFilterLogic = Analysis.TagFilterLogic.ENDSWITH;
	//		}
	//
	//		return tagFilterLogic;
	//	}

	private void showFeedbackPanel(String alertType, String message) {
		feedbackPanel.setStyleName("alert " + alertType);
		feedbackLabel.setText(message);
		feedbackPanel.setVisible(true);
	}

	private void hideFeedbackPanel() {
		feedbackPanel.setVisible(false);
	}

	@UiHandler("closeFeedbackPanel")
	void onCloseFeedbackPanelClick(ClickEvent e) {
		hideFeedbackPanel();
	}

	// refresh cell table
	private void refreshAnalysisList() {
		logger.info("Refreshing analysis list...");

		// get new row count
		logger.finer("Requesting service getAnalysisCount...");
		Services.getAnalysisGeneratorService().getAnalysisCount(new AsyncCallback<Integer>() {
			@Override
			public void onFailure(Throwable caught) {
				// serious error, show error page
				logger.severe("Caught service exception " + caught);
				Utils.showErrorPage(caught.getMessage());
			}

			@Override
			public void onSuccess(Integer result) {
				logger.finer("Service getAnalysisCount returned successfully.");
				analysisList.setRowCount(result, true);
			}
		});
		analysisList.setVisibleRangeAndClearData(analysisList.getVisibleRange(), true);

	}

	private void deleteAnalysis(Analysis analysis) {
		// deletes analysis when button clicked
		logger.finer("Requesting service deleteAnalysis...");
		Services.getAnalysisGeneratorService().deleteAnalysis(analysis.getId(),
				new AsyncCallback<Void>() {
			@Override
			public void onFailure(Throwable caught) {
				logger.severe("Caught service exception " + caught);
				Utils.showErrorPage(caught.getMessage());
			}

			@Override
			public void onSuccess(Void result) {
				logger.finer("Service deleteAnalysis returned successfully.");
				hideDetailPanel();
				refreshAnalysisList();
				showFeedbackPanel("alert-danger", "Analysis deleted!");
			}
		});
	}

	@UiHandler("newAnalysis")
	void onNewAnalysisClick(ClickEvent e) {
		showDetailPanel();
	}

	private void showDetailPanel() {
		Analysis newAnalysis = new Analysis();
		newAnalysis.setId(-1); // -1 means the analysis is new
		newAnalysis.setCreateDate(new Date());
		newAnalysis.setName("");
		newAnalysis.setDescription("");
		newAnalysis.setTagFilterLogic(Analysis.TagFilterLogic.NOFILTER);
		newAnalysis.setTagKeyword("");
		newAnalysis.setCorpusID(0);
		newAnalysis.setCorpusName("");
		newAnalysis.setFeatureSetID(0);	
		newAnalysis.setFeatureSetName("");
		showDetailPanel(newAnalysis);
	}

	@UiHandler("selectAll")
	void onSelectAllClick(ClickEvent e) {
		for (Analysis corpus : analysisList.getVisibleItems()) {
			analysisListSelectionModel.setSelected(corpus, true);
		}
	}

	@UiHandler("deleteSelected")
	void onDeleteSelectedClick(ClickEvent e) {

		int selectedCount = getSelectedCount();
		if (selectedCount == 0) {
			showFeedbackPanel("alert-danger", "No analysis is selected for deletion!");
			return;
		}

		if (!Window.confirm("Do you really want to delete the " + selectedCount + " selected analysis? "
				+ "This operation will be irreversable!")) {
			return;
		}

		logger.finer("User clicked delete selected button.");
		for (Analysis analysis : analysisListSelectionModel.getSelectedSet()) {
			analysisListSelectionModel.setSelected(analysis, false);
			deleteAnalysis(analysis);
		}
	}

	@UiHandler("selectClear")
	void onSelectClearClick(ClickEvent e) {
		for (Analysis analysis : analysisList.getVisibleItems()) {
			analysisListSelectionModel.setSelected(analysis, false);
		}
	}

	@UiHandler("selectReverse")
	void onSelectReverseClick(ClickEvent e) {
		for (Analysis analysis : analysisList.getVisibleItems()) {
			analysisListSelectionModel.setSelected(analysis, !analysisListSelectionModel.isSelected(analysis));
		}
	}

	private int getSelectedCount() {
//		int selectedCount = 0;
//		for (Analysis analysis : analysisListSelectionModel.getSelectedSet()) {
//			selectedCount++;
//		}
//		return selectedCount;
		return analysisListSelectionModel.getSelectedSet().size();
	}

	@UiHandler("nRecords")
	void onNRecordsClick(ClickEvent e) {
		analysisList.setVisibleRange(0, Integer.parseInt(nRecords.getSelectedValue()));
	}
	
}

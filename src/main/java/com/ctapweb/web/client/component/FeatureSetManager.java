package com.ctapweb.web.client.component;

import java.util.List;
import java.util.logging.Logger;

import com.ctapweb.web.client.service.Services;
import com.ctapweb.web.shared.AnalysisEngine;
import com.ctapweb.web.shared.Utils;
import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
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

public class FeatureSetManager extends Composite {

	private static FeatureSetManagerUiBinder uiBinder = GWT.create(FeatureSetManagerUiBinder.class);

	interface FeatureSetManagerUiBinder extends UiBinder<Widget, FeatureSetManager> {
	}

	//for the icon button in the cell table
	public interface IconButtonTemplate extends SafeHtmlTemplates {
		//		@Template("<span class=\"{3}\">{0}: <a href=\"{1}\">{2}</a></span>")
		@Template("<em class=\"fa {0}\" ></em>")
		SafeHtml iconButtonInCellTable(String icon);

		//for buttons with icon labels
		@Template("<span class=\"btn-label\"><i class=\"fa {0}\"></i></span>{1}")
		SafeHtml labeledButton(String label, String text);

	}

	private static final IconButtonTemplate ICONBUTTONTEMPLATE = 
			GWT.create(IconButtonTemplate.class);

	Logger logger = Logger.getLogger(FeatureSetManager.class.getName());

	//selection model
	MultiSelectionModel<AnalysisEngine> selectedFeatureListSelectionModel;
	MultiSelectionModel<AnalysisEngine> availableFeatureSelectionModel;


	//data provider
	AsyncDataProvider<AnalysisEngine> selectedFeatureDataProvider;
	AsyncDataProvider<AnalysisEngine> availableFeatureDataProvider;

	CellTable<AnalysisEngine> selectedFeatureCellTable; 
	CellTable<AnalysisEngine> availableFeatureCellTable; 

	SimplePager pagerSelectedFeatureList;
	SimplePager pagerAvailableFeatureCellTable;

	long featureSetID;

	@UiField InlineLabel featureSetName;
	@UiField HTMLPanel featureListsWrapper;

	@UiField Anchor selectAll;
	@UiField Anchor selectClear;
	@UiField Anchor selectReverse;
	@UiField Button deleteSelected;
	@UiField Button addSelected;
	@UiField Button showAll;
	@UiField Anchor selectAllRightPanel;
	@UiField Anchor selectClearRightPanel;
	@UiField Anchor selectReverseRightPanel;
	@UiField Button showAllRightPanel;

	@UiField ListBox nRecords;
	@UiField ListBox nRecordsRightPanel;
	@UiField HTMLPanel selectedFeatureListPanel;
	@UiField HTMLPanel selectedFeatureCellTablePanel;
	@UiField HTMLPanel availableFeatureListPanel;
	@UiField HTMLPanel availableFeatureCellTablePanel;

	@UiField HTMLPanel feedbackPanel;
	@UiField InlineLabel feedbackLabel;
	@UiField Button closeFeedbackPanel;
	@UiField HTMLPanel feedbackPanelRightPanel;
	@UiField InlineLabel feedbackLabelRightPanel;
	@UiField Button closeFeedbackPanelRightPanel;

	@UiField HTMLPanel aeDetailsPanel;
	@UiField TextBox aeCreatedOn;
	@UiField TextBox aeName;
	@UiField TextBox aeVersion;
	@UiField TextBox aeVendor;
	@UiField TextArea aeDescription;
	@UiField Button closeAEDetail;
	
	@UiField TextBox search;
	@UiField TextBox searchRightPanel;

	public FeatureSetManager() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	public FeatureSetManager(final long featureSetID) {
		initWidget(uiBinder.createAndBindUi(this));

		this.featureSetID = featureSetID;

		setFeatureSetName();

		setWidgetStyle();

		showAvailableFeatures();

		showSelectedFeatures();

	}

	//Shows all features the system provides.
	private void showAvailableFeatures() {
		availableFeatureCellTable = new CellTable<AnalysisEngine>(AnalysisEngine.KEY_PROVIDER);

		availableFeatureCellTable.setWidth("100%");

		availableFeatureSelectionModel = 
				new MultiSelectionModel<>(AnalysisEngine.KEY_PROVIDER);
		availableFeatureCellTable.setSelectionModel(availableFeatureSelectionModel, 
				DefaultSelectionEventManager.<AnalysisEngine> createCheckboxManager());

		initializeColumnsAllAvailableFeatureCellTable();

		//RPC request to get all feature count
		logger.finer("Requesting getFeatureCount service...");
		Services.getFeatureSelectorService().getFeatureCount(new AsyncCallback<Integer>() {
			@Override
			public void onFailure(Throwable caught) {
				logger.severe("Caught service exception " + caught);
				Utils.showErrorPage(caught.getMessage());
			}

			@Override
			public void onSuccess(Integer result) {
				logger.finer("Service getFeatureCount returned successfully.");
				availableFeatureCellTable.setRowCount(result, true);
			}
		});

		availableFeatureCellTable.setVisibleRange(0, 10);

		availableFeatureDataProvider = new AsyncDataProvider<AnalysisEngine>() {
			@Override
			protected void onRangeChanged(HasData<AnalysisEngine> display) {
				final Range range = display.getVisibleRange();

				final int rangeStart = range.getStart();
				final int rangeLength = range.getLength();

				//an RPC request to get the text data
				logger.finer("Requesting getFeatureList service from within availableFeatureDataProvider...");
				Services.getFeatureSelectorService().getFeatureList(rangeStart, rangeLength, 
						new AsyncCallback<List<AnalysisEngine>>() {

					@Override
					public void onSuccess(List<AnalysisEngine> result) {
						logger.finer("Service getFeatureList returned successfully.");
						updateRowData(rangeStart, result);
					}

					@Override
					public void onFailure(Throwable caught) {
						logger.severe("Caught service exception " + caught);
						Utils.showErrorPage(caught.getMessage());
					}
				});

			}
		};

		availableFeatureDataProvider.addDataDisplay(availableFeatureCellTable);

		pagerAvailableFeatureCellTable = new SimplePager();
		pagerAvailableFeatureCellTable.setDisplay(availableFeatureCellTable);	

		availableFeatureCellTablePanel.clear();
		availableFeatureCellTablePanel.add(availableFeatureCellTable);
		availableFeatureCellTablePanel.add(pagerAvailableFeatureCellTable);
	}

	
	//Shows all the available features whose name contains the keyword.
	private void showAvailableFeatures(final String keyword) {
		availableFeatureCellTable = new CellTable<AnalysisEngine>(AnalysisEngine.KEY_PROVIDER);

		availableFeatureCellTable.setWidth("100%");

		availableFeatureSelectionModel = 
				new MultiSelectionModel<>(AnalysisEngine.KEY_PROVIDER);
		availableFeatureCellTable.setSelectionModel(availableFeatureSelectionModel, 
				DefaultSelectionEventManager.<AnalysisEngine> createCheckboxManager());

		initializeColumnsAllAvailableFeatureCellTable();

		//RPC request to get all feature count
		logger.finer("Requesting getFeatureCount service...");
		Services.getFeatureSelectorService().getFeatureCount(keyword, 
				new AsyncCallback<Integer>() {
			@Override
			public void onFailure(Throwable caught) {
				logger.severe("Caught service exception " + caught);
				Utils.showErrorPage(caught.getMessage());
			}

			@Override
			public void onSuccess(Integer result) {
				logger.finer("Service getFeatureCount returned successfully.");
				availableFeatureCellTable.setRowCount(result, true);
			}
		});

		availableFeatureCellTable.setVisibleRange(0, 10);

		availableFeatureDataProvider = new AsyncDataProvider<AnalysisEngine>() {
			@Override
			protected void onRangeChanged(HasData<AnalysisEngine> display) {
				final Range range = display.getVisibleRange();

				final int rangeStart = range.getStart();
				final int rangeLength = range.getLength();

				//an RPC request to get the text data
				logger.finer("Requesting getFeatureList service from within availableFeatureDataProvider...");
				Services.getFeatureSelectorService().getFeatureList(keyword, rangeStart, rangeLength, 
						new AsyncCallback<List<AnalysisEngine>>() {

					@Override
					public void onSuccess(List<AnalysisEngine> result) {
						logger.finer("Service getFeatureList returned successfully.");
						updateRowData(rangeStart, result);
					}

					@Override
					public void onFailure(Throwable caught) {
						logger.severe("Caught service exception " + caught);
						Utils.showErrorPage(caught.getMessage());
					}
				});
			}
		};

		availableFeatureDataProvider.addDataDisplay(availableFeatureCellTable);

		pagerAvailableFeatureCellTable = new SimplePager();
		pagerAvailableFeatureCellTable.setDisplay(availableFeatureCellTable);	

		availableFeatureCellTablePanel.clear();
		availableFeatureCellTablePanel.add(availableFeatureCellTable);
		availableFeatureCellTablePanel.add(pagerAvailableFeatureCellTable);
	}
	
	//Shows the features selected by this feature set.
	private void showSelectedFeatures() {
		selectedFeatureCellTable = 
				new CellTable<AnalysisEngine>(AnalysisEngine.KEY_PROVIDER);
		selectedFeatureCellTable.setWidth("100%");
		//set the selection model, this multiselection model selects a record 
		//whenever a checkbox in the row is selected 
		selectedFeatureListSelectionModel = 
				new MultiSelectionModel<>(AnalysisEngine.KEY_PROVIDER);
		selectedFeatureCellTable.setSelectionModel(selectedFeatureListSelectionModel, 
				DefaultSelectionEventManager.<AnalysisEngine> createCheckboxManager());

		//initialize columns to the cell table
		initializeColumnsSelectedFeatureList();

		//RPC request to get feature count for this feature set 
		logger.finer("Requesting getFeatureCount service...");
		Services.getFeatureSelectorService().getFeatureCount(featureSetID,
				new AsyncCallback<Integer>() {
			@Override
			public void onFailure(Throwable caught) {
				logger.severe("Caught service exception " + caught);
				Utils.showErrorPage(caught.getMessage());
			}

			@Override
			public void onSuccess(Integer result) {
				logger.finer("Service getFSFeatureCount returned successfully.");
				selectedFeatureCellTable.setRowCount(result, true);
			}
		});


		// list 10 items per page
		selectedFeatureCellTable.setVisibleRange(0, 10);

		//data providers
		//		AsyncDataProvider<Corpus> corpusDataProvider = new AsyncDataProvider<Corpus>() {
		selectedFeatureDataProvider = new AsyncDataProvider<AnalysisEngine>() {
			@Override
			protected void onRangeChanged(HasData<AnalysisEngine> display) {
				final Range range = display.getVisibleRange();

				final int rangeStart = range.getStart();
				final int rangeLength = range.getLength();

				//an RPC request to get the text data
				logger.finer("Requesting getFeatureList service from within data provider...");
				Services.getFeatureSelectorService().getFeatureList(featureSetID, rangeStart, rangeLength, 
						new AsyncCallback<List<AnalysisEngine>>() {

							@Override
							public void onSuccess(List<AnalysisEngine> result) {
								logger.finer("Service getFeatureList returned successfully.");
								updateRowData(rangeStart, result);
							}

							@Override
							public void onFailure(Throwable caught) {
								logger.severe("Caught service exception " + caught);
								Utils.showErrorPage(caught.getMessage());
							}
						});
			}
		};


		//connect the list to the data provider
		selectedFeatureDataProvider.addDataDisplay(selectedFeatureCellTable);

		// Create paging controls.
		pagerSelectedFeatureList = new SimplePager();
		pagerSelectedFeatureList.setDisplay(selectedFeatureCellTable);

		selectedFeatureCellTablePanel.clear();
		selectedFeatureCellTablePanel.add(selectedFeatureCellTable);
		selectedFeatureCellTablePanel.add(pagerSelectedFeatureList);
	}

	//Shows the features selected by this feature set whose name contains the keyword.
	private void showSelectedFeatures(final String keyword) {
		selectedFeatureCellTable = 
				new CellTable<AnalysisEngine>(AnalysisEngine.KEY_PROVIDER);
		selectedFeatureCellTable.setWidth("100%");
		//set the selection model, this multiselection model selects a record 
		//whenever a checkbox in the row is selected 
		selectedFeatureListSelectionModel = 
				new MultiSelectionModel<>(AnalysisEngine.KEY_PROVIDER);
		selectedFeatureCellTable.setSelectionModel(selectedFeatureListSelectionModel, 
				DefaultSelectionEventManager.<AnalysisEngine> createCheckboxManager());

		//initialize columns to the cell table
		initializeColumnsSelectedFeatureList();

		//RPC request to get feature count for this feature set 
		logger.finer("Requesting getFeatureCount service...");
		Services.getFeatureSelectorService().getFeatureCount(featureSetID, keyword,
				new AsyncCallback<Integer>() {
			@Override
			public void onFailure(Throwable caught) {
				logger.severe("Caught service exception " + caught);
				Utils.showErrorPage(caught.getMessage());
			}

			@Override
			public void onSuccess(Integer result) {
				logger.finer("Service getFSFeatureCount returned successfully.");
				selectedFeatureCellTable.setRowCount(result, true);
			}
		});


		// list 10 items per page
		selectedFeatureCellTable.setVisibleRange(0, 10);

		//data providers
		//		AsyncDataProvider<Corpus> corpusDataProvider = new AsyncDataProvider<Corpus>() {
		selectedFeatureDataProvider = new AsyncDataProvider<AnalysisEngine>() {
			@Override
			protected void onRangeChanged(HasData<AnalysisEngine> display) {
				final Range range = display.getVisibleRange();

				final int rangeStart = range.getStart();
				final int rangeLength = range.getLength();

				//an RPC request to get the text data
				logger.finer("Requesting getFeatureList service from within data provider...");
				Services.getFeatureSelectorService().getFeatureList(featureSetID, keyword, 
						rangeStart, rangeLength, new AsyncCallback<List<AnalysisEngine>>() {

							@Override
							public void onSuccess(List<AnalysisEngine> result) {
								logger.finer("Service getFeatureList returned successfully.");
								updateRowData(rangeStart, result);
							}

							@Override
							public void onFailure(Throwable caught) {
								logger.severe("Caught service exception " + caught);
								Utils.showErrorPage(caught.getMessage());
							}
						});

			}
		};


		//connect the list to the data provider
		selectedFeatureDataProvider.addDataDisplay(selectedFeatureCellTable);

		// Create paging controls.
		pagerSelectedFeatureList = new SimplePager();
		pagerSelectedFeatureList.setDisplay(selectedFeatureCellTable);

		selectedFeatureCellTablePanel.clear();
		selectedFeatureCellTablePanel.add(selectedFeatureCellTable);
		selectedFeatureCellTablePanel.add(pagerSelectedFeatureList);
	}
	
	private void setWidgetStyle() {
		deleteSelected.setHTML(ICONBUTTONTEMPLATE.labeledButton("fa-trash", "Remove Selected"));
		addSelected.setHTML(ICONBUTTONTEMPLATE.labeledButton("fa-plus", "Add Selected"));
		selectAll.getElement().getStyle().setCursor(Cursor.POINTER);
		selectClear.getElement().getStyle().setCursor(Cursor.POINTER);
		selectReverse.getElement().getStyle().setCursor(Cursor.POINTER);
		selectAllRightPanel.getElement().getStyle().setCursor(Cursor.POINTER);
		selectClearRightPanel.getElement().getStyle().setCursor(Cursor.POINTER);
		selectReverseRightPanel.getElement().getStyle().setCursor(Cursor.POINTER);
		showAll.setHTML(ICONBUTTONTEMPLATE.labeledButton("fa-filter", "Show All"));
		showAllRightPanel.setHTML(ICONBUTTONTEMPLATE.labeledButton("fa-filter", "Show All"));
	}
	
	private void setFeatureSetName() {
		//set feature set name in page title
		logger.finer("Requesting getFeatureSetName service...");
		Services.getFeatureSelectorService().getFeatureSetName(
				featureSetID, new AsyncCallback<String>() {

					@Override
					public void onSuccess(String result) {
						logger.finer("Service getFeatureSetName returned successfully.");
						featureSetName.setText(result);
					}

					@Override
					public void onFailure(Throwable caught) {
						logger.severe("Caught service exception " + caught);
						Utils.showErrorPage(caught.getMessage());
					}
				});
	}

	private void initializeColumnsSelectedFeatureList() {
		//the checkbox column
		Column<AnalysisEngine, Boolean> checkColumn = new Column<AnalysisEngine, Boolean>(
				new CheckboxCell(true, false)) {
			@Override
			public Boolean getValue(AnalysisEngine ae) {
				// Get the value from the selection model.
				return selectedFeatureListSelectionModel.isSelected(ae);
			}
		};
		selectedFeatureCellTable.addColumn(checkColumn, "");
		selectedFeatureCellTable.setColumnWidth(checkColumn, "3%");

		//the id column
		TextColumn<AnalysisEngine> idColumn = new TextColumn<AnalysisEngine>() {
			@Override
			public String getValue(AnalysisEngine ae) {
				return ae.getId()+"";
			}
		};
		selectedFeatureCellTable.addColumn(idColumn, "ID");
		selectedFeatureCellTable.setColumnWidth(idColumn, "3%");

		//the name column
		TextColumn<AnalysisEngine> titleColumn = new TextColumn<AnalysisEngine>() {
			@Override
			public String getValue(AnalysisEngine ae) {
				return ae.getName();
			}
		};
		selectedFeatureCellTable.addColumn(titleColumn, "Feature Name");

		//the detials button column
		SafeHtml detailsIcon =
				ICONBUTTONTEMPLATE.iconButtonInCellTable("fa-info-circle");
		ActionCell<AnalysisEngine> detailsCell = new ActionCell<>(detailsIcon, 
				new ActionCell.Delegate<AnalysisEngine>() {
			@Override
			public void execute(AnalysisEngine ae) {
				clearFeedbackPanels();
				showFeatureDetails(ae);
			}
		});
		Column<AnalysisEngine, AnalysisEngine> detailsColumn = 
				new Column<AnalysisEngine, AnalysisEngine>(detailsCell) {

			@Override
			public AnalysisEngine getValue(AnalysisEngine ae) {
				return ae;
			}
		};
		selectedFeatureCellTable.addColumn(detailsColumn, "Details");
		selectedFeatureCellTable.setColumnWidth(detailsColumn, "3%");

		//the remove button column
		SafeHtml deleteIcon =
				ICONBUTTONTEMPLATE.iconButtonInCellTable("fa-trash-o");
		ActionCell<AnalysisEngine> deleteCell = new ActionCell<>(deleteIcon, 
				new ActionCell.Delegate<AnalysisEngine>() {
			@Override
			public void execute(AnalysisEngine ae) {
				clearFeedbackPanels();
				if(!Window.confirm("Do you really want to remove " + ae.getName() 
				+ " from the feature set?" )) {
					return;
				}
				logger.finer("User click remove feature button.");
				removeFeature(ae);
//				refreshSelectedFeatureList();
			}
		});
		Column<AnalysisEngine, AnalysisEngine> deleteColumn = 
				new Column<AnalysisEngine, AnalysisEngine>(deleteCell) {

			@Override
			public AnalysisEngine getValue(AnalysisEngine ae) {
				return ae;
			}
		};
		selectedFeatureCellTable.addColumn(deleteColumn, "Remove");
		selectedFeatureCellTable.setColumnWidth(deleteColumn, "3%");

	}

	private void initializeColumnsAllAvailableFeatureCellTable() {
		//the checkbox column
		Column<AnalysisEngine, Boolean> checkColumn = new Column<AnalysisEngine, Boolean>(
				new CheckboxCell(true, false)) {
			@Override
			public Boolean getValue(AnalysisEngine object) {
				// Get the value from the selection model.
				return availableFeatureSelectionModel.isSelected(object);
			}
		};
		availableFeatureCellTable.addColumn(checkColumn, "");
		availableFeatureCellTable.setColumnWidth(checkColumn, "3%");

		//the id column
		TextColumn<AnalysisEngine> idColumn = new TextColumn<AnalysisEngine>() {
			@Override
			public String getValue(AnalysisEngine ae) {
				return ae.getId()+"";
			}
		};
		availableFeatureCellTable.addColumn(idColumn, "ID");
		availableFeatureCellTable.setColumnWidth(idColumn, "3%");

		//the name column
		TextColumn<AnalysisEngine> titleColumn = new TextColumn<AnalysisEngine>() {
			@Override
			public String getValue(AnalysisEngine ae) {
				return ae.getName();
			}
		};
		availableFeatureCellTable.addColumn(titleColumn, "Feature Name");

		//the detials button column
		SafeHtml detailsIcon =
				ICONBUTTONTEMPLATE.iconButtonInCellTable("fa-info-circle");
		ActionCell<AnalysisEngine> detailsCell = new ActionCell<>(detailsIcon, 
				new ActionCell.Delegate<AnalysisEngine>() {
			@Override
			public void execute(AnalysisEngine ae) {
				clearFeedbackPanels();
				showFeatureDetails(ae);
			}
		});
		Column<AnalysisEngine, AnalysisEngine> detailsColumn = 
				new Column<AnalysisEngine, AnalysisEngine>(detailsCell) {

			@Override
			public AnalysisEngine getValue(AnalysisEngine ae) {
				return ae;
			}
		};
		availableFeatureCellTable.addColumn(detailsColumn, "Details");
		availableFeatureCellTable.setColumnWidth(detailsColumn, "3%");

		//the add button column
		SafeHtml addIcon =
				ICONBUTTONTEMPLATE.iconButtonInCellTable("fa-plus");
		ActionCell<AnalysisEngine> addCell = new ActionCell<>(addIcon, 
				new ActionCell.Delegate<AnalysisEngine>() {
			@Override
			public void execute(AnalysisEngine ae) {
				logger.finer("User clicked add button to add AE to feature set.");
				clearFeedbackPanels();
				
				logger.finer("Adding feature to feature set...");
				addFeatureToFS(ae);
//				refreshSelectedFeatureList();
			}
		});
		Column<AnalysisEngine, AnalysisEngine> addColumn = 
				new Column<AnalysisEngine, AnalysisEngine>(addCell) {

			@Override
			public AnalysisEngine getValue(AnalysisEngine ae) {
				return ae;
			}
		};
		availableFeatureCellTable.addColumn(addColumn, "Add");
		availableFeatureCellTable.setColumnWidth(addColumn, "3%");


	}

	private void hideFeatureDetails() {
		featureListsWrapper.setStyleName("col-lg-12");
		aeDetailsPanel.setVisible(false);
	}

	private void showFeatureDetails(AnalysisEngine ae) {
		featureListsWrapper.setStyleName("col-lg-9");
		aeDetailsPanel.setVisible(true);

		aeCreatedOn.setText(ae.getCreateDate().toString());
		aeName.setText(ae.getName());
		aeVersion.setText(ae.getVersion());
		aeVendor.setText(ae.getVendor());

		String descStr = "";
		for(String line: ae.getDescription().split("\\n")) {
			if(line.trim().toLowerCase().startsWith("aae dependency")) {
				break;
			} else {
				descStr += line + "\n";
			}
		}
		aeDescription.setText(descStr);
	}

	private void removeFeature(AnalysisEngine complexityFeature) {

		logger.finer("Requesting removeFeatureFromFS service...");
		Services.getFeatureSelectorService().removeFeatureFromFS(
				featureSetID, complexityFeature.getId(), new AsyncCallback<Void>() {

			@Override
			public void onFailure(Throwable caught) {
				logger.severe("Caught service exception " + caught);
				Utils.showErrorPage(caught.getMessage());
			}

			@Override
			public void onSuccess(Void result) {
				logger.finer("Service removeFeatureFromFS returned successfully.");
				hideFeatureDetails();
				showFeedbackPanel("alert-danger", "Feature removed from feature set!");
				refreshSelectedFeatureList();
			}
		});
	}

	private void showFeedbackPanel(String alertType, String message) {
		feedbackPanel.setStyleName("alert " + alertType);
		feedbackLabel.setText(message);
		feedbackPanel.setVisible(true);
	}
	private void showFeedbackPanelRight(String alertType, String message) {
		feedbackPanelRightPanel.setStyleName("alert " + alertType);
		feedbackLabelRightPanel.setText(message);
		feedbackPanelRightPanel.setVisible(true);
	}

	private void clearFeedbackPanels() {
		feedbackPanel.setVisible(false);
		feedbackPanelRightPanel.setVisible(false);
	}

	//refresh cell table
	private void refreshSelectedFeatureList() {
		//get new row count
		Services.getFeatureSelectorService().getFeatureCount(featureSetID,
				new AsyncCallback<Integer>() {
			@Override
			public void onFailure(Throwable caught) {
				//serious error, show error page
				logger.warning("Can't get selected feature count, user not logged in or database error.");
				Utils.showErrorPage(caught.getMessage());
			}

			@Override
			public void onSuccess(Integer result) {
				selectedFeatureCellTable.setRowCount(result, true);
			}
		});
		selectedFeatureCellTable.setVisibleRangeAndClearData(selectedFeatureCellTable.getVisibleRange(), true);

	}

	@UiHandler("deleteSelected")
	void onDeleteSelectedClick(ClickEvent e) {

		clearFeedbackPanels();

		int selectedCount  = getSelectedCount(selectedFeatureListSelectionModel);
		if(selectedCount == 0) {
			showFeedbackPanel("alert-danger", "No feature is selected for removal!" );
			return;
		}

		if(!Window.confirm("Do you really want to delete the " + selectedCount + " selected features? "
				)) {
			return;
		}

		logger.finer("User clieck delete selected button.");
		for(AnalysisEngine complexityFeature: selectedFeatureListSelectionModel.getSelectedSet()) {
			selectedFeatureListSelectionModel.setSelected(complexityFeature, false);
			removeFeature(complexityFeature);
		}
//		refreshSelectedFeatureList();
	}

	@UiHandler("addSelected")
	void onAddSelectedClick(ClickEvent e) {
		hideFeatureDetails();
		clearFeedbackPanels();

		int selectedCount  = getSelectedCount(availableFeatureSelectionModel);
		if(selectedCount == 0) {
			showFeedbackPanelRight("alert-danger", "No feature is selected for adding to the feature set!" );
			return;
		}

		for(AnalysisEngine complexityFeature: availableFeatureSelectionModel.getSelectedSet()) {
			addFeatureToFS(complexityFeature);
			availableFeatureSelectionModel.setSelected(complexityFeature, false);
		}
	}

	private int getSelectedCount(MultiSelectionModel<AnalysisEngine> selectionModel) {
		int selectedCount = 0;
		//		for(ComplexityFeature complexityFeature: selectedFeatureListSelectionModel.getSelectedSet()) {
		for(AnalysisEngine complexityFeature: selectionModel.getSelectedSet()) {
			selectedCount++;
		}
		return selectedCount;
	}

	@UiHandler("nRecords")
	void onNRecordsClick(ClickEvent e) {
		selectedFeatureCellTable.setVisibleRange(0, Integer.parseInt(nRecords.getSelectedValue()));
	}
	@UiHandler("nRecordsRightPanel")
	void onNRecordsRightPanelClick(ClickEvent e) {
		availableFeatureCellTable.setVisibleRange(0, Integer.parseInt(nRecordsRightPanel.getSelectedValue()));
	}

	@UiHandler("selectAll")
	void onSelectAllClick(ClickEvent e) {
		clearFeedbackPanels();
		for(AnalysisEngine complexityFeature : selectedFeatureCellTable.getVisibleItems()) {
			selectedFeatureListSelectionModel.setSelected(complexityFeature, true);
		}
	}
	@UiHandler("selectAllRightPanel")
	void onSelectAllRightPanelClick(ClickEvent e) {
		clearFeedbackPanels();
		for(AnalysisEngine complexityFeature : availableFeatureCellTable.getVisibleItems()) {
			availableFeatureSelectionModel.setSelected(complexityFeature, true);
		}
	}


	@UiHandler("selectClear")
	void onSelectClearClick(ClickEvent e) {
		clearFeedbackPanels();
		for(AnalysisEngine complexityFeature : selectedFeatureCellTable.getVisibleItems()) {
			selectedFeatureListSelectionModel.setSelected(complexityFeature, false);
		}
	}
	@UiHandler("selectClearRightPanel")
	void onSelectClearRightPanelClick(ClickEvent e) {
		for(AnalysisEngine complexityFeature : availableFeatureCellTable.getVisibleItems()) {
			availableFeatureSelectionModel.setSelected(complexityFeature, false);
		}
	}

	@UiHandler("selectReverse")
	void onSelectReverseClick(ClickEvent e) {
		for(AnalysisEngine complexityFeature : selectedFeatureCellTable.getVisibleItems()) {
			selectedFeatureListSelectionModel.setSelected(complexityFeature, 
					!selectedFeatureListSelectionModel.isSelected(complexityFeature));
		}
	}
	@UiHandler("selectReverseRightPanel")
	void onSelectReverseRightPanelClick(ClickEvent e) {
		for(AnalysisEngine complexityFeature : availableFeatureCellTable.getVisibleItems()) {
			availableFeatureSelectionModel.setSelected(complexityFeature, 
					!availableFeatureSelectionModel.isSelected(complexityFeature));
		}
	}

	@UiHandler("closeFeedbackPanel")
	void onCloseFeedbackPanelClick(ClickEvent e) {
		feedbackPanel.setVisible(false);
	}
	@UiHandler("closeFeedbackPanelRightPanel")
	void onCloseFeedbackPanelRightPanelClick(ClickEvent e) {
		feedbackPanelRightPanel.setVisible(false);
	}

	private void addFeatureToFS(AnalysisEngine ae) {

		logger.finer("Requesting addToFeatureSet service...");
		Services.getFeatureSelectorService().addToFeatureSet(ae.getId(), featureSetID, 
				new AsyncCallback<Void>() {
			@Override
			public void onFailure(Throwable caught) {
				logger.severe("Caught service exception " + caught);
				Utils.showErrorPage(caught.getMessage());
			}

			@Override
			public void onSuccess(Void result) {
				logger.finer("Service getFeatureSetName returned successfully.");
				showFeedbackPanelRight("alert-success", 
						"Successfully added feature to the current feature set!");
				refreshSelectedFeatureList();
			}
		});
	}

	@UiHandler("closeAEDetail")
	void onCloseAEDetailClick(ClickEvent e) {
		featureListsWrapper.setStyleName("col-lg-12");
		aeDetailsPanel.setVisible(false);
	}
	
	@UiHandler("search")
	void onSearchKeyDown(KeyDownEvent e) {
		if (e.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
			String keyword = search.getText();
			if(keyword.isEmpty()) {
				showSelectedFeatures();
			} else {
				showSelectedFeatures(keyword);
			}
		}
	}
	
	@UiHandler("searchRightPanel")
	void onSearchRightPanelKeyDown(KeyDownEvent e) {
		if (e.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
			String keyword = searchRightPanel.getText();
			if(keyword.isEmpty()) {
				showAvailableFeatures();
			} else {
				showAvailableFeatures(keyword);
			}
		}
	}

	@UiHandler("showAllRightPanel")
	void onShowAllRightPanelClick(ClickEvent e) {
		showAvailableFeatures();
	}
	
	@UiHandler("showAll")
	void onShowAllClick(ClickEvent e) {
		showSelectedFeatures();
	}
}
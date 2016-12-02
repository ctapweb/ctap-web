package com.ctapweb.web.client.component;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import com.ctapweb.web.client.HistoryToken;
import com.ctapweb.web.client.service.Services;
import com.ctapweb.web.shared.FeatureSet;
import com.ctapweb.web.shared.SharedProperties;
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
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.History;
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

public class FeatureSelector extends Composite {

	private static FeatureSelectorUiBinder uiBinder = GWT.create(FeatureSelectorUiBinder.class);

	interface FeatureSelectorUiBinder extends UiBinder<Widget, FeatureSelector> {
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

	@UiField Button newFeatureSet;
	@UiField Anchor selectAll;
	@UiField Anchor selectClear;
	@UiField Anchor selectReverse;
	@UiField Button deleteSelected;

	@UiField ListBox nRecords;
	@UiField HTMLPanel featureSetListPanel;

	@UiField HTMLPanel featureSetDetailPanel;
	@UiField TextBox createdOn;
	@UiField TextBox featureSetName;
	@UiField TextArea featureSetDescription;
	@UiField Hidden featureSetIdHidden;
	@UiField Button saveDetail;
	@UiField Button closeDetail;

	@UiField HTMLPanel feedbackPanel;
	@UiField InlineLabel feedbackLabel;
	@UiField Button closeFeedbackPanel;

	Logger logger = Logger.getLogger(FeatureSelector.class.getName());

	// selection model
	static final MultiSelectionModel<FeatureSet> featureSetListSelectionModel = new MultiSelectionModel<>(
			FeatureSet.KEY_PROVIDER);

	//
	AsyncDataProvider<FeatureSet> featureSetDataProvider;

	// get user cookie
	private String userCookieValue = Cookies.getCookie(SharedProperties.USERCOOKIENAME);

	final CellTable<FeatureSet> featureSetList = new CellTable<FeatureSet>(FeatureSet.KEY_PROVIDER);
	SimplePager pager;

	public FeatureSelector() {
		initWidget(uiBinder.createAndBindUi(this));

		// inialize some widgets
		newFeatureSet.setHTML(ICONBUTTONTEMPLATE.labeledButton("fa-file", "New Feature Set"));
		deleteSelected.setHTML(ICONBUTTONTEMPLATE.labeledButton("fa-trash", "Delete Selected"));
		selectAll.getElement().getStyle().setCursor(Cursor.POINTER);
		selectClear.getElement().getStyle().setCursor(Cursor.POINTER);
		selectReverse.getElement().getStyle().setCursor(Cursor.POINTER);

		// initialize the cell table
		featureSetList.setWidth("100%");

		// set the selection model, this multiselection model selects a record
		// whenever a checkbox in the row is selected
		featureSetList.setSelectionModel(featureSetListSelectionModel,
				DefaultSelectionEventManager.<FeatureSet> createCheckboxManager());

		// initialize columns to the cell table
		initializeColumns(featureSetList);

		// RPC request to get feature set count
		Services.getFeatureSelectorService().getFeatureSetCount(new AsyncCallback<Integer>() {
			@Override
			public void onFailure(Throwable caught) {
				// serious error, show error page
				logger.warning("Can't get feature set count, user not logged in or database error.");
				Utils.showErrorPage(caught.getMessage());
			}

			@Override
			public void onSuccess(Integer result) {
				featureSetList.setRowCount(result, true);
			}
		});

		// list 10 items per page
		featureSetList.setVisibleRange(0, 10);

		// data provider
		featureSetDataProvider = new AsyncDataProvider<FeatureSet>() {
			@Override
			protected void onRangeChanged(HasData<FeatureSet> display) {
				final Range range = display.getVisibleRange();

				final int rangeStart = range.getStart();
				final int rangeLength = range.getLength();

				// an RPC request to get the corpus data
				Services.getFeatureSelectorService().getFeatureSetList(rangeStart, rangeLength,
						new AsyncCallback<List<FeatureSet>>() {

							@Override
							public void onSuccess(List<FeatureSet> result) {
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
		featureSetDataProvider.addDataDisplay(featureSetList);

		// Create paging controls.
		// SimplePager pager = new SimplePager();
		pager = new SimplePager();
		pager.setDisplay(featureSetList);

		featureSetListPanel.add(featureSetList);
		featureSetListPanel.add(pager);
	}

	private void initializeColumns(CellTable<FeatureSet> featureSetList) {
		// the checkbox column
		Column<FeatureSet, Boolean> checkColumn = new Column<FeatureSet, Boolean>(new CheckboxCell(true, false)) {
			@Override
			public Boolean getValue(FeatureSet object) {
				// Get the value from the selection model.
				return featureSetListSelectionModel.isSelected(object);
			}
		};
		featureSetList.addColumn(checkColumn, "");
		featureSetList.setColumnWidth(checkColumn, "3%");

		// the id column
		TextColumn<FeatureSet> idColumn = new TextColumn<FeatureSet>() {
			@Override
			public String getValue(FeatureSet featureSet) {
				return featureSet.getId() + "";
			}
		};
		featureSetList.addColumn(idColumn, "ID");
		featureSetList.setColumnWidth(idColumn, "3%");

		// the name column
		TextColumn<FeatureSet> nameColumn = new TextColumn<FeatureSet>() {
			@Override
			public String getValue(FeatureSet featureSet) {
				return featureSet.getName();
			}
		};
		featureSetList.addColumn(nameColumn, "Feature Set Name");

		// the corpus description column
		TextColumn<FeatureSet> descriptionColumn = new TextColumn<FeatureSet>() {
			@Override
			public String getValue(FeatureSet featureSet) {
				String desc = featureSet.getDescription();

				if (desc.length() > 97) {
					desc = desc.substring(0, 97) + "...";
				}
				return desc;
			}
		};
		featureSetList.addColumn(descriptionColumn, "Description");
		featureSetList.setColumnWidth(descriptionColumn, "50%");

		// the created date column
		TextColumn<FeatureSet> createdColumn = new TextColumn<FeatureSet>() {
			@Override
			public String getValue(FeatureSet featureSet) {
				return DateTimeFormat.getFormat("dd/MM/yy").format(featureSet.getCreateDate());
			}
		};
		featureSetList.addColumn(createdColumn, "Created On");
		featureSetList.setColumnWidth(createdColumn, "8%");

		SafeHtml openIcon = ICONBUTTONTEMPLATE.iconButtonInCellTable("fa-folder-open-o");
		ActionCell<FeatureSet> openCell = new ActionCell<FeatureSet>(openIcon, new ActionCell.Delegate<FeatureSet>() {
			@Override
			public void execute(FeatureSet featureSet) {
				// opens the corpus
				History.newItem(HistoryToken.featuresetmanager + "?featureSetID=" + featureSet.getId());
			}

		});
		Column<FeatureSet, FeatureSet> openColumn = new Column<FeatureSet, FeatureSet>(openCell) {

			@Override
			public FeatureSet getValue(FeatureSet featureSet) {
				return featureSet;
			}
		};
		featureSetList.addColumn(openColumn, "Open");
		featureSetList.setColumnWidth(openColumn, "3%");

		// the edit button column
		SafeHtml editIcon = ICONBUTTONTEMPLATE.iconButtonInCellTable("fa-pencil-square-o");
		ActionCell<FeatureSet> editCell = new ActionCell<>(editIcon, new ActionCell.Delegate<FeatureSet>() {
			@Override
			public void execute(FeatureSet featureSet) {
				showDetailPanel(featureSet);
			}
		});
		Column<FeatureSet, FeatureSet> editColumn = new Column<FeatureSet, FeatureSet>(editCell) {

			@Override
			public FeatureSet getValue(FeatureSet featureSet) {
				return featureSet;
			}
		};
		featureSetList.addColumn(editColumn, "Edit");
		featureSetList.setColumnWidth(editColumn, "3%");

		// the delete button column
		SafeHtml deleteIcon = ICONBUTTONTEMPLATE.iconButtonInCellTable("fa-trash-o");
		ActionCell<FeatureSet> deleteCell = new ActionCell<>(deleteIcon, new ActionCell.Delegate<FeatureSet>() {
			@Override
			public void execute(FeatureSet featureSet) {
				if (!Window.confirm("Do you really want to delete " + featureSet.getName())) {
					return;
				}
				deleteFeatureSet(featureSet);
			}
		});
		Column<FeatureSet, FeatureSet> deleteColumn = new Column<FeatureSet, FeatureSet>(deleteCell) {

			@Override
			public FeatureSet getValue(FeatureSet featureSet) {
				return featureSet;
			}
		};
		featureSetList.addColumn(deleteColumn, "Delete");
		featureSetList.setColumnWidth(deleteColumn, "3%");

	}

	private void showDetailPanel(FeatureSet featureSet) {
		feedbackPanel.setVisible(false);
		createdOn.setText(featureSet.getCreateDate().toString());
		featureSetName.setText(featureSet.getName());
		featureSetDescription.setText(featureSet.getDescription());
		featureSetIdHidden.setValue(featureSet.getId() + "");

		featureSetListPanel.setStyleName("col-lg-9");
		featureSetDetailPanel.setVisible(true);
	}

	private void hideDetailPanel() {
		featureSetDetailPanel.setVisible(false);
		featureSetListPanel.setStyleName("col-lg-12");
	}

	@UiHandler("closeDetail")
	void onCloseDetailClick(ClickEvent e) {
		hideDetailPanel();
	}

	@UiHandler("saveDetail")
	void onSaveDetailClick(ClickEvent e) {
		FeatureSet featureSet = new FeatureSet();
		featureSet.setId(Integer.parseInt(featureSetIdHidden.getValue()));
		featureSet.setName(featureSetName.getText());
		featureSet.setDescription(featureSetDescription.getText());

		// check if info is empty
		if (featureSet.getName().isEmpty()) {
			showFeedbackPanel("alert-danger", "You need to enter a name for your feature set.");
			return;
		}

		if (featureSet.getId() == -1) {
			// new feature set
			Services.getFeatureSelectorService().addFeatureSet(featureSet, new AsyncCallback<Void>() {

				@Override
				public void onFailure(Throwable caught) {
					Utils.showErrorPage(caught.getMessage());
				}

				@Override
				public void onSuccess(Void result) {
					hideDetailPanel();
					refreshFeatureSetList();
					showFeedbackPanel("alert-info", "New feature set created!");
				}
			});
		} else {
			// update feature set info
			Services.getFeatureSelectorService().updateFeatureSet(featureSet,
					new AsyncCallback<Void>() {

						@Override
						public void onFailure(Throwable caught) {
							Utils.showErrorPage(caught.getMessage());
						}

						@Override
						public void onSuccess(Void result) {
							hideDetailPanel();
							// refresh cell table
							refreshFeatureSetList();
							showFeedbackPanel("alert-info", "Feature set details updated!");
						}
					});
		}

	}

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
	private void refreshFeatureSetList() {
		// get new row count
		Services.getFeatureSelectorService().getFeatureSetCount(new AsyncCallback<Integer>() {
			@Override
			public void onFailure(Throwable caught) {
				// serious error, show error page
				logger.warning("Can't get feature set count, user not logged in or database error.");
				Utils.showErrorPage(caught.getMessage());
			}

			@Override
			public void onSuccess(Integer result) {
				featureSetList.setRowCount(result, true);
			}
		});
		featureSetList.setVisibleRangeAndClearData(featureSetList.getVisibleRange(), true);

	}

	private void deleteFeatureSet(FeatureSet featureSet) {

		// delete when button clicked
		Services.getFeatureSelectorService().deleteFeatureSet(featureSet, new AsyncCallback<Void>() {

			@Override
			public void onFailure(Throwable caught) {
				Utils.showErrorPage(caught.getMessage());
			}

			@Override
			public void onSuccess(Void result) {
				hideDetailPanel();
				refreshFeatureSetList();
				showFeedbackPanel("alert-danger", "Feature set deleted!");
			}
		});
	}

	@UiHandler("newFeatureSet")
	void onNewFeatureSetClick(ClickEvent e) {
		FeatureSet newFeatureSet = new FeatureSet();
		newFeatureSet.setId(-1); // -1 means the corpus is new
		newFeatureSet.setCreateDate(new Date());
		newFeatureSet.setName("");
		newFeatureSet.setDescription("");
		showDetailPanel(newFeatureSet);
	}

	@UiHandler("selectAll")
	void onSelectAllClick(ClickEvent e) {
		for (FeatureSet featureSet : featureSetList.getVisibleItems()) {
			featureSetListSelectionModel.setSelected(featureSet, true);
		}
	}

	@UiHandler("deleteSelected")
	void onDeleteSelectedClick(ClickEvent e) {

		int selectedCount = getSelectedCount();
		if (selectedCount == 0) {
			showFeedbackPanel("alert-danger", "No feature set is selected for deletion!");
			return;
		}

		if (!Window.confirm("Do you really want to delete the " + selectedCount + " selected feature sets? "
				+ "This operation will be irreversable!")) {
			return;
		}

		for (FeatureSet featureSet : featureSetListSelectionModel.getSelectedSet()) {
			featureSetListSelectionModel.setSelected(featureSet, false);
			deleteFeatureSet(featureSet);
		}
	}

	@UiHandler("selectClear")
	void onSelectClearClick(ClickEvent e) {
		for (FeatureSet featureSet : featureSetList.getVisibleItems()) {
			featureSetListSelectionModel.setSelected(featureSet, false);
		}
	}

	@UiHandler("selectReverse")
	void onSelectReverseClick(ClickEvent e) {
		for (FeatureSet featureSet : featureSetList.getVisibleItems()) {
			featureSetListSelectionModel.setSelected(featureSet, !featureSetListSelectionModel.isSelected(featureSet));
		}
	}

	private int getSelectedCount() {
		int selectedCount = 0;
		for (FeatureSet featureSet : featureSetListSelectionModel.getSelectedSet()) {
			selectedCount++;
		}
		return selectedCount;
	}

	@UiHandler("nRecords")
	void onNRecordsClick(ClickEvent e) {
		featureSetList.setVisibleRange(0, Integer.parseInt(nRecords.getSelectedValue()));
	}
}
package com.ctapweb.web.client.component.admin;

import java.util.List;
import java.util.logging.Logger;

import org.moxieapps.gwt.uploader.client.Uploader;
import org.moxieapps.gwt.uploader.client.events.FileDialogCompleteEvent;
import org.moxieapps.gwt.uploader.client.events.FileDialogCompleteHandler;
import org.moxieapps.gwt.uploader.client.events.FileDialogStartEvent;
import org.moxieapps.gwt.uploader.client.events.FileDialogStartHandler;
import org.moxieapps.gwt.uploader.client.events.FileQueueErrorEvent;
import org.moxieapps.gwt.uploader.client.events.FileQueueErrorHandler;
import org.moxieapps.gwt.uploader.client.events.FileQueuedEvent;
import org.moxieapps.gwt.uploader.client.events.FileQueuedHandler;
import org.moxieapps.gwt.uploader.client.events.UploadCompleteEvent;
import org.moxieapps.gwt.uploader.client.events.UploadCompleteHandler;
import org.moxieapps.gwt.uploader.client.events.UploadErrorEvent;
import org.moxieapps.gwt.uploader.client.events.UploadErrorHandler;
import org.moxieapps.gwt.uploader.client.events.UploadProgressEvent;
import org.moxieapps.gwt.uploader.client.events.UploadProgressHandler;
import org.moxieapps.gwt.uploader.client.events.UploadSuccessEvent;
import org.moxieapps.gwt.uploader.client.events.UploadSuccessHandler;

import com.ctapweb.web.client.service.Services;
import com.ctapweb.web.shared.AnalysisEngine;
import com.ctapweb.web.shared.Utils;
import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.json.client.JSONObject;
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
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.Range;
import com.google.gwt.widgetideas.client.ProgressBar;

public class AdminAE extends Composite {

	private static AdminAEUiBinder uiBinder = GWT.create(AdminAEUiBinder.class);

	interface AdminAEUiBinder extends UiBinder<Widget, AdminAE> {
	}

	// for the icon button in the cell table
	public interface IconButtonTemplate extends SafeHtmlTemplates {
		@Template("<em class=\"fa {0}\" ></em>")
		SafeHtml iconButtonInCellTable(String icon);

		// for buttons with icon labels
		@Template("<span class=\"btn-label\"><i class=\"fa {0}\"></i></span>{1}")
		SafeHtml labeledButton(String label, String text);

	}

	private static final IconButtonTemplate ICONBUTTONTEMPLATE = GWT.create(IconButtonTemplate.class);

	@UiField Button newAE;
	@UiField Anchor selectAll;
	@UiField Anchor selectClear;
	@UiField Anchor selectReverse;
	@UiField Button deleteSelected;
	@UiField Button importAE;

	@UiField ListBox nRecords;
	@UiField HTMLPanel aeListPanel;
	@UiField HTMLPanel aeCellTablePanel;

	@UiField HTMLPanel newAEPanel;
	@UiField SimplePanel fileUploadPanelLeft;
	@UiField SimplePanel fileUploadPanelRight;
	@UiField Button closeNewAEPanel;

	@UiField HTMLPanel aeDetailPanel;
	@UiField TextBox createdOn;
	@UiField TextBox aeName;
	@UiField ListBox aeType;
	@UiField TextBox aeVersion;
	@UiField TextBox aeVendor;
	@UiField TextArea aeDescription;
	@UiField TextBox aeDescriptorFileName;
	@UiField TextArea aeDescriptorFileContent;
	@UiField Hidden aeIdHidden;
	@UiField Button closeAEDetail;

	@UiField
	HTMLPanel feedbackPanel;
	@UiField
	InlineLabel feedbackLabel;
	@UiField
	Button closeFeedbackPanel;

	Logger logger = Logger.getLogger(AdminAE.class.getName());

	// selection model
	static final MultiSelectionModel<AnalysisEngine> aeListSelectionModel = 
			new MultiSelectionModel<>(AnalysisEngine.KEY_PROVIDER);

	//data provider
	AsyncDataProvider<AnalysisEngine> aeDataProvider;

	final CellTable<AnalysisEngine> aeList = new CellTable<AnalysisEngine>(AnalysisEngine.KEY_PROVIDER);
	SimplePager pager;

	// file uploader in the new ae and edit ae panel
	private final Uploader uploader = new Uploader();
	JSONObject formFields = new JSONObject(); // for passing additional info

	public AdminAE() {
		logger.info("Opening AdminAE page...");
		initWidget(uiBinder.createAndBindUi(this));
		// inialize some widgets
		newAE.setHTML(ICONBUTTONTEMPLATE.labeledButton("fa-file", "New AE"));
		importAE.setHTML(ICONBUTTONTEMPLATE.labeledButton("fa-share", "Import AE"));
		deleteSelected.setHTML(ICONBUTTONTEMPLATE.labeledButton("fa-trash", "Delete Selected"));
		selectAll.getElement().getStyle().setCursor(Cursor.POINTER);
		selectClear.getElement().getStyle().setCursor(Cursor.POINTER);
		selectReverse.getElement().getStyle().setCursor(Cursor.POINTER);

		aeList.setWidth("100%");

		// set the selection model, this multiselection model selects a record
		// whenever a checkbox in the row is selected
		aeList.setSelectionModel(aeListSelectionModel,
				DefaultSelectionEventManager.<AnalysisEngine> createCheckboxManager());

		// initialize columns to the cell table
		initializeColumns(aeList);

		// RPC request to get ae count
		logger.info("Requesting getAECount service...");
		Services.getAdminService().getAECount(new AsyncCallback<Long>() {
			@Override
			public void onFailure(Throwable caught) {
				// serious error, show error page
				logger.severe("Caught service exception " + caught);
				Utils.showErrorPage(caught.getMessage());
			}

			@Override
			public void onSuccess(Long result) {
				logger.info("getAECount returned successfully with result: " + result);
				aeList.setRowCount(result.intValue(), true);
			}
		});

		// list 10 items per page
		aeList.setVisibleRange(0, 10);

		// data provider
		aeDataProvider = new AsyncDataProvider<AnalysisEngine>() {
			@Override
			protected void onRangeChanged(HasData<AnalysisEngine> display) {
				final Range range = display.getVisibleRange();

				final int rangeStart = range.getStart();
				final int rangeLength = range.getLength();

				// an RPC request to get the AE list
				logger.info("Requesting getAEList service from within data provider...");
				Services.getAdminService().getAEList(rangeStart, rangeLength,
						new AsyncCallback<List<AnalysisEngine>>() {

					@Override
					public void onSuccess(List<AnalysisEngine> result) {
						logger.info("Service getAEList returned list of analysis engines successfully.");
						updateRowData(rangeStart, result);
					}

					@Override
					public void onFailure(Throwable caught) {
						logger.severe("Caught service exception: " + caught);
						Utils.showErrorPage(caught.getMessage());
					}
				});

			}
		};

		// connect the list to the data provider
		aeDataProvider.addDataDisplay(aeList);

		// Create paging controls.
		pager = new SimplePager();
		pager.setDisplay(aeList);

		//		aeListPanel.add(aeList);
		//		aeListPanel.add(pager);
		aeCellTablePanel.add(aeList);
		aeCellTablePanel.add(pager);
	}

	private void initializeColumns(CellTable<AnalysisEngine> aeList) {
		// the checkbox column
		Column<AnalysisEngine, Boolean> checkColumn = new Column<AnalysisEngine, Boolean>(
				new CheckboxCell(true, false)) {
			@Override
			public Boolean getValue(AnalysisEngine object) {
				// Get the value from the selection model.
				return aeListSelectionModel.isSelected(object);
			}
		};
		aeList.addColumn(checkColumn, "");
		aeList.setColumnWidth(checkColumn, "3%");

		// the id column
		TextColumn<AnalysisEngine> idColumn = new TextColumn<AnalysisEngine>() {
			@Override
			public String getValue(AnalysisEngine ae) {
				return ae.getId() + "";
			}
		};
		aeList.addColumn(idColumn, "ID");
		aeList.setColumnWidth(idColumn, "3%");

		// the type column
		TextColumn<AnalysisEngine> typeColumn = new TextColumn<AnalysisEngine>() {
			@Override
			public String getValue(AnalysisEngine ae) {
				return ae.getType().toString();
			}
		};
		aeList.addColumn(typeColumn, "AE Type");
		aeList.setColumnWidth(typeColumn, "5%");

		// the name column
		TextColumn<AnalysisEngine> nameColumn = new TextColumn<AnalysisEngine>() {
			@Override
			public String getValue(AnalysisEngine ae) {
				return ae.getName();
			}
		};
		aeList.addColumn(nameColumn, "AE Name");

		// the description column
		TextColumn<AnalysisEngine> descriptionColumn = new TextColumn<AnalysisEngine>() {
			@Override
			public String getValue(AnalysisEngine ae) {
				String desc = ae.getDescription();

				if (desc.length() > 97) {
					desc = desc.substring(0, 97) + "...";
				}
				return desc;
			}
		};
		aeList.addColumn(descriptionColumn, "Description");
//		aeList.setColumnWidth(descriptionColumn, "30%");

		// the description column
		TextColumn<AnalysisEngine> dependencyColumn = new TextColumn<AnalysisEngine>() {
			@Override
			public String getValue(AnalysisEngine ae) {
				String depStr = "";
				for(AnalysisEngine depAE: ae.getAeDependency()) {
					depStr += "[" + depAE.getName() + "]";
				}
				return depStr;
			}
		};
		aeList.addColumn(dependencyColumn, "Dependency");
		aeList.setColumnWidth(dependencyColumn, "20%");

		// the created date column
		TextColumn<AnalysisEngine> createdColumn = new TextColumn<AnalysisEngine>() {
			@Override
			public String getValue(AnalysisEngine ae) {
				// SimpleDateFormat dateFormat = new
				// SimpleDateFormat("dd/MM/yy");
				return DateTimeFormat.getFormat("dd/MM/yy").format(ae.getCreateDate());
				// return dateFormat.format(corpus.getCreateDate().toString());
			}
		};
		aeList.addColumn(createdColumn, "Created On");
		aeList.setColumnWidth(createdColumn, "8%");

		// the info button column
		//		SafeHtml editIcon = ICONBUTTONTEMPLATE.iconButtonInCellTable("fa-pencil-square-o");
		SafeHtml detailsIcon = ICONBUTTONTEMPLATE.iconButtonInCellTable("fa-info-circle");
		ActionCell<AnalysisEngine> detailsCell = new ActionCell<>(detailsIcon, new ActionCell.Delegate<AnalysisEngine>() {
			@Override
			public void execute(AnalysisEngine ae) {
				logger.finer("User clicked edit AE button.");
				showDetailPanel(ae);
			}
		});
		Column<AnalysisEngine, AnalysisEngine> detailsColumn = new Column<AnalysisEngine, AnalysisEngine>(detailsCell) {

			@Override
			public AnalysisEngine getValue(AnalysisEngine ae) {
				return ae;
			}
		};
		aeList.addColumn(detailsColumn, "Details");
		aeList.setColumnWidth(detailsColumn, "3%");

		// the delete button column
		SafeHtml deleteIcon = ICONBUTTONTEMPLATE.iconButtonInCellTable("fa-trash-o");
		ActionCell<AnalysisEngine> deleteCell = new ActionCell<>(deleteIcon, new ActionCell.Delegate<AnalysisEngine>() {
			@Override
			public void execute(AnalysisEngine ae) {
				if (!Window.confirm("Do you really want to delete " + ae.getName())) {
					return;
				}
				logger.finer("User clicked delete AE button.");
				deleteAE(ae);
			}
		});
		Column<AnalysisEngine, AnalysisEngine> deleteColumn = new Column<AnalysisEngine, AnalysisEngine>(deleteCell) {

			@Override
			public AnalysisEngine getValue(AnalysisEngine ae) {
				return ae;
			}
		};
		aeList.addColumn(deleteColumn, "Delete");
		aeList.setColumnWidth(deleteColumn, "3%");

	}

	private void setAETypeSelected(String analysisEngineType) {
		for(int i = 0; i < aeType.getItemCount(); i++) {
			if(aeType.getValue(i).equals(analysisEngineType)) {
				aeType.setSelectedIndex(i);
			}
		}
	}

	//for editing AE
	private void showDetailPanel(AnalysisEngine ae) {
		feedbackPanel.setVisible(false);
		hideSidePanels();

		createdOn.setText(ae.getCreateDate().toString());
		aeName.setText(ae.getName());
		setAETypeSelected(ae.getType());
		aeVersion.setText(ae.getVersion());
		aeVendor.setText(ae.getVendor());
		aeDescription.setText(ae.getDescription());
		aeDescriptorFileName.setText(ae.getDescriptorFileName());
		aeDescriptorFileContent.setText(ae.getDescriptorFileContent());
		aeIdHidden.setValue(ae.getId() + "");

		aeListPanel.setStyleName("col-lg-9");
		aeDetailPanel.setVisible(true);

	}

	//for adding new AE
	private void showNewAEPanel() {
		feedbackPanel.setVisible(false);
		hideSidePanels();

		setFileUploader();

		aeListPanel.setStyleName("col-lg-9");
		newAEPanel.setVisible(true);

	}

	private void setFileUploader() {
		// the file uploader
		// create the multiple file uploader widget: code borrowed form gwtuploader
		final ProgressBar progressBar = new ProgressBar(0.0, 1.0, 0.0, new CancelProgressBarTextFormatter());
		final Image cancelButton = new Image("img/cancel.png");

		final HorizontalPanel progressPanel = new HorizontalPanel();

		uploader.setUploadURL("/ctap/uploadAEDescriptorServlet")
		.setButtonText("<span class=\"btn btn-success fileinput-button\">"
				+ "<i class=\"fa fa-fw fa-plus\"></i> " + "<span>Add a file...</span> " + "</span>")
		.setButtonWidth(160).setButtonHeight(50).setFileSizeLimit("100 MB")
		.setButtonCursor(Uploader.Cursor.HAND).setButtonAction(Uploader.ButtonAction.SELECT_FILE)
		.setFileQueuedHandler(new FileQueuedHandler() {
			@Override
			public boolean onFileQueued(final FileQueuedEvent fileQueuedEvent) {
				String fileName = fileQueuedEvent.getFile().getName();
				//see if file name is legitimate
				if(!fileName.endsWith("Annotator.xml") && 
						!fileName.endsWith("Feature.xml")) {
					showFeedbackPanel("alert-danger", 
							"AE descriptor file name must end with 'Annotator.xml' or 'Feature.xml' by conventions.");
					//clear the queue
					uploader.cancelUpload(false);
					return false;
				}

				// Create a Progress Bar for this file
				progressBar.setTitle(fileName);
				progressBar.setHeight("22px");
				progressBar.setWidth("200px");

				// Add Cancel Button Image
				cancelButton.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						uploader.cancelUpload(fileQueuedEvent.getFile().getId(), false);
						progressBar.setProgress(-1.0d);
						cancelButton.removeFromParent();
					}
				});

				progressPanel.add(progressBar);
				progressPanel.add(cancelButton);

				hideFeedbackPanel();
				uploader.startUpload();

				return true;
			}
		}).setFileDialogStartHandler(new FileDialogStartHandler() {
			@Override
			public boolean onFileDialogStartEvent(FileDialogStartEvent fileDialogStartEvent) {
				if (uploader.getStats().getUploadsInProgress() <= 0) {
					progressBar.removeFromParent();
					cancelButton.removeFromParent();
				}
				return true;
			}
		}).setUploadProgressHandler(new UploadProgressHandler() {
			@Override
			public boolean onUploadProgress(UploadProgressEvent uploadProgressEvent) {
				progressBar.setProgress(
						(double) uploadProgressEvent.getBytesComplete() / uploadProgressEvent.getBytesTotal());
				return true;
			}
		}).setUploadSuccessHandler(new UploadSuccessHandler() {
			@Override
			public boolean onUploadSuccess(UploadSuccessEvent uploadSuccessEvent) {
				cancelButton.removeFromParent();
				return true;
			}
		}).setUploadCompleteHandler(new UploadCompleteHandler() {

			@Override
			public boolean onUploadComplete(UploadCompleteEvent uploadCompleteEvent) {
				if (uploader.getStats().getFilesQueued() <= 0) {
					// upload completed
					refreshAEList();
					hideSidePanels();
					showFeedbackPanel("alert-success", 
							"Uploaded AE descriptor: added new AE or updated AE descritpor!");
				}
				return false;
			}
		}).setFileDialogCompleteHandler(new FileDialogCompleteHandler() {
			@Override
			public boolean onFileDialogComplete(FileDialogCompleteEvent fileDialogCompleteEvent) {
				if (fileDialogCompleteEvent.getTotalFilesInQueue() > 0
						&& uploader.getStats().getUploadsInProgress() <= 0) {
					progressBar.setProgress(0.0);

					//									 uploader.startUpload();
				}
				return true;
			}
		}).setFileQueueErrorHandler(new FileQueueErrorHandler() {
			@Override
			public boolean onFileQueueError(FileQueueErrorEvent fileQueueErrorEvent) {
				progressBar.setProgress(0.0);
				cancelButton.removeFromParent();
				showFeedbackPanel("alert-danger",
						"Upload of file " + fileQueueErrorEvent.getFile().getName() + " failed due to ["
								+ fileQueueErrorEvent.getErrorCode().toString() + "]: "
								+ fileQueueErrorEvent.getMessage());
				return true;
			}
		}).setUploadErrorHandler(new UploadErrorHandler() {
			@Override
			public boolean onUploadError(UploadErrorEvent uploadErrorEvent) {
				progressBar.setProgress(0.0);
				cancelButton.removeFromParent();
				showFeedbackPanel("alert-danger",
						"Upload of file " + uploadErrorEvent.getFile().getName() + " failed due to ["
								+ uploadErrorEvent.getErrorCode().toString() + "]: "
								+ uploadErrorEvent.getMessage());
				return true;
			}
		});

		fileUploadPanelLeft.setWidget(uploader);
		fileUploadPanelRight.setWidget(progressPanel);
	}

	private void hideDetailPanel() {
		aeDetailPanel.setVisible(false);
		aeListPanel.setStyleName("col-lg-12");
	}

	private void hideSidePanels() {
		newAEPanel.setVisible(false);
		aeDetailPanel.setVisible(false);
		aeListPanel.setStyleName("col-lg-12");
	}


	@UiHandler("closeAEDetail")
	void onCloseAEDetailClick(ClickEvent e) {
		hideSidePanels();
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
	private void refreshAEList() {
		// get new row count
		Services.getAdminService().getAECount(new AsyncCallback<Long>() {
			@Override
			public void onFailure(Throwable caught) {
				// serious error, show error page
				Utils.showErrorPage(caught.getMessage());
			}

			@Override
			public void onSuccess(Long result) {
				aeList.setRowCount(result.intValue(), true);
			}
		});
		aeList.setVisibleRangeAndClearData(aeList.getVisibleRange(), true);

	}

	private void deleteAE(AnalysisEngine ae) {

		// delete ae when button clicked
		logger.finer("Requesting deleteAE service...");
		Services.getAdminService().deleteAE(ae, new AsyncCallback<Void>() {

			@Override
			public void onFailure(Throwable caught) {
				logger.severe("Caught service exception " + caught);
				Utils.showErrorPage(caught.getMessage());
			}

			@Override
			public void onSuccess(Void result) {
				logger.finer("Servie deleteAE returned successfully.");
				hideSidePanels();
				refreshAEList();
				showFeedbackPanel("alert-danger", "Analysis engine deleted!");
			}
		});
	}

	@UiHandler("newAE")
	void onNewAEClick(ClickEvent e) {
		logger.info("User clicked new AE button.");
		showNewAEPanel();

	}

	@UiHandler("selectAll")
	void onSelectAllClick(ClickEvent e) {
		for (AnalysisEngine ae : aeList.getVisibleItems()) {
			aeListSelectionModel.setSelected(ae, true);
		}
	}

	@UiHandler("deleteSelected")
	void onDeleteSelectedClick(ClickEvent e) {

		int selectedCount = getSelectedCount();
		if (selectedCount == 0) {
			showFeedbackPanel("alert-danger", "No analysis engine is selected for deletion!");
			return;
		}

		if (!Window.confirm("Do you really want to delete the " + selectedCount + " selected analysis engines? "
				+ "This operation will be irreversable!")) {
			return;
		}

		logger.finer("User clicked delete selected button.");
		for (AnalysisEngine ae : aeListSelectionModel.getSelectedSet()) {
			aeListSelectionModel.setSelected(ae, false);
			deleteAE(ae);
		}
	}

	@UiHandler("selectClear")
	void onSelectClearClick(ClickEvent e) {
		for (AnalysisEngine ae : aeList.getVisibleItems()) {
			aeListSelectionModel.setSelected(ae, false);
		}
	}

	@UiHandler("selectReverse")
	void onSelectReverseClick(ClickEvent e) {
		for (AnalysisEngine ae : aeList.getVisibleItems()) {
			aeListSelectionModel.setSelected(ae, !aeListSelectionModel.isSelected(ae));
		}
	}

	private int getSelectedCount() {
		int selectedCount = 0;
		for (AnalysisEngine ae : aeListSelectionModel.getSelectedSet()) {
			selectedCount++;
		}
		return selectedCount;
	}

	@UiHandler("nRecords")
	void onNRecordsClick(ClickEvent e) {
		logger.finer("User clicked nRecords button.");
		aeList.setVisibleRange(0, Integer.parseInt(nRecords.getSelectedValue()));
	}

	protected class CancelProgressBarTextFormatter extends ProgressBar.TextFormatter {
		@Override
		protected String getText(ProgressBar bar, double curProgress) {
			if (curProgress < 0) {
				return "Cancelled";
			}
			return ((int) (100 * bar.getPercent())) + "%";
		}
	}

	@UiHandler("importAE")
	void onImportAEClick(ClickEvent e) {
		logger.finer("User clicked importAE button. Requesting importAE service...");

		Services.getAdminService().importAE(new AsyncCallback<Void>() {
			@Override
			public void onFailure(Throwable caught) {
				logger.severe("Caught service exception: " + caught);
				Utils.showErrorPage(caught.getMessage());
			}

			@Override
			public void onSuccess(Void result) {
				logger.finer("Service importAE returned successfully.");
				refreshAEList();
				showFeedbackPanel("alert-success", "Imported AE descriptors from 'feature-uima' project. "
						+ "AEs added or updated successfully. ");
			}
		});
	}

	@UiHandler("closeNewAEPanel")
	void onCloseNewAEPanelClick(ClickEvent e) {
		hideSidePanels();
	}
}

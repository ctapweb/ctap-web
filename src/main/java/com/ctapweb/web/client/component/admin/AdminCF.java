package com.ctapweb.web.client.component.admin;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import com.ctapweb.web.client.service.Services;
import com.ctapweb.web.shared.AnalysisEngine;
import com.ctapweb.web.shared.ComplexityFeature;
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

public class AdminCF extends Composite {

	private static AdminCFUiBinder uiBinder = GWT.create(AdminCFUiBinder.class);

	interface AdminCFUiBinder extends UiBinder<Widget, AdminCF> {
	}

	//for the icon button in the cell table
	public interface IconButtonTemplate extends SafeHtmlTemplates {
		@Template("<em class=\"fa {0}\" ></em>")
		SafeHtml iconButtonInCellTable(String icon);

		//for buttons with icon labels
		@Template("<span class=\"btn-label\"><i class=\"fa {0}\"></i></span>{1}")
		SafeHtml labeledButton(String label, String text);

	}

	private static final IconButtonTemplate ICONBUTTONTEMPLATE = 
			GWT.create(IconButtonTemplate.class);

	@UiField Button newCF;
	@UiField Anchor selectAll;
	@UiField Anchor selectClear;
	@UiField Anchor selectReverse;
	@UiField Button deleteSelected;

	@UiField ListBox nRecords;
	@UiField HTMLPanel cfListPanel;

	@UiField HTMLPanel cfDetailPanel;
	@UiField TextBox createdOn;
	@UiField TextBox cfName;
	@UiField TextArea cfDescription;
	//	@UiField SimplePanel fileUploadPanelLeft;
	//	@UiField SimplePanel fileUploadPanelRight;
	//	@UiField TextBox aeDescriptorFile;
	@UiField Hidden cfIdHidden;
	@UiField Button saveCFDetail;
	@UiField Button closeCFDetail;

	//select ae controls
	@UiField ListBox aeSelected;
	@UiField ListBox aeAll;
	@UiField Button selectAE;
	@UiField Button removeAE;



	@UiField HTMLPanel feedbackPanel;
	@UiField InlineLabel feedbackLabel;
	@UiField Button closeFeedbackPanel;

	Logger logger = Logger.getLogger(AdminCF.class.getName());

	//selection model
	static final  MultiSelectionModel<ComplexityFeature> cfListSelectionModel = 
			new MultiSelectionModel<>(ComplexityFeature.KEY_PROVIDER);

	AsyncDataProvider<ComplexityFeature> cfDataProvider;

	//get user cookie
	private String userCookieValue = Cookies.getCookie(SharedProperties.USERCOOKIENAME);

	final CellTable<ComplexityFeature> cfList = 
			new CellTable<ComplexityFeature>(ComplexityFeature.KEY_PROVIDER);

	SimplePager pager;

	//for controlling the aes used in this cf
	//	List<AnalysisEngine> allAEList = new ArrayList<>();
	//	List<AnalysisEngine> selectedAEList = new ArrayList<>();

	//	//file uploader in the new ae and edit ae panel
	//	private final Uploader uploader = new Uploader();
	//	JSONObject formFields = new JSONObject(); //for passing additional info	

	public AdminCF() {
		initWidget(uiBinder.createAndBindUi(this));

		//inialize some widgets
		newCF.setHTML(ICONBUTTONTEMPLATE.labeledButton("fa-file", "New CF"));
		deleteSelected.setHTML(ICONBUTTONTEMPLATE.labeledButton("fa-trash", "Delete Selected"));
		selectAll.getElement().getStyle().setCursor(Cursor.POINTER);
		selectClear.getElement().getStyle().setCursor(Cursor.POINTER);
		selectReverse.getElement().getStyle().setCursor(Cursor.POINTER);

		//initiate the cell table
		cfList.setWidth("100%");

		//set the selection model, this multiselection model selects a record 
		//whenever a checkbox in the row is selected 
		cfList.setSelectionModel(cfListSelectionModel, 
				DefaultSelectionEventManager.<ComplexityFeature> createCheckboxManager());

		//initialize columns to the cell table
		initializeColumns(cfList);

		//RPC request to get corpus count
		Services.getAdminService().getCFCount(userCookieValue, 
				new AsyncCallback<Long>() {
			@Override
			public void onFailure(Throwable caught) {
				//serious error, show error page
				logger.warning("Can't get CF count, user not logged in (as admin) or database error.");
				Utils.showErrorPage(caught.getMessage());
			}

			@Override
			public void onSuccess(Long result) {
				cfList.setRowCount(result.intValue(), true);
			}
		});

		// list 10 items per page
		cfList.setVisibleRange(0, 10);

		//data provider
		cfDataProvider = new AsyncDataProvider<ComplexityFeature>() {
			@Override
			protected void onRangeChanged(HasData<ComplexityFeature> display) {
				final Range range = display.getVisibleRange();

				final int rangeStart = range.getStart();
				final int rangeLength = range.getLength();

				//an RPC request to get the CF list
				Services.getAdminService().getCFList(userCookieValue, rangeStart, rangeLength, 
						new AsyncCallback<List<ComplexityFeature>>() {

					@Override
					public void onSuccess(List<ComplexityFeature> result) {
						updateRowData(rangeStart, result);
					}

					@Override
					public void onFailure(Throwable caught) {
						Utils.showErrorPage(caught.getMessage());
					}
				});

			}
		};

		//connect the list to the data provider
		cfDataProvider.addDataDisplay(cfList);

		// Create paging controls.
		//		SimplePager pager = new SimplePager();
		pager = new SimplePager();
		pager.setDisplay(cfList);

		cfListPanel.add(cfList);
		cfListPanel.add(pager);
	}

	private void initializeColumns(CellTable<ComplexityFeature> cfList) {
		//the checkbox column
		Column<ComplexityFeature, Boolean> checkColumn = new Column<ComplexityFeature, Boolean>(
				new CheckboxCell(true, false)) {
			@Override
			public Boolean getValue(ComplexityFeature object) {
				// Get the value from the selection model.
				return cfListSelectionModel.isSelected(object);
			}
		};
		cfList.addColumn(checkColumn, "");
		cfList.setColumnWidth(checkColumn, "3%");

		//the id column
		TextColumn<ComplexityFeature> idColumn = new TextColumn<ComplexityFeature>() {
			@Override
			public String getValue(ComplexityFeature cf) {
				return cf.getId()+"";
			}
		};
		cfList.addColumn(idColumn, "ID");
		cfList.setColumnWidth(idColumn, "3%");

		//the name column
		TextColumn<ComplexityFeature> nameColumn = new TextColumn<ComplexityFeature>() {
			@Override
			public String getValue(ComplexityFeature cf) {
				return cf.getName();
			}
		};
		cfList.addColumn(nameColumn, "CF Name");

		//the description column
		TextColumn<ComplexityFeature> descriptionColumn = new TextColumn<ComplexityFeature>() {
			@Override
			public String getValue(ComplexityFeature cf) {
				String desc = cf.getDescription();

				if(desc.length() >  97) {
					desc = desc.substring(0, 97) + "...";
				}
				return desc;
			}
		};
		cfList.addColumn(descriptionColumn, "Description");
		cfList.setColumnWidth(descriptionColumn, "20%");

		// the AEs column
		TextColumn<ComplexityFeature> aesColumn = new TextColumn<ComplexityFeature>() {
			@Override
			public String getValue(ComplexityFeature cf) {
				String aes = "";
				for(AnalysisEngine ae: cf.getAeList()) {
					aes += "[" + ae.getName() + "] ";
				}
				return aes; 
			}
		};
		cfList.addColumn(aesColumn, "AEs");
		cfList.setColumnWidth(aesColumn, "40%");

		//the descriptor column
		//		TextColumn<ComplexityFeature> descriptorColumn = new TextColumn<ComplexityFeature>() {
		//			@Override
		//			public String getValue(ComplexityFeature ae) {
		//				String desc = ae.getDescriptorFile();
		//
		//				if(desc.length() >  97) {
		//					desc = desc.substring(0, 97) + "...";
		//				}
		//				return desc;
		//			}
		//		};
		//		cfList.addColumn(descriptorColumn, "Descriptor File");
		//		cfList.setColumnWidth(descriptorColumn, "30%");

		//the created date column
		TextColumn<ComplexityFeature> createdColumn = new TextColumn<ComplexityFeature>() {
			@Override
			public String getValue(ComplexityFeature cf) {
				return DateTimeFormat.getFormat("dd/MM/yy").format(cf.getCreateDate());
			}
		};
		cfList.addColumn(createdColumn, "Created On");
		cfList.setColumnWidth(createdColumn, "8%");

		//
		//		SafeHtml openIcon =
		//				ICONBUTTONTEMPLATE.iconButtonInCellTable("fa-folder-open-o");
		//		ActionCell<AnalysisEngine> openCell = new ActionCell<AnalysisEngine>(openIcon, new ActionCell.Delegate<AnalysisEngine>() {
		//			@Override
		//			public void execute(AnalysisEngine corpus) {
		//				//opens the corpus
		//				History.newItem(HistoryToken.textmanager + "?corpusID=" + corpus.getId());
		//			}
		//
		//		});
		//		Column<AnalysisEngine, AnalysisEngine> openColumn = 
		//				new Column<AnalysisEngine, AnalysisEngine>(openCell) {
		//
		//			@Override
		//			public AnalysisEngine getValue(AnalysisEngine corpus) {
		//				return corpus;
		//			}
		//		};
		//		aeList.addColumn(openColumn, "Open");
		//		aeList.setColumnWidth(openColumn, "3%");

		//the edit button column
		SafeHtml editIcon =
				ICONBUTTONTEMPLATE.iconButtonInCellTable("fa-pencil-square-o");
		ActionCell<ComplexityFeature> editCell = new ActionCell<>(editIcon, 
				new ActionCell.Delegate<ComplexityFeature>() {
			@Override
			public void execute(ComplexityFeature cf) {
//				Window.alert("clicked");
				showDetailPanel(cf);
			}
		});
		Column<ComplexityFeature, ComplexityFeature> editColumn = 
				new Column<ComplexityFeature, ComplexityFeature>(editCell) {

			@Override
			public ComplexityFeature getValue(ComplexityFeature cf) {
				return cf;
			}
		};
		cfList.addColumn(editColumn, "Edit");
		cfList.setColumnWidth(editColumn, "3%");

		//the delete button column
		SafeHtml deleteIcon =
				ICONBUTTONTEMPLATE.iconButtonInCellTable("fa-trash-o");
		ActionCell<ComplexityFeature> deleteCell = new ActionCell<>(deleteIcon, 
				new ActionCell.Delegate<ComplexityFeature>() {
			@Override
			public void execute(ComplexityFeature cf) {
				if(!Window.confirm("Do you really want to delete " + cf.getName())) {
					return;
				}
				deleteCF(cf);
			}
		});
		Column<ComplexityFeature, ComplexityFeature> deleteColumn = 
				new Column<ComplexityFeature, ComplexityFeature>(deleteCell) {

			@Override
			public ComplexityFeature getValue(ComplexityFeature cf) {
				return cf;
			}
		};
		cfList.addColumn(deleteColumn, "Delete");
		cfList.setColumnWidth(deleteColumn, "3%");


	}

	private void showDetailPanel(final ComplexityFeature cf) {
		feedbackPanel.setVisible(false);
		createdOn.setText(cf.getCreateDate().toString());
		cfName.setText(cf.getName());
		cfDescription.setText(cf.getDescription());
		cfIdHidden.setValue(cf.getId()+"");

		//clear aeSelected and aeAll
		aeSelected.clear();
		aeAll.clear();
		
		//get all ae list, first get ae count
		Services.getAdminService().getAECount(new AsyncCallback<Long>() {

			@Override
			public void onFailure(Throwable caught) {
				Utils.showErrorPage(caught.getMessage());
			}

			@Override
			public void onSuccess(Long result) {
				//get all AEs
				if(result != 0) {
					Services.getAdminService().getAEList(0, result.intValue(), 
							new AsyncCallback<List<AnalysisEngine>>() {

						@Override
						public void onFailure(Throwable caught) {
							Utils.showErrorPage(caught.getMessage());
						}

						@Override
						public void onSuccess(List<AnalysisEngine> result) {
							aeAll.clear();
							//populate the listbox
							for(AnalysisEngine ae: result) {
								//include only those that haven't been selected by the cf
								if(!cf.getAeList().contains(ae)) {
									aeAll.addItem(ae.getName(), ae.getId()+"");
								}
							}

						}
					});
				}
			}
		});

		//populate the selected ae list box
		for(AnalysisEngine ae: cf.getAeList()) {
			aeSelected.addItem(ae.getName(), ae.getId()+"");
		}

		cfListPanel.setStyleName("col-lg-7");
		cfDetailPanel.setVisible(true);
	}

	private void hideDetailPanel() {
		cfDetailPanel.setVisible(false);
		cfListPanel.setStyleName("col-lg-12");
	}

	@UiHandler("closeCFDetail")
	void onCloseCFDetailClick(ClickEvent e) {
		hideDetailPanel();
	}

	@UiHandler("saveCFDetail")
	void onSaveCFDetailClick(ClickEvent e) {
		ComplexityFeature cf = new ComplexityFeature();
		cf.setId(Integer.parseInt(cfIdHidden.getValue()));
		cf.setName(cfName.getText());
		cf.setDescription(cfDescription.getText());
		cf.setAeList(new ArrayList<AnalysisEngine>());

		//gets the selected aes and save them in the aelist
		for(int i = 0; i < aeSelected.getItemCount(); i++) {
			int aeID = Integer.parseInt(aeSelected.getValue(i));
			AnalysisEngine ae = new AnalysisEngine();
			ae.setId(aeID);
			cf.getAeList().add(ae);
		}

		//check if cf info is empty
		if(cfName.getText().isEmpty()) {
			showFeedbackPanel("alert-danger", "You need to enter a name "
					+ "for your complexity feature.");
			return;
		}

		//		//post params are sent as form fields
		//		formFields.put("userCookieValue", new JSONString(userCookieValue));
		//		formFields.put("aeID", new JSONString(cfIdHidden.getValue()+""));
		//		formFields.put("aeName", new JSONString(cfName.getText()));
		//		formFields.put("aeDescription", new JSONString(cfDescription.getText()));
		//		formFields.put("aeDescriptorFile", new JSONString(aeDescriptorFile.getText()));
		//		uploader.setPostParams(formFields);
		//
		//		uploader.startUpload();

		if(cf.getId() == -1) {
			//new CF
			Services.getAdminService().addCF(userCookieValue, 
					cf, new AsyncCallback<Void>() {

				@Override
				public void onFailure(Throwable caught) {
					Utils.showErrorPage(caught.getMessage());
				}

				@Override
				public void onSuccess(Void result) {
					hideDetailPanel();
					refreshCFList();
					showFeedbackPanel("alert-info", "New CF created!");
				}
			});
		} else {
			//update CF info
			Services.getAdminService().
			updateCF(userCookieValue, cf, new AsyncCallback<Void>() {

				@Override
				public void onFailure(Throwable caught) {
					Utils.showErrorPage(caught.getMessage());
				}

				@Override
				public void onSuccess(Void result) {
					hideDetailPanel();
					//refresh cell table
					refreshCFList();
					showFeedbackPanel("alert-info", "CF detail updated!");
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

	//refresh cell table
	private void refreshCFList() {
		//get new row count
		Services.getAdminService().getCFCount(userCookieValue, 
				new AsyncCallback<Long>() {
			@Override
			public void onFailure(Throwable caught) {
				//serious error, show error page
				logger.warning("Can't get CF count, user not logged in or database error.");
				Utils.showErrorPage(caught.getMessage());
			}

			@Override
			public void onSuccess(Long result) {
				cfList.setRowCount(result.intValue(), true);
			}
		});
		cfList.setVisibleRangeAndClearData(cfList.getVisibleRange(), true);

	}

	private void deleteCF(ComplexityFeature cf) {


		//delete ae when button clicked
		Services.getAdminService().deleteCF(userCookieValue, cf,
				new AsyncCallback<Void>() {

			@Override
			public void onFailure(Throwable caught) {
				Utils.showErrorPage(caught.getMessage());
			}

			@Override
			public void onSuccess(Void result) {
				hideDetailPanel();
				refreshCFList();
				showFeedbackPanel("alert-danger", "Analysis engine deleted!");
			}
		});
	}

	@UiHandler("newCF") 
	void onNewCFClick(ClickEvent e) {
		ComplexityFeature newCF = new ComplexityFeature();
		newCF.setId(-1); //-1 means the corpus is new
		newCF.setCreateDate(new Date());
		newCF.setName("");
		newCF.setDescription("");
		newCF.setAeList(new ArrayList<AnalysisEngine>());

		//		newCF.setDescriptorFile("");
		showDetailPanel(newCF);



	}

	@UiHandler("selectAll")
	void onSelectAllClick(ClickEvent e) {
		for(ComplexityFeature cf : cfList.getVisibleItems()) {
			cfListSelectionModel.setSelected(cf, true);
		}
	}

	@UiHandler("deleteSelected")
	void onDeleteSelectedClick(ClickEvent e) {

		int selectedCount  = getSelectedCount();
		if(selectedCount == 0) {
			showFeedbackPanel("alert-danger", "No analysis engine is selected for deletion!" );
			return;
		}

		if(!Window.confirm("Do you really want to delete the " + selectedCount + " selected analysis engines? "
				+ "This operation will be irreversable!")) {
			return;
		}

		for(ComplexityFeature ae: cfListSelectionModel.getSelectedSet()) {
			cfListSelectionModel.setSelected(ae, false);
			deleteCF(ae);
		}
	}

	@UiHandler("selectClear")
	void onSelectClearClick(ClickEvent e) {
		for(ComplexityFeature cf : cfList.getVisibleItems()) {
			cfListSelectionModel.setSelected(cf, false);
		}
	}

	@UiHandler("selectReverse")
	void onSelectReverseClick(ClickEvent e) {
		for(ComplexityFeature cf : cfList.getVisibleItems()) {
			cfListSelectionModel.setSelected(cf, 
					!cfListSelectionModel.isSelected(cf));
		}
	}

	private int getSelectedCount() {
		int selectedCount = 0;
		for( ComplexityFeature cf: cfListSelectionModel.getSelectedSet()) {
			selectedCount++;
		}
		return selectedCount;
	}

	@UiHandler("nRecords")
	void onNRecordsClick(ClickEvent e) {
		cfList.setVisibleRange(0, Integer.parseInt(nRecords.getSelectedValue()));
	}

	//	protected class CancelProgressBarTextFormatter extends ProgressBar.TextFormatter {  
	//		@Override  
	//		protected String getText(ProgressBar bar, double curProgress) {  
	//			if (curProgress < 0) {  
	//				return "Cancelled";  
	//			}  
	//			return ((int) (100 * bar.getPercent())) + "%";  
	//		}  
	//	}

	@UiHandler("selectAE")
	void onSelectAEClick(ClickEvent e) {
		//move ae from aeAll to aeSelected
		if(aeAll.getSelectedIndex() != -1) {
			aeSelected.addItem(aeAll.getSelectedItemText(), aeAll.getSelectedValue());
			aeAll.removeItem(aeAll.getSelectedIndex());
		}
	}

	@UiHandler("removeAE") 
	void onRemoveAEClick(ClickEvent e) {
		//move ae from aeSelected to aeAll
		if(aeSelected.getSelectedIndex() != -1) {
			aeAll.addItem(aeSelected.getSelectedItemText(), aeSelected.getSelectedValue());
			aeSelected.removeItem(aeSelected.getSelectedIndex());
		}
	}
}
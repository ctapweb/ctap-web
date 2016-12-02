package com.ctapweb.web.client.component;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import com.ctapweb.web.client.HistoryToken;
import com.ctapweb.web.client.service.Services;
import com.ctapweb.web.shared.Corpus;
import com.ctapweb.web.shared.CorpusFolder;
import com.ctapweb.web.shared.Utils;
import com.ctapweb.web.shared.exception.ResourceAlreadyExistsException;
import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.ClickableTextCell;
import com.google.gwt.cell.client.FieldUpdater;
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
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

public class CorpusManager extends Composite {

	private static CorpusManagerUiBinder uiBinder = GWT.create(CorpusManagerUiBinder.class);

	interface CorpusManagerUiBinder extends UiBinder<Widget, CorpusManager> {
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

	@UiField Button newCorpus;
	@UiField Anchor selectAll;
	@UiField Anchor selectClear;
	@UiField Anchor selectReverse;
	@UiField Button deleteSelected;

	@UiField ListBox nRecords;
	@UiField ListBox nFolderRecords;
	@UiField HTMLPanel corpusListPanel;
	@UiField HTMLPanel folderCellTablePanel;
	@UiField HTMLPanel corpusCellTablePanel;
	@UiField Button showAllCorpora;

	@UiField TextBox folderNameTextbox;
	@UiField Button addFolderBtn;

	@UiField HTMLPanel corpusDetailPanel;
	@UiField TextBox createdOn;
	@UiField TextBox corpusName;
	@UiField ListBox folderListBox;
	@UiField TextArea corpusDescription;
	@UiField Hidden corpusIdHidden;
	@UiField Button saveCorpusDetail;
	@UiField Button closeCorpusDetail;

	@UiField HTMLPanel sweetAlertPanel;
	@UiField HTMLPanel saErrorIcon;
	@UiField HTMLPanel saWarningIcon;
	@UiField HTMLPanel saInfoIcon;
	@UiField HTMLPanel saSuccessIcon;
	@UiField Label saMessage1;
	@UiField Label saMessage2;
	@UiField Button saOKBtn;

	Logger logger = Logger.getLogger(CorpusManager.class.getName());

	// selection model
	MultiSelectionModel<Corpus> corpusListSelectionModel;
	SingleSelectionModel<CorpusFolder> folderListSelectionModel;

	// data provider
	AsyncDataProvider<Corpus> corpusDataProvider;
	AsyncDataProvider<CorpusFolder> folderDataProvider;

	//cell tables
	CellTable<Corpus> corpusList; 
	CellTable<CorpusFolder> folderList;

	//pagers
	SimplePager pager;
	SimplePager folderPager;

	public CorpusManager() {
		logger.finer("Opening corpus manager page...");
		initWidget(uiBinder.createAndBindUi(this));

		// inialize some widgets
		newCorpus.setHTML(ICONBUTTONTEMPLATE.labeledButton("fa-file", "New Corpus"));
		deleteSelected.setHTML(ICONBUTTONTEMPLATE.labeledButton("fa-trash", "Delete Selected"));
		selectAll.getElement().getStyle().setCursor(Cursor.POINTER);
		selectClear.getElement().getStyle().setCursor(Cursor.POINTER);
		selectReverse.getElement().getStyle().setCursor(Cursor.POINTER);
		showAllCorpora.setHTML(ICONBUTTONTEMPLATE.labeledButton("fa-filter", "Show All"));
		folderNameTextbox.getElement().setAttribute("placeholder", "Folder name");

		showAllFolder();

		showAllCorpora();
	}

	private void initializeColumns(CellTable<Corpus> corpusList) {
		// the checkbox column
		Column<Corpus, Boolean> checkColumn = new Column<Corpus, Boolean>(new CheckboxCell(true, false)) {
			@Override
			public Boolean getValue(Corpus object) {
				// Get the value from the selection model.
				return corpusListSelectionModel.isSelected(object);
			}
		};
		corpusList.addColumn(checkColumn, "");
		corpusList.setColumnWidth(checkColumn, "3%");

		// the id column
		TextColumn<Corpus> idColumn = new TextColumn<Corpus>() {
			@Override
			public String getValue(Corpus corpus) {
				return corpus.getId() + "";
			}
		};
		corpusList.addColumn(idColumn, "ID");
		corpusList.setColumnWidth(idColumn, "3%");

		// the folder name column
		TextColumn<Corpus> folderColumn = new TextColumn<Corpus>() {
			@Override
			public String getValue(Corpus corpus) {
				return corpus.getFolderName();
			}
		};
		corpusList.addColumn(folderColumn, "Folder");

		// the corpus name column
		TextColumn<Corpus> nameColumn = new TextColumn<Corpus>() {
			@Override
			public String getValue(Corpus corpus) {
				return corpus.getName();
			}
		};
		corpusList.addColumn(nameColumn, "Corpus Name");

		// the corpus description column
		TextColumn<Corpus> descriptionColumn = new TextColumn<Corpus>() {
			@Override
			public String getValue(Corpus corpus) {
				String desc = corpus.getDescription();

				if (desc.length() > 97) {
					desc = desc.substring(0, 97) + "...";
				}
				return desc;
			}
		};
		corpusList.addColumn(descriptionColumn, "Description");
		corpusList.setColumnWidth(descriptionColumn, "30%");

		// the created date column
		TextColumn<Corpus> createdColumn = new TextColumn<Corpus>() {
			@Override
			public String getValue(Corpus corpus) {
				return DateTimeFormat.getFormat("dd/MM/yy").format(corpus.getCreateDate());
			}
		};
		corpusList.addColumn(createdColumn, "Created On");
		corpusList.setColumnWidth(createdColumn, "8%");

		SafeHtml openIcon = ICONBUTTONTEMPLATE.iconButtonInCellTable("fa-folder-open-o");
		ActionCell<Corpus> openCell = new ActionCell<Corpus>(openIcon, new ActionCell.Delegate<Corpus>() {
			@Override
			public void execute(Corpus corpus) {
				// opens the corpus
				logger.finer("User clicked 'open corpus' button.");
				History.newItem(HistoryToken.textmanager + "?corpusID=" + corpus.getId());
			}

		});
		Column<Corpus, Corpus> openColumn = new Column<Corpus, Corpus>(openCell) {

			@Override
			public Corpus getValue(Corpus corpus) {
				return corpus;
			}
		};
		corpusList.addColumn(openColumn, "Open");
		corpusList.setColumnWidth(openColumn, "3%");

		// the edit button column
		SafeHtml editIcon = ICONBUTTONTEMPLATE.iconButtonInCellTable("fa-pencil-square-o");
		ActionCell<Corpus> editCell = new ActionCell<>(editIcon, new ActionCell.Delegate<Corpus>() {
			@Override
			public void execute(Corpus corpus) {
				logger.finer("User clicked 'edit corpus' button, opening corpus details panel...");
				showCorpusDetailPanel(corpus);
			}
		});
		Column<Corpus, Corpus> editColumn = new Column<Corpus, Corpus>(editCell) {

			@Override
			public Corpus getValue(Corpus corpus) {
				return corpus;
			}
		};
		corpusList.addColumn(editColumn, "Edit");
		corpusList.setColumnWidth(editColumn, "3%");

		// the delete button column
		SafeHtml deleteIcon = ICONBUTTONTEMPLATE.iconButtonInCellTable("fa-trash-o");
		ActionCell<Corpus> deleteCell = new ActionCell<>(deleteIcon, new ActionCell.Delegate<Corpus>() {
			@Override
			public void execute(Corpus corpus) {
				if (!Window.confirm("Do you really want to delete " + corpus.getName())) {
					return;
				}
				logger.finer("User clicked 'delete corpus' button.");
				deleteCorpus(corpus);
			}
		});
		Column<Corpus, Corpus> deleteColumn = new Column<Corpus, Corpus>(deleteCell) {

			@Override
			public Corpus getValue(Corpus corpus) {
				return corpus;
			}
		};
		corpusList.addColumn(deleteColumn, "Delete");
		corpusList.setColumnWidth(deleteColumn, "3%");

	}

	private void initializeFolderListColumns(CellTable<CorpusFolder> folderList) {

		//folder name column
		Column<CorpusFolder, String> nameColumn = new Column<CorpusFolder, String>(new ClickableTextCell()) {
			@Override
			public String getValue(CorpusFolder corpusFolder) {
				return corpusFolder.getName();
			}
		};
		nameColumn.setFieldUpdater(new FieldUpdater<CorpusFolder, String>() {
			@Override
			public void update(int index, CorpusFolder corpusFolder, String value) {
				logger.finer("User clicked folder name, showing corpora in the folder...");
				showCorporaInSelectedFolder();
			}
		});
		folderList.addColumn(nameColumn, "Name");

		// the number of corpora column
		TextColumn<CorpusFolder> numColumn = new TextColumn<CorpusFolder>() {
			@Override
			public String getValue(CorpusFolder corpusFolder) {
				return corpusFolder.getNumCorpora() + "";
			}
		};
		folderList.addColumn(numColumn, "Corpora");
		folderList.setColumnWidth(numColumn, "3%");


		// the delete button column
		SafeHtml deleteIcon = ICONBUTTONTEMPLATE.iconButtonInCellTable("fa-trash-o");
		ActionCell<CorpusFolder> deleteCell = new ActionCell<>(deleteIcon, 
				new ActionCell.Delegate<CorpusFolder>() {
			@Override
			public void execute(CorpusFolder corpusFolder) {
				if (!Window.confirm("Do you really want to delete the " + corpusFolder.getName() 
				+ " folder and all its containing corpora.")) {
					return;
				}

				logger.finer("User clicked delete folder button, deleting folder..." );
				deleteFolder(corpusFolder);
			}
		});
		Column<CorpusFolder, CorpusFolder> deleteColumn = new Column<CorpusFolder, CorpusFolder>(deleteCell) {

			@Override
			public CorpusFolder getValue(CorpusFolder corpusFolder) {
				return corpusFolder;
			}
		};
		folderList.addColumn(deleteColumn, "Delete");
		folderList.setColumnWidth(deleteColumn, "3%");
	}

	private void showCorporaInSelectedFolder() {
		final long folderID = folderListSelectionModel.getSelectedObject().getId();

		corpusListSelectionModel = new MultiSelectionModel<>(Corpus.KEY_PROVIDER);
		corpusList = new CellTable<Corpus>(Corpus.KEY_PROVIDER);
		corpusList.setWidth("100%");
		corpusList.setSelectionModel(corpusListSelectionModel,
				DefaultSelectionEventManager.<Corpus> createCheckboxManager());

		initializeColumns(corpusList);

		// RPC request to get corpus count
		logger.finer("Requesting service getCorpusCount with folderID=" + folderID + "...");
		Services.getCorpusManagerService().getCorpusCount(folderID, 
				new AsyncCallback<Integer>() {
			@Override
			public void onFailure(Throwable caught) {
				// serious error, show error page
				logger.severe("Caught server exception " + caught);
				Utils.showErrorPage(caught.getMessage());
			}

			@Override
			public void onSuccess(Integer result) {
				logger.finer("Service returned number of corpora in folder: " + result +".");
				corpusList.setRowCount(result, true);
			}
		});

		//initial items per page 
		corpusList.setVisibleRange(0, Integer.parseInt(nRecords.getSelectedValue()));

		corpusDataProvider = new AsyncDataProvider<Corpus>() {
			@Override
			protected void onRangeChanged(HasData<Corpus> display) {
				final Range range = display.getVisibleRange();

				final int rangeStart = range.getStart();
				final int rangeLength = range.getLength();

				// an RPC request to get the corpus data
				logger.finer("Corpus data provider requesting service getCorpusList...");
				Services.getCorpusManagerService().getCorpusList(folderID, rangeStart, rangeLength,
						new AsyncCallback<List<Corpus>>() {

					@Override
					public void onSuccess(List<Corpus> result) {
						logger.finer("Service returned corpus list successfully. Updating corpus list...");
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

		corpusDataProvider.addDataDisplay(corpusList);

		pager = new SimplePager();
		pager.setDisplay(corpusList);

		corpusCellTablePanel.clear();     
		corpusCellTablePanel.add(corpusList);
		corpusCellTablePanel.add(pager);

	}

	private void showCorpusDetailPanel(Corpus corpus) {
		//		feedbackPanel.setVisible(false);
		createdOn.setText(corpus.getCreateDate().toString());
		corpusName.setText(corpus.getName());
		corpusDescription.setText(corpus.getDescription());
		corpusIdHidden.setValue(corpus.getId() + "");
		final long folderID = corpus.getFolderID();

		corpusListPanel.setStyleName("col-lg-9");
		corpusDetailPanel.setVisible(true);

		//get folder list
		logger.finer("Corpus details panel requesting service getCorpusFolderCount...");
		Services.getCorpusManagerService().getCorpusFolderCount(new AsyncCallback<Integer>() {

			@Override
			public void onFailure(Throwable caught) {
				logger.severe("Caught service exception "  + caught);
				Utils.showErrorPage(caught.getMessage());
			}

			@Override
			public void onSuccess(Integer result) {
				logger.finer("getCorpusFolderCount returned results successfully, "
						+ "requesting getCorpusFolderList service...");
				Services.getCorpusManagerService().getCorpusFolderList(0, result, 
						new AsyncCallback<List<CorpusFolder>>() {

					@Override
					public void onFailure(Throwable caught) {
						logger.severe("Caught service exception " + caught);
						Utils.showErrorPage(caught.getMessage());
					}

					@Override
					public void onSuccess(List<CorpusFolder> result) {
						logger.finer("getCorpusFolderList service returned successfully, setting folder list box...");
						folderListBox.clear();
						folderListBox.addItem("Select a folder...", "0");
						folderListBox.setSelectedIndex(0);

						int i = 1;
						for(CorpusFolder corpusFolder: result) {
							folderListBox.addItem(corpusFolder.getName(), corpusFolder.getId()+"");

							//set folder selected
							if(folderID == corpusFolder.getId()) {
								folderListBox.setSelectedIndex(i);
							}
							i++;
						}
					}
				});
			}
		});

	}

	private void hideCorpusDetailPanel() {
		corpusDetailPanel.setVisible(false);
		corpusListPanel.setStyleName("col-lg-12");
	}

	@UiHandler("closeCorpusDetail")
	void onCloseCorpusDetailClick(ClickEvent e) {
		hideCorpusDetailPanel();
	}

	@UiHandler("saveCorpusDetail")
	void onSaveCorpusDetailClick(ClickEvent e) {
		logger.finer("User clicked save corpus details button, doing the action...");
		Corpus corpus = new Corpus();
		corpus.setId(Long.parseLong(corpusIdHidden.getValue()));
		corpus.setFolderID(Long.parseLong(folderListBox.getSelectedValue()));
		corpus.setName(corpusName.getText());
		corpus.setDescription(corpusDescription.getText());

		// check if corpus info is empty
		if (corpus.getName().isEmpty()) {
			showFeedbackPanel(FeedbackType.ERROR, "You need to enter a name for your corpus.", "");
			return;
		}

		if (corpus.getId() == -1) {
			// new corpus
			logger.finer("Requesting addCorpus service...");
			Services.getCorpusManagerService().addCorpus(corpus, new AsyncCallback<Void>() {

				@Override
				public void onFailure(Throwable caught) {
					logger.severe("Caught service exception " + caught);
					Utils.showErrorPage(caught.getMessage());
				}

				@Override
				public void onSuccess(Void result) {
					logger.finer("addCorpus service returned successfully. New corpus created!");
					hideCorpusDetailPanel();
					refreshLists();
					showFeedbackPanel(FeedbackType.SUCCESS, "New corpus created!", "");
				}
			});
		} else {
			// update corpus info
			logger.fine("Requesting service updateCorpus...");
			Services.getCorpusManagerService().updateCorpus(corpus, new AsyncCallback<Void>() {

				@Override
				public void onFailure(Throwable caught) {
					logger.severe("Caught service exception " + caught);
					Utils.showErrorPage(caught.getMessage());
				}

				@Override
				public void onSuccess(Void result) {
					logger.finer("updateCorpus service returned successfully. Corpus details updated!");
					hideCorpusDetailPanel();
					// refresh cell table
					refreshLists();
					showFeedbackPanel(FeedbackType.SUCCESS, "Corpus details updated!", "");
				}
			});
		}

	}

	private void showFeedbackPanel(FeedbackType feedbackType, String message1, String message2) {
		//set messages
		saMessage1.setText(message1);
		saMessage2.setText(message2);
		switch (feedbackType) {
		case ERROR:
			saErrorIcon.setVisible(true);
			saInfoIcon.setVisible(false);
			saSuccessIcon.setVisible(false);
			saWarningIcon.setVisible(false);
			break;
		case INFO:
			saErrorIcon.setVisible(false);
			saInfoIcon.setVisible(true);
			saSuccessIcon.setVisible(false);
			saWarningIcon.setVisible(false);
			break;
		case SUCCESS:
			saErrorIcon.setVisible(false);
			saInfoIcon.setVisible(false);
			saSuccessIcon.setVisible(true);
			saWarningIcon.setVisible(false);
			break;
		case WARNING:
			saErrorIcon.setVisible(false);
			saInfoIcon.setVisible(false);
			saSuccessIcon.setVisible(false);
			saWarningIcon.setVisible(true);
			break;
		}


		sweetAlertPanel.setVisible(true);

	}
	private enum FeedbackType {
		ERROR, WARNING, INFO, SUCCESS
	}

	//	private void hideFeedbackPanel() {
	//		feedbackPanel.setVisible(false);
	//	}

	//	@UiHandler("closeFeedbackPanel")
	//	void onCloseFeedbackPanelClick(ClickEvent e) {
	//		hideFeedbackPanel();
	//	}

	// refresh corpus list cell table
	private void refreshLists() {
		logger.finer("Refreshing corpus and folder lists...");
		showAllFolder();
		showAllCorpora();
	}

	private void deleteCorpus(Corpus corpus) {
		// delete corpus when button clicked
		logger.fine("Requesting service deleteCorpus...");
		Services.getCorpusManagerService().deleteCorpus(corpus, new AsyncCallback<Void>() {

			@Override
			public void onFailure(Throwable caught) {
				logger.severe("Caught service exception " + caught);
				Utils.showErrorPage(caught.getMessage());
			}

			@Override
			public void onSuccess(Void result) {
				logger.fine("deleteCorpus service returned successfully.");
				hideCorpusDetailPanel();
				refreshLists();
				showFeedbackPanel(FeedbackType.ERROR, "Corpus deleted!", "");
			}
		});
	}

	private void deleteFolder(final CorpusFolder corpusFolder) {

		// delete corpus folder when button clicked
		logger.finer("Requesting service deleteCorpusFolder...");
		Services.getCorpusManagerService().deleteCorpusFolder(corpusFolder, 
				new AsyncCallback<Void>() {

			@Override
			public void onFailure(Throwable caught) {
				logger.finer("Caught service exception "  + caught);
				Utils.showErrorPage(caught.getMessage());
			}

			@Override
			public void onSuccess(Void result) {
				logger.finer("Service finished successfully.");
				hideCorpusDetailPanel();
				refreshLists();
				showFeedbackPanel(FeedbackType.ERROR, "Corpus folder " + corpusFolder.getName()
				+ " and containing corpora deleted!", "");
			}
		});
	}

	@UiHandler("newCorpus")
	void onNewCorpusClick(ClickEvent e) {
		logger.finer("User clicked new corpus button, showing corpus details panel...");
		Corpus newCorpus = new Corpus();
		newCorpus.setId(-1); // -1 means the corpus is new
		newCorpus.setCreateDate(new Date());
		newCorpus.setFolderID(0);
		newCorpus.setName("");
		newCorpus.setDescription("");
		showCorpusDetailPanel(newCorpus);
	}

	@UiHandler("selectAll")
	void onSelectAllClick(ClickEvent e) {
		for (Corpus corpus : corpusList.getVisibleItems()) {
			corpusListSelectionModel.setSelected(corpus, true);
		}
	}

	@UiHandler("deleteSelected")
	void onDeleteSelectedClick(ClickEvent e) {
		logger.finer("User clicked deleted selected button, doing the action...");

		Set<Corpus> selectedCorpora = corpusListSelectionModel.getSelectedSet();
		int selectedCount;

		if(selectedCorpora == null || selectedCorpora.size() == 0) {
			//no items selected
			showFeedbackPanel(FeedbackType.WARNING, "No corpus is selected for deletion!", "");
			return;
		} else {
			selectedCount = selectedCorpora.size();
		}

		if (!Window.confirm("Do you really want to delete the " + selectedCount + " selected copora? "
				+ "This operation will be irreversable!")) {
			return;
		}

		for (Corpus corpus : corpusListSelectionModel.getSelectedSet()) {
			corpusListSelectionModel.setSelected(corpus, false);
			deleteCorpus(corpus);
		}
	}

	@UiHandler("selectClear")
	void onSelectClearClick(ClickEvent e) {
		for (Corpus corpus : corpusList.getVisibleItems()) {
			corpusListSelectionModel.setSelected(corpus, false);
		}
	}

	@UiHandler("selectReverse")
	void onSelectReverseClick(ClickEvent e) {
		for (Corpus corpus : corpusList.getVisibleItems()) {
			corpusListSelectionModel.setSelected(corpus, !corpusListSelectionModel.isSelected(corpus));
		}
	}

	@UiHandler("nRecords")
	void onNRecordsClick(ClickEvent e) {
		logger.finer("User clicked nRecords listbox, changing number of items listed...");
		corpusList.setVisibleRange(0, Integer.parseInt(nRecords.getSelectedValue()));
	}

	@UiHandler("nFolderRecords")
	void onNFolderRecordsClick(ClickEvent e) {
		logger.finer("User clicked nFolderRecords listbox, changing number of items listed...");
		folderList.setVisibleRange(0, Integer.parseInt(nFolderRecords.getSelectedValue()));
	}

	@UiHandler("addFolderBtn")
	void onAddFolderBtnClick(ClickEvent e) {
		logger.finer("User clicked add folder button, adding new folder...");
		final String folderName = folderNameTextbox.getText();

		//check if it is empty
		if(folderName.isEmpty()) {
			showFeedbackPanel(FeedbackType.ERROR, "Folder name can't be empty!", "");
			return;
		}

		//rpc call to add the new folder
		logger.finer("Requesting service addCorpusFolder...");
		Services.getCorpusManagerService().addCorpusFolder(folderName, new AsyncCallback<Void>() {
			@Override
			public void onFailure(Throwable caught) {
				logger.severe("Caught service exception " + caught);
				if (caught instanceof ResourceAlreadyExistsException) {
					//folder name already exists
					ResourceAlreadyExistsException e = (ResourceAlreadyExistsException) caught;
					showFeedbackPanel(FeedbackType.ERROR, "Folder names conflict!", 
							"A folder with the same name already exists in the database. Please choose a different name.");
				} else {
					Utils.showErrorPage(caught.getMessage());
				}
			}

			@Override
			public void onSuccess(Void result) {
				logger.finer("Service returned successfully.");
				//refresh folder list
				folderNameTextbox.setText("");
				showFeedbackPanel(FeedbackType.SUCCESS, "New folder created!", "");
				refreshLists();
			}
		});
	}

	private void showAllCorpora() {
		logger.finer("Setting up display for corpora list...");
		corpusListSelectionModel = new MultiSelectionModel<>(Corpus.KEY_PROVIDER);
		corpusList = new CellTable<Corpus>(Corpus.KEY_PROVIDER);
		corpusList.setWidth("100%");
		corpusList.setSelectionModel(corpusListSelectionModel,
				DefaultSelectionEventManager.<Corpus> createCheckboxManager());

		initializeColumns(corpusList);

		// RPC request to get corpus count
		logger.finer("Requesting service getCorpusCount...");
		Services.getCorpusManagerService().getCorpusCount(new AsyncCallback<Integer>() {
			@Override
			public void onFailure(Throwable caught) {
				// serious error, show error page
				logger.severe("Caught service exception " + caught);
				Utils.showErrorPage(caught.getMessage());
			}

			@Override
			public void onSuccess(Integer result) {
				logger.finer("Service returned result successfully, setting corpus list row count...");
				corpusList.setRowCount(result, true);
			}
		});

		//initial items per page 
		corpusList.setVisibleRange(0, Integer.parseInt(nRecords.getSelectedValue()));

		corpusDataProvider = new AsyncDataProvider<Corpus>() {
			@Override
			protected void onRangeChanged(HasData<Corpus> display) {
				final Range range = display.getVisibleRange();

				final int rangeStart = range.getStart();
				final int rangeLength = range.getLength();

				// an RPC request to get the corpus data
				logger.finer("Requesting service getCorpusList from corpusDataProvider...");
				Services.getCorpusManagerService().getCorpusList(rangeStart, rangeLength,
						new AsyncCallback<List<Corpus>>() {

					@Override
					public void onSuccess(List<Corpus> result) {
						logger.finer("Service returned result successfully, setting corpus list row data...");
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

		corpusDataProvider.addDataDisplay(corpusList);
		pager = new SimplePager();
		pager.setDisplay(corpusList);

		corpusCellTablePanel.clear();     
		corpusCellTablePanel.add(corpusList);
		corpusCellTablePanel.add(pager);

	}

	@UiHandler("showAllCorpora")
	void onShowAllCorporaClick(ClickEvent e) {
		logger.finer("User clicked 'Show All Corpora' button, displaying all corpora...");
		showAllCorpora();

		//clear folder selection
		folderListSelectionModel.clear();
	}

	private void showAllFolder() {
		logger.finer("Setting up corpus folder display...");
		folderNameTextbox.setText("");

		folderListSelectionModel = 
				new SingleSelectionModel<>(CorpusFolder.KEY_PROVIDER);
		folderListSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {

			@Override
			public void onSelectionChange(SelectionChangeEvent event) {
				String selectedFolderName = folderListSelectionModel.getSelectedObject().getName();
				folderNameTextbox.setText(selectedFolderName);
			}
		} );


		folderList = new CellTable<CorpusFolder>(CorpusFolder.KEY_PROVIDER);
		folderList.setWidth("100%");
		folderList.setSelectionModel(folderListSelectionModel);

		initializeFolderListColumns(folderList);

		//rpc request to get folder count
		logger.finer("Requesting serivce getCorpusFolderCount...");
		Services.getCorpusManagerService().getCorpusFolderCount(new AsyncCallback<Integer>() {

			@Override
			public void onFailure(Throwable caught) {
				logger.severe("Caught service exception " + caught);
				Utils.showErrorPage(caught.getMessage());
			}

			@Override
			public void onSuccess(Integer result) {
				logger.finer("Service returned result successfully, setting folder list row count...");
				folderList.setRowCount(result, true);
			}
		});

		folderList.setVisibleRange(0, Integer.parseInt(nFolderRecords.getSelectedValue()));

		folderDataProvider = new AsyncDataProvider<CorpusFolder>() {
			@Override
			protected void onRangeChanged(HasData<CorpusFolder> display) {
				final Range range = display.getVisibleRange();

				final int rangeStart = range.getStart();
				final int rangeLength = range.getLength();

				// an RPC request to get the folder data
				logger.finer("Requesting service getCorpusFolderList...");
				Services.getCorpusManagerService().getCorpusFolderList(rangeStart, rangeLength, 
						new AsyncCallback<List<CorpusFolder>>() {

					@Override
					public void onFailure(Throwable caught) {
						logger.severe("Caught service exception " + caught);
						Utils.showErrorPage(caught.getMessage());
					}

					@Override
					public void onSuccess(List<CorpusFolder> result) {
						logger.finer("Service returned result successfully, setting folder list row data...");
						updateRowData(rangeStart, result);
					}
				});
			}
		};

		folderDataProvider.addDataDisplay(folderList);
		folderPager = new SimplePager();
		folderPager.setDisplay(folderList);

		folderCellTablePanel.clear();
		folderCellTablePanel.add(folderList);
		folderCellTablePanel.add(folderPager);
	}

	@UiHandler("saOKBtn")
	void onSAOKBtnClick(ClickEvent e) {
		sweetAlertPanel.setVisible(false);
	}

	@UiHandler("renameFolderBtn")
	void onRenameFolderBtnClick(ClickEvent e) {
		logger.finer("User clicked 'Rename Folder' button, doing rename action...");
		//prepare the corpus folder object
		long folderID = folderListSelectionModel.getSelectedObject().getId();
		final String folderName = folderNameTextbox.getText();
		final CorpusFolder corpusFolder = new CorpusFolder();
		corpusFolder.setId(folderID);
		corpusFolder.setName(folderName);

		//check if foldername is empty
		if(folderName.isEmpty()) {
			showFeedbackPanel(FeedbackType.ERROR, "Folder name can't be empty!", "");
			return;
		}

		//rpc call to update the folder name
		logger.finer("Requesting service updateCorpusFolder...");
		Services.getCorpusManagerService().updateCorpusFolder(corpusFolder, 
				new AsyncCallback<Void>() {
			@Override
			public void onFailure(Throwable caught) {
				logger.finer("Caught service exception " + caught);
				if (caught instanceof ResourceAlreadyExistsException) {
					ResourceAlreadyExistsException e = (ResourceAlreadyExistsException) caught;
					showFeedbackPanel(FeedbackType.ERROR, e.getMessage(), "");
				}
				Utils.showErrorPage(caught.getMessage());
			}
			@Override
			public void onSuccess(Void result) {
				logger.finer("Service returned successfully.");
				//refresh folder list
				showFeedbackPanel(FeedbackType.SUCCESS, "Folder name  updated!", "");
				refreshLists();
			}
		});
	}
}

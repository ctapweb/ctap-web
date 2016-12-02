package com.ctapweb.web.client.component;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

import com.ctapweb.web.client.service.Services;
import com.ctapweb.web.shared.CorpusText;
import com.ctapweb.web.shared.Tag;
import com.ctapweb.web.shared.Utils;
import com.ctapweb.web.shared.exception.ResourceAlreadyExistsException;
import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.ClickableTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DragLeaveEvent;
import com.google.gwt.event.dom.client.DragLeaveHandler;
import com.google.gwt.event.dom.client.DragOverEvent;
import com.google.gwt.event.dom.client.DragOverHandler;
import com.google.gwt.event.dom.client.DropEvent;
import com.google.gwt.event.dom.client.DropHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
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
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.gwt.widgetideas.client.ProgressBar;

public class TextManager extends Composite {

	private static TextManagerUiBinder uiBinder = GWT.create(TextManagerUiBinder.class);

	interface TextManagerUiBinder extends UiBinder<Widget, TextManager> {
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

	Logger logger = Logger.getLogger(TextManager.class.getName());

	// selection models
	MultiSelectionModel<CorpusText> textListSelectionModel;
	SingleSelectionModel<Tag> tagListSelectionModel;

	// data providers
	AsyncDataProvider<CorpusText> textDataProvider;
	AsyncDataProvider<Tag> tagDataProvider;

	// cell tables
	CellTable<CorpusText> textList;
	CellTable<Tag> tagList;

	SimplePager pager;
	SimplePager tagPager;

	long corpusID;
	
	//an indicator for the label widget that is used as a drop file area
	boolean labelHasHandler = false;

	@UiField InlineLabel corpusName;

	@UiField Button newText;
	@UiField Button importTexts;
	@UiField Anchor exportIntoOneFile;
	@UiField Anchor exportIntoMultipleFiles;
	@UiField Anchor selectAll;
	@UiField Anchor selectClear;
	@UiField Anchor selectReverse;
	@UiField Button deleteSelected;
	@UiField Button showAllText;

	@UiField ListBox nRecords;
	@UiField HTMLPanel textListPanel;
	@UiField HTMLPanel textCellTablePanel;

	@UiField ListBox nTagRecords;
	@UiField HTMLPanel tagCellTablePanel;
	@UiField TextBox tagNameTextbox;
	@UiField Button addTagBtn;
	@UiField Button renameTagBtn;
	@UiField Button tagSelectedBtn;

	@UiField HTMLPanel textDetailPanel;
	@UiField TextBox createdOn;
	@UiField TextBox textTitle;
	@UiField ListBox tagListBox;
	@UiField TextArea textContent;
	@UiField Hidden textIdHidden;
	@UiField Button saveDetail;
	@UiField Button closeDetail;

	@UiField HTMLPanel importPanel;
	@UiField ListBox tagListBoxImport;
	@UiField SimplePanel fileUploadPanelLeft;
	@UiField SimplePanel fileUploadPanelRight;
	@UiField Label dropFiles;
	@UiField Label statsSelected;
	@UiField Label statsUploaded;
	@UiField Label statsRemaining;
	@UiField Label statsCancelled;
	@UiField Label statsError;
	@UiField Button closeImportPanel;

	@UiField HTMLPanel sweetAlertPanel;
	@UiField HTMLPanel saErrorIcon;
	@UiField HTMLPanel saWarningIcon;
	@UiField HTMLPanel saInfoIcon;
	@UiField HTMLPanel saSuccessIcon;
	@UiField Label saMessage1;
	@UiField Label saMessage2;
	@UiField Button saOKBtn;

	public TextManager() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	public TextManager(final long corpusID) {
		logger.finer("Opening text manager page...");
		initWidget(uiBinder.createAndBindUi(this));

		this.corpusID = corpusID;

		// get corpus name
		logger.finer("Requesting service getCorpusName...");
		Services.getCorpusManagerService().getCorpusName(corpusID, new AsyncCallback<String>() {

			@Override
			public void onSuccess(String result) {
				logger.finer("getCorpusName returned successfully, setting corpus name on page head...");
				corpusName.setText(result);
			}

			@Override
			public void onFailure(Throwable caught) {
				logger.severe("Caught service exception " + caught);
				Utils.showErrorPage(caught.getMessage());
			}
		});

		// initialize some widgets
		newText.setHTML(ICONBUTTONTEMPLATE.labeledButton("fa-file-text", "New Text"));
		importTexts.setHTML(ICONBUTTONTEMPLATE.labeledButton("fa-database", "Import..."));
		exportIntoOneFile.getElement().getStyle().setCursor(Cursor.POINTER);
		exportIntoMultipleFiles.getElement().getStyle().setCursor(Cursor.POINTER);
		deleteSelected.setHTML(ICONBUTTONTEMPLATE.labeledButton("fa-trash", "Delete Selected"));
		selectAll.getElement().getStyle().setCursor(Cursor.POINTER);
		selectClear.getElement().getStyle().setCursor(Cursor.POINTER);
		selectReverse.getElement().getStyle().setCursor(Cursor.POINTER);
		showAllText.setHTML(ICONBUTTONTEMPLATE.labeledButton("fa-filter", "Show All"));

		showAllTags();
		showAllTexts();

	}

	@UiHandler("tagSelectedBtn")
	void onTagSelectedBtnClick(ClickEvent e) {
		Tag selectedTag = tagListSelectionModel.getSelectedObject();
		Set<CorpusText> selectedTexts = textListSelectionModel.getSelectedSet();

		//check if selected a tag and some texts
		if(selectedTag == null || selectedTexts == null) {
			showFeedbackPanel(FeedbackType.WARNING, 
					"Please select a tag from the left panel and the texts you want to tag from the right panel!", "");
			return;
		}

		//tag selected  texts
		for(CorpusText text: selectedTexts) {
			Services.getCorpusManagerService().tagText(text, selectedTag, 
					new AsyncCallback<Void>() {

				@Override
				public void onFailure(Throwable caught) {
					Utils.showErrorPage(caught.getMessage());
				}

				@Override
				public void onSuccess(Void result) {
					showFeedbackPanel(FeedbackType.SUCCESS, "Tagged selected texts!", "");
					refreshLists();
				}
			});
		}
	}

	@UiHandler("showAllText")
	void onShowAllTextClick(ClickEvent e) {
		logger.finer("User clicked show all text button.");
		showAllTexts();
	}

	private void showAllTexts() {
		logger.finer("Setting up text display...");
		textListSelectionModel = new MultiSelectionModel<>(
				CorpusText.KEY_PROVIDER);

		textList = new CellTable<CorpusText>(CorpusText.KEY_PROVIDER);

		textList.setWidth("100%");

		textList.setSelectionModel(textListSelectionModel,
				DefaultSelectionEventManager.<CorpusText> createCheckboxManager());

		initializeColumns(textList);

		// RPC request to get text count
		logger.finer("Requesting service getTextCount...");
		Services.getCorpusManagerService().getTextCount(corpusID, new AsyncCallback<Integer>() {
			@Override
			public void onFailure(Throwable caught) {
				// serious error, show error page
				logger.severe("Caught service exception " + caught);
				Utils.showErrorPage(caught.getMessage());
			}

			@Override
			public void onSuccess(Integer result) {
				logger.finer("getTextCount returned successfully, updating row count...");
				textList.setRowCount(result, true);
			}
		});

		textList.setVisibleRange(0, Integer.parseInt(nRecords.getSelectedValue()));

		textDataProvider = new AsyncDataProvider<CorpusText>() {
			@Override
			protected void onRangeChanged(HasData<CorpusText> display) {
				final Range range = display.getVisibleRange();

				final int rangeStart = range.getStart();
				final int rangeLength = range.getLength();

				// an RPC request to get the text data
				logger.finer("Requesting service getTextList from within textDataProvider...");
				Services.getCorpusManagerService().getTextList(corpusID, rangeStart, rangeLength,
						new AsyncCallback<List<CorpusText>>() {

					@Override
					public void onSuccess(List<CorpusText> result) {
						logger.finer("getTextList returned successfully, updating row data...");
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

		// connect the list to the data provider
		textDataProvider.addDataDisplay(textList);

		// Create paging controls.
		pager = new SimplePager();
		pager.setDisplay(textList);

		textCellTablePanel.clear();     
		textCellTablePanel.add(textList);
		textCellTablePanel.add(pager);
	}

	private void showAllTags() {
		logger.finer("Setting up tag display...");
		tagNameTextbox.setText("");

		tagListSelectionModel = 
				new SingleSelectionModel<>(Tag.KEY_PROVIDER);
		tagListSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {

			@Override
			public void onSelectionChange(SelectionChangeEvent event) {
				String selectedTagName = tagListSelectionModel.getSelectedObject().getName();
				tagNameTextbox.setText(selectedTagName);
			}
		} );


		tagList = new CellTable<Tag>(Tag.KEY_PROVIDER);
		tagList.setWidth("100%");
		tagList.setSelectionModel(tagListSelectionModel);

		initializeTagListColumns(tagList);

		//rpc request to get tag count
		logger.finer("Requesting service getTagCount...");
		Services.getCorpusManagerService().getTagCount(new AsyncCallback<Integer>() {

			@Override
			public void onFailure(Throwable caught) {
				logger.severe("Caught service exception " + caught);
				Utils.showErrorPage(caught.getMessage());
			}

			@Override
			public void onSuccess(Integer result) {
				logger.finer("getTagCount returned successfully, setting tag list row count...");
				tagList.setRowCount(result, true);
			}
		});

		tagList.setVisibleRange(0, Integer.parseInt(nTagRecords.getSelectedValue()));

		tagDataProvider = new AsyncDataProvider<Tag>() {
			@Override
			protected void onRangeChanged(HasData<Tag> display) {
				final Range range = display.getVisibleRange();

				final int rangeStart = range.getStart();
				final int rangeLength = range.getLength();

				// an RPC request to get the tag data
				logger.finer("Requesting service getTagList from tagDataProvider...");
				Services.getCorpusManagerService().getTagList(corpusID, rangeStart, rangeLength, 
						new AsyncCallback<List<Tag>>() {

					@Override
					public void onFailure(Throwable caught) {
						logger.severe("Caught service exception " + caught);
						Utils.showErrorPage(caught.getMessage());
					}

					@Override
					public void onSuccess(List<Tag> tList) {
						logger.finer("getTagList returned successfully, updating row data...");
						updateRowData(rangeStart, tList);
					}
				});
			}
		};

		tagDataProvider.addDataDisplay(tagList);
		tagPager = new SimplePager();
		tagPager.setDisplay(tagList);

		tagCellTablePanel.clear();
		tagCellTablePanel.add(tagList);
		tagCellTablePanel.add(tagPager);
	}

	@UiHandler("nTagRecords")
	void onNFolderRecordsClick(ClickEvent e) {
		logger.finer("User clicked nTagRecords.");
		tagList.setVisibleRange(0, Integer.parseInt(nTagRecords.getSelectedValue()));
	}

	@UiHandler("renameTagBtn")
	void onRenameTagBtnClick(ClickEvent e) {
		logger.finer("User clicked rename tag button, renaming the tag...");
		//prepare the tag object
		long tagID = tagListSelectionModel.getSelectedObject().getId();
		final String tagName = tagNameTextbox.getText();
		final Tag tag = new Tag();
		tag.setId(tagID);
		tag.setName(tagName);

		//check if it is empty
		if(tagName.isEmpty()) {
			showFeedbackPanel(FeedbackType.ERROR, "Tag name can't be empty!", "");
			return;
		}

		//rpc call to update the tag name
		logger.finer("Requesting service updateTag...");
		Services.getCorpusManagerService().updateTag(tag, 
				new AsyncCallback<Void>() {

			@Override
			public void onFailure(Throwable caught) {
				logger.severe("Caught service exception " + caught);
				if (caught instanceof ResourceAlreadyExistsException) {
					showFeedbackPanel(FeedbackType.ERROR, "Tag names conflict!",
							"A tag with the same name already exists in the database. Please choose a different name.");
				}
				Utils.showErrorPage(caught.getMessage());
			}

			@Override
			public void onSuccess(Void result) {
				logger.finer("updateTag service returned successfully, refreshing lists...");
				//refresh folder list
				showFeedbackPanel(FeedbackType.SUCCESS, "Tag name updated!", "");
				refreshLists();
			}
		});
	}

	@UiHandler("saOKBtn")
	void onSAOKBtnClick(ClickEvent e) {
		sweetAlertPanel.setVisible(false);
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

	@UiHandler("addTagBtn")
	void onAddTagBtnClick(ClickEvent e) {
		logger.finer("User clicked new tag button, adding new tag...");
		final String tagName = tagNameTextbox.getText();

		//check if it is empty
		if(tagName.isEmpty()) {
			//			showFeedbackPanel("alert-danger", "Folder name can't be empty!");
			showFeedbackPanel(FeedbackType.ERROR, "Tag name can't be empty!", "");
			return;
		}

		logger.finer("Requesting service addTag...");
		Services.getCorpusManagerService().addTag(tagName, 
				new AsyncCallback<Void>() {

			@Override
			public void onFailure(Throwable caught) {
				logger.severe("Caught service exception " + caught);
				if (caught instanceof ResourceAlreadyExistsException) {
					showFeedbackPanel(FeedbackType.ERROR, "Tag names conflict!",
							"A tag with the same name already exists in the database. Please choose a different name.");
					return;
				}
				Utils.showErrorPage(caught.getMessage());
			}

			@Override
			public void onSuccess(Void result) {
				logger.finer("addTag returned successfully, updating row data...");
				//refresh folder list
				tagNameTextbox.setText("");
				showFeedbackPanel(FeedbackType.SUCCESS, "New tag added!", "");
				refreshLists();
			}
		});

	}

	// refresh corpus list cell table
	private void refreshLists() {
		logger.finer("Refreshing lists...");
		showAllTags();
		showAllTexts();
	}

	private void initializeTagListColumns(CellTable<Tag> tagList) {

		//tag name column
		Column<Tag, String> nameColumn = new Column<Tag, String>(new ClickableTextCell()) {
			@Override
			public String getValue(Tag tag) {
				return tag.getName();
			}
		};
		nameColumn.setFieldUpdater(new FieldUpdater<Tag, String>() {
			@Override
			public void update(int index, Tag corpusFolder, String value) {
				logger.finer("User clicked tag name, showing texts tagged with it...");
				showTextsWithSelectedTag();
			}
		});
		tagList.addColumn(nameColumn, "Name");

		// the number of text column
		TextColumn<Tag> numColumn = new TextColumn<Tag>() {
			@Override
			public String getValue(Tag tag) {
				return tag.getNumText() + "";
			}
		};
		tagList.addColumn(numColumn, "Texts");
		tagList.setColumnWidth(numColumn, "3%");


		// the delete button column
		SafeHtml deleteIcon = ICONBUTTONTEMPLATE.iconButtonInCellTable("fa-trash-o");
		ActionCell<Tag> deleteCell = new ActionCell<>(deleteIcon, 
				new ActionCell.Delegate<Tag>() {
			@Override
			public void execute(Tag tag) {
				if (!Window.confirm("Do you really want to delete " + tag.getName() + ".")) {
					return;
				}
				logger.finer("User clicked delete tag button, deleting the tag...");
				deleteTag(tag);
			}
		});
		Column<Tag, Tag> deleteColumn = new Column<Tag, Tag>(deleteCell) {

			@Override
			public Tag getValue(Tag tag) {
				return tag;
			}
		};
		tagList.addColumn(deleteColumn, "Delete");
		tagList.setColumnWidth(deleteColumn, "3%");
	}

	private void deleteTag(final Tag tag) {

		// delete tag when button clicked
		logger.finer("Requesting service deleteTag...");
		Services.getCorpusManagerService().deleteTag(tag, 
				new AsyncCallback<Void>() {

			@Override
			public void onFailure(Throwable caught) {
				logger.severe("Caught service exception " + caught);
				Utils.showErrorPage(caught.getMessage());
			}

			@Override
			public void onSuccess(Void result) {
				logger.finer("deleteTag service returned successfully, updating row data...");
				hideSidebarPanels();
				refreshLists();
				showFeedbackPanel(FeedbackType.ERROR, "Tag " + tag.getName()
				+ " deleted!", "");
			}
		});
	}

	private void showTextsWithSelectedTag() {
		final long tagID = tagListSelectionModel.getSelectedObject().getId();

		textListSelectionModel = new MultiSelectionModel<>(CorpusText.KEY_PROVIDER);
		textList = new CellTable<CorpusText>(CorpusText.KEY_PROVIDER);
		textList.setWidth("100%");
		textList.setSelectionModel(textListSelectionModel,
				DefaultSelectionEventManager.<CorpusText> createCheckboxManager());

		initializeColumns(textList);

		// RPC request to get text count
		logger.finer("Requesting service getTextCount with corpusID=" + corpusID + " and tagID=" + tagID +"...");
		Services.getCorpusManagerService().getTextCount(corpusID, tagID, 
				new AsyncCallback<Integer>() {
			@Override
			public void onFailure(Throwable caught) {
				logger.severe("Caught service exception " + caught);
				// serious error, show error page
				Utils.showErrorPage(caught.getMessage());
			}

			@Override
			public void onSuccess(Integer result) {
				logger.finer("getTextCount returned successfully, updating row count...");
				textList.setRowCount(result, true);
			}
		});

		//initial items per page 
		textList.setVisibleRange(0, Integer.parseInt(nRecords.getSelectedValue()));

		textDataProvider = new AsyncDataProvider<CorpusText>() {
			@Override
			protected void onRangeChanged(HasData<CorpusText> display) {
				final Range range = display.getVisibleRange();

				final int rangeStart = range.getStart();
				final int rangeLength = range.getLength();

				// an RPC request to get the text data
				logger.finer("Requesting service getTextList from within textDataProvider...");
				Services.getCorpusManagerService().getTextList(corpusID, tagID, rangeStart, rangeLength,
						new AsyncCallback<List<CorpusText>>() {

					@Override
					public void onSuccess(List<CorpusText> result) {
						logger.finer("getTextList returned successfully, updating row data...");
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

		textDataProvider.addDataDisplay(textList);

		pager = new SimplePager();
		pager.setDisplay(textList);

		textCellTablePanel.clear();     
		textCellTablePanel.add(textList);
		textCellTablePanel.add(pager);

	}

	private void initializeColumns(CellTable<CorpusText> textList) {
		// the checkbox column
		Column<CorpusText, Boolean> checkColumn = new Column<CorpusText, Boolean>(new CheckboxCell(true, false)) {
			@Override
			public Boolean getValue(CorpusText object) {
				// Get the value from the selection model.
				return textListSelectionModel.isSelected(object);
			}
		};
		textList.addColumn(checkColumn, "");
		textList.setColumnWidth(checkColumn, "3%");

		// the id column
		TextColumn<CorpusText> idColumn = new TextColumn<CorpusText>() {
			@Override
			public String getValue(CorpusText corpusText) {
				return corpusText.getId() + "";
			}
		};
		textList.addColumn(idColumn, "ID");
		textList.setColumnWidth(idColumn, "3%");

		// the tags column
		TextColumn<CorpusText> tagColumn = new TextColumn<CorpusText>() {
			@Override
			public String getValue(CorpusText corpusText) {
				Set<Tag> tagSet = corpusText.getTagSet();
				String tagSetString = "";
				if(tagSet != null) {
					for(Tag tag: tagSet) {
						tagSetString += "[" + tag.getName() + "] ";
					}
				}
				return tagSetString;
			}
		};
		textList.addColumn(tagColumn, "Tag");
		textList.setColumnWidth(tagColumn, "12%");

		// the text title column
		TextColumn<CorpusText> titleColumn = new TextColumn<CorpusText>() {
			@Override
			public String getValue(CorpusText corpusText) {
				return corpusText.getTitle();
			}
		};
		textList.addColumn(titleColumn, "Text Title");

		// the text content column
		TextColumn<CorpusText> textContent = new TextColumn<CorpusText>() {
			@Override
			public String getValue(CorpusText corpusText) {
				String content = corpusText.getContent();

				if (content != null && content.length() > 50) {
					content = content.substring(0, 50) + "...";
				}
				return content;
			}
		};
		textList.addColumn(textContent, "Content");
//		textList.setColumnWidth(textContent, "30%");

		// the created date column
		TextColumn<CorpusText> createdColumn = new TextColumn<CorpusText>() {
			@Override
			public String getValue(CorpusText corpusText) {
				return DateTimeFormat.getFormat("dd/MM/yy").format(corpusText.getCreateDate());
			}
		};
		textList.addColumn(createdColumn, "Created On");
		textList.setColumnWidth(createdColumn, "9%");

		// the edit button column
		SafeHtml editIcon = ICONBUTTONTEMPLATE.iconButtonInCellTable("fa-pencil-square-o");
		ActionCell<CorpusText> editCell = new ActionCell<>(editIcon, new ActionCell.Delegate<CorpusText>() {
			@Override
			public void execute(CorpusText corpusText) {
				logger.finer("User clicked edit text button.");
				showTextDetailPanel(corpusText);
			}
		});
		Column<CorpusText, CorpusText> editColumn = new Column<CorpusText, CorpusText>(editCell) {

			@Override
			public CorpusText getValue(CorpusText corpusText) {
				return corpusText;
			}
		};
		textList.addColumn(editColumn, "Edit");
		textList.setColumnWidth(editColumn, "3%");

		// the delete button column
		SafeHtml deleteIcon = ICONBUTTONTEMPLATE.iconButtonInCellTable("fa-trash-o");
		ActionCell<CorpusText> deleteCell = new ActionCell<>(deleteIcon, new ActionCell.Delegate<CorpusText>() {
			@Override
			public void execute(CorpusText corpusText) {
				if (!Window.confirm("Do you really want to delete " + corpusText.getTitle())) {
					return;
				}
				logger.finer("User clicked delete text button.");
				deleteText(corpusText);

			}
		});
		Column<CorpusText, CorpusText> deleteColumn = new Column<CorpusText, CorpusText>(deleteCell) {

			@Override
			public CorpusText getValue(CorpusText corpusText) {
				return corpusText;
			}
		};
		textList.addColumn(deleteColumn, "Delete");
		textList.setColumnWidth(deleteColumn, "3%");

	}

	private void showTextDetailPanel(final CorpusText corpusText) {
		//		feedbackPanel.setVisible(false);
		createdOn.setText(corpusText.getCreateDate().toString());
		textTitle.setText(corpusText.getTitle());
		textContent.setText(corpusText.getContent());
		textIdHidden.setValue(corpusText.getId() + "");

		//get tag list
		tagListBox.clear();
		Services.getCorpusManagerService().getTagCount(new AsyncCallback<Integer>() {

			@Override
			public void onFailure(Throwable caught) {
				Utils.showErrorPage(caught.getMessage());
			}

			@Override
			public void onSuccess(Integer tagCount) {
				Services.getCorpusManagerService().getTagList(corpusID, 0, tagCount, 
						new AsyncCallback<List<Tag>>() {

					@Override
					public void onFailure(Throwable caught) {
						Utils.showErrorPage(caught.getMessage());
					}

					@Override
					public void onSuccess(List<Tag> tagList) {
						for(Tag tag: tagList) {
							tagListBox.addItem(tag.getName(), tag.getId() + "");
						}

						//set selected
						List<Long> tagIDs = new ArrayList<>();
						for(Tag tag : corpusText.getTagSet()) {
							tagIDs.add(tag.getId());
						}

						for(int i = 0; i < tagListBox.getItemCount(); i ++) {
							if(tagIDs.contains(Long.parseLong(tagListBox.getValue(i)))) {
								tagListBox.setItemSelected(i, true);
							}
						}
					}
				});
			}
		});

		textListPanel.setStyleName("col-lg-9");
		textDetailPanel.setVisible(true);

		importPanel.setVisible(false);

	}

	/**
	 * Delete text from the text table.
	 * 
	 * @param corpusText
	 */
	private void deleteText(CorpusText corpusText) {
		logger.finer("Requesting service deleteText...");
		Services.getCorpusManagerService().deleteText(corpusText, new AsyncCallback<Void>() {

			@Override
			public void onFailure(Throwable caught) {
				logger.severe("Caught service exception " + caught);
				Utils.showErrorPage(caught.getMessage());
			}

			@Override
			public void onSuccess(Void result) {
				logger.finer("deleteText service returned successfully.");
				hideSidebarPanels();
				showFeedbackPanel(FeedbackType.ERROR, "Text deleted!", "");
				refreshLists();
			}
		});
	}

	@UiHandler("newText")
	void onNewTextClick(ClickEvent e) {
		logger.finer("User clicked new text button, adding new text...");
		CorpusText newText = new CorpusText();
		newText.setId(-1); // -1 means the text is new
		newText.setCreateDate(new Date());
		newText.setTitle("");
		newText.setContent("");
		showTextDetailPanel(newText);
	}

	@UiHandler("saveDetail")
	void onSaveDetailClick(ClickEvent e) {
		logger.finer("User clicked save details button.");
		CorpusText corpusText = new CorpusText();
		corpusText.setId(Integer.parseInt(textIdHidden.getValue()));
		corpusText.setCorpusID(corpusID);
		corpusText.setTitle(textTitle.getText());
		corpusText.setContent(textContent.getText());

		//get the selected tags
		Set<Tag> selectedTags = new HashSet<>();
		for(int i = 0; i < tagListBox.getItemCount(); i ++) {
			if(tagListBox.isItemSelected(i)) {
				Tag tag = new Tag();
				tag.setId(Long.parseLong(tagListBox.getValue(i)));
				selectedTags.add(tag);
			}
		}
		corpusText.setTagSet(selectedTags);

		// check if text info is empty
		if (corpusText.getTitle().isEmpty()) {
			showFeedbackPanel(FeedbackType.WARNING, "You need to enter a title for your text.", "");
			return;
		}

		if (corpusText.getId() == -1) {
			// new text
			logger.finer("Requesting service addTextToCorpus...");
			Services.getCorpusManagerService().addTextToCorpus(corpusText, new AsyncCallback<Void>() {

				@Override
				public void onFailure(Throwable caught) {
					logger.severe("Caught service exception " + caught);
					Utils.showErrorPage(caught.getMessage());
				}

				@Override
				public void onSuccess(Void result) {
					logger.finer("addTextToCorpus returned successfully, new text added to corpus...");
					hideSidebarPanels();
					refreshLists();
					showFeedbackPanel(FeedbackType.SUCCESS, "New text added to corpus!", "");
				}
			});
		} else {
			// existing text, update corpus info
			logger.finer("Requesting service updateText...");
			Services.getCorpusManagerService().updateText(corpusText, new AsyncCallback<Void>() {

				@Override
				public void onFailure(Throwable caught) {
					logger.severe("Caught service exception " + caught);
					Utils.showErrorPage(caught.getMessage());
				}

				@Override
				public void onSuccess(Void result) {
					logger.finer("updateText service returned successfully, text details updated...");
					hideSidebarPanels();
					// refresh cell table
					refreshLists();
					showFeedbackPanel(FeedbackType.SUCCESS, "Text details updated!", "");
				}
			});
		}
	}

	private void hideSidebarPanels() {
		textDetailPanel.setVisible(false);
		importPanel.setVisible(false);
		textListPanel.setStyleName("col-lg-12");
	}

	@UiHandler("closeDetail")
	void onCloseDetailClick(ClickEvent e) {
		logger.finer("User clicked close details panel button, adding new tag...");
		hideSidebarPanels();
	}

	@UiHandler("deleteSelected")
	void onDeleteSelectedClick(ClickEvent e) {

		hideSidebarPanels();

		int selectedCount = getSelectedCount();
		if (selectedCount == 0) {
			showFeedbackPanel(FeedbackType.WARNING, "No text is selected for deletion!", "");
			return;
		}

		if (!Window.confirm("Do you really want to delete the " + selectedCount + " selected texts? "
				+ "This operation will be irreversable!")) {
			return;
		}

		logger.finer("User clicked delete selected button, deleting selected texts...");
		for (CorpusText corpusText : textListSelectionModel.getSelectedSet()) {
			textListSelectionModel.setSelected(corpusText, false);
			deleteText(corpusText);
		}
	}

	private int getSelectedCount() {
//		int selectedCount = 0;
//		for (CorpusText corpusText : textListSelectionModel.getSelectedSet()) {
//			selectedCount++;
//		}
//		return selectedCount;
		return	textListSelectionModel.getSelectedSet().size();
	}

	@UiHandler("nRecords")
	void onNRecordsClick(ClickEvent e) {
		textList.setVisibleRange(0, Integer.parseInt(nRecords.getSelectedValue()));
	}

	@UiHandler("selectAll")
	void onSelectAllClick(ClickEvent e) {
		for (CorpusText corpusText : textList.getVisibleItems()) {
			textListSelectionModel.setSelected(corpusText, true);
		}
	}

	@UiHandler("selectClear")
	void onSelectClearClick(ClickEvent e) {
		for (CorpusText corpusText : textList.getVisibleItems()) {
			textListSelectionModel.setSelected(corpusText, false);
		}
	}

	@UiHandler("selectReverse")
	void onSelectReverseClick(ClickEvent e) {
		for (CorpusText corpusText : textList.getVisibleItems()) {
			textListSelectionModel.setSelected(corpusText, !textListSelectionModel.isSelected(corpusText));
		}
	}

	private void populateImportPanelTagList() {
		tagListBoxImport.clear();
		logger.finer("Requesting service getTagCount...");
		Services.getCorpusManagerService().getTagCount(new AsyncCallback<Integer>() {

			@Override
			public void onFailure(Throwable caught) {
				logger.severe("Caught service exception " + caught);
				Utils.showErrorPage(caught.getMessage());
			}

			@Override
			public void onSuccess(Integer tagCount) {
				logger.finer("getTagCount returned successfully, requesting getTagList service...");
				Services.getCorpusManagerService().getTagList(corpusID, 0, tagCount, 
						new AsyncCallback<List<Tag>>() {

					@Override
					public void onFailure(Throwable caught) {
						logger.severe("Caught service exception " + caught);
						Utils.showErrorPage(caught.getMessage());
					}

					@Override
					public void onSuccess(List<Tag> tagList) {
						logger.finer("getTagList returned successfully, tag list box...");
						for(Tag tag: tagList) {
							tagListBoxImport.addItem(tag.getName(), tag.getId() + "");
						}
					}
				});
			}
		});	
	}

	@UiHandler("importTexts")
	void onImportTextsClick(ClickEvent e) {
		logger.finer("User clicked import texts button. Showing import corpus panel...");
		textListPanel.setStyleName("col-lg-9");
		importPanel.setVisible(true);

		textDetailPanel.setVisible(false);
		//populate tag list
		logger.finer("Populating tag list box...");
		populateImportPanelTagList();

		//set up uploader widgets
		logger.fine("Setting up uploader widgets...");
		setupUploaderWidgets();
	}

	private void setupUploaderWidgets() {
		// create the multiple file uploader widget: code borrowed form gwtuploader
		final VerticalPanel progressBarPanel = new VerticalPanel();
		final Map<String, ProgressBar> progressBars = new LinkedHashMap<String, ProgressBar>();
		final Map<String, Image> cancelButtons = new LinkedHashMap<String, Image>();
		final Uploader uploader = new Uploader();
		// post params are sent as form fields
//		final JSONObject formFields = new JSONObject();
//		formFields.put("corpusID", new JSONString(corpusID + ""));

//		uploader.setPostParams(formFields);

		uploader.setUploadURL("ctap/importCorpusServlet")
		.setButtonText("<span class=\"btn btn-success fileinput-button\">"
				+ "<i class=\"fa fa-fw fa-plus\"></i> " + "<span>Add files...</span> " + "</span>")
		.setButtonWidth(160).setButtonHeight(50).setFileSizeLimit("100 MB")
		.setButtonCursor(Uploader.Cursor.HAND)
		.setButtonAction(Uploader.ButtonAction.SELECT_FILES)
		.setFileQueuedHandler(new FileQueuedHandler() {
			@Override
			public boolean onFileQueued(final FileQueuedEvent fileQueuedEvent) {
				logger.finer("New file queued for upload: " + fileQueuedEvent.getFile().getName());
				// Create a Progress Bar for this file
				final ProgressBar progressBar = new ProgressBar(0.0, 1.0, 0.0,
						new CancelProgressBarTextFormatter());
				progressBar.setTitle(fileQueuedEvent.getFile().getName());
				progressBar.setHeight("20px");
				progressBar.setWidth("120px");
				progressBars.put(fileQueuedEvent.getFile().getId(), progressBar);

				// Add Cancel Button Image
				final Image cancelButton = new Image("img/cancel.png");
				// cancelButton.setStyleName("cancelButton");
				cancelButton.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						uploader.cancelUpload(fileQueuedEvent.getFile().getId(), false);
						progressBars.get(fileQueuedEvent.getFile().getId()).setProgress(-1.0d);
						cancelButton.removeFromParent();
					}
				});
				cancelButtons.put(fileQueuedEvent.getFile().getId(), cancelButton);

				// Add the Bar and Button to the interface
				HorizontalPanel progressBarAndButtonPanel = new HorizontalPanel();
				progressBarAndButtonPanel.add(progressBar);
				progressBarAndButtonPanel.add(cancelButton);
				progressBarPanel.add(progressBarAndButtonPanel);

				return true;
			}
		}).setUploadProgressHandler(new UploadProgressHandler() {
			@Override
			public boolean onUploadProgress(UploadProgressEvent uploadProgressEvent) {
				ProgressBar progressBar = progressBars.get(uploadProgressEvent.getFile().getId());
				progressBar.setProgress(
						(double) uploadProgressEvent.getBytesComplete() / uploadProgressEvent.getBytesTotal());
				return true;
			}
		}).setUploadCompleteHandler(new UploadCompleteHandler() {
			@Override
			public boolean onUploadComplete(UploadCompleteEvent uploadCompleteEvent) {
				logger.finer("File upload completed: " + uploadCompleteEvent.getFile().getName());
				cancelButtons.get(uploadCompleteEvent.getFile().getId()).removeFromParent();
				uploader.startUpload();
				// show upload stats
				setUploadStats(uploader.getStats().getSuccessfulUploads(), uploader.getStats().getFilesQueued(),
						uploader.getStats().getUploadsCancelled(), uploader.getStats().getUploadErrors());
				// refresh text list after all files uploaded
				if (uploader.getStats().getFilesQueued() == 0) {
					//also close the import panel
					refreshLists();
				}
				return true;
			}
		}).setFileDialogStartHandler(new FileDialogStartHandler() {
			@Override
			public boolean onFileDialogStartEvent(FileDialogStartEvent fileDialogStartEvent) {
				if (uploader.getStats().getUploadsInProgress() <= 0) {
					// Clear the uploads that have completed, if none
					// are in process
					progressBarPanel.clear();
					progressBars.clear();
					cancelButtons.clear();
				}
				return true;
			}
		}).setFileDialogCompleteHandler(new FileDialogCompleteHandler() {
			@Override
			public boolean onFileDialogComplete(FileDialogCompleteEvent fileDialogCompleteEvent) {
				int filesInQueue = fileDialogCompleteEvent.getTotalFilesInQueue();
				if (filesInQueue > 0) {
					logger.finer("User selected " + filesInQueue + " files for upload.");
					
					JSONObject formFields = new JSONObject();
					formFields.put("corpusID", new JSONString(corpusID + ""));

					uploader.setPostParams(formFields);

					if (uploader.getStats().getUploadsInProgress() <= 0) {
						//get selected tag IDs
						for(int i = 0; i < tagListBoxImport.getItemCount(); i++) {
							if(tagListBoxImport.isItemSelected(i)) {
								formFields.put("tag"+i, new JSONString(tagListBoxImport.getValue(i)));
							}
						}

						uploader.startUpload();
					}
				}
				statsSelected.setText("Selected: " + uploader.getStats().getFilesQueued());
				return true;
			}
		}).setFileQueueErrorHandler(new FileQueueErrorHandler() {
			@Override
			public boolean onFileQueueError(FileQueueErrorEvent fileQueueErrorEvent) {
				String queuedErrorMsg = "Upload of file " + fileQueueErrorEvent.getFile().getName()
						+ " failed due to [" + fileQueueErrorEvent.getErrorCode().toString() + "]: "
						+ fileQueueErrorEvent.getMessage();
				logger.severe("File queued error: " + queuedErrorMsg);
				showFeedbackPanel(FeedbackType.ERROR, "Queue Error:", queuedErrorMsg);
				return true;
			}
		}).setUploadErrorHandler(new UploadErrorHandler() {
			@Override
			public boolean onUploadError(UploadErrorEvent uploadErrorEvent) {
				String uploadErrorMsg = "Upload of file " + uploadErrorEvent.getFile().getName()
						+ " failed due to [" + uploadErrorEvent.getErrorCode().toString() + "]: "
						+ uploadErrorEvent.getMessage();
				logger.severe("Upload file error: " + uploadErrorMsg);
				cancelButtons.get(uploadErrorEvent.getFile().getId()).removeFromParent();
				showFeedbackPanel(FeedbackType.ERROR, "Upload Error: ", uploadErrorMsg);
				return true;
			}
		});

		VerticalPanel uploadWidgetPanel = new VerticalPanel();
		uploadWidgetPanel.add(uploader);

		// create a file drop box
		//to prevent multiple handlers attached to the label, check if handlers 
		// have already been added
		if (!labelHasHandler && Uploader.isAjaxUploadWithProgressEventsSupported()) {
			dropFiles.addDragOverHandler(new DragOverHandler() {
				@Override
				public void onDragOver(DragOverEvent event) {
					if (!uploader.getButtonDisabled()) {
						dropFiles.addStyleName("dropFilesLabelHover");
					}
				}
			});
			dropFiles.addDragLeaveHandler(new DragLeaveHandler() {
				@Override
				public void onDragLeave(DragLeaveEvent event) {
					dropFiles.removeStyleName("dropFilesLabelHover");
				}
			});
			dropFiles.addDropHandler(new DropHandler() {
				@Override
				public void onDrop(DropEvent event) {
					logger.finer("User dropped files to uploader.");
					dropFiles.removeStyleName("dropFilesLabelHover");

					if (uploader.getStats().getUploadsInProgress() <= 0) {
						progressBarPanel.clear();
						progressBars.clear();
						cancelButtons.clear();
					}

					uploader.addFilesToQueue(Uploader.getDroppedFiles(event.getNativeEvent()));
					event.preventDefault();
				}
			});
			labelHasHandler = true;
		}
		uploadWidgetPanel.add(dropFiles);

		fileUploadPanelRight.setWidget(progressBarPanel);
		fileUploadPanelLeft.setWidget(uploadWidgetPanel);
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

	private void setUploadStats(int uploaded, int remaining, int cancelled, int error) {
		// show upload status
		statsUploaded.setText("Uploaded: " + uploaded);
		statsRemaining.setText("Remaining: " + remaining);
		statsCancelled.setText("Cancelled: " + cancelled);
		statsError.setText("Error: " + error);
	}

	@UiHandler("closeImportPanel")
	void onCloseImportPanleClick(ClickEvent e) {
		importPanel.setVisible(false);
		textListPanel.setStyleName("col-lg-12");
	}

	//	@UiHandler("closeFeedbackPanel")
	//	void onCloseFeedbackPanelClick(ClickEvent e) {
	//		feedbackPanel.setVisible(false);
	//	}

	@UiHandler("exportIntoOneFile")
	void onExportIntoOneFileClick(ClickEvent e) {
		logger.finer("User clicked export into one file button.");
		exportCorpus(true);
	}

	@UiHandler("exportIntoMultipleFiles")
	void onExportIntoMultipleFilesClick(ClickEvent e) {
		logger.finer("User clicked export into multiple files button.");
		exportCorpus(false);
	}

	private void exportCorpus(boolean inOneFile) {
		logger.finer("Requesting export file service...");
		String url = GWT.getModuleBaseURL() + "exportCorpusServlet?corpusID="
				+ corpusID + "&inOneFile=" + inOneFile;
		Window.open(url, "_self", "status=0, toolbar=0, menubar=0, location=0");

	}
}

package com.ctapweb.web.client.component;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.ctapweb.web.client.HistoryToken;
import com.ctapweb.web.client.component.admin.AdminAE;
import com.ctapweb.web.client.component.admin.AdminCF;
import com.ctapweb.web.client.component.admin.DBManager;
import com.ctapweb.web.client.service.Services;
import com.ctapweb.web.shared.SharedProperties;
import com.ctapweb.web.shared.UserInfo;
import com.ctapweb.web.shared.Utils;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

public class UserHome extends Composite {

	private static UserHomeUiBinder uiBinder = GWT.create(UserHomeUiBinder.class);

	interface UserHomeUiBinder extends UiBinder<Widget, UserHome> {
	}

	@UiField Anchor collapseMenu;
	@UiField HTMLPanel mainHTMLPanel;
	@UiField Label userGreeting;

	@UiField Anchor dashBoard;
	@UiField Anchor corpusManager;
	@UiField Anchor featureSelector;
	@UiField Anchor analysisGenerator;
	@UiField Anchor resultVisualizer;
	@UiField Anchor editProfile;
	@UiField Anchor sendFeedback;
	@UiField Anchor documentation;
	@UiField Anchor signout;

	@UiField HTMLPanel adminNav;
	@UiField Anchor dbManager;
	@UiField Anchor analysisEngine;

	Logger logger = Logger.getLogger(UserHome.class.getName());

	// get user cookie
	private String userCookieValue = Cookies.getCookie(SharedProperties.USERCOOKIENAME);

	public UserHome() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	public UserHome(UserInfo userInfo) {
		logger.finer("Opening User Home page...");

		initWidget(uiBinder.createAndBindUi(this));
		collapseMenu.getElement().getStyle().setCursor(Cursor.POINTER);

		// set greetings
		userGreeting.setText(userInfo.getEmail());

		// show admin menu if user is admin
		logger.finer("Requesting service isUserAdmin...");
		Services.getAdminService().isUserAdmin(new AsyncCallback<Boolean>() {

			@Override
			public void onFailure(Throwable caught) {
				logger.log(Level.SEVERE, "Caught service exception " + caught);
				Utils.showErrorPage(caught.getMessage());
			}

			@Override
			public void onSuccess(Boolean result) {
				if(result) {
					//true, user is admin
					logger.finer("Service successfully returned result: " + result + ".");
					adminNav.setVisible(true);
				}
			}
		});

		showUserPage();
	}

	/**
	 * Shows corresponding user page according to history token.
	 */
	private void showUserPage() {

		String historyToken = History.getToken();
		String[] tokenSplit = historyToken.split("\\?");
		String pageToken = tokenSplit[0];

		logger.finer("Displaying " + pageToken + " module...");
		switch (pageToken) {
		case HistoryToken.corpusmanager:
			clearAndAddToMainPanel(new CorpusManager());
			break;
		case HistoryToken.textmanager:
			if (tokenSplit.length > 1) {
				// get the corpus=n part
				String corpusStr[] = tokenSplit[1].split("=");
				if (corpusStr.length > 1 && corpusStr[0].equals("corpusID")) {
					// try to parse the corpusID
					int corpusID;
					try {
						corpusID = Integer.parseInt(corpusStr[1]);
					} catch (NumberFormatException e) {
						logger.warning("CorpusID not a number, please provide correct format (corpusID=x). "
								+ "Redirecting to corpus manager...");
						History.newItem(HistoryToken.corpusmanager);
						break;
					}
					clearAndAddToMainPanel(new TextManager(corpusID));
				} else {
					logger.warning("Incorrect parameter format, text manager accepts only one paramerter in the "
							+ "form of corpusID=x. Redirecting to corpus manager...");
					History.newItem(HistoryToken.corpusmanager);

				}
			} else {
				logger.warning("No corpus ID provided for text manager, redirecting to corpus manager...");
				History.newItem(HistoryToken.corpusmanager);
			}
			break;
		case HistoryToken.featureselector:
			clearAndAddToMainPanel(new FeatureSelector());
			break;
		case HistoryToken.featuresetmanager:
			if (tokenSplit.length > 1) {
				// get the featureSetID=n part
				String featureSetStr[] = tokenSplit[1].split("=");
				if (featureSetStr.length > 1 && featureSetStr[0].equals("featureSetID")) {
					// try to parse the feature set id
					int featureSetID;
					try {
						featureSetID = Integer.parseInt(featureSetStr[1]);
					} catch (NumberFormatException e) {
						logger.warning("Feature set id not a number, please provide correct format (featureSetID=x). "
								+ "Redirecting to feature selector...");
						History.newItem(HistoryToken.featureselector);
						break;
					}
					clearAndAddToMainPanel(new FeatureSetManager(featureSetID));
				} else {
					logger.warning("Incorrect parameter format, feature set manager accepts only one paramerter in the "
							+ "form of featureSetID=x. Redirecting to feature selector...");
					History.newItem(HistoryToken.featureselector);

				}
			} else {
				logger.warning(
						"No feature set ID provided for feature set manager, redirecting to feature selector...");
				History.newItem(HistoryToken.featureselector);
			}
			break;
		case HistoryToken.analysisgenerator:
			clearAndAddToMainPanel(new AnalysisGenerator());
			break;
		case HistoryToken.resultvisualizer:
			clearAndAddToMainPanel(new ResultVisualizer());
			break;
		case HistoryToken.groupsetmanager:
			if (tokenSplit.length > 1) {
				// get the analysisID=n part
				String analysisIDStr[] = tokenSplit[1].split("=");
				if (analysisIDStr.length > 1 && analysisIDStr[0].equals("analysisID")) {
					// try to parse the feature set id
					int analysisID;
					try {
						analysisID = Integer.parseInt(analysisIDStr[1]);
					} catch (NumberFormatException e) {
						logger.warning("Analysis id not a number, please provide correct format (analysisID=x). "
								+ "Redirecting to result visualizer...");
						History.newItem(HistoryToken.resultvisualizer);
						break;
					}
//					clearAndAddToMainPanel(new GroupSetManager(analysisID));
				} else {
					logger.warning("Incorrect parameter format, group set manager accepts only one paramerter in the "
							+ "form of analysisID=x. Redirecting to result visualizer...");
					History.newItem(HistoryToken.resultvisualizer);
				}
			} else {
				logger.warning("No analysis ID provided for feature set manager, redirecting to result visualizer...");
				History.newItem(HistoryToken.resultvisualizer);
			}
			break;
		case HistoryToken.groupmanager:
			if (tokenSplit.length > 1) {
				// get the groupID=n part
				String groupIDStr[] = tokenSplit[1].split("=");
				if (groupIDStr.length > 1 && groupIDStr[0].equals("groupID")) {
					// try to parse the feature set id
					int groupID;
					try {
						groupID = Integer.parseInt(groupIDStr[1]);
					} catch (NumberFormatException e) {
						logger.warning("Group id not a number, please provide correct format (groupID=x). "
								+ "Redirecting to result visualizer...");
						History.newItem(HistoryToken.resultvisualizer);
						break;
					}
//					clearAndAddToMainPanel(new GroupManager(groupID));
				} else {
					logger.warning("Incorrect parameter format, group manager accepts only one paramerter in the "
							+ "form of groupID=x. Redirecting to result visualizer...");
					History.newItem(HistoryToken.resultvisualizer);
				}
			} else {
				logger.warning("No group ID provided for group manager, redirecting to result visualizer...");
				History.newItem(HistoryToken.resultvisualizer);
			}
			break;
		case HistoryToken.editprofile:
			clearAndAddToMainPanel(new EditProfile());
			break;
		case HistoryToken.sendfeedback:
			clearAndAddToMainPanel(new SendFeedback());
			break;
		case HistoryToken.documentation:
			clearAndAddToMainPanel(new Documentation());
			break;
		case HistoryToken.adminDB:
			clearAndAddToMainPanel(new DBManager());
			break;
		case HistoryToken.adminAE:
			clearAndAddToMainPanel(new AdminAE());
			break;

		case HistoryToken.adminCF:
			clearAndAddToMainPanel(new AdminCF());
			break;

		case HistoryToken.userhome:
		case HistoryToken.dashboard:
		default:
			clearAndAddToMainPanel(new Dashboard());

		}

	}

	private void clearAndAddToMainPanel(Widget widget) {
		mainHTMLPanel.clear();
		mainHTMLPanel.add(widget);
	}

	@UiHandler("dashBoard")
	void onDashBoardClick(ClickEvent e) {
		History.newItem(HistoryToken.dashboard);
	}

	@UiHandler("corpusManager")
	void onCorpusManagerClick(ClickEvent e) {
		History.newItem(HistoryToken.corpusmanager);
	}

	@UiHandler("featureSelector")
	void onFeatureSelectorClick(ClickEvent e) {
		History.newItem(HistoryToken.featureselector);
	}

	@UiHandler("analysisGenerator")
	void onAnalysisGeneratorClick(ClickEvent e) {
		History.newItem(HistoryToken.analysisgenerator);
	}

	@UiHandler("resultVisualizer")
	void onResultVisualizerClick(ClickEvent e) {
		History.newItem(HistoryToken.resultvisualizer);
	}

	@UiHandler("dbManager")
	void onInitDBClick(ClickEvent e) {
		History.newItem(HistoryToken.adminDB);
	}

	@UiHandler("analysisEngine")
	void onAnalysisEngineClick(ClickEvent e) {
		History.newItem(HistoryToken.adminAE);
	}

	@UiHandler("editProfile")
	void onEditProfileClick(ClickEvent e) {
		History.newItem(HistoryToken.editprofile);
	}

	@UiHandler("sendFeedback")
	void onSendFeedbackClick(ClickEvent e) {
		History.newItem(HistoryToken.sendfeedback);
	}

	@UiHandler("documentation")
	void onDocumentationClick(ClickEvent e) {
		History.newItem(HistoryToken.documentation);
	}

	@UiHandler("signout")
	void onSignoutClick(ClickEvent e) {
		Services.getUserService().signout(new AsyncCallback<Void>() {

			@Override
			public void onFailure(Throwable caught) {
				Utils.showErrorPage(caught.getMessage());
			}

			@Override
			public void onSuccess(Void result) {
				Cookies.removeCookie(SharedProperties.USERCOOKIENAME);
				History.newItem(HistoryToken.signin);
			}
		});
	}

	@UiHandler("collapseMenu")
	void onCollapseMenuClick(ClickEvent e) {
		String bodyClass = RootPanel.get().getElement().getAttribute("class");
		if (bodyClass.isEmpty()) {
			RootPanel.get().setStyleName("aside-collapsed");
		} else {
			RootPanel.get().setStyleName("");
		}
	}
}

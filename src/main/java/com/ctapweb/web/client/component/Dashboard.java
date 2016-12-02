package com.ctapweb.web.client.component;

import java.util.List;
import java.util.logging.Logger;

import com.ctapweb.web.client.service.Services;
import com.ctapweb.web.shared.Analysis;
import com.ctapweb.web.shared.AnalysisEngine;
import com.ctapweb.web.shared.Corpus;
import com.ctapweb.web.shared.FeatureSet;
import com.ctapweb.web.shared.SharedProperties;
import com.ctapweb.web.shared.Utils;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;

public class Dashboard extends Composite {

	private static DashboardUiBinder uiBinder = GWT.create(DashboardUiBinder.class);

	interface DashboardUiBinder extends UiBinder<Widget, Dashboard> {
	}

	@UiField Label corpusCount;
	@UiField Label featuresetCount;
	@UiField Label analysisCount;
	@UiField HTMLPanel corpusListPanel;
	@UiField HTMLPanel featuresetListPanel;
	@UiField HTMLPanel analysisListPanel;
	@UiField HTMLPanel cfListPanel;

	Logger logger = Logger.getLogger(CorpusManager.class.getName());

	//get user cookie
	private String userCookieValue = Cookies.getCookie(SharedProperties.USERCOOKIENAME);

	// data provider
	AsyncDataProvider<Corpus> corpusDataProvider;
	AsyncDataProvider<FeatureSet> featureSetDataProvider;
	AsyncDataProvider<Analysis> analysisDataProvider;
	AsyncDataProvider<AnalysisEngine> cfDataProvider;

	final CellTable<Corpus> corpusList = new CellTable<Corpus>(Corpus.KEY_PROVIDER);
	final CellTable<FeatureSet> featureSetList = new CellTable<FeatureSet>(FeatureSet.KEY_PROVIDER);
	final CellTable<Analysis> analysisList = new CellTable<Analysis>(Analysis.KEY_PROVIDER);
	final CellTable<AnalysisEngine> cfList = 
			new CellTable<AnalysisEngine>(AnalysisEngine.KEY_PROVIDER);

	SimplePager corpusListPager;
	SimplePager featureSetListPager;
	SimplePager analysisListPager;
	SimplePager cfListPager;


	public Dashboard() {
		initWidget(uiBinder.createAndBindUi(this));

		//get corpus count
		Services.getCorpusManagerService().getCorpusCount(new AsyncCallback<Integer>() {

			@Override
			public void onSuccess(Integer result) {
				corpusCount.setText(result+"");
			}

			@Override
			public void onFailure(Throwable caught) {
				Utils.showErrorPage(caught.getMessage());
			}
		});

		//get feature set count
		Services.getFeatureSelectorService().getFeatureSetCount(new AsyncCallback<Integer>() {

			@Override
			public void onFailure(Throwable caught) {
				Utils.showErrorPage(caught.getMessage());
			}

			@Override
			public void onSuccess(Integer result) {
				featuresetCount.setText(result + "");
			}
		});

		//get analysis count
		Services.getAnalysisGeneratorService().getAnalysisCount( 
				new AsyncCallback<Integer>() {

			@Override
			public void onFailure(Throwable caught) {
				Utils.showErrorPage(caught.getMessage());
			}

			@Override
			public void onSuccess(Integer result) {
				analysisCount.setText(result + "");
			}
		});

		//show the lists
		showCorpusList();
		showFeaturesetList();
		showAnalysisList();
		showFeatureList();

	}

	private void showCorpusList() {
		corpusList.setWidth("100%");

		//initialize the columns
		//the id column
		TextColumn<Corpus> idColumn = new TextColumn<Corpus>() {
			@Override
			public String getValue(Corpus corpus) {
				return corpus.getId()+"";
			}
		};
		corpusList.addColumn(idColumn, "ID");
		corpusList.setColumnWidth(idColumn, "10%");

		//the corpus name column
		TextColumn<Corpus> nameColumn = new TextColumn<Corpus>() {
			@Override
			public String getValue(Corpus corpus) {
				return corpus.getName();
			}
		};
		corpusList.addColumn(nameColumn, "Corpus Name");

		//the corpus description column
		TextColumn<Corpus> descriptionColumn = new TextColumn<Corpus>() {
			@Override
			public String getValue(Corpus corpus) {
				String desc = corpus.getDescription();

				if(desc.length() >  97) {
					desc = desc.substring(0, 97) + "...";
				}
				return desc;
			}
		};
		corpusList.addColumn(descriptionColumn, "Description");
		corpusList.setColumnWidth(descriptionColumn, "40%");

		//the created date column
		TextColumn<Corpus> createdColumn = new TextColumn<Corpus>() {
			@Override
			public String getValue(Corpus corpus) {
				//				SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");
				return DateTimeFormat.getFormat("dd/MM/yy").format(corpus.getCreateDate());
				//				return dateFormat.format(corpus.getCreateDate().toString());
			}
		};
		corpusList.addColumn(createdColumn, "Created On");
		corpusList.setColumnWidth(createdColumn, "15%");

		//RPC request to get corpus count
		Services.getCorpusManagerService().getCorpusCount( 
				new AsyncCallback<Integer>() {
			@Override
			public void onFailure(Throwable caught) {
				//serious error, show error page
				logger.warning("Can't get corpus count, user not logged in or database error.");
				Utils.showErrorPage(caught.getMessage());
			}

			@Override
			public void onSuccess(Integer result) {
				corpusList.setRowCount(result, true);
			}
		});

		// list 10 items per page
		corpusList.setVisibleRange(0, 10);

		//data provider
		//		AsyncDataProvider<Corpus> corpusDataProvider = new AsyncDataProvider<Corpus>() {
		corpusDataProvider = new AsyncDataProvider<Corpus>() {
			@Override
			protected void onRangeChanged(HasData<Corpus> display) {
				final Range range = display.getVisibleRange();

				final int rangeStart = range.getStart();
				final int rangeLength = range.getLength();

				//an RPC request to get the corpus data
				Services.getCorpusManagerService().getCorpusList(rangeStart, rangeLength, 
						new AsyncCallback<List<Corpus>>() {

					@Override
					public void onSuccess(List<Corpus> result) {
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
		corpusDataProvider.addDataDisplay(corpusList);

		// Create paging controls.
		//		SimplePager pager = new SimplePager();
		corpusListPager = new SimplePager();
		corpusListPager.setDisplay(corpusList);

		corpusListPanel.add(corpusList);
		corpusListPanel.add(corpusListPager);		
	}

	private void showFeaturesetList() {
		//initialize the cell table
		featureSetList.setWidth("100%");

		//initialize columns to the cell table

		//the id column
		TextColumn<FeatureSet> idColumn = new TextColumn<FeatureSet>() {
			@Override
			public String getValue(FeatureSet featureSet) {
				return featureSet.getId()+"";
			}
		};
		featureSetList.addColumn(idColumn, "ID");
		featureSetList.setColumnWidth(idColumn, "5%");

		//the name column
		TextColumn<FeatureSet> nameColumn = new TextColumn<FeatureSet>() {
			@Override
			public String getValue(FeatureSet featureSet) {
				return featureSet.getName();
			}
		};
		featureSetList.addColumn(nameColumn, "Feature Set Name");

		//the description column
		TextColumn<FeatureSet> descriptionColumn = new TextColumn<FeatureSet>() {
			@Override
			public String getValue(FeatureSet featureSet) {
				String desc = featureSet.getDescription();

				if(desc.length() >  97) {
					desc = desc.substring(0, 97) + "...";
				}
				return desc;
			}
		};
		featureSetList.addColumn(descriptionColumn, "Description");
		featureSetList.setColumnWidth(descriptionColumn, "50%");

		//the created date column
		TextColumn<FeatureSet> createdColumn = new TextColumn<FeatureSet>() {
			@Override
			public String getValue(FeatureSet featureSet) {
				return DateTimeFormat.getFormat("dd/MM/yy").format(featureSet.getCreateDate());
			}
		};
		featureSetList.addColumn(createdColumn, "Created On");
		featureSetList.setColumnWidth(createdColumn, "15%");

		//RPC request to get feature set count
		Services.getFeatureSelectorService().getFeatureSetCount(new AsyncCallback<Integer>() {
			@Override
			public void onFailure(Throwable caught) {
				//serious error, show error page
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

		//data provider
		featureSetDataProvider = new AsyncDataProvider<FeatureSet>() {
			@Override
			protected void onRangeChanged(HasData<FeatureSet> display) {
				final Range range = display.getVisibleRange();

				final int rangeStart = range.getStart();
				final int rangeLength = range.getLength();

				//an RPC request to get the corpus data
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

		//connect the list to the data provider
		featureSetDataProvider.addDataDisplay(featureSetList);

		// Create paging controls.
		//		SimplePager pager = new SimplePager();
		featureSetListPager = new SimplePager();
		featureSetListPager.setDisplay(featureSetList);

		featuresetListPanel.add(featureSetList);
		featuresetListPanel.add(featureSetListPager);	
	}

	private void showAnalysisList() {
		analysisList.setWidth("100%");


		//the id column
		TextColumn<Analysis> idColumn = new TextColumn<Analysis>() {
			@Override
			public String getValue(Analysis analysis) {
				return analysis.getId()+"";
			}
		};
		analysisList.addColumn(idColumn, "ID");
		analysisList.setColumnWidth(idColumn, "3%");

		//the name column
		TextColumn<Analysis> nameColumn = new TextColumn<Analysis>() {
			@Override
			public String getValue(Analysis analysis) {
				return analysis.getName();
			}
		};
		analysisList.addColumn(nameColumn, "Analysis Name");
		analysisList.setColumnWidth(nameColumn, "20%");

		//the description column
		TextColumn<Analysis> descriptionColumn = new TextColumn<Analysis>() {
			@Override
			public String getValue(Analysis analysis) {
				String desc = analysis.getDescription();

				if(desc.length() >  97) {
					desc = desc.substring(0, 97) + "...";
				}
				return desc;
			}
		};
		analysisList.addColumn(descriptionColumn, "Description");
		analysisList.setColumnWidth(descriptionColumn, "20%");

		//the corpus column
		TextColumn<Analysis> corpusColumn = new TextColumn<Analysis>() {
			@Override
			public String getValue(Analysis analysis) {
				return analysis.getCorpusName();
			}
		};
		analysisList.addColumn(corpusColumn, "Corpus");
		analysisList.setColumnWidth(corpusColumn, "20%");

		//the feature set column
		TextColumn<Analysis> featureSetColumn = new TextColumn<Analysis>() {
			@Override
			public String getValue(Analysis analysis) {
				return analysis.getFeatureSetName();
			}
		};
		analysisList.addColumn(featureSetColumn, "Feature Set");
		//				analysisList.setColumnWidth(featureSetColumn, "20%");

		//the created date column
		TextColumn<Analysis> createdColumn = new TextColumn<Analysis>() {
			@Override
			public String getValue(Analysis analysis) {
				return DateTimeFormat.getFormat("dd/MM/yy").format(analysis.getCreateDate());
			}
		};
		analysisList.addColumn(createdColumn, "Created On");
		analysisList.setColumnWidth(createdColumn, "15%");

		//RPC request to get corpus count
		Services.getAnalysisGeneratorService().getAnalysisCount(
				new AsyncCallback<Integer>() {
			@Override
			public void onFailure(Throwable caught) {
				//serious error, show error page
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

		//data provider
		//		AsyncDataProvider<Corpus> corpusDataProvider = new AsyncDataProvider<Corpus>() {
		analysisDataProvider = new AsyncDataProvider<Analysis>() {
			@Override
			protected void onRangeChanged(HasData<Analysis> display) {
				final Range range = display.getVisibleRange();

				final int rangeStart = range.getStart();
				final int rangeLength = range.getLength();

				//an RPC request to get the analysis data
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

		//connect the list to the data provider
		analysisDataProvider.addDataDisplay(analysisList);

		// Create paging controls.
		analysisListPager = new SimplePager();
		analysisListPager.setDisplay(analysisList);

		analysisListPanel.add(analysisList);
		analysisListPanel.add(analysisListPager);
	}

	private void showFeatureList() {
		cfList.setWidth("100%");

		//the id column
		TextColumn<AnalysisEngine> idColumn = new TextColumn<AnalysisEngine>() {
			@Override
			public String getValue(AnalysisEngine cf) {
				return cf.getId()+"";
			}
		};
		cfList.addColumn(idColumn, "ID");
		cfList.setColumnWidth(idColumn, "3%");

		//the name column
		TextColumn<AnalysisEngine> nameColumn = new TextColumn<AnalysisEngine>() {
			@Override
			public String getValue(AnalysisEngine cf) {
				return cf.getName();
			}
		};
		cfList.addColumn(nameColumn, "CF Name");

		//the description column
		TextColumn<AnalysisEngine> descriptionColumn = new TextColumn<AnalysisEngine>() {
			@Override
			public String getValue(AnalysisEngine cf) {
				String desc = cf.getDescription();

				if(desc.length() >  97) {
					desc = desc.substring(0, 97) + "...";
				}
				return desc;
			}
		};
		cfList.addColumn(descriptionColumn, "Description");
		cfList.setColumnWidth(descriptionColumn, "50%");

//		//the created date column
//		TextColumn<ComplexityFeature> createdColumn = new TextColumn<ComplexityFeature>() {
//			@Override
//			public String getValue(ComplexityFeature cf) {
//				return DateTimeFormat.getFormat("dd/MM/yy").format(cf.getCreateDate());
//			}
//		};
//		cfList.addColumn(createdColumn, "Created On");
//		cfList.setColumnWidth(createdColumn, "15%");

		//RPC request to get all feature count
		Services.getFeatureSelectorService().getFeatureCount(new AsyncCallback<Integer>() {
			@Override
			public void onFailure(Throwable caught) {
				//serious error, show error page
				logger.warning("Can't get CF count, user not logged in (as admin) or database error.");
				Utils.showErrorPage(caught.getMessage());
			}

			@Override
			public void onSuccess(Integer result) {
				cfList.setRowCount(result, true);
			}
		});

		// list 10 items per page
		cfList.setVisibleRange(0, 10);

		//data provider
		cfDataProvider = new AsyncDataProvider<AnalysisEngine>() {
			@Override
			protected void onRangeChanged(HasData<AnalysisEngine> display) {
				final Range range = display.getVisibleRange();

				final int rangeStart = range.getStart();
				final int rangeLength = range.getLength();

				//an RPC request to get all CF list
				Services.getFeatureSelectorService().getFeatureList(rangeStart, rangeLength, 
						new AsyncCallback<List<AnalysisEngine>>() {

					@Override
					public void onSuccess(List<AnalysisEngine> result) {
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
		cfListPager = new SimplePager();
		cfListPager.setDisplay(cfList);

		cfListPanel.add(cfList);
		cfListPanel.add(cfListPager);
	}
}

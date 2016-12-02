/**
 * 
 */
package com.ctapweb.web.server.analysis;


import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Timer;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.uima.UIMAFramework;
import org.apache.uima.collection.CollectionProcessingEngine;
import org.apache.uima.collection.impl.metadata.cpe.CpeDescriptorFactory;
import org.apache.uima.collection.metadata.CasProcessorConfigurationParameterSettings;
import org.apache.uima.collection.metadata.CpeCasProcessor;
import org.apache.uima.collection.metadata.CpeCollectionReader;
import org.apache.uima.collection.metadata.CpeComponentDescriptor;
import org.apache.uima.collection.metadata.CpeDescription;
import org.apache.uima.collection.metadata.CpeDescriptorException;
import org.apache.uima.resource.ResourceInitializationException;

import com.ctapweb.web.server.DBConnectionManager;
import com.ctapweb.web.server.ServerProperties;
import com.ctapweb.web.server.logging.LogMarker;
import com.ctapweb.web.server.user.AnalysisGeneratorServiceImpl;
import com.ctapweb.web.shared.Analysis;
import com.ctapweb.web.shared.AnalysisEngine;
import com.ctapweb.web.shared.exception.DatabaseException;

/**
 * Constructs the analysis environment and runs the analysis.
 * @author xiaobin
 *
 */
public class RunAnalysis {
	Connection dbConnection = DBConnectionManager.getDbConnection();
	AnalysisGeneratorServiceImpl analysisGeneratorServiceImpl = new AnalysisGeneratorServiceImpl();
	Logger logger = LogManager.getLogger();

	private Analysis analysis;

	//for getting the base class path
	ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

	//descriptor path
	private String casConsumerDescriptorPath =
			classLoader.getResource(ServerProperties.CPE_DESCRIPTOR_PATH_BASE).getFile() + 
			"DatabaseWriterCasConsumer.xml";
 
	private String collectionReaderDescriptorPath = 
			classLoader.getResource(ServerProperties.CPE_DESCRIPTOR_PATH_BASE).getFile() + 
			"CorpusTextCollectionReader.xml";

//	private String casConsumerDescriptorPath = 
//			ServerProperties.CPE_DESCRIPTOR_PATH_BASE + "DatabaseWriterCasConsumer.xml";

	//the cpe descriptor, components of the cpe are added to this descriptor
	CollectionProcessingEngine cpe;

	public RunAnalysis(Analysis analysis) 
			throws DatabaseException, CpeDescriptorException, 
			SQLException, IOException, ResourceInitializationException {
		logger.info(LogMarker.CTAP_SERVER_MARKER, "Initialing RunAnalysis object...");

		long cpeInitStartTime = System.currentTimeMillis();//Start time of CPE initialization 
		
		this.analysis = analysis;
		
		//a cpe is a UIMA pipeline
		logger.info("Producing a CPE for the analysis...");
		cpe = UIMAFramework.produceCollectionProcessingEngine(createCPEDescription());

		// Create and register a Status Callback Listener
		StatusCallbackListenerImpl statusCallbackListener = new StatusCallbackListenerImpl(analysis);
		statusCallbackListener.setCpeInitStartTime(cpeInitStartTime);
		cpe.addStatusCallbackListener(statusCallbackListener);

	}

	/**
	 * Runs the analysis.
	 * @throws ResourceInitializationException 
	 */
	public void run() throws ResourceInitializationException {
		long analysisID = analysis.getId();

		//a timer to check running status periodically
		//check the status from DB, decides what to do next
		logger.info("Creating timer to check running status...");
		Timer timer = new Timer();
		timer.schedule(new CpeStatusTimerTask(analysisID, cpe), 
				1 * 1000, 1 * 1000); //check status every 1 seconds

		//run the cpe
		logger.info("Initiating and running the cpe...");
		cpe.process();


	}

	private void addCollectionReader(CpeDescription cpeDescription) 
			throws CpeDescriptorException {
		//initiate the collection reader
		CpeCollectionReader collectionReader = CpeDescriptorFactory.produceCollectionReader();
		CpeComponentDescriptor collectionReaderDescriptor = 
				CpeDescriptorFactory.produceComponentDescriptor(collectionReaderDescriptorPath);
		collectionReader.setDescriptor(collectionReaderDescriptor);
		CasProcessorConfigurationParameterSettings collectionReaderParameters = 
				CpeDescriptorFactory.produceCasProcessorConfigurationParameterSettings();
		collectionReaderParameters.setParameterValue(
				CorpusTextCollectionReader.PARAM_ANALYSISID, Math.toIntExact(analysis.getId()));
		collectionReader.setConfigurationParameterSettings(collectionReaderParameters);

		cpeDescription.addCollectionReader(collectionReader);

	}

	//add AEs to the cpe, the AEs are obtained by looking at the feature set designated by the analysis.
	// A feature set contains multiple text features, each of which consists of a number of AEs.
	// For a specific CPE, each AE is added to the cpe only once, or the results would be incorrect.
	private void addCasProcessors(CpeDescription cpeDescription) 
			throws SQLException, IOException, CpeDescriptorException {
		//a list of feature extractor AEs selected by the analysis
		List<AnalysisEngine> cleanAEs = AnalysisUtils.getCleanAEs(analysis.getId());

		String aeListStr = "";
		for(AnalysisEngine ae: cleanAEs) {
			aeListStr += "[" + ae.getName() + "] ";
		}
		logger.info(LogMarker.CTAP_SERVER_MARKER, "Obtained all AEs to be added as CAS processors: " 
				+ aeListStr);

		logger.info(LogMarker.CTAP_SERVER_MARKER, "Populating the CPE description with Cas processors...");
		populateCpeWithCasProcessors(cpeDescription, cleanAEs);

	}

	private void populateCpeWithCasProcessors(CpeDescription cpeDescription, List<AnalysisEngine> aes) 
			throws IOException, CpeDescriptorException {
		//		File aeTempDir = new File(FileUtils.getTempDirectoryPath(), analysis.getId()+"");

		//for each ae in aes, construct a cas processor and add it to the cpe descriptor
		for(AnalysisEngine ae: aes) {
			//writes the ae descriptor content into a tmp file and return the file path
			//			File aeDescriptorFile = new File(aeTempDir, ae.getDescriptorFileName());
			//			FileUtils.forceMkdir(aeTempDir);
			//			FileUtils.write(aeDescriptorFile, ae.getDescriptorFileContent());

			//looks for files from class path
			File annotatorFolder = FileUtils.getFile(
					classLoader.getResource(ServerProperties.ANNOTATOR_FOLDER).getFile());
			File featureAEFolder = FileUtils.getFile(
					classLoader.getResource(ServerProperties.FEATUREAE_FOLDER).getFile());

			String aeType = ae.getType();
			String aeFileName = ae.getDescriptorFileName();
			File aeDescriptorFile = null;
			if(AnalysisEngine.AEType.ANNOTATOR.equals(aeType)) {
				aeDescriptorFile = new File(annotatorFolder, aeFileName);
			} else if(AnalysisEngine.AEType.FEATURE_EXTRACTOR.equals(aeType)) {
				aeDescriptorFile = new File(featureAEFolder, aeFileName);
			}

			logger.info("Initializing AE " + ae.getName() + " and adding it to CPE description...");
			CpeCasProcessor casProcessor = CpeDescriptorFactory.produceCasProcessor(ae.getName());
			CpeComponentDescriptor componentDescriptor = 
					CpeDescriptorFactory.produceComponentDescriptor(aeDescriptorFile.getPath());
			casProcessor.setCpeComponentDescriptor(componentDescriptor);
			CasProcessorConfigurationParameterSettings casProcessorParametersSettings = 
					CpeDescriptorFactory.produceCasProcessorConfigurationParameterSettings();
			//parameter name 'aeID', same for every AE. 
			//Transform to Integer because UIMA only allows integer parameter value
			casProcessorParametersSettings.setParameterValue(
					"aeID", Math.toIntExact(ae.getId())); 

			casProcessor.setConfigurationParameterSettings(casProcessorParametersSettings);
			cpeDescription.addCasProcessor(casProcessor);
		}
	}

	private CpeDescription createCPEDescription()
			throws CpeDescriptorException, DatabaseException, SQLException, IOException {
		//initiate an empty description
		logger.info(LogMarker.CTAP_SERVER_MARKER, "Initiating an empty cpe description...");
		CpeDescription cpeDescription  = CpeDescriptorFactory.produceDescriptor();

		//adds the collection reader, which reads text from corpus
		logger.info(LogMarker.CTAP_SERVER_MARKER, "Adding the corpus text collection reader...");
		addCollectionReader(cpeDescription);

		logger.info(LogMarker.CTAP_SERVER_MARKER, "Adding CAS processors (AEs) to cpe...");
		addCasProcessors(cpeDescription);

		//add a cas consumer to the cpe
		logger.info(LogMarker.CTAP_SERVER_MARKER, "Adding CAS consumer to cpe...");
		addCasConsumer(cpeDescription);

		return cpeDescription;
	}

	private void addCasConsumer(CpeDescription cpeDescription) throws CpeDescriptorException {
		CpeCasProcessor casConsumer = CpeDescriptorFactory.produceCasProcessor("casConsumer");
		CpeComponentDescriptor casConsumerDescriptor = 
				CpeDescriptorFactory.produceComponentDescriptor(casConsumerDescriptorPath);
		casConsumer.setCpeComponentDescriptor(casConsumerDescriptor);
		CasProcessorConfigurationParameterSettings casConsumerParameters =
				CpeDescriptorFactory.produceCasProcessorConfigurationParameterSettings();
		casConsumerParameters.setParameterValue(
				DatabaseWriterCasConsumer.PARAM_ANALYSISID, Math.toIntExact(analysis.getId()));
		casConsumer.setConfigurationParameterSettings(casConsumerParameters);
		cpeDescription.addCasProcessor(casConsumer);
	}

	/**
	 * Callback Listener. Receives event notifications from CPE.
	 */
	//	class StatusCallbackListenerImpl implements StatusCallbackListener {
	//		int entityCount = 0;
	//		int totalEntityCount;
	//
	//		long size = 0;
	//
	//		/**
	//		 * Called when the initialization is completed.
	//		 * 
	//		 * @see org.apache.uima.collection.processing.StatusCallbackListener#initializationComplete()
	//		 */
	//		@Override
	//		public void initializationComplete() {      
	//			cpeInitCompleteTime = System.currentTimeMillis();
	//			logger.info("CPE initialization completed.");
	//
	//			//get total number of texts to be analyzed
	//			try {
	//				totalEntityCount = (new CorpusManagerServiceImpl())
	//						.getTextCount(analysis.getCorpusID());
	//			} catch (UserNotLoggedInException e) {
	//				e.printStackTrace();
	//				logger.warn(e.getMessage());
	//			} catch (AccessToResourceDeniedException e) {
	//				e.printStackTrace();
	//				logger.warn(e.getMessage());
	//			} catch (DatabaseException e) {
	//				e.printStackTrace();
	//			} 
	//
	//		}
	//
	//		/**
	//		 * Called when the batchProcessing is completed.
	//		 * 
	//		 * @see org.apache.uima.collection.processing.StatusCallbackListener#batchProcessComplete()
	//		 * 
	//		 */
	//		@Override
	//		public void batchProcessComplete() {
	//			logger.info("Completed " + entityCount + " documents");
	//			if (size > 0) {
	//				logger.info("; " + size + " characters");
	//			}
	//			long elapsedTime = System.currentTimeMillis() - cpeInitStartTime;
	//			logger.info("Time Elapsed : " + elapsedTime + " ms ");
	//		}
	//
	//		/**
	//		 * Called when the collection processing is completed.
	//		 * 
	//		 * @see org.apache.uima.collection.processing.StatusCallbackListener#collectionProcessComplete()
	//		 */
	//		@Override
	//		public void collectionProcessComplete() {
	//
	//			//update analysis status
	//			AnalysisStatus analysisStatus = new AnalysisStatus();
	//			analysisStatus.setAnalysisID(analysis.getId());
	//			analysisStatus.setProgress(1.0);
	//			analysisStatus.setStatus(AnalysisStatus.Status.FINISHED);
	//
	//			try {
	//				analysisGeneratorServiceImpl.updateAnalysisStatus(analysisStatus);
	//			} catch (Exception e) {
	//				e.printStackTrace();
	//				logger.warn(e.getMessage());
	//			}
	//
	//			long time = System.currentTimeMillis();
	//			logger.info("Completed " + entityCount + " documents");
	//			if (size > 0) {
	//				logger.info("; " + size + " characters");
	//			}
	//			long initTime = cpeInitCompleteTime - cpeInitStartTime; 
	//			long processingTime = time - cpeInitCompleteTime;
	//			long elapsedTime = initTime + processingTime;
	//			logger.info("Total Time Elapsed: " + elapsedTime + " ms ");
	//			logger.info("Initialization Time: " + initTime + " ms");
	//			logger.info("Processing Time: " + processingTime + " ms");
	//
	//			logger.info(cpe.getPerformanceReport().toString());
	//			// stop the JVM. Otherwise main thread will still be blocked waiting for
	//			// user to press Enter.
	//			//			System.exit(1);
	//		}
	//
	//		/**
	//		 * Called when the CPM is paused.
	//		 * 
	//		 * @see org.apache.uima.collection.processing.StatusCallbackListener#paused()
	//		 */
	//		@Override
	//		public void paused() {
	//			logger.info("Paused");
	//		}
	//
	//		/**
	//		 * Called when the CPM is resumed after a pause.
	//		 * 
	//		 * @see org.apache.uima.collection.processing.StatusCallbackListener#resumed()
	//		 */
	//		@Override
	//		public void resumed() {
	//			logger.info("Resumed");
	//		}
	//
	//		/**
	//		 * Called when the CPM is stopped abruptly due to errors.
	//		 * 
	//		 * @see org.apache.uima.collection.processing.StatusCallbackListener#aborted()
	//		 */
	//		@Override
	//		public void aborted() {
	//			logger.info("Aborted");
	//			// stop the JVM. Otherwise main thread will still be blocked waiting for
	//			// user to press Enter.
	//			//			System.exit(1);
	//		}
	//
	//		/**
	//		 * Called when the processing of a Document is completed. <br>
	//		 * The process status can be looked at and corresponding actions taken.
	//		 * 
	//		 * @param aCas
	//		 *          CAS corresponding to the completed processing
	//		 * @param aStatus
	//		 *          EntityProcessStatus that holds the status of all the events for aEntity
	//		 */
	//		@Override
	//		public void entityProcessComplete(CAS aCas, EntityProcessStatus aStatus) {
	//			if (aStatus.isException()) {
	//				List exceptions = aStatus.getExceptions();
	//				for (int i = 0; i < exceptions.size(); i++) {
	//					((Throwable) exceptions.get(i)).printStackTrace();
	//				}
	//				return;
	//			}
	//
	//			entityCount++;
	//
	//			//update database
	//			updateAnalysisProgress((double) entityCount / totalEntityCount);
	//
	//			String docText = aCas.getDocumentText();
	//			if (docText != null) {
	//				size += docText.length();
	//			}
	//			logger.info("Processed " + entityCount + " document with " + docText.length() + " characters.");
	//
	//			//wait for some time, for debugging
	//			//			try {
	//			//				Thread.sleep(5000);
	//			//			} catch (InterruptedException e) {
	//			//				e.printStackTrace();
	//			//			}
	//		}
	//
	//		private void updateAnalysisProgress(double progress) {
	//			String updateStr = ""
	//					+ "UPDATE analysis_status "
	//					+ "SET progress=?, last_update=CURRENT_TIMESTAMP "
	//					+ "WHERE analysis_id=?";
	//			PreparedStatement ps;
	//			try {
	//				ps = dbConnection.prepareStatement(updateStr);
	//				ps.setDouble(1, progress);
	//				ps.setLong(2, analysis.getId());
	//				ps.executeUpdate();
	//			} catch (SQLException e) {
	//				e.printStackTrace();
	//				logger.warn(e.getMessage());
	//			}
	//
	//		}
	//	}



}

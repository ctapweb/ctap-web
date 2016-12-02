package com.ctapweb.web.server.analysis;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.EntityProcessStatus;
import org.apache.uima.collection.StatusCallbackListener;
import org.apache.uima.jcas.JCas;

import com.ctapweb.web.server.DBConnectionManager;
import com.ctapweb.web.server.analysis.type.CorpusTextInfo;
import com.ctapweb.web.server.logging.LogMarker;
import com.ctapweb.web.shared.Analysis;
import com.ctapweb.web.shared.AnalysisStatus;

public class StatusCallbackListenerImpl implements StatusCallbackListener {

	private Logger logger = LogManager.getLogger();
	Connection dbConnection = DBConnectionManager.getDbConnection();

	Analysis analysis;

	private long cpeInitStartTime; // the time the cpe starts initialization, to be passed in
	private long cpeInitCompleteTime;
	int completedEntityCount = 0;
	int totalEntityCount;

	long size = 0;

	public StatusCallbackListenerImpl(Analysis analysis) {
		super();
		this.analysis = analysis;
	}

	public long getCpeInitStartTime() {
		return cpeInitStartTime;
	}

	public void setCpeInitStartTime(long cpeInitStartTime) {
		this.cpeInitStartTime = cpeInitStartTime;
	}

	public long getCpeInitCompleteTime() {
		return cpeInitCompleteTime;
	}

	public void setCpeInitCompleteTime(long cpeInitCompleteTime) {
		this.cpeInitCompleteTime = cpeInitCompleteTime;
	}
	/**
	 * Called when the initialization is completed.
	 * 
	 * @see org.apache.uima.collection.processing.StatusCallbackListener#initializationComplete()
	 */
	@Override
	public void initializationComplete() {      
		logger.info(LogMarker.CTAP_SERVER_MARKER, "CPE initialization completed.");

		long analysisID = analysis.getId();
		cpeInitCompleteTime = System.currentTimeMillis();

		try {
			//get total number of texts to be analyzed
			logger.trace(LogMarker.CTAP_SERVER_MARKER, "Getting number of entities to be analyzed...");
			totalEntityCount = AnalysisUtils.getAnalysisTexts(analysisID).size();

			//clear previous results for this analysis
			logger.trace(LogMarker.CTAP_SERVER_MARKER, "Clear previous results for the analysis...");
			AnalysisUtils.clearAnalysisResults(analysisID);
			
			//set analysis status
			AnalysisStatus analysisStatus = new AnalysisStatus();
			analysisStatus.setAnalysisID(analysisID);
			analysisStatus.setStatus(AnalysisStatus.Status.RUNNING);
			analysisStatus.setProgress(0);
			
			logger.info("Start running the cpe, setting analysis status to running...");
			AnalysisUtils.updateAnalysisStatus(analysisStatus);
			
		} catch (SQLException e) {
			logger.throwing(e);
		}
		
	}

	/**
	 * Called when the batchProcessing is completed.
	 * 
	 * @see org.apache.uima.collection.processing.StatusCallbackListener#batchProcessComplete()
	 * 
	 */
	@Override
	public void batchProcessComplete() {
		logger.info(LogMarker.CTAP_SERVER_MARKER, "CPE batch process completed.");
		//		logger.info("Completed " + entityCount + " documents");
		//		if (size > 0) {
		//			logger.info("; " + size + " characters");
		//		}
		//		long elapsedTime = System.currentTimeMillis() - cpeInitStartTime;
		//		logger.info("Time Elapsed : " + elapsedTime + " ms ");
	}

	/**
	 * Called when the collection processing is completed.
	 * 
	 * @see org.apache.uima.collection.processing.StatusCallbackListener#collectionProcessComplete()
	 */
	@Override
	public void collectionProcessComplete() {
		logger.info(LogMarker.CTAP_SERVER_MARKER, "Collection process completed.");

		//update analysis status
		AnalysisStatus analysisStatus = new AnalysisStatus();
		analysisStatus.setAnalysisID(analysis.getId());
		analysisStatus.setProgress(1.0);
		analysisStatus.setStatus(AnalysisStatus.Status.FINISHED);

		try {
			AnalysisUtils.updateAnalysisStatus(analysisStatus);
		} catch (SQLException e) {
			logger.throwing(e);
		}

		long completeTime = System.currentTimeMillis();
		//		if (size > 0) {
		//			logger.info("; " + size + " characters");
		//		}
		long initTime = cpeInitCompleteTime - cpeInitStartTime; 
		long processingTime = completeTime - cpeInitCompleteTime;
		long elapsedTime = initTime + processingTime;
		logger.info("Completed " + completedEntityCount + " documents");
		logger.info("Total Time Elapsed: " + elapsedTime + " ms ");
		logger.info("Initialization Time: " + initTime + " ms");
		logger.info("Processing Time: " + processingTime + " ms");

		//		logger.info(cpe.getPerformanceReport().toString());
		// stop the JVM. Otherwise main thread will still be blocked waiting for
		// user to press Enter.
		//			System.exit(1);
	}

	/**
	 * Called when the CPM is paused.
	 * 
	 * @see org.apache.uima.collection.processing.StatusCallbackListener#paused()
	 */
	@Override
	public void paused() {
		logger.info("CPE process paused.");
		
		AnalysisStatus analysisStatus = new AnalysisStatus();
		analysisStatus.setAnalysisID(analysis.getId());
		analysisStatus.setProgress(0);
		analysisStatus.setStatus(AnalysisStatus.Status.PAUSED);
		
		try {
			AnalysisUtils.updateAnalysisStatus(analysisStatus);
		} catch (SQLException e) {
			logger.catching(e);
		}
		
	}

	/**
	 * Called when the CPM is resumed after a pause.
	 * 
	 * @see org.apache.uima.collection.processing.StatusCallbackListener#resumed()
	 */
	@Override
	public void resumed() {
		logger.info("CPE process resumed.");
		
		AnalysisStatus analysisStatus = new AnalysisStatus();
		analysisStatus.setAnalysisID(analysis.getId());
		analysisStatus.setProgress(0);
		analysisStatus.setStatus(AnalysisStatus.Status.RUNNING);
		
		try {
			AnalysisUtils.updateAnalysisStatus(analysisStatus);
		} catch (SQLException e) {
			logger.catching(e);
		}
	}

	/**
	 * Called when the CPM is stopped abruptly due to errors.
	 * 
	 * @see org.apache.uima.collection.processing.StatusCallbackListener#aborted()
	 */
	@Override
	public void aborted() {
		logger.info("CPE process aborted.");
		
		AnalysisStatus analysisStatus = new AnalysisStatus();
		analysisStatus.setAnalysisID(analysis.getId());
		analysisStatus.setProgress(0);
		analysisStatus.setStatus(AnalysisStatus.Status.STOPPED);
		
		try {
			AnalysisUtils.updateAnalysisStatus(analysisStatus);
		} catch (SQLException e) {
			logger.catching(e);
		}
	}

	/**
	 * Called when the processing of a Document is completed. <br>
	 * The process status can be looked at and corresponding actions taken.
	 * 
	 * @param cas
	 *          CAS corresponding to the completed processing
	 * @param entityProcessStatus
	 *          EntityProcessStatus that holds the status of all the events for aEntity
	 */
	@Override
	public void entityProcessComplete(CAS cas, EntityProcessStatus entityProcessStatus) {
		//get text id from cas
		try {
			long textID = 0;
			JCas jCas = cas.getJCas();
			Iterator<CorpusTextInfo> iter = jCas.getAllIndexedFS(CorpusTextInfo.class);
			if(iter.hasNext()) {
				textID = iter.next().getId();
			}

			//record exceptions if occurred
			if (entityProcessStatus.isException()) {
				logger.warn(LogMarker.CTAP_SERVER_MARKER, 
						"Exceptions thrown while processing text " + textID + ":");
				for (Exception e : entityProcessStatus.getExceptions()) {
					logger.throwing(e);
				}
				return;
			}
			
			//update analysis status
			completedEntityCount++;
			
			//update analysis status 
			double progress = (double) completedEntityCount / totalEntityCount;
			AnalysisStatus analysisStatus = new AnalysisStatus();
			analysisStatus.setAnalysisID(analysis.getId());
			analysisStatus.setProgress(progress);
			
			AnalysisUtils.updateAnalysisProgress(analysisStatus);
			
			logger.info(LogMarker.CTAP_SERVER_MARKER, 
					"Text {} successfully processed. Total texts completed: {}.", textID, completedEntityCount);
			
		} catch (CASException | SQLException e) {
			logger.catching(e);
		}

//		updateAnalysisProgress((double) completedEntityCount / totalEntityCount);

//		String docText = cas.getDocumentText();
//		if (docText != null) {
//			size += docText.length();
//		}
//		logger.info("Processed " + completedEntityCount+ " documents with " + docText.length() + " characters.");

		//wait for some time, for debugging
		//			try {
		//				Thread.sleep(5000);
		//			} catch (InterruptedException e) {
		//				e.printStackTrace();
		//			}
	}

//	private void updateAnalysisProgress(double progress) {
//		String updateStr = ""
//				+ "UPDATE analysis_status "
//				+ "SET progress=?, last_update=CURRENT_TIMESTAMP "
//				+ "WHERE analysis_id=?";
//		PreparedStatement ps;
//		try {
//			ps = dbConnection.prepareStatement(updateStr);
//			ps.setDouble(1, progress);
//			ps.setLong(2, analysis.getId());
//			ps.executeUpdate();
//		} catch (SQLException e) {
//			e.printStackTrace();
//			logger.warn(e.getMessage());
//		}
//
//	}

	
}

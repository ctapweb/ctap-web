/**
 * 
 */
package com.ctapweb.web.server.analysis;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.TimerTask;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.uima.collection.CollectionProcessingEngine;

import com.ctapweb.web.server.DBConnectionManager;
import com.ctapweb.web.server.logging.LogMarker;
import com.ctapweb.web.shared.AnalysisStatus;

/**
 * A timer task that checks the cpe's running status and controls the cpe accordingly.
 * @author xiaobin
 *
 */
public class CpeStatusTimerTask extends TimerTask {

	private Logger logger = LogManager.getLogger();
	private long analysisID;
	private CollectionProcessingEngine cpe;
	private Connection dbConnection = DBConnectionManager.getDbConnection();
//	private String pauseLimit = "30 MINUTES"; //if an analysis has been paused for more than this time, it will be killed.
	private String pauseLimit = "1 MINUTES"; //if an analysis has been paused for more than this time, it will be killed.

	public CpeStatusTimerTask(long analysisID, CollectionProcessingEngine cpe) {
		super();
		this.analysisID = analysisID;
		this.cpe = cpe;
	}

	/* (non-Javadoc)
	 * @see java.util.TimerTask#run()
	 */
	@Override
	public void run() {
		logger.info(LogMarker.CTAP_SERVER_MARKER, "Checking analysis status for analysis " + analysisID + "...");
		AnalysisStatus analysisStatus;
		try {
			analysisStatus =AnalysisUtils.getAnalysisStatus(analysisID);
			String status = analysisStatus.getStatus();

			if(status.equals(AnalysisStatus.Status.STOPPED)) {
				logger.info(LogMarker.CTAP_SERVER_MARKER, 
						"Status STOPPED detected for analysis {}, stopping...", analysisID);
				if(cpe.isProcessing()) {
					cpe.stop();
					cancel(); //stop timer
				}
			} else if (status.equals(AnalysisStatus.Status.PAUSED)) {
				if(!cpe.isPaused()) {
					logger.info(LogMarker.CTAP_SERVER_MARKER, 
							"Status PAUSED detected for analysis {}, pausing...", analysisID);
					cpe.pause();
				}

				//if analysis has been paused for more than half an hour, 
				// stop the analysis to release resources
				if(isPauseTooLong(pauseLimit)) {
					logger.info(LogMarker.CTAP_SERVER_MARKER, 
							"Pause longer than {}, killing cpe for analysis {} ...", pauseLimit, analysisID);
					
					//set analysis status
					analysisStatus.setStatus(AnalysisStatus.Status.STOPPED);
					AnalysisUtils.updateAnalysisStatus(analysisStatus);
					
					//kills the cpe
					cpe.kill();
					
					//cancel timer task
					cancel();
				}

			} else if (status.equals(AnalysisStatus.Status.RUNNING)) {
				if(cpe.isPaused()) {
					logger.info(LogMarker.CTAP_SERVER_MARKER, 
							"Status RUNNING detected for analysis {}, resumming cpe process...", analysisID);
					cpe.resume();
				} 
			} else if (status.equals(AnalysisStatus.Status.FINISHED)) {
				cancel();
			}
		} catch (SQLException e) {
			logger.catching(e);
		}
	}

	private boolean isPauseTooLong(String timeElapsed) throws SQLException {
		boolean isPauseTooLong = false;

		String queryStr = ""
								+ "SELECT (CURRENT_TIMESTAMP - last_update) > INTERVAL ' "+timeElapsed+" ' "
//				+ "SELECT (CURRENT_TIMESTAMP - last_update) > INTERVAL ? "
				+ "     AS is_too_long "
				+ "FROM analysis_status "
				+ "WHERE analysis_id=? "
				+ "     AND status=?";
		PreparedStatement ps = dbConnection.prepareStatement(queryStr);
//		ps.setString(1, timeElapsed);
		ps.setLong(1, analysisID);
		ps.setString(2, AnalysisStatus.Status.PAUSED);
		ResultSet rs = ps.executeQuery();
		if(rs.next()) {
			isPauseTooLong = rs.getBoolean("is_too_long");
			//				logger.info("pauseElapsed too long? " + isPauseTooLong);
		}

		return isPauseTooLong;
	}
}

/**
 * 
 */
package com.ctapweb.web.shared;

import java.io.Serializable;
import java.util.Date;

import com.google.gwt.view.client.ProvidesKey;

/**
 * A DTO for analysis status.
 * @author xiaobin
 *
 */
public class AnalysisStatus implements Serializable {

	private long id;
	private long analysisID;
	private double progress;
	private String status;
	private Date lastUpdate;

	/**
	 * The key provider that provides the unique ID of a contact.
	 */
	public static final ProvidesKey<AnalysisStatus> KEY_PROVIDER = new ProvidesKey<AnalysisStatus>() {
		@Override
		public Object getKey(AnalysisStatus item) {
			return item == null ? null : item.getId();
		}
	};

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getAnalysisID() {
		return analysisID;
	}

	public void setAnalysisID(long analysisID) {
		this.analysisID = analysisID;
	}

	public double getProgress() {
		return progress;
	}

	public void setProgress(double progress) {
		this.progress = progress;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Date getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public static class Status {
		public static String STOPPED = "STOPPED";
		public static String RUNNING = "RUNNING";
		public static String PAUSED = "PAUSED";
		public static String FINISHED = "FINISHED";
	}
}

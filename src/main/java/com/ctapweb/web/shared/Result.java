/**
 * 
 */
package com.ctapweb.web.shared;

import java.io.Serializable;
import java.util.Date;

import com.google.gwt.view.client.ProvidesKey;

/**
 * A DTO for analysis.
 * @author xiaobin
 *
 */
public class Result implements Serializable {

	private long id;
	private long analysisID;
	private long textID;
	private long featureID;
	private double value;

	/**
	 * The key provider that provides the unique ID of a contact.
	 */
	public static final ProvidesKey<Result> KEY_PROVIDER = new ProvidesKey<Result>() {
		@Override
		public Object getKey(Result item) {
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

	public long getTextID() {
		return textID;
	}

	public void setTextID(long textID) {
		this.textID = textID;
	}

	public long getFeatureID() {
		return featureID;
	}

	public void setFeatureID(long featureID) {
		this.featureID = featureID;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}


	
}

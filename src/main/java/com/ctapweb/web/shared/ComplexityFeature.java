/**
 * 
 */
package com.ctapweb.web.shared;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.google.gwt.view.client.ProvidesKey;

/**
 * A DTO for Complexity features.
 * @author xiaobin
 * 
 */
public class ComplexityFeature implements Serializable{

	private long id;
	private String name;
	private String description;
	private Date createDate;
	private List<AnalysisEngine> aeList;


	/**
	 * The key provider that provides the unique ID of a contact.
	 */
	public static final ProvidesKey<ComplexityFeature> KEY_PROVIDER = new ProvidesKey<ComplexityFeature>() {
		@Override
		public Object getKey(ComplexityFeature item) {
			return item == null ? null : item.getId();
		}
	};	

	public ComplexityFeature() {

	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	public Date getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	public List<AnalysisEngine> getAeList() {
		return aeList;
	}
	public void setAeList(List<AnalysisEngine> aeList) {
		this.aeList = aeList;
	}
}

/**
 * 
 */
package com.ctapweb.web.shared;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.google.gwt.view.client.ProvidesKey;

/**
 * A DTO for accessing feature sets.
 * @author xiaobin
 *
 */
public class FeatureSet implements Serializable {
	
	private long id;
	private long ownerId;
	private String name;
	private String description;
	private Date createDate;
//	private List<ComplexityFeature> featureList;
	
    public static final ProvidesKey<FeatureSet> KEY_PROVIDER = new ProvidesKey<FeatureSet>() {
	      @Override
	      public Object getKey(FeatureSet item) {
	        return item == null ? null : item.getId();
	      }
	    };	

	 /**
	  * Empty constructor required by seriablization.
	  */
	  public FeatureSet() {
		  
	  }

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(long ownerId) {
		this.ownerId = ownerId;
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


	  
}

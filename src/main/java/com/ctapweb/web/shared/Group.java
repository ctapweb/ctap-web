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
public class Group implements Serializable {
	
	private long id;
	private long analysisID;
	private String name;
	private String description;
	private Date createDate;
	
    public static final ProvidesKey<Group> KEY_PROVIDER = new ProvidesKey<Group>() {
	      @Override
	      public Object getKey(Group item) {
	        return item == null ? null : item.getId();
	      }
	    };	

	 /**
	  * Empty constructor required by seriablization.
	  */
	  public Group() {
		  
	  }

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

/**
 * 
 */
package com.ctapweb.web.shared;

import java.io.Serializable;
import java.util.Date;

import com.google.gwt.view.client.ProvidesKey;

/** A class for storing corpus information.
 * @author xiaobin
 *
 */
public class Corpus implements Serializable {
	
	private long id;
	private long ownerID;
	private String name;
	private long folderID;
	private String folderName;
	private String description;
	private Date createDate;

	/**
     * The key provider that provides the unique ID of a contact.
     */
    public static final ProvidesKey<Corpus> KEY_PROVIDER = new ProvidesKey<Corpus>() {
      @Override
      public Object getKey(Corpus item) {
        return item == null ? null : item.getId();
      }
    };

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getOwnerID() {
		return ownerID;
	}

	public void setOwnerID(long ownerID) {
		this.ownerID = ownerID;
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

	public long getFolderID() {
		return folderID;
	}

	public void setFolderID(long folderID) {
		this.folderID = folderID;
	}

	public String getFolderName() {
		return folderName;
	}

	public void setFolderName(String folderName) {
		this.folderName = folderName;
	}	

}

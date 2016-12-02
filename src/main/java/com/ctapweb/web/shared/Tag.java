/**
 * 
 */
package com.ctapweb.web.shared;

import java.io.Serializable;
import java.util.Date;

import com.google.gwt.view.client.ProvidesKey;

/** A class for storing tag information.
 * @author xiaobin
 *
 */
public class Tag implements Serializable {
	
	private long id;
	private long ownerID;
	private String name;
	private Date createDate;
	private int numText;

	/**
     * The key provider that provides the unique ID of a contact.
     */
    public static final ProvidesKey<Tag> KEY_PROVIDER = new ProvidesKey<Tag>() {
      @Override
      public Object getKey(Tag item) {
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

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public int getNumText() {
		return numText;
	}

	public void setNumText(int numText) {
		this.numText = numText;
	}	
	
}

/**
 * 
 */
package com.ctapweb.web.shared;

import java.io.Serializable;
import java.util.Date;

import com.google.gwt.view.client.ProvidesKey;

/** A class for storing corpus folder information.
 * @author xiaobin
 *
 */
public class CorpusFolder implements Serializable {
	
	private long id;
	private long ownerID;
	private String name;
	private Date createDate;
	private int numCorpora;

	/**
     * The key provider that provides the unique ID of a contact.
     */
    public static final ProvidesKey<CorpusFolder> KEY_PROVIDER = new ProvidesKey<CorpusFolder>() {
      @Override
      public Object getKey(CorpusFolder item) {
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

	public int getNumCorpora() {
		return numCorpora;
	}

	public void setNumCorpora(int numCorpora) {
		this.numCorpora = numCorpora;
	}	
	


}

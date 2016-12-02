/**
 * 
 */
package com.ctapweb.web.shared;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.google.gwt.view.client.ProvidesKey;

/** For storing text of a corpus.
 * @author xiaobin
 *
 */
public class CorpusText implements Serializable {
	private long id  = 0;
	private long corpusID = 0;
	private String title = "";
	private String content = "";
	private Set<Tag> tagSet = new HashSet<>();
	private Date createDate = new Date();

	public CorpusText() {
		
	}
	
	/**
     * The key provider that provides the unique ID of a contact.
     */
    public static final ProvidesKey<CorpusText> KEY_PROVIDER = new ProvidesKey<CorpusText>() {
      @Override
      public Object getKey(CorpusText item) {
        return item == null ? null : item.getId();
      }
    };

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getCorpusID() {
		return corpusID;
	}

	public void setCorpusID(long corpusID) {
		this.corpusID = corpusID;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public Set<Tag> getTagSet() {
		return tagSet;
	}

	public void setTagSet(Set<Tag> tagSet) {
		this.tagSet = tagSet;
	}

	public String getTagString() {
		String tagString = "";

		for(Tag tag: tagSet) {
			tagString += "[" + tag.getName() + "] ";
		}

		return tagString;
	}

	
}

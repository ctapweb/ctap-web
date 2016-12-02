/**
 * 
 */
package com.ctapweb.web.shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.view.client.ProvidesKey;


/**
 * A DTO for Analysis Engines. 
 * @author xiaobin
 *
 */
public class AnalysisEngine implements Serializable {
	
	private long id = 0;
	private String name = ""; 
	private String type = AEType.ANNOTATOR; 
	private String version = "";
	private String vendor = "";
	private String description = "";
	private String descriptorFileName = "";
	private String descriptorFileContent = "";
	private List<AnalysisEngine> aeDependency = new ArrayList<>();
	private Date  createDate = new Date();
	
	/**
     * The key provider that provides the unique ID of a contact.
     */
    public static final ProvidesKey<AnalysisEngine> KEY_PROVIDER = new ProvidesKey<AnalysisEngine>() {
      @Override
      public Object getKey(AnalysisEngine item) {
        return item == null ? null : item.getId();
      }
    };	
    
	/**
	 * Empty constructor required by serialization.
	 */
	public AnalysisEngine() {
		
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

	@Override
	public boolean equals(Object obj) {
		//if two analysis engines have the same id, they are the same
		return this.id == ((AnalysisEngine)obj).getId() ? true: false;
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

	public String getDescriptorFileName() {
		return descriptorFileName;
	}

	public void setDescriptorFileName(String descriptorFile) {
		this.descriptorFileName = descriptorFile;
	}


	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getVendor() {
		return vendor;
	}

	public void setVendor(String vendor) {
		this.vendor = vendor;
	}

	public String getDescriptorFileContent() {
		return descriptorFileContent;
	}

	public void setDescriptorFileContent(String descriptorFileContent) {
		this.descriptorFileContent = descriptorFileContent;
	}

	public List<AnalysisEngine> getAeDependency() {
		return aeDependency;
	}

	public void setAeDependency(List<AnalysisEngine> aeDependency) {
		this.aeDependency = aeDependency;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public class AEType {
		public static final String ANNOTATOR = "ANNOTATOR";
		public static final String FEATURE_EXTRACTOR = "FEATURE_EXTRACTOR";
	}
	
}

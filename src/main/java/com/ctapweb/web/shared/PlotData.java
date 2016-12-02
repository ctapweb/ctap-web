/**
 * 
 */
package com.ctapweb.web.shared;

import java.io.Serializable;

/**
 * A DTO for plot data.
 * @author xiaobin
 *
 */
public class PlotData implements Serializable {

	private String categoryName;
	private double value;

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String name) {
		this.categoryName = name;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

}

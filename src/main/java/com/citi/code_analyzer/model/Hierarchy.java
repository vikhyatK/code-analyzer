package com.citi.code_analyzer.model;

import java.util.ArrayList;
import java.util.List;

public class Hierarchy {

	private String fullyQualifiedName;
	private List<String> restDetails = new ArrayList<>();

	public Hierarchy(String fullyQualifiedName, List<String> restDetails) {
		super();
		this.fullyQualifiedName = fullyQualifiedName;
		this.restDetails = restDetails;
	}

	public Hierarchy() {
		super();
	}

	public String getFullyQualifiedName() {
		return fullyQualifiedName;
	}

	public void setFullyQualifiedName(String fullyQualifiedName) {
		this.fullyQualifiedName = fullyQualifiedName;
	}

	public List<String> getRestDetails() {
		return restDetails;
	}

	public void setRestDetails(List<String> restDetails) {
		this.restDetails = restDetails;
	}

}

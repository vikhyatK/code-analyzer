package com.citi.code_analyzer.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataOperation {

	private String operation;
	private List<MethodDto> methods = new ArrayList<>();;
	private Map<MethodDto, List<List<Hierarchy>>> hierarchy = new HashMap<>();

	public DataOperation(String operation, List<MethodDto> methods) {
		super();
		this.operation = operation;
		this.methods = methods;
	}

	public DataOperation() {
		super();
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public List<MethodDto> getMethods() {
		return methods;
	}

	public void setMethods(List<MethodDto> methods) {
		this.methods = methods;
	}

	public Map<MethodDto, List<List<Hierarchy>>> getHierarchy() {
		return hierarchy;
	}

	public void setHierarchy(Map<MethodDto, List<List<Hierarchy>>> hierarchy) {
		this.hierarchy = hierarchy;
	}

}

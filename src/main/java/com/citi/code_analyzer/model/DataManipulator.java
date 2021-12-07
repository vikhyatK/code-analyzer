package com.citi.code_analyzer.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

public class DataManipulator {

	private String className;
	private List<DataOperation> operation = new ArrayList<>();

	public DataManipulator(String className, List<DataOperation> operation) {
		super();
		this.className = className;
		this.operation = operation;
	}

	public DataManipulator() {
		super();
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public List<DataOperation> getOperation() {
		return operation;
	}

	public void setOperation(List<DataOperation> operation) {
		this.operation = operation;
	}

	public boolean isEmpty() {
		return CollectionUtils.isEmpty(operation) || (operation.size() == 1 && (Objects.isNull(operation.get(0))
				|| CollectionUtils.isEmpty(operation.get(0).getMethods())
				|| (operation.get(0).getMethods().size() == 1 && (Objects.isNull(operation.get(0).getMethods().get(0))
						|| StringUtils.isBlank(operation.get(0).getMethods().get(0).getSimpleName())))));
	}

}

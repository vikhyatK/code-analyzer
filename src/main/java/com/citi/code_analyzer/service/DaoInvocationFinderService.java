package com.citi.code_analyzer.service;

import java.util.Arrays;
import java.util.List;

import com.citi.code_analyzer.model.DataManipulator;
import com.citi.code_analyzer.model.DataOperation;
import com.citi.code_analyzer.model.MethodDto;

public class DaoInvocationFinderService {

	public static List<DataManipulator> findMethodsManipulatingTable(String tableName) {
//		DataOperation operation = new DataOperation();
//		operation.setOperation("Update");
//		operation.setMethods(Arrays.asList(new MethodDto("updateStatus", Arrays.asList("java.lang.String status", "java.lang.Integer id")), new MethodDto("save", null),
//				new MethodDto("findByStatusAndStateAndDistrictIgnoreCase", null)));
//		DataManipulator dm = new DataManipulator();
//		dm.setClassName("com.kef.org.rest.repository.SeniorCitizenRepository");
//		dm.setOperation(Arrays.asList(operation));
//		return Arrays.asList(dm);
		
		DataOperation operation = new DataOperation();
		operation.setOperation("UPDATE");
		operation.setMethods(
				Arrays.asList(new MethodDto("m6", Arrays.asList("java.lang.String str", "int i")),
						new MethodDto("m6", Arrays.asList("int", "String")),
						new MethodDto("findByStatusAndStateAndDistrictIgnoreCase", null)));
		DataManipulator dm = new DataManipulator();
		dm.setClassName("test.Test3");
		dm.setOperation(Arrays.asList(operation));
		return Arrays.asList(dm);
	}

}

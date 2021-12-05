package com.citi.code_analyzer.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.visitor.filter.TypeFilter;

public class MethodService {

	public static Map<String, List<String>> findMethodsManipulatingTable(String tableName) {
		Map<String, List<String>> classToMethodNameMap = new HashMap<>();
//		classToMethodNameMap.put("com.kef.org.rest.service.VolunteerService", Arrays.asList("getVolunteerListByQuery", "loadFileAsResource"));
		classToMethodNameMap.put("test.Test3", Arrays.asList("m6"));
		return classToMethodNameMap;
	}

	public static Map<String, List<List<String>>> getHierarchy(Launcher launcher, Map<String, List<String>> classToMethodName) {
		if (MapUtils.isEmpty(classToMethodName)) {
			System.err.println("None of the methods process this data");
		}
		Factory factory = launcher.getFactory();
		CtModel ctModel = launcher.getModel();
		Map<String, List<List<String>>> map = new HashMap<>();
		classToMethodName.entrySet().forEach(entry -> {
			CtClass<?> aClass = factory.Class().get(entry.getKey());
			if (aClass == null) {
				System.err.println(String.format("Class [%s] not found", entry.getKey()));
				return;
			}
			entry.getValue().forEach(methodName -> {
				CtMethod<?> ctMethod = aClass.getMethodsByName(methodName).get(0);
				List<String> visitedMethods = new ArrayList<>();
				List<List<String>> collectedHirarchy = collectHirarchy(ctModel, ctMethod, visitedMethods);
				map.put(methodName, collectedHirarchy);
			});
		});
		printHierarchy(map);
		return map;
	}
	
	private static List<List<String>> collectHirarchy(CtModel ctModel, CtMethod<?> method,
			List<String> visitedMethods) {
		List<List<String>> toReturn = new ArrayList<>();
		if (Objects.isNull(method)) {
			toReturn.add(visitedMethods);
			return toReturn;
		}
		visitedMethods.add(method.getReference().getDeclaringType().getQualifiedName() + "."
				+ method.getReference().getSignature());
		List<CtMethod<?>> callers = ctModel.getElements(new TypeFilter<CtInvocation<?>>(CtInvocation.class) {
			@Override
			public boolean matches(CtInvocation<?> element) {
				CtExecutableReference<?> executable = element.getExecutable();
				if (executable.getSimpleName().equals(method.getSimpleName())
						&& executable.isOverriding(method.getReference())) {
					return true;
				}
				return false;
			}
		}).stream().map(i -> {
			CtMethod<?> parent = i.getParent(CtMethod.class);
			return parent;
		}).collect(Collectors.toList());
		if(CollectionUtils.isEmpty(callers)) {
			toReturn.add(visitedMethods);
			return toReturn;
		}
		for (CtMethod<?> ctMethod : callers) {
			List<String> duplicateMethodHierarchyForNewPath = new ArrayList<>();
			duplicateMethodHierarchyForNewPath.addAll(visitedMethods);
			toReturn.addAll(collectHirarchy(ctModel, ctMethod, duplicateMethodHierarchyForNewPath));
		}
		return toReturn;
	}
	
	private static void printHierarchy(Map<String, List<List<String>>> map) {
		map.entrySet().forEach(entry -> {
			System.out.println("Hierarchy for [" + entry.getKey() + "]");
			int count = 1;
			System.out.println(String.format("Total call hierarchies found : [%d]", entry.getValue().size()));
			for(List<String> hierarchy: entry.getValue()) {
				System.out.println("Hierarchy " + count++);
				String tab = " |__ ";
				for(int i = 0; i < hierarchy.size() ; i++) {
					if(i == 0) {
						System.out.println(hierarchy.get(i));	
					} else {
						System.out.println(tab + hierarchy.get(i));
						tab = "    " + tab;
					}
				}
			}
			System.out.println();
		});
	}
}

package com.citi.code_analyzer.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.MapUtils;

import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.visitor.CtScanner;
import spoon.reflect.visitor.filter.TypeFilter;

public class MethodService {

	public static Map<String, List<String>> findManipulatingMethodsOfTable(String tableName) {
		Map<String, List<String>> classToMethodNameMap = new HashMap<>();
		classToMethodNameMap.put("com.kef.org.rest.service.VolunteerService", Arrays.asList("findvolunteerDetails"));
		return classToMethodNameMap;
	}

	public static void getStack(Launcher launcher, Map<String, List<String>> classToMethodName) {
		if (MapUtils.isEmpty(classToMethodName)) {
			System.err.println("None of the methods process this data");
		}
		Factory factory = launcher.getFactory();
		CtModel ctModel = launcher.getModel();
		classToMethodName.entrySet().forEach(entry -> {
			CtClass<?> aClass = factory.Class().get(entry.getKey());
			if(aClass == null ) {
				System.err.println(String.format("Class [%s] not found", entry.getKey()));
				return;
			}
			entry.getValue().forEach(methodName -> {
				CtMethod<?> ctMethod = aClass.getMethodsByName(methodName).get(0);
				List<CtMethod> callers = ctModel.getElements(new TypeFilter<CtInvocation>(CtInvocation.class) {
					@Override
					public boolean matches(CtInvocation element) {
						CtExecutableReference<?> executable = element.getExecutable();
						if (executable.getSimpleName().equals(ctMethod.getSimpleName())
								&& executable.isOverriding(ctMethod.getReference())) {
							return true;
						}
						return false;
					}
				}).stream().map(i -> {
					CtMethod parent = i.getParent(CtMethod.class);
					System.out.println(i.getExecutable().getDeclaringType().getQualifiedName() + "." + i.getExecutable().getSignature());
					System.out.println(parent.getDeclaringType().getQualifiedName() + "." + parent.getSignature());
					return parent;
				}).collect(Collectors.toList());

				ctModel.getRootPackage().accept(new CtScanner() {
					@Override
					public <T> void visitCtInvocation(CtInvocation<T> invocation) {
						CtExecutableReference<T> executable = invocation.getExecutable();
						for (int i = 0; i < callers.size(); i++) {
							CtMethod method = callers.get(i);
							if (method.getSignature().equals(executable.getSignature())) {
								CtMethod parent = invocation.getParent(CtMethod.class);
								System.out.println(
										parent.getDeclaringType().getQualifiedName() + "." + parent.getSignature());
							}
						}
						super.visitCtInvocation(invocation);
					}
				});
			});

		});
	}
}

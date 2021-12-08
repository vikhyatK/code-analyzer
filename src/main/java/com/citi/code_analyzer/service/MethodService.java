package com.citi.code_analyzer.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import com.citi.code_analyzer.model.DataManipulator;
import com.citi.code_analyzer.model.DataOperation;
import com.citi.code_analyzer.model.Hierarchy;
import com.citi.code_analyzer.model.MethodDto;

import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtInterface;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.visitor.filter.TypeFilter;

public class MethodService {

	public static List<DataManipulator> getHierarchy(Launcher launcher, List<DataManipulator> dataManipulators) {
		if (CollectionUtils.isEmpty(dataManipulators) || (dataManipulators.size() == 1 && dataManipulators.get(0).isEmpty())) {
			System.err.println("None of the methods process this data");
		}
		Factory factory = launcher.getFactory();
		CtModel ctModel = launcher.getModel();
		dataManipulators.forEach(dm -> {
			CtClass<?> aClass = factory.Class().get(dm.getClassName());
			if (aClass == null) {
				CtInterface<?> aInterface = factory.Interface().get(dm.getClassName());
				if(aInterface == null) {
					System.err.println(String.format("Class [%s] not found", dm.getClassName()));
					return;
				}
				interfaceFlow(ctModel, dm, aInterface);
				return;
			}
			classFlow(ctModel, dm, aClass);
		});
		printHierarchy(dataManipulators);
		return dataManipulators;
	}

	private static void interfaceFlow(CtModel ctModel, DataManipulator dm, CtInterface<?> aInterface) {
		dm.getOperation().forEach(operation -> {
			operation.getMethods().forEach(method -> {
				String methodName = method.getSimpleName();
				List<CtMethod<?>> methodsByNameList = aInterface.getMethodsByName(methodName);
				if (CollectionUtils.isEmpty(methodsByNameList)) {
					Set<CtMethod<?>> allMethods = aInterface.getAllMethods();
					for(CtMethod<?> m : allMethods) {
						if (m.getSimpleName().equals(methodName) && !CollectionUtils.isEmpty(m.getParameters())) {
							methodsByNameList = selectMethodBasedOnParamters(method, allMethods);
						} else if (m.getSimpleName().equals(methodName)){
							methodsByNameList = Arrays.asList(m);
						}
					}
					collectHierarchy(ctModel, operation, method, methodsByNameList);
					return;
				} else if (!CollectionUtils.isEmpty(method.getParameters())) {
					methodsByNameList = selectMethodBasedOnParamters(method, methodsByNameList);
				}
				collectHierarchy(ctModel, operation, method, methodsByNameList);
			});
		});
	}

	private static void classFlow(CtModel ctModel, DataManipulator dm, CtClass<?> aClass) {
		dm.getOperation().forEach(operation -> {
			operation.getMethods().forEach(method -> {
				String methodName = method.getSimpleName();
				List<CtMethod<?>> methodsByNameList = aClass.getMethodsByName(methodName);
				if (CollectionUtils.isEmpty(methodsByNameList)) {
					Set<CtMethod<?>> allMethods = aClass.getAllMethods();
					for(CtMethod<?> m : allMethods) {
						if (m.getSimpleName().equals(methodName) && !CollectionUtils.isEmpty(m.getParameters())) {
							methodsByNameList = selectMethodBasedOnParamters(method, allMethods);
						} else if (m.getSimpleName().equals(methodName)){
							methodsByNameList = Arrays.asList(m);
						}
					}
					collectHierarchy(ctModel, operation, method, methodsByNameList);
					return;
				} else if (!CollectionUtils.isEmpty(method.getParameters())) {
					methodsByNameList = selectMethodBasedOnParamters(method, methodsByNameList);
				}
				collectHierarchy(ctModel, operation, method, methodsByNameList);
			});
		});
	}
	
	private static List<CtMethod<?>> selectMethodBasedOnParamters(MethodDto method,
			Collection<? extends CtMethod<?>> methodsByNameList) {
		List<CtMethod<?>> selectedMethod = new ArrayList<>();
		for (CtMethod<?> methodByName : methodsByNameList) {
			List<CtParameter<?>> parameters = methodByName.getParameters();
			if(!CollectionUtils.isEmpty(parameters) && parameters.size() == method.getParameters().size()) {
				boolean paramMatches = true;
				for(int i = 0; i < parameters.size(); i++) {
					if(!(parameters.get(i).toString().equals(method.getParameters().get(i)) ||
							parameters.get(i).getType().getSimpleName().equals(method.getParameters().get(i)))) {
						paramMatches = false;
						break;
					}
				}
				if(paramMatches) {
					selectedMethod = Arrays.asList(methodByName);
				}
			}
		}
		return selectedMethod;
	}

	private static void collectHierarchy(CtModel ctModel, DataOperation operation, MethodDto method,
			List<CtMethod<?>> methodsByNameList) {
		if(CollectionUtils.isEmpty(methodsByNameList)) {
			operation.getHierarchy().computeIfAbsent(method, m -> new ArrayList<>());
			return;
		}
		CtMethod<?> ctMethod = methodsByNameList.get(0);
		List<Hierarchy> visitedMethods = new ArrayList<>();
		List<List<Hierarchy>> collectedHirarchy = collectHirarchy(ctModel, ctMethod, visitedMethods);
		operation.getHierarchy().computeIfAbsent(method, m -> collectedHirarchy);
	}
	
	private static List<List<Hierarchy>> collectHirarchy(CtModel ctModel, CtMethod<?> method,
			List<Hierarchy> visitedMethods) {
		List<List<Hierarchy>> toReturn = new ArrayList<>();
		if (Objects.isNull(method)) {
			toReturn.add(visitedMethods);
			return toReturn;
		}
		visitedMethods.add(new Hierarchy(method.getReference().getDeclaringType().getQualifiedName() + "."
				+ method.getReference().getSignature(), null));
		List<CtMethod<?>> callers = ctModel.getElements(new TypeFilter<CtInvocation<?>>(CtInvocation.class) {
			@Override
			public boolean matches(CtInvocation<?> element) {
				CtExecutableReference<?> executable = element.getExecutable();
				if (executable.getSignature().equals(method.getSignature())
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
			List<Hierarchy> duplicateMethodHierarchyForNewPath = new ArrayList<>();
			duplicateMethodHierarchyForNewPath.addAll(visitedMethods);
			toReturn.addAll(collectHirarchy(ctModel, ctMethod, duplicateMethodHierarchyForNewPath));
		}
		return toReturn;
	}
	
	private static void printHierarchy(List<DataManipulator> dataManipulators) {
		dataManipulators.forEach(dm -> {
			System.out.println(String.format("Class Name : [%s]", dm.getClassName()));
			final String tab = "    ";
			dm.getOperation().forEach(operation -> {
				System.out.println(String.format(tab + "Operation Name : [%s]", operation.getOperation()));
				operation.getHierarchy().entrySet().forEach(entry -> {
					if (CollectionUtils.isEmpty(entry.getValue())) {
						System.out.println(String.format(tab + tab + "Hierarchy for [%s] param %s NOT FOUND.", entry.getKey().getSimpleName(), entry.getKey().getParameters()));
						System.out.println();
						return;
					}
					if (entry.getValue().get(0).size() == 1) {
						System.out.println(String.format(tab + tab + "Method [%s] param %s NOT INVOKED. ", entry.getKey().getSimpleName(), entry.getKey().getParameters()));
						System.out.println();
						return;
					}
					System.out.println(String.format(tab + tab + "Hierarchy for [%s] param %s", entry.getKey().getSimpleName(), entry.getKey().getParameters()));
					int count = 1;
					System.out.println(String.format(tab + tab + "Total call hierarchies found : [%d]", entry.getValue().size()));
					for (List<Hierarchy> hierarchy : entry.getValue()) {
						System.out.println(tab + tab + "Hierarchy " + count++ + " --------------");
						String downArrow = tab + tab + " |__ ";
						for (int i = 0; i < hierarchy.size(); i++) {
							List<String> restDetails = hierarchy.get(i).getRestDetails();
							String restDetail = "";
							if(!CollectionUtils.isEmpty(restDetails)) {
								restDetail = "Mapping []" + String.join(" ", restDetails);
							}
							if (i == 0) {
								System.out.println(tab + tab + hierarchy.get(i).getFullyQualifiedName() + restDetail);
							} else {
								System.out.println(downArrow + hierarchy.get(i).getFullyQualifiedName() + restDetail);
								downArrow = tab + downArrow;
							}
						}
					}
					System.out.println();
				});
			});
		});
	}
	
	private static String getMethodMapping(final CtMethod<?> method) {
		Optional<String> restControllerPath = Optional.empty();
		String requestMapping = null;
		for (CtAnnotation<?> annotation : method.getDeclaringType().getAnnotations()) {
			if (annotation.getType().getSimpleName().equals("RestController")) {
				if (!MapUtils.isEmpty(annotation.getAllValues())
						&& Objects.nonNull(annotation.getAllValues().get("value"))
						&& !StringUtils.isBlank(annotation.getAllValues().get("value").toString())) {
					restControllerPath = Optional.of(annotation.getAllValues().get("value").toString());
				}
				for (CtAnnotation<?> methodAnnotations : method.getAnnotations()) {
					if (methodAnnotations.getType().getSimpleName().equals("RequestMapping")
							&& !MapUtils.isEmpty(annotation.getAllValues())
							&& Objects.nonNull(annotation.getAllValues().get("value"))
							&& !StringUtils.isBlank(annotation.getAllValues().get("value").toString())) {
						requestMapping = annotation.getAllValues().get("value").toString();
						if (Objects.nonNull(annotation.getAllValues().get("method"))
								&& !StringUtils.isBlank(annotation.getAllValues().get("method").toString())) {
							requestMapping = requestMapping
									+ " [".concat(annotation.getAllValues().get("method").toString()).concat("]");
						} else {
							requestMapping = requestMapping + " [GET]";
						}
					}
				}
				requestMapping = restControllerPath.isPresent() ? restControllerPath.get().concat(requestMapping) : requestMapping;
			}
		}
		return requestMapping;
	}

//    private static String getRequestType(final RequestMapping requestMapping) {
//        return requestMapping.method() != null && requestMapping.method().length > 0 ?
//                Arrays.stream(requestMapping.method())
//                        .map(m -> "[".concat(m.name()).concat("]"))
//                        .collect(Collectors.joining(" ")) :
//                "[GET]";
//    }
}

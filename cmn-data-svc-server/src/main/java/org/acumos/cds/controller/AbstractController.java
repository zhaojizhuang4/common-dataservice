/*-
 * ===============LICENSE_START=======================================================
 * Acumos
 * ===================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property & Tech Mahindra. All rights reserved.
 * ===================================================================================
 * This Acumos software file is distributed by AT&T and Tech Mahindra
 * under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * This file is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ===============LICENSE_END=========================================================
 */

package org.acumos.cds.controller;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.acumos.cds.CodeNameType;
import org.acumos.cds.service.CodeNameService;
import org.acumos.cds.util.EELFLoggerDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Base controller class that provides utility methods.
 */
public abstract class AbstractController {

	private static final EELFLoggerDelegate logger = EELFLoggerDelegate.getLogger(AbstractController.class);

	@Autowired
	private CodeNameService codeNameService;

	protected static final String NO_ENTRY_WITH_ID = "No entry with ID ";

	/**
	 * For general use in these methods and subclasses
	 */
	protected final ObjectMapper mapper;

	/**
	 * Hello Spring, here's your no-arg constructor.
	 */
	public AbstractController() {
		mapper = new ObjectMapper();
	}

	/**
	 * This is a brutal hack: remove the Pageable query parameters that Spring
	 * mistakenly (?) adds to the multi value map supplied to the controller.
	 * 
	 * @param queryParameters
	 *            Map of query parameters
	 */
	protected void cleanPageableParameters(MultiValueMap<String, String> queryParameters) {
		final String[] pageable = { "page", "size", "sort" };
		for (String p : pageable)
			if (queryParameters.containsKey(p))
				queryParameters.remove(p);
	}

	/**
	 * Creates query parameters with appropriate types. Uses reflection to check
	 * field names and discover field types; converts the values appropriately.
	 * Handles single/multiple values appropriately. Any field may be declared on a
	 * superclass.
	 * 
	 * @param modelClass
	 *            Model class with fields named in the query parameters
	 * @param queryParameters
	 *            Maps field names to (list of) field values
	 * @return Map of String to Object
	 * @throws IllegalArgumentException
	 *             If an unexpected data type is used
	 * @throws NoSuchFieldException
	 *             If a field name is not found in the class
	 */
	protected Map<String, Object> convertQueryParameters(Class<?> modelClass,
			MultiValueMap<String, String> queryParameters) throws NoSuchFieldException {

		Class<?> clazz = modelClass;
		HashMap<String, Object> convertedQryParm = new HashMap<>();
		// Track the fields that were found and processed
		Set<String> fieldNames = new HashSet<>();
		fieldNames.addAll(queryParameters.keySet());
		while (clazz != null) {
			logger.trace(EELFLoggerDelegate.debugLogger, "convertQueryParameters: checking class {}", clazz.getName());
			Field[] fields = clazz.getDeclaredFields();
			Iterator<Map.Entry<String, List<String>>> iter = queryParameters.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry<String, List<String>> queryParm = iter.next();
				Field f = null;
				for (Field field : fields)
					if (queryParm.getKey().equals(field.getName())) {
						// Matched a field
						f = field;
						fieldNames.remove(field.getName());
						break;
					}
				if (f == null)
					continue;
				logger.trace(EELFLoggerDelegate.debugLogger, "convertQueryParameters: field {} type is {}", f.getName(),
						f.getType());
				if (f.getType().equals(String.class)) {
					if (queryParm.getValue().size() == 1)
						convertedQryParm.put(queryParm.getKey(), queryParm.getValue().get(0));
					else
						convertedQryParm.put(queryParm.getKey(), queryParm.getValue().toArray());
				} else if (f.getType().equals(Boolean.class) || f.getType().equals(boolean.class)) {
					// Array not useful for Boolean
					convertedQryParm.put(queryParm.getKey(), Boolean.valueOf(queryParm.getValue().get(0)));
				} else if (f.getType().equals(Integer.class)) {
					if (queryParm.getValue().size() == 1) {
						convertedQryParm.put(queryParm.getKey(), Integer.parseInt(queryParm.getValue().get(0)));
					} else {
						Integer[] array = new Integer[queryParm.getValue().size()];
						for (int i = 0; i < array.length; ++i)
							array[i] = Integer.parseInt(queryParm.getValue().get(i));
						convertedQryParm.put(queryParm.getKey(), array);
					}
				} else if (f.getType().equals(Long.class)) {
					if (queryParm.getValue().size() == 1) {
						convertedQryParm.put(queryParm.getKey(), Long.parseLong(queryParm.getValue().get(0)));
					} else {
						Long[] array = new Long[queryParm.getValue().size()];
						for (int i = 0; i < array.length; ++i)
							array[i] = Long.parseLong(queryParm.getValue().get(i));
						convertedQryParm.put(queryParm.getKey(), array);
					}
				} else if (f.getType().equals(Date.class)) {
					if (queryParm.getValue().size() == 1) {
						convertedQryParm.put(queryParm.getKey(), new Date(Long.parseLong(queryParm.getValue().get(0))));
					} else {
						Date[] array = new Date[queryParm.getValue().size()];
						for (int i = 0; i < array.length; ++i)
							array[i] = new Date(Long.parseLong(queryParm.getValue().get(i)));
						convertedQryParm.put(queryParm.getKey(), array);
					}
				} else
					throw new IllegalArgumentException("Unhandled type " + f.getType().toString() + " on field "
							+ f.getName() + " in class " + clazz.getName());
			}
			// recurse up
			clazz = clazz.getSuperclass();
		}
		// Report names of any parameters (fields) that were not found in the classes
		if (!fieldNames.isEmpty()) {
			String fieldName = fieldNames.iterator().next();
			throw new NoSuchFieldException("Failed to find field " + fieldName);
		}
		return convertedQryParm;
	}

	/**
	 * Searches the exception-cause stack for an exception caused by violated
	 * constraints.
	 * 
	 * @param thrown
	 *            Exception
	 * @return A ConstraintViolationException if found in the exception-cause stack,
	 *         otherwise the original argument.
	 */
	protected Exception findConstraintViolationException(Exception thrown) {
		Exception bestMatch = thrown;
		// Use a new variable to silence Sonar
		Exception ex = thrown;
		while (ex != null) {
			// This once searched for database-specific exceptions by name, for example
			// com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException,
			// but the project is not using MySql and Sonar flagged the usage of a class
			// name as a critical bug.
			if (ex instanceof javax.validation.ConstraintViolationException
					|| ex instanceof org.hibernate.exception.ConstraintViolationException)
				bestMatch = ex;

			// Go as far up the stack as possible
			if (ex.getCause() instanceof Exception)
				ex = (Exception) ex.getCause();
			else
				break;
		}
		return bestMatch;
	}

	/**
	 * Validates the specified code against the specified type
	 * 
	 * @param code
	 *            Code value
	 * @param type
	 *            Value set name
	 * @throws IllegalArgumentException
	 *             if the code is null or not recognized
	 */
	protected void validateCode(String code, CodeNameType type) {
		if (code == null)
			throw new IllegalArgumentException("Unexpected null for CodeNameType " + type.name());
		if (!codeNameService.validateCode(code, type))
			throw new IllegalArgumentException("Unexpected code " + code + " for CodeNameType " + type.name());
	}

}

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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.acumos.cds.query.SearchCriteria;
import org.acumos.cds.query.SearchCriterion;
import org.acumos.cds.util.EELFLoggerDelegate;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Base controller class that provides utility methods.
 */
public abstract class AbstractController {

	private static final EELFLoggerDelegate logger = EELFLoggerDelegate.getLogger(AbstractController.class);

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
	 * Creates query parameters with appropriate types. Uses reflection to discover
	 * field types and converts the values appropriately. Any field may be declared
	 * on a superclass.
	 * 
	 * @param modelClass
	 *            Model class with fields named in the query parameters
	 * @param queryParameters
	 *            Maps field names to expected field values
	 * @return Map of String to Object
	 * @throws Exception
	 *             on any failure; e.g., unknown field, unparseable number, etc.
	 */
	protected Map<String, Object> convertQueryParameters(Class<?> modelClass, Map<String, String> queryParameters)
			throws NoSuchFieldException {
		Class<?> clazz = modelClass;
		HashMap<String, Object> convertedQryParm = new HashMap<>();
		HashMap<String, String> queryParamsCopy = new HashMap<>();
		queryParamsCopy.putAll(queryParameters);
		while (clazz != null) {
			logger.trace(EELFLoggerDelegate.debugLogger, "convertQueryParameters: checking class {}", clazz.getName());
			Field[] fields = clazz.getDeclaredFields();
			Iterator<Map.Entry<String, String>> iter = queryParamsCopy.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry<String, String> queryParm = iter.next();
				Field f = null;
				for (Field field : fields)
					if (queryParm.getKey().equals(field.getName())) {
						f = field;
						iter.remove();
						break;
					}
				if (f == null)
					continue;
				logger.trace(EELFLoggerDelegate.debugLogger, "convertQueryParameters: type is " + f.getType());
				if (f.getType().equals(String.class))
					convertedQryParm.put(queryParm.getKey(), queryParm.getValue());
				else if (f.getType().equals(Boolean.class) || f.getType().equals(boolean.class))
					convertedQryParm.put(queryParm.getKey(), Boolean.valueOf(queryParm.getValue()));
				else if (f.getType().equals(Integer.class))
					convertedQryParm.put(queryParm.getKey(), Integer.parseInt(queryParm.getValue()));
				else if (f.getType().equals(Long.class))
					convertedQryParm.put(queryParm.getKey(), Long.parseLong(queryParm.getValue()));
				else if (f.getType().equals(Date.class))
					convertedQryParm.put(queryParm.getKey(), new Date(Long.parseLong(queryParm.getValue())));
				else
					throw new IllegalArgumentException("Unhandled type " + f.getType().toString() + " on field "
							+ f.getName() + " in class " + clazz.getName());
			}
			// recurse up
			clazz = clazz.getSuperclass();
		}
		// Check if any params were missed
		if (queryParamsCopy.size() > 0) {
			String fieldName = queryParamsCopy.keySet().iterator().next();
			throw new IllegalArgumentException("Failed to find field name " + fieldName);
		}
		return convertedQryParm;
	}

	/**
	 * Converts criteria Strings to appropriately typed values. Uses reflection on
	 * the clas to discover field types, then converts the values appropriately.
	 * Note that any field may be declared on a superclass, so must recurse up the
	 * inheritance path.
	 * 
	 * @param modelClass
	 *            Model class with fields named in the query criteria
	 * @param searchCriteria
	 *            List of field, operator, value tuples
	 * @throws NoSuchFieldException
	 *             on unknown field
	 * @throws NumberFormatException
	 *             on unparseable number
	 */
	protected void convertSearchCriteriaValues(Class<?> modelClass, SearchCriteria searchCriteria)
			throws NoSuchFieldException {

		// Set up to detect unknown field names
		List<String> fieldNames = new ArrayList<>();
		Iterator<SearchCriterion> iter = searchCriteria.iterator();
		while (iter.hasNext())
			fieldNames.add(iter.next().getKey());
		
		Class<?> clazz = modelClass;
		while (clazz != null) {
			logger.trace(EELFLoggerDelegate.debugLogger, "convertSearchCriteriaValues: checking class {}",
					clazz.getName());
			Field[] fields = clazz.getDeclaredFields();
			Iterator<SearchCriterion> scIter = searchCriteria.iterator();
			while (scIter.hasNext()) {
				SearchCriterion criterion = scIter.next();
				Field f = null;
				for (Field field : fields)
					if (criterion.getKey().equals(field.getName())) {
						f = field;
						break;
					}
				if (f == null)
					continue;
				// Found the field
				fieldNames.remove(criterion.getKey());
				logger.trace(EELFLoggerDelegate.debugLogger, "convertSearchCriteriaValues: type is " + f.getType());
				if (f.getType().equals(String.class))
					; // nothing to do, it's already String
				else if (f.getType().equals(Boolean.class) || f.getType().equals(boolean.class))
					criterion.setValue(Boolean.valueOf(criterion.getValue().toString()));
				else if (f.getType().equals(Integer.class))
					criterion.setValue(Integer.parseInt(criterion.getValue().toString()));
				else if (f.getType().equals(Long.class))
					criterion.setValue(Long.parseLong(criterion.getValue().toString()));
				else if (f.getType().equals(Date.class))
					criterion.setValue(new Date(Long.parseLong(criterion.getValue().toString())));
				else
					throw new IllegalArgumentException("Unhandled type " + f.getType().toString() + " on field "
							+ f.getName() + " in class " + clazz.getName());
			}
			// recurse up
			clazz = clazz.getSuperclass();
		}
		// Check if any params were missed
		if (!fieldNames.isEmpty())
			throw new IllegalArgumentException("Failed to find field name " + fieldNames.get(0));
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
			// Don't stop here, go as far back as possible

			if (ex.getCause() instanceof Exception)
				ex = (Exception) ex.getCause();
			else
				break;
		}
		return bestMatch;
	}

}

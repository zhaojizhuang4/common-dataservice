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

package org.acumos.cds.service;

import java.util.List;
import java.util.Map;

import org.acumos.cds.domain.MLPSolution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Defines methods to query solution information.
 */
public interface SolutionSearchService {

	/**
	 * Gets all instances matching all query parameters.
	 * 
	 * @param queryParameters
	 *            field-name, field-value pairs; ignored if null or empty.
	 * @param isOr
	 *            If true, the query is a disjunction ("or"); otherwise the query is
	 *            a conjunction ("and").
	 * @return List of instances, which may be empty.
	 */
	List<MLPSolution> getSolutions(Map<String, ? extends Object> queryParameters, boolean isOr);

	/**
	 * Gets a page of instances matching all query parameters.
	 * 
	 * @param nameKeyword
	 *            Searches the name field for the keyword using case-insensitive
	 *            LIKE after surrounding with wildcard '%' characters; ignored if
	 *            null or empty.
	 * @param descriptionKeyword
	 *            Searches the description field for the keyword using
	 *            case-insensitive LIKE after surrounding with wildcard '%'
	 *            characters; ignored if null or empty.
	 * @param authorKeyword
	 *            Not implemented
	 * @param active
	 *            Active status: true or false; required.
	 * @param accessTypeCodes
	 *            Limits match to solutions with one of the specified values
	 *            including null (not the 4-character sequence "null"); ignored if
	 *            null or empty.
	 * @param modelTypeCodes
	 *            Limits match to solutions with one of the specified values
	 *            including null (not the 4-character sequence "null"); ignored if
	 *            null or empty.
	 * @param validationStatusCodes
	 *            Limits match to solutions with one of the specified values
	 *            including null (not the 4-character sequence "null"); ignored if
	 *            null or empty.
	 * @param tags
	 *            Not implemented
	 * @param pageable
	 *            Page and sort info
	 * @return Page of matches
	 */
	Page<MLPSolution> findPortalSolutions(String nameKeyword, String descriptionKeyword, String authorKeyword,
			boolean active, String[] accessTypeCodes, String[] modelTypeCodes, String[] validationStatusCodes,
			String[] tags, Pageable pageable);

}

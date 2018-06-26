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

import java.util.Date;
import java.util.Map;

import org.acumos.cds.domain.MLPSolution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Defines methods to query solution information.
 */
public interface SolutionSearchService {

	/**
	 * Searches for instances matching all or one of the query parameters, depending
	 * on the isOr parameter.
	 * 
	 * @param queryParameters
	 *            field-name, field-value pairs. Value may be scalar or array.
	 * @param isOr
	 *            If true, the query is a disjunction ("or"); otherwise the query is
	 *            a conjunction ("and").
	 * @param pageable
	 *            Page and sort criteria
	 * @return Page of instances, which may be empty.
	 */
	Page<MLPSolution> findSolutions(Map<String, ? extends Object> queryParameters, boolean isOr, Pageable pageable);

	/**
	 * Gets a page of solutions matching all query parameters.
	 * 
	 * @param nameKeywords
	 *            Searches the name field for the keywords using case-insensitive
	 *            LIKE after surrounding with wildcard '%' characters; ignored if
	 *            null or empty
	 * @param descriptionKeywords
	 *            Searches the description field for the keywords using
	 *            case-insensitive LIKE after surrounding with wildcard '%'
	 *            characters; ignored if null or empty
	 * @param active
	 *            Active status: true or false; required.
	 * @param ownerIds
	 *            Limits match to solutions with one of the specified values;
	 *            ignored if null or empty
	 * @param modelTypeCodes
	 *            Limits match to solutions with one of the specified values
	 *            including null (not the 4-character sequence "null"); ignored if
	 *            null or empty
	 * @param accessTypeCodes
	 *            Limits match to solutions containing revisions with one of the
	 *            specified values including null (not the 4-character sequence
	 *            "null"); ignored if null or empty
	 * @param validationStatusCodes
	 *            Limits match to solutions containing revisions with one of the
	 *            specified values including null (not the 4-character sequence
	 *            "null"); ignored if null or empty
	 * @param tags
	 *            Limits match to solutions with one of the specified tags; ignored
	 *            if null or empty
	 * @param pageable
	 *            Page and sort info
	 * @return Page of matches
	 */
	Page<MLPSolution> findPortalSolutions(String[] nameKeywords, String[] descriptionKeywords, boolean active,
			String[] ownerIds, String[] modelTypeCodes, String[] accessTypeCodes, String[] validationStatusCodes,
			String[] tags, Pageable pageable);

	/**
	 * Gets a page of user-accessible solutions. This includes the user's own
	 * private solutions as well as solutions shared with the user via the
	 * solution-access-map table.
	 * 
	 * @param nameKeywords
	 *            Searches the name field for the keywords using case-insensitive
	 *            LIKE after surrounding with wildcard '%' characters; ignored if
	 *            null or empty
	 * @param descriptionKeywords
	 *            Searches the description field for the keywords using
	 *            case-insensitive LIKE after surrounding with wildcard '%'
	 *            characters; ignored if null or empty
	 * @param active
	 *            Active status: true or false; required.
	 * @param userId
	 *            ID of the user who owns or has access to the solutions; required.
	 * @param modelTypeCodes
	 *            Limits match to solutions with one of the specified values
	 *            including null (not the 4-character sequence "null"); ignored if
	 *            null or empty
	 * @param accessTypeCodes
	 *            Limits match to solutions containing revisions with one of the
	 *            specified values including null (not the 4-character sequence
	 *            "null"); ignored if null or empty
	 * @param validationStatusCodes
	 *            Limits match to solutions containing revisions with one of the
	 *            specified values including null (not the 4-character sequence
	 *            "null"); ignored if null or empty
	 * @param tags
	 *            Limits match to solutions with one of the specified tags; ignored
	 *            if null or empty
	 * @param pageable
	 *            Page and sort info
	 * @return Page of matches
	 */
	public Page<MLPSolution> findUserSolutions(String[] nameKeywords, String[] descriptionKeywords, boolean active,
			String userId, String[] modelTypeCodes, String[] accessTypeCodes, String[] validationStatusCodes,
			String[] tags, Pageable pageable);

	/**
	 * Gets a page of solutions with a change after the specified date. A match is
	 * found if the solution's modified field, an associated revision's modified
	 * field, or an associated artifact's modified field has a value larger than the
	 * specified date. Only finds solutions that have 1+ revision(s) and in turn 1+
	 * artifact(s). A freshly created solution with no revisions will not be found.
	 * 
	 * @param active
	 *            Active status: true or false; required
	 * @param accessTypeCodes
	 *            Limits match to solutions with one of the specified values
	 *            including null (not the 4-character sequence "null"); ignored if
	 *            null or empty
	 * @param validationStatusCodes
	 *            Limits match to solutions with one of the specified values
	 *            including null (not the 4-character sequence "null"); ignored if
	 *            null or empty
	 * @param modifiedDate
	 *            Last-modified date
	 * @param pageable
	 *            Page and sort info
	 * @return Page of matches
	 */
	Page<MLPSolution> findSolutionsByModifiedDate(boolean active, String[] accessTypeCodes,
			String[] validationStatusCodes, Date modifiedDate, Pageable pageable);

}

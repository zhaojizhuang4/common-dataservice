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

package org.acumos.cds.repository;

import org.acumos.cds.domain.MLPSolutionValidation;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface SolutionValidationRepository
		extends CrudRepository<MLPSolutionValidation, MLPSolutionValidation.SolutionValidationPK> {

	/**
	 * Finds validation records for the specified solution and revision IDs.
	 * 
	 * @param solutionId
	 *            Solution ID
	 * @param revisionId
	 *            RevisionID
	 * @return Iterable of MLPSolutionValidation
	 */
	@Query("SELECT v FROM MLPSolutionValidation v WHERE v.solutionId = :solutionId and v.revisionId = :revisionId")
	Iterable<MLPSolutionValidation> findBySolutionIdRevisionId(@Param("solutionId") String solutionId,
			@Param("revisionId") String revisionId);

	/**
	 * Deletes all entries for the specified solution and revision IDs.
	 * 
	 * @param solutionId
	 *            Solution ID
	 * @param revisionId
	 *            RevisionID
	 */
	@Modifying
	@Transactional // throws exception without this
	@Query(value = "DELETE FROM MLPSolutionValidation v WHERE v.solutionId = :solutionId and v.revisionId = :revisionId")
	void deleteBySolutionIdRevisionId(@Param("solutionId") String solutionId, @Param("revisionId") String revisionId);

	/**
	 * Deletes all entries for the specified solution, which supports cascading
	 * delete.
	 * 
	 * @param solutionId
	 *            Solution ID
	 */
	@Modifying
	@Transactional // throws exception without this
	@Query(value = "DELETE FROM MLPSolutionValidation v WHERE v.solutionId = :solutionId")
	void deleteBySolutionId(@Param("solutionId") String solutionId);

}

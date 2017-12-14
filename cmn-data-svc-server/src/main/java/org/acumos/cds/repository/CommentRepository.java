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

import org.acumos.cds.domain.MLPComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface CommentRepository extends PagingAndSortingRepository<MLPComment, String> {

	/**
	 * Gets the count of comments within a thread.
	 * 
	 * @param threadId
	 *            Thread ID
	 * @return Count of comments
	 */
	@Query("SELECT COUNT(commentId) FROM MLPComment WHERE threadId = :threadId")
	Long countThreadComments(@Param("threadId") String threadId);

	/**
	 * Gets a page of comments with the specified thread ID using Spring magic.
	 * 
	 * @param threadId
	 *            Thread ID
	 * @param pageable
	 *            Page and sort criteria
	 * @return Page of MLPComment
	 */
	Page<MLPComment> findByThreadId(String threadId, Pageable pageable);

	/**
	 * Deletes all entries for the specified thread ID
	 * 
	 * @param threadId
	 *            Thread ID
	 */
	@Modifying
	@Transactional // throws exception without this
	@Query(value = "DELETE FROM MLPComment d WHERE d.threadId = :threadId")
	void deleteThreadComments(@Param("threadId") String threadId);

}

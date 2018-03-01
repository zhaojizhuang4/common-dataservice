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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.acumos.cds.domain.MLPStepResult;
import org.acumos.cds.util.EELFLoggerDelegate;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Hibernate-assisted methods to search step result information.
 */
@Service("stepResultSearchService")
@Transactional
public class StepResultSearchServiceImpl extends AbstractSearchServiceImpl implements StepResultSearchService {

	private final EELFLoggerDelegate logger = EELFLoggerDelegate.getLogger(StepResultSearchServiceImpl.class);

	@Autowired
	private SessionFactory sessionFactory;

	@SuppressWarnings("unchecked")
	@Override
	public Page<MLPStepResult> findStepResults(Map<String, Object> queryParameters, boolean isOr, Pageable pageable) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(MLPStepResult.class);
		super.buildCriteria(criteria, queryParameters, isOr);

		// Count the total rows
		criteria.setProjection(Projections.rowCount());
		Long count = (Long) criteria.uniqueResult();
		if (count == 0)
			return new PageImpl<>(new ArrayList<>(), pageable, count);

		// Reset the count criteria; add pagination and sort
		criteria.setProjection(null);
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		super.applyPageableCriteria(criteria, pageable);

		// Get a page of results and send it back with the total available
		List<MLPStepResult> items = criteria.list();
		logger.debug(EELFLoggerDelegate.debugLogger, "findStepResults: result size={}", items.size());
		return new PageImpl<>(items, pageable, count);
	}

}

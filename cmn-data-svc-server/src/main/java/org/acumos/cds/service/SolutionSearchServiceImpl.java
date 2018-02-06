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

import org.acumos.cds.domain.MLPSolution;
import org.acumos.cds.transport.RestPageResponse;
import org.acumos.cds.util.EELFLoggerDelegate;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Hibernate-assisted methods to search solutions.
 */
@Service("solutionSearchService")
@Transactional
public class SolutionSearchServiceImpl extends AbstractSearchServiceImpl implements SolutionSearchService {

	private final EELFLoggerDelegate logger = EELFLoggerDelegate.getLogger(SolutionSearchServiceImpl.class);

	@Autowired
	private SessionFactory sessionFactory;

	@SuppressWarnings("unchecked")
	@Override
	public Page<MLPSolution> findSolutions(Map<String, ? extends Object> queryParameters, boolean isOr, Pageable pageable) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(MLPSolution.class);
		super.buildCriteria(criteria, queryParameters, isOr);

		// Count the total rows
		criteria.setProjection(Projections.rowCount());
		Long count = (Long) criteria.uniqueResult();
		if (count == 0)
			return new RestPageResponse<>(new ArrayList<MLPSolution>(), pageable, count);

		// Reset the count criteria; add pagination and sort
		criteria.setProjection(null);
		criteria.setResultTransformer(Criteria.ROOT_ENTITY);
		super.applyPageableCriteria(criteria, pageable);

		// Get a page of results and send it back with the total available
		List<MLPSolution> items = criteria.list();
		logger.debug(EELFLoggerDelegate.debugLogger, "findSolutions: result size={}", items.size());
		return new RestPageResponse<>(items, pageable, count);
	}

	/**
	 * This implementation is awkward for several reasons:
	 * <UL>
	 * <LI>the need to use LIKE queries on certain fields
	 * <LI>the need to search tags, which are not attributes on the entity itself
	 * but instead are implemented via a mapping table</LI>
	 * </UL>
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Page<MLPSolution> findPortalSolutions(String[] nameKeywords, String[] descKeywords, boolean active,
			String[] ownerIds, String[] accessTypeCode, String[] modelTypeCode, String[] validationStatusCode,
			String[] tags, Pageable pageable) {

		Criteria solCriteria = sessionFactory.getCurrentSession().createCriteria(MLPSolution.class);

		// Always check active status
		solCriteria.add(Restrictions.eq("active", active));

		if (nameKeywords != null && nameKeywords.length > 0)
			solCriteria.add(buildLikeListCriterion("name", nameKeywords));
		if (descKeywords != null && descKeywords.length > 0)
			solCriteria.add(buildLikeListCriterion("description", descKeywords));
		if (ownerIds != null && ownerIds.length > 0)
			solCriteria.add(Restrictions.in("ownerId", ownerIds));
		if (accessTypeCode != null && accessTypeCode.length > 0)
			solCriteria.add(buildEqualsListCriterion("accessTypeCode", accessTypeCode));
		if (modelTypeCode != null && modelTypeCode.length > 0)
			solCriteria.add(buildEqualsListCriterion("modelTypeCode", modelTypeCode));
		if (validationStatusCode != null && validationStatusCode.length > 0)
			solCriteria.add(buildEqualsListCriterion("validationStatusCode", validationStatusCode));
		if (tags != null && tags.length > 0) {
			// "tags" is the field name in MLPSolution
			Criteria tagCriteria = solCriteria.createCriteria("tags");
			// "tag" is the field name in MLPTag
			tagCriteria.add(Restrictions.in("tag", tags));
		}

		// Count the total rows
		solCriteria.setProjection(Projections.rowCount());
		Long count = (Long) solCriteria.uniqueResult();
		if (count == 0)
			return new RestPageResponse<>(new ArrayList<MLPSolution>(), pageable, count);

		// Reset the count criteria; add pagination and sort
		solCriteria.setProjection(null);
		solCriteria.setResultTransformer(Criteria.ROOT_ENTITY);
		super.applyPageableCriteria(solCriteria, pageable);

		// Get a page of results
		List<MLPSolution> items = solCriteria.list();
		logger.debug(EELFLoggerDelegate.debugLogger, "findPortalSolutions: result size={}", items.size());
		return new RestPageResponse<>(items, pageable, count);
	}

}

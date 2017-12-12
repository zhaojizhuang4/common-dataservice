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

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.acumos.cds.domain.MLPSolution;
import org.acumos.cds.transport.RestPageResponse;
import org.acumos.cds.util.EELFLoggerDelegate;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Junction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
	public List<MLPSolution> getSolutions(Map<String, ? extends Object> queryParameters, boolean isOr) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(MLPSolution.class);
		super.buildCriteria(criteria, queryParameters, isOr);
		List<MLPSolution> items = criteria.list();
		logger.debug(EELFLoggerDelegate.debugLogger,"getSolutions: result size={}", items.size());
		return items;
	}

	/**
	 * Builds a disjunction ("OR") criterion to check all values in the list, with
	 * special handling for null.
	 * 
	 * @param fieldName
	 *            POJO field name
	 * @param values
	 *            String values; null is permittedØ
	 * @return Criterion
	 */
	private Criterion buildValueListCriterion(String fieldName, String[] values) {
		Junction junction = Restrictions.disjunction();
		for (String v : values) {
			if (v == null)
				junction.add(Restrictions.isNull(fieldName));
			else
				junction.add(Restrictions.eq(fieldName, v));
		}
		return junction;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Page<MLPSolution> findPortalSolutions(String nameKeyword, String descKeyword, String authorKeyword,
			boolean active, String[] accessTypeCode, String[] modelTypeCode, String[] validationStatusCode,
			String[] tags, Pageable pageable) {

		Criteria solCriteria = sessionFactory.getCurrentSession().createCriteria(MLPSolution.class);

		// Always check active status
		solCriteria.add(Restrictions.eq("active", new Boolean(active)));

		if (nameKeyword != null && !nameKeyword.isEmpty()) {
			Criterion criterion = Restrictions.ilike("name", '%' + nameKeyword + '%');
			solCriteria.add(criterion);
		}
		if (descKeyword != null && !descKeyword.isEmpty()) {
			Criterion criterion = Restrictions.ilike("description", '%' + descKeyword + '%');
			solCriteria.add(criterion);
		}
		if (authorKeyword != null && !authorKeyword.isEmpty()) {
			// TODO
			logger.info("findPortalSolutions: Author not implemented");
		}
		if (accessTypeCode != null && accessTypeCode.length > 0)
			solCriteria.add(buildValueListCriterion("accessTypeCode", accessTypeCode));
		if (modelTypeCode != null && modelTypeCode.length > 0)
			solCriteria.add(buildValueListCriterion("modelTypeCode", modelTypeCode));
		if (validationStatusCode != null && validationStatusCode.length > 0)
			solCriteria.add(buildValueListCriterion("validationStatusCode", validationStatusCode));
		if (tags != null && tags.length > 0) {
			// "tags" is the field name in MLPSolution
			Criteria tagCriteria = solCriteria.createCriteria("tags");
			// "tag" is the field name in MLPTag
			tagCriteria.add(Restrictions.in("tag", tags));
		}

		// Count the total rows
		solCriteria.setProjection(Projections.rowCount());
		Long count = (Long) solCriteria.uniqueResult();

		// Reset the criteria, add pagination and sort
		solCriteria.setProjection(null);
		solCriteria.setResultTransformer(Criteria.ROOT_ENTITY);
		solCriteria.setFirstResult(pageable.getOffset());
		solCriteria.setMaxResults(pageable.getPageSize());
		if (pageable.getSort() != null) {
			Iterator<Sort.Order> orderIter = pageable.getSort().iterator();
			while (orderIter.hasNext()) {
				Sort.Order sortOrder = orderIter.next();
				Order order;
				if (sortOrder.isAscending())
					order = Order.asc(sortOrder.getProperty());
				else
					order = Order.desc(sortOrder.getProperty());
				solCriteria.addOrder(order);
			}
		}
		// Get a page of results
		List<MLPSolution> items = solCriteria.list();

		logger.debug(EELFLoggerDelegate.debugLogger, "findPortalSolutions: result size={}", items.size());
		RestPageResponse<MLPSolution> page = new RestPageResponse<>(items, pageable, count);

		return page;
	}

}

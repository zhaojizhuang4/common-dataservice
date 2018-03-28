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
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.acumos.cds.domain.MLPSolution;
import org.acumos.cds.domain.MLPSolutionFOM;
import org.acumos.cds.util.EELFLoggerDelegate;
import org.hibernate.AssertionFailure;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Hibernate-assisted methods to search solutions.
 * <P>
 * These two aspects must be observed to get pagination working as expected:
 * <OL>
 * <LI>For all to-many mappings, force use of separate select instead of left
 * outer join. This is far less efficient due to repeated trips to the database.
 * </LI>
 * <LI>Specify an unambiguous ordering. This at least is cheap, just add
 * order-by the ID field.</LI>
 * </OL>
 * I'm not the only one who has fought Hibernate to get paginated search
 * results:
 * 
 * <PRE>
 * https://stackoverflow.com/questions/300491/how-to-get-distinct-results-in-hibernate-with-joins-and-row-based-limiting-pagi
 * https://stackoverflow.com/questions/9418268/hibernate-distinct-results-with-pagination
 * https://stackoverflow.com/questions/11038234/pagination-with-hibernate-criteria-and-distinct-root-entity
 * https://stackoverflow.com/questions/42910271/duplicate-records-with-hibernate-joins-and-pagination
 * </PRE>
 */
@Service("solutionSearchService")
@Transactional
public class SolutionSearchServiceImpl extends AbstractSearchServiceImpl implements SolutionSearchService {

	private final EELFLoggerDelegate logger = EELFLoggerDelegate.getLogger(SolutionSearchServiceImpl.class);

	@Autowired
	private SessionFactory sessionFactory;

	private final String revAlias = "revs";
	private final String artAlias = "arts";
	private final String solutionId = "solutionId";

	@SuppressWarnings("unchecked")
	@Override
	public Page<MLPSolution> findSolutions(Map<String, ? extends Object> queryParameters, boolean isOr,
			Pageable pageable) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(MLPSolution.class);
		super.buildCriteria(criteria, queryParameters, isOr);

		// Adjust fetch mode on tags to block Hibernate from using left outer join,
		// which builds a cross product that contains duplicate rows.
		criteria.setFetchMode("tags", FetchMode.SELECT);

		// Count the total rows
		criteria.setProjection(Projections.rowCount());
		Long count = (Long) criteria.uniqueResult();
		if (count == 0)
			return new PageImpl<>(new ArrayList<>(), pageable, count);

		// Reset the count criteria; add pagination and sort
		criteria.setProjection(null);
		// This should not do any harm; had problems elsewhere without
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		// Add pagination and sort
		super.applyPageableCriteria(criteria, pageable);
		// Fallback order on a unique field. Without this the pagination
		// yields odd results; e.g., request 10 items but only get 8.
		criteria.addOrder(Order.asc("solutionId"));

		// Get a page of results and send it back with the total available
		List<MLPSolution> items = criteria.list();
		logger.debug(EELFLoggerDelegate.debugLogger, "findSolutions: result size={}", items.size());
		return new PageImpl<>(items, pageable, count);
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

		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(MLPSolution.class);

		// Adjust fetch mode to block Hibernate from using left outer join,
		// which builds a cross product that contains duplicate rows.
		criteria.setFetchMode("tags", FetchMode.SELECT);

		// Always check active status
		criteria.add(Restrictions.eq("active", active));

		if (nameKeywords != null && nameKeywords.length > 0)
			criteria.add(buildLikeListCriterion("name", nameKeywords));
		if (descKeywords != null && descKeywords.length > 0)
			criteria.add(buildLikeListCriterion("description", descKeywords));
		if (ownerIds != null && ownerIds.length > 0)
			criteria.add(Restrictions.in("ownerId", ownerIds));
		if (accessTypeCode != null && accessTypeCode.length > 0)
			criteria.add(buildEqualsListCriterion("accessTypeCode", accessTypeCode));
		if (modelTypeCode != null && modelTypeCode.length > 0)
			criteria.add(buildEqualsListCriterion("modelTypeCode", modelTypeCode));
		if (validationStatusCode != null && validationStatusCode.length > 0)
			criteria.add(buildEqualsListCriterion("validationStatusCode", validationStatusCode));
		if (tags != null && tags.length > 0) {
			// "tags" is the field name in MLPSolution
			Criteria tagCriteria = criteria.createCriteria("tags");
			// "tag" is the field name in MLPTag
			tagCriteria.add(Restrictions.in("tag", tags));
		}

		// Count the total rows. This does NOT run the left outer joins for tags etc.
		criteria.setProjection(Projections.rowCount());
		Long count = (Long) criteria.uniqueResult();
		if (count == 0)
			return new PageImpl<>(new ArrayList<>(), pageable, count);

		// Reset the count criteria; add pagination and sort
		criteria.setProjection(null);
		// This should not do any harm; had problems elsewhere without
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		// Add pagination and sort
		super.applyPageableCriteria(criteria, pageable);
		// Fallback order on a unique field. Without this the pagination
		// yields odd results; e.g., request 10 items but only get 8.
		criteria.addOrder(Order.asc("solutionId"));

		// Get a page of results
		List<MLPSolution> items = criteria.list();
		// This detects programmer errors
		if (items.isEmpty())
			throw new AssertionFailure("findPortalSolutions: unexpected empty result");
		logger.debug(EELFLoggerDelegate.debugLogger, "findPortalSolutions: result size={}", items.size());
		return new PageImpl<>(items, pageable, count);
	}

	/**
	 * Runs a query on the SolutionFOM entity, returns a page after converting
	 * objects to plain solution.
	 * 
	 * @param criteria
	 *            Criteria to evaluate
	 * @param pageable
	 *            Page and sort criteria
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	private Page<MLPSolution> runSolutionFomQuery(Criteria criteria, Pageable pageable) {

		// Include user's sort request
		if (pageable.getSort() != null)
			super.applySortCriteria(criteria, pageable);
		// Add order on a unique field. Without this the pagination
		// can yield odd results; e.g., request 10 items but only get 8.
		criteria.addOrder(Order.asc(solutionId));
		// Hibernate should coalesce the results, yielding only solutions
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

		// Getting the complete result could be brutally expensive.
		List foms = criteria.list();
		if (foms.isEmpty() || foms.size() < pageable.getOffset())
			return new PageImpl<>(new ArrayList<>(), pageable, 0);

		// Get a page of FOM solutions and convert each to plain solution
		List<MLPSolution> items = new ArrayList<>();
		int lastItemInPage = pageable.getOffset() + pageable.getPageSize();
		int limit = lastItemInPage < foms.size() ? lastItemInPage : foms.size();
		for (int i = pageable.getOffset(); i < limit; ++i) {
			Object o = foms.get(i);
			if (o instanceof MLPSolutionFOM)
				items.add(((MLPSolutionFOM) o).toMLPSolution());
			else
				throw new AssertionFailure("Unexpected type: " + o.getClass().getName());
		}

		return new PageImpl<>(items, pageable, foms.size());
	}

	/*
	 * This query checks properties of the solution (access type) AND properties of
	 * associated entities (modified date), which requires an inner join and yields
	 * a large cross product that Hibernate will coalesce. Because of the joins it's
	 * unsafe to apply limit parameters at the database. Therefore this method
	 * fetches the full result from the database then reduces the result size here,
	 * which is inefficient.
	 * 
	 * Uses the full object mapping (FOM) version of the Solution class.
	 */
	@Override
	public Page<MLPSolution> findSolutionsByModifiedDate(boolean active, String[] accessTypeCode,
			String[] validationStatusCode, Date date, Pageable pageable) {

		// build the query using FOM to access child attributes
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(MLPSolutionFOM.class);
		criteria.createAlias("revisions", revAlias);
		criteria.createAlias(revAlias + ".artifacts", artAlias);
		criteria.createAlias(revAlias + ".solution", "sol");
		criteria.add(Restrictions.eq("active", active));
		if (accessTypeCode != null && accessTypeCode.length > 0)
			criteria.add(Restrictions.in("accessTypeCode", accessTypeCode));
		if (validationStatusCode != null && validationStatusCode.length > 0)
			criteria.add(Restrictions.in("validationStatusCode", validationStatusCode));
		// Construct a disjunction to find any updated item.
		// Unfortunately this requires hard-coded field names
		Criterion solModified = Restrictions.ge("modified", date);
		Criterion revModified = Restrictions.ge(revAlias + ".modified", date);
		Criterion artModified = Restrictions.ge(artAlias + ".modified", date);
		Disjunction itemModifiedAfter = Restrictions.disjunction();
		itemModifiedAfter.add(solModified);
		itemModifiedAfter.add(revModified);
		itemModifiedAfter.add(artModified);
		criteria.add(itemModifiedAfter);

		Page<MLPSolution> result = runSolutionFomQuery(criteria, pageable);
		logger.debug(EELFLoggerDelegate.debugLogger, "findSolutionsByModifiedDate: result size={}",
				result.getNumberOfElements());
		return result;
	}

}

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

import java.lang.invoke.MethodHandles;
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
 * Defines hibernate-assisted methods to search solutions on user-specified
 * fields and yield paginated results. Any field value can be omitted, which is
 * the key difference from the methods in the solution repository class.
 * 
 * <P>
 * These two aspects must be observed to get pagination working as expected:
 * <OL>
 * <LI>For all to-many mappings, force use of separate select instead of left
 * outer join. This is far less efficient due to repeated trips to the database,
 * and becomes impossible if you must check properties on mapped (i.e., not the
 * root) entities.</LI>
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

	private static final EELFLoggerDelegate logger = EELFLoggerDelegate.getLogger(MethodHandles.lookup().lookupClass());

	@Autowired
	private SessionFactory sessionFactory;

	private final String revAlias = "revs";
	private final String artAlias = "arts";
	private final String ownerAlias = "ownr";
	private final String tagAlias = "tag";
	private final String accAlias = "acc";
	private final String solutionId = "solutionId";

	/*
	 * This criteria only checks properties of the solution entity, not of any
	 * associated entities, so inner joins and their cross products are avoidable.
	 * Therefore it's safe to use limit criteria in the database, which saves the
	 * effort of computing a big result and discarding all but the desired page.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Page<MLPSolution> findSolutions(Map<String, ? extends Object> queryParameters, boolean isOr,
			Pageable pageable) {

		Date beginDate = new Date();
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(MLPSolution.class);
		super.buildCriteria(criteria, queryParameters, isOr);

		// Adjust fetch mode to block Hibernate from using left outer join;
		// instead it runs a select to get tags for each solution in the result.
		criteria.setFetchMode("tags", FetchMode.SELECT);

		// Count the total rows
		criteria.setProjection(Projections.rowCount());
		Long count = (Long) criteria.uniqueResult();
		if (count == 0)
			return new PageImpl<>(new ArrayList<>(), pageable, count);

		// Reset the count criteria
		criteria.setProjection(null);
		// This should not do any harm; had problems elsewhere without
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		// Add pagination and sorting to limit size
		super.applyPageableCriteria(criteria, pageable);
		// Ensure a total order using the ID field.
		criteria.addOrder(Order.asc("solutionId"));

		// Get a page of results and send it back with the total available
		List<MLPSolution> items = criteria.list();
		logger.audit(beginDate, "findSolutions: result size={}", items.size());
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
	 * This query checks properties of the solution AND associated entities
	 * especially revisions, which requires an inner join and yields a large cross
	 * product that Hibernate will coalesce. Because of the joins it's unsafe to
	 * apply limit parameters at the database. Therefore this method fetches the
	 * full result from the database then reduces the result size here, which is
	 * inefficient.
	 *
	 * This implementation is made yet more awkward due to the requirement to
	 * perform LIKE queries on certain fields.
	 */
	@Override
	public Page<MLPSolution> findPortalSolutions(String[] nameKeywords, String[] descKeywords, boolean active,
			String[] userIds, String[] modelTypeCode, String[] accessTypeCode, String[] validationStatusCode,
			String[] tags, String[] authorKeywords, String[] publisherKeywords, Pageable pageable) {

		Date beginDate = new Date();
		// build the query using FOM to access child attributes
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(MLPSolutionFOM.class);
		// Attributes on the solution
		criteria.add(Restrictions.eq("active", active));
		if (nameKeywords != null && nameKeywords.length > 0)
			criteria.add(buildLikeListCriterion("name", nameKeywords));
		if (descKeywords != null && descKeywords.length > 0)
			criteria.add(buildLikeListCriterion("description", descKeywords));
		if (modelTypeCode != null && modelTypeCode.length > 0)
			criteria.add(buildEqualsListCriterion("modelTypeCode", modelTypeCode));
		if ((accessTypeCode != null && accessTypeCode.length > 0)
				|| (validationStatusCode != null && validationStatusCode.length > 0)
				|| (authorKeywords != null && authorKeywords.length > 0)
				|| (publisherKeywords != null && publisherKeywords.length > 0)) {
			criteria.createAlias("revisions", revAlias);
			if (accessTypeCode != null && accessTypeCode.length > 0)
				criteria.add(buildEqualsListCriterion(revAlias + ".accessTypeCode", accessTypeCode));
			if (validationStatusCode != null && validationStatusCode.length > 0)
				criteria.add(buildEqualsListCriterion(revAlias + ".validationStatusCode", validationStatusCode));
			if (authorKeywords != null && authorKeywords.length > 0)
				criteria.add(buildLikeListCriterion(revAlias + ".authors", authorKeywords));
			if (publisherKeywords != null && publisherKeywords.length > 0)
				criteria.add(buildLikeListCriterion(revAlias + ".publisher", publisherKeywords));
		}
		if (userIds != null && userIds.length > 0) {
			criteria.createAlias("owner", ownerAlias);
			criteria.add(Restrictions.in(ownerAlias + ".userId", userIds));
		}
		if (tags != null && tags.length > 0) {
			// Tags are optional, so must use outer join
			criteria.createAlias("tags", tagAlias, org.hibernate.sql.JoinType.LEFT_OUTER_JOIN);
			criteria.add(Restrictions.in(tagAlias + ".tag", tags));
		}
		Page<MLPSolution> result = runSolutionFomQuery(criteria, pageable);
		logger.audit(beginDate, "findPortalSolutions: result size={}", result.getNumberOfElements());
		return result;
	}

	/*
	 * See comments on {@link #findPortalSolutions(String[], String[], boolean,
	 * String[], String[], String[], String[], String[], Pageable)}
	 *
	 * Baffling problem occurred here:
	 * 
	 * Caused by: java.lang.IllegalArgumentException: Can not set java.lang.String
	 * field org.acumos.cds.domain.MLPUser.userId to java.lang.String
	 * 
	 * Which was due to a missing alias for the owner/userId field; i.e., my error,
	 * not some obscure Hibernate issue.
	 */
	@Override
	public Page<MLPSolution> findUserSolutions(String[] nameKeywords, String[] descKeywords, boolean active,
			String userId, String[] modelTypeCode, String[] accessTypeCode, String[] validationStatusCode,
			String[] tags, Pageable pageable) {

		Date beginDate = new Date();
		// build the query using FOM to access child attributes
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(MLPSolutionFOM.class);
		// Find user's own models AND others via access map which requires outer join
		criteria.createAlias("owner", ownerAlias);
		Criterion owner = Restrictions.eq(ownerAlias + ".userId", userId);
		criteria.createAlias("accessUsers", accAlias, org.hibernate.sql.JoinType.LEFT_OUTER_JOIN);
		Criterion access = Restrictions.eq(accAlias + ".userId", userId);
		criteria.add(Restrictions.or(owner, access));

		// Attributes on the solution
		criteria.add(Restrictions.eq("active", active));
		if (nameKeywords != null && nameKeywords.length > 0)
			criteria.add(buildLikeListCriterion("name", nameKeywords));
		if (descKeywords != null && descKeywords.length > 0)
			criteria.add(buildLikeListCriterion("description", descKeywords));
		if (modelTypeCode != null && modelTypeCode.length > 0)
			criteria.add(buildEqualsListCriterion("modelTypeCode", modelTypeCode));
		if ((accessTypeCode != null && accessTypeCode.length > 0)
				|| (validationStatusCode != null && validationStatusCode.length > 0)) {
			criteria.createAlias("revisions", revAlias);
			if (accessTypeCode != null && accessTypeCode.length > 0)
				criteria.add(buildEqualsListCriterion(revAlias + ".accessTypeCode", accessTypeCode));
			if (validationStatusCode != null && validationStatusCode.length > 0)
				criteria.add(buildEqualsListCriterion(revAlias + ".validationStatusCode", validationStatusCode));
		}
		if (tags != null && tags.length > 0) {
			// Tags are optional, so must use outer join
			criteria.createAlias("tags", tagAlias, org.hibernate.sql.JoinType.LEFT_OUTER_JOIN);
			criteria.add(Restrictions.in(tagAlias + ".tag", tags));
		}
		Page<MLPSolution> result = runSolutionFomQuery(criteria, pageable);
		logger.audit(beginDate, "findUserSolutions: result size={}", result.getNumberOfElements());
		return result;
	}

	/*
	 * This query checks properties of the solution AND associated entities, like
	 * the revision's access type, which requires an inner join and yields a large
	 * cross product that Hibernate will coalesce. Because of the joins it's unsafe
	 * to apply limit parameters at the database. Therefore this method fetches the
	 * full result from the database then reduces the result size here, which is
	 * inefficient.
	 */
	@Override
	public Page<MLPSolution> findSolutionsByModifiedDate(boolean active, String[] accessTypeCode,
			String[] validationStatusCode, Date date, Pageable pageable) {

		Date beginDate = new Date();
		// build the query using FOM to access child attributes
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(MLPSolutionFOM.class);
		// A solution should ALWAYS have revisions.
		criteria.createAlias("revisions", revAlias);
		// A revision should ALWAYS have artifacts
		criteria.createAlias(revAlias + ".artifacts", artAlias);
		// Attributes on the solution
		criteria.add(Restrictions.eq("active", active));
		if (accessTypeCode != null && accessTypeCode.length > 0)
			criteria.add(Restrictions.in(revAlias + ".accessTypeCode", accessTypeCode));
		if (validationStatusCode != null && validationStatusCode.length > 0)
			criteria.add(Restrictions.in(revAlias + ".validationStatusCode", validationStatusCode));
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
		logger.audit(beginDate, "findSolutionsByModifiedDate: result size={}", result.getNumberOfElements());
		return result;
	}

}

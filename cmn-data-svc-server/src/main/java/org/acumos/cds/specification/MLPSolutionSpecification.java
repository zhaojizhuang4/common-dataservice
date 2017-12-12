package org.acumos.cds.specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.acumos.cds.domain.MLPSolution;
import org.acumos.cds.query.SearchCriterion;
import org.acumos.cds.query.SearchOperation;
import org.springframework.data.jpa.domain.Specification;

/**
 * Supports searches for an MLPSolution by field value.
 * 
 * Inspired by
 * http://www.baeldung.com/rest-api-search-language-spring-data-specifications
 */
public class MLPSolutionSpecification implements Specification<MLPSolution> {

	private SearchCriterion criterion;

	/**
	 * Creates a search specification with the specified criterion.
	 * 
	 * @param criteria
	 *            SearchCriterion
	 */
	public MLPSolutionSpecification(final SearchCriterion criteria) {
		this.criterion = criteria;
	}

	@Override
	public Predicate toPredicate(Root<MLPSolution> queryRoot, CriteriaQuery<?> query, CriteriaBuilder builder) {
		if (SearchOperation.EQUALS == criterion.getOperation()) {
			return builder.equal(queryRoot.get(criterion.getKey()), criterion.getValue());
		} else if (SearchOperation.NOT_EQUALS == criterion.getOperation()) {
			return builder.notEqual(queryRoot.get(criterion.getKey()), criterion.getValue());
		} else if (SearchOperation.LIKE == criterion.getOperation()) {
			return builder.like(queryRoot.<String>get(criterion.getKey()), criterion.getValue().toString());
		} else if (SearchOperation.GTE == criterion.getOperation()) {
			return builder.greaterThanOrEqualTo(queryRoot.<String>get(criterion.getKey()),
					criterion.getValue().toString());
		} else if (SearchOperation.LTE == criterion.getOperation()) {
			return builder.lessThanOrEqualTo(queryRoot.<String>get(criterion.getKey()),
					criterion.getValue().toString());
		} else if (SearchOperation.NULL == criterion.getOperation()) {
			return builder.isNull(queryRoot.<String>get(criterion.getKey()));
		} else if (SearchOperation.IN == criterion.getOperation()) {
			Expression<String> exp = queryRoot.<String>get(criterion.getKey());
			if (criterion.getValue().getClass().isArray()) {
				Object[] array = (Object[]) criterion.getValue();
				return exp.in(array);
			} else {
				throw new IllegalArgumentException("IN operator requires array, not " + criterion.getValue());
			}
		} else {
			throw new UnsupportedOperationException(criterion.getOperation().name());
		}
	}
}

package org.acumos.cds.query;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Supports a list of criteria in a tiny REST-friendly query language. Builds or
 * parses a complex query criteria that's usable as a GET request parameter.
 * Avoid characters '?', '=' and '&amp;'.
 */
public class SearchCriteria {

	public static final String QUERY_PARAM_NAME = "search";

	private static final String SEPARATOR = ";";

	private List<SearchCriterion> criterionList = new ArrayList<>();

	/**
	 * Creates a list with one member
	 * 
	 * @param sc
	 *            SearchCriterion
	 */
	public SearchCriteria(SearchCriterion sc) {
		and(sc);
	}

	/**
	 * Creates a list of criterion elements by parsing the string. For example:
	 * "a:b;|c_e,f".
	 * 
	 * @param criteriaString
	 *            Criteria to be parsed
	 */
	public SearchCriteria(String criteriaString) {
		String[] elements = criteriaString.split(SEPARATOR);
		for (int i = 0; i < elements.length; ++i) {
			SearchCriterion sc = new SearchCriterion(elements[i]);
			// First predicate is irrelevant, squelch it.
			if (i == 0)
				sc.setOrPredicate(null);
			criterionList.add(sc);
		}
	}

	/**
	 * Adds the criterion to the list using logical predicate AND.
	 * 
	 * @param sc
	 *            SearchCriterion
	 * @return this for convenience of chaining
	 */
	public SearchCriteria and(SearchCriterion sc) {
		SearchCriterion clone = new SearchCriterion(sc.getKey(), sc.getOperation(), sc.getValue());
		criterionList.add(clone);
		return this;
	}

	/**
	 * Adds the criterion to the list using logical predicate OR.
	 * 
	 * @param sc
	 *            SearchCriterion
	 * @return this for convenience of chaining
	 */
	public SearchCriteria or(SearchCriterion sc) {
		SearchCriterion clone = new SearchCriterion(true, sc.getKey(), sc.getOperation(), sc.getValue());
		criterionList.add(clone);
		return this;
	}

	/**
	 * Answers an iterator over the criterion elements
	 * 
	 * @return an iterator over the criterion elements
	 */
	public Iterator<SearchCriterion> iterator() {
		return criterionList.iterator();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		Iterator<SearchCriterion> iter = criterionList.iterator();
		while (iter.hasNext()) {
			sb.append(iter.next().toString());
			if (iter.hasNext())
				sb.append(SEPARATOR);
		}
		return sb.toString();
	}

}

package org.acumos.cds.query;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Supports a single criterion in a tiny REST-friendly query language. Avoid
 * characters '?', '=' and '&amp;' because those are magic in a GET request.
 */
public class SearchCriterion {

	// private static Logger logger = LoggerFactory.getLogger(SearchCriteria.class);

	private static final char OR = '|';
	// TODO: this simplistic pattern does not allow whitespace or punctuation in
	// values.
	private final String criterionRegex = "([" + OR + "]?)(\\w+)([" + SearchOperation.getCodes() + "])([\\w,]+)";
	private final Pattern criterionPattern = Pattern.compile(criterionRegex);

	Boolean isOrPredicate;
	private String key;
	private SearchOperation operation;
	private Object value;

	/**
	 * Builds a search criterion with the logical predicate AND.
	 * 
	 * See {@link #SearchCriterion(Boolean, String, SearchOperation, Object)}
	 * 
	 * @param key
	 *            Java class field name
	 * @param operation
	 *            E.g., EQUALS
	 * @param value
	 *            Object for which the toString() method yields a good value; must
	 *            be a Collection if the operation is "IN".
	 */
	public SearchCriterion(final String key, final SearchOperation operation, final Object value) {
		this(null, key, operation, value);
	}

	/**
	 * Builds a search criterion using the predicate, key, operation and value.
	 * 
	 * @param isOrPredicate
	 *            Use null or False for "and" (form a conjunction); use True for
	 *            "or" (form a disjunction) when chaining criteria together.
	 * @param key
	 *            Java class field name
	 * @param operation
	 *            E.g., EQUALS
	 * @param value
	 *            Object for which the toString() method yields a good value; must
	 *            be an array or a Collection if the operation is "IN".
	 */
	public SearchCriterion(final Boolean isOrPredicate, final String key, final SearchOperation operation,
			final Object value) {
		if (operation == SearchOperation.IN && !value.getClass().isArray() && !(value instanceof Collection))
			throw new IllegalArgumentException("Operation IN requires array or collection value");
		this.isOrPredicate = isOrPredicate;
		this.key = key;
		this.operation = operation;
		this.value = value;
	}

	/**
	 * Builds a SearchCriterion by parsing the criteria string for predicate, key,
	 * operation and value. For example, "a:b" or "|e_f,g".
	 * 
	 * @param criterionString
	 *            String with optional predicate, mandatory key, mandatory operation
	 *            and mandatory value(s).
	 * @throws IllegalArgumentException
	 *             If parse fails
	 */
	public SearchCriterion(String criterionString) throws IllegalArgumentException {
		Matcher matcher = criterionPattern.matcher(criterionString);
		if (!matcher.find())
			throw new IllegalArgumentException("Failed to parse: " + criterionString);
		// for (int i = 1; i <= 4; ++i)
		// logger.debug("Group " + i + ": " + matcher.group(i));
		this.isOrPredicate = "|".equals(matcher.group(1));
		this.key = matcher.group(2);
		SearchOperation op = SearchOperation.forCode(matcher.group(3).charAt(0));
		this.operation = op;
		// If embedded commas are found, treat as value list.
		if (matcher.group(4).indexOf(',') > 0)
			this.value = matcher.group(4).split(",");
		else
			this.value = matcher.group(4);
	}

	/**
	 * Answers whether the "OR" logical predicate was set.
	 * 
	 * @return False or null for "and"; True for "or"
	 */
	public Boolean isOrPredicate() {
		return isOrPredicate;
	}

	/**
	 * Sets the status of the "OR" logical predicate.
	 * 
	 * @param isOrPredicate
	 *            True for "or"; False or null for "and"
	 */
	public void setOrPredicate(final Boolean isOrPredicate) {
		this.isOrPredicate = isOrPredicate;
	}

	public String getKey() {
		return key;
	}

	public void setKey(final String key) {
		this.key = key;
	}

	public SearchOperation getOperation() {
		return operation;
	}

	public void setOperation(final SearchOperation operation) {
		this.operation = operation;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(final Object value) {
		this.value = value;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (isOrPredicate != null && isOrPredicate)
			sb.append(OR);
		sb.append(key);
		sb.append(operation.getCode());
		if (value.getClass().isArray()) {
			Object[] array = (Object[]) value;
			for (int i = 0; i < array.length; ++i) {
				if (i > 0)
					sb.append(',');
				sb.append(array[i]);
			}
		} else if (value instanceof Collection) {
			@SuppressWarnings("rawtypes")
			Iterator iter = ((Collection) value).iterator();
			while (iter.hasNext()) {
				sb.append(iter.next().toString());
				if (iter.hasNext())
					sb.append(',');
			}
		} else {
			// Server expects Date type as Long (not String)
			if (value instanceof Date)
				sb.append(Long.toString(((Date) value).getTime()));
			else
				sb.append(value.toString());
		}
		return sb.toString();
	}

}
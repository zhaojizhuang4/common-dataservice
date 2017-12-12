package org.acumos.cds.query;

/**
 * Defines operations in a tiny REST-friendly query language. Avoid characters
 * '?', '=' and '&amp;'.
 */
public enum SearchOperation {

	EQUALS(':'), NOT_EQUALS('!'), NULL('@'), LIKE('~'), LTE('<'), GTE('>'), IN('_');

	private char code;

	private SearchOperation(final char code) {
		this.code = code;
	}

	public char getCode() {
		return code;
	}

	/**
	 * @param code
	 *            Operation code
	 * @return SearchOperation for the specified code; null if no match is found
	 */
	public static SearchOperation forCode(char code) {
		for (SearchOperation so : SearchOperation.values())
			if (code == so.getCode())
				return so;
		return null;
	}

	/**
	 * @return String of all valid operation codes.
	 */
	public static String getCodes() {
		StringBuilder sb = new StringBuilder();
		for (SearchOperation so : SearchOperation.values())
			sb.append(so.getCode());
		return sb.toString();
	}

}

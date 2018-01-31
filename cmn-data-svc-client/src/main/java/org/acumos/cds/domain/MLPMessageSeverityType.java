package org.acumos.cds.domain;

import java.io.Serializable;

/**
 * Model for an message severity type, a code-name pair.
 */
public class MLPMessageSeverityType extends MLPCodeNamePair implements Serializable {

	private static final long serialVersionUID = 5553825913000333176L;

	public MLPMessageSeverityType() {
		super();
	}

	public MLPMessageSeverityType(String code, String name) {
		super(code, name);
	}

}

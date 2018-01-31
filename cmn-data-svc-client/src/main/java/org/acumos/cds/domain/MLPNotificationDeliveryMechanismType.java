package org.acumos.cds.domain;

import java.io.Serializable;

/**
 * Model for an message severity type type, a code-name pair.
 */
public class MLPNotificationDeliveryMechanismType extends MLPCodeNamePair implements Serializable {

	private static final long serialVersionUID = -8062583843763021240L;

	public MLPNotificationDeliveryMechanismType() {
		super();
	}

	public MLPNotificationDeliveryMechanismType(String code, String name) {
		super(code, name);
	}

}

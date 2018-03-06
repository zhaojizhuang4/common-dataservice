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

package org.acumos.cds.domain;

import java.io.Serializable;

/**
 * Model for validation status, a code-name pair.
 * 
 * @deprecated Use {@link org.acumos.cds.ValidationStatusCode}
 */
public class MLPValidationStatus extends MLPCodeNamePair implements Serializable {

	private static final long serialVersionUID = -3885553055494881292L;

	public MLPValidationStatus() {
		super();
	}

	public MLPValidationStatus(String code, String name) {
		super(code, name);
	}

}

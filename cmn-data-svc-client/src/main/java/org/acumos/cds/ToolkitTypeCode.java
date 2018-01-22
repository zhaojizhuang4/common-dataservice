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

package org.acumos.cds;

/**
 * This enum provides MLP toolkit type codes for developer convenience. The
 * valid values are maintained in a database table modeled by the class
 * {@link org.acumos.cds.domain.MLPToolkitType}.
 */
public enum ToolkitTypeCode {
	CP("Composite Solution"), //
	DS("Design Studio"), //
	H2("H2O"), //
	RC("R"), //
	SK("Scikit-Learn"), //
	TF("TensorFlow"), //
	TC("Training Client"), //
	BR("Data Broker"); //

	private String typeName;

	private ToolkitTypeCode(final String typeName) {
		this.typeName = typeName;
	}

	public String getTypeName() {
		return typeName;
	}

}

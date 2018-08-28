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
 * This enum defines Acumos artifact type codes. Because these codes are highly
 * likely to vary between installations, it's NOT SAFE to rely on this Java enum
 * as providing the definitive value set.
 * 
 * @deprecated The value set should be obtained by calling method
 *             {@link org.acumos.cds.client.ICommonDataServiceRestClient#getCodeNamePairs(CodeNameType)}.
 * 
 */
@Deprecated
public enum ArtifactTypeCode {

	BP("BLUEPRINT FILE"), //
	CD("CDUMP FILE"), //
	DI("DOCKER IMAGE"), //
	DS("DATA SOURCE"), //
	MD("METADATA"), //
	MH("MODEL-H2O"), //
	MI("MODEL IMAGE"), //
	MR("MODEL-R"), //
	MS("MODEL-SCIKIT"), //
	MT("MODEL-TENSORFLOW"), //
	TE("TOSCA TEMPLATE"), //
	TG("TOSCA Generator Input File"), //
	TS("TOSCA SCHEMA"), //
	TT("TOSCA TRANSLATE"), //
	PJ("PROTOBUF FILE");

	private String typeName;

	private ArtifactTypeCode(final String typeName) {
		this.typeName = typeName;
	}

	public String getTypeName() {
		return typeName;
	}

}

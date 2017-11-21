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
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Size;

/**
 * Model for access type, a code-name pair.
 */
@Entity
@Table(name = "C_ACCESS_TYPE")
public class MLPAccessType implements MLPEntity, Serializable {

	private static final long serialVersionUID = -3836564419430582350L;

	@Id
	@Column(name = "TYPE_CD", updatable = false, nullable = false, columnDefinition = "CHAR(2)")
	@Size(max = 2)
	private String accessCode;

	@Column(name = "TYPE_NAME", columnDefinition = "CHAR(100)")
	@Size(max = 100)
	private String accessName;

	public String getAccessCode() {
		return accessCode;
	}

	public void setAccessCode(String typeCode) {
		this.accessCode = typeCode;
	}

	public String getAccessName() {
		return accessName;
	}

	public void setAccessName(String accessName) {
		this.accessName = accessName;
	}

	@Override
	public boolean equals(Object that) {
		if (that == null)
			return false;
		if (!(that instanceof MLPAccessType))
			return false;
		MLPAccessType thatObj = (MLPAccessType) that;
		return Objects.equals(accessCode, thatObj.accessCode) && Objects.equals(accessName, thatObj.accessName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(accessCode, accessName);
	}

	@Override
	public String toString() {
		return this.getClass().getName() + "[code=" + accessCode + ", name=" + accessName + "]";
	}

}

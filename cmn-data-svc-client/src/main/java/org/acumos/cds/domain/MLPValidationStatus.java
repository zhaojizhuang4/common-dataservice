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
 * Model for validation status, a code-name pair.
 */
@Entity
@Table(name = "C_VALIDATION_STATUS")
public class MLPValidationStatus implements MLPEntity, Serializable {

	private static final long serialVersionUID = -6136016586898282688L;

	@Id
	@Column(name = "STATUS_CD", updatable = false, nullable = false, columnDefinition = "CHAR(2)")
	@Size(max = 2)
	private String statusCode;

	@Column(name = "STATUS_NAME", columnDefinition = "VARCHAR(100)")
	@Size(max = 100)
	private String statusName;

	public String getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}

	public String getStatusName() {
		return statusName;
	}

	public void setStatusName(String statusName) {
		this.statusName = statusName;
	}

	@Override
	public boolean equals(Object that) {
		if (that == null)
			return false;
		if (!(that instanceof MLPValidationStatus))
			return false;
		MLPValidationStatus thatObj = (MLPValidationStatus) that;
		return Objects.equals(statusCode, thatObj.statusCode) && Objects.equals(statusName, thatObj.statusName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(statusCode, statusName);
	}

	@Override
	public String toString() {
		return this.getClass().getName() + "[code=" + statusCode + ", name=" + statusName + "]";
	}

}

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

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.Size;

/**
 * Defines fields, getters and setters to avoid code repetitions.
 * 
 * Spring has a bit of magic for everything, must use @MappedSuperclass here.
 */
@MappedSuperclass
public abstract class MLPTypeCodeEntity implements MLPEntity {

	@Id
	@Column(name = "TYPE_CD", updatable = false, nullable = false, columnDefinition = "CHAR(2)")
	@Size(max = 2)
	private String typeCode;

	@Column(name = "TYPE_NAME", columnDefinition = "VARCHAR(100)")
	@Size(max = 100)
	private String typeName;

	public String getTypeCode() {
		return typeCode;
	}

	public void setTypeCode(String typeCode) {
		this.typeCode = typeCode;
	}

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	@Override
	public boolean equals(Object that) {
		if (that == null)
			return false;
		if (!(that instanceof MLPTypeCodeEntity))
			return false;
		MLPTypeCodeEntity thatObj = (MLPTypeCodeEntity) that;
		return Objects.equals(typeCode, thatObj.typeCode) && Objects.equals(typeName, thatObj.typeName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(typeCode, typeName);
	}

	@Override
	public String toString() {
		return this.getClass().getName() + "[code=" + getTypeCode() + ", name=" + getTypeName() + "]";
	}

}

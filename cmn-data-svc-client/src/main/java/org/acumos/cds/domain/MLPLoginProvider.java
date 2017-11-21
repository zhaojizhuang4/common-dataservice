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
 * Model for login provider, a code-name pair.
 */
@Entity
@Table(name = "C_LOGIN_PROVIDER")
public class MLPLoginProvider implements MLPEntity, Serializable {

	private static final long serialVersionUID = -1405595448809901709L;

	@Id
	@Column(name = "PROVIDER_CD", updatable = false, nullable = false, columnDefinition = "CHAR(2)")
	@Size(max = 2)
	private String providerCode;

	@Column(name = "PROVIDER_NAME", columnDefinition="VARCHAR(100)")
	@Size(max = 100)
	private String providerName;

	public String getProviderCode() {
		return providerCode;
	}

	public void setProviderCode(String providerCode) {
		this.providerCode = providerCode;
	}

	public String getProviderName() {
		return providerName;
	}

	public void setProviderName(String providerName) {
		this.providerName = providerName;
	}

	@Override
	public boolean equals(Object that) {
		if (that == null)
			return false;
		if (!(that instanceof MLPLoginProvider))
			return false;
		MLPLoginProvider thatObj = (MLPLoginProvider) that;
		return Objects.equals(providerCode, thatObj.providerCode) && Objects.equals(providerName, thatObj.providerName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(providerCode, providerName);
	}

	@Override
	public String toString() {
		return this.getClass().getName() + "[code=" + providerCode + ", name=" + providerName + "]";
	}

}

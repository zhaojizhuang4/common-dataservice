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

/**
 * Model for site configuration details, basically a key-value store.
 */
@Entity
@Table(name = "C_SITE_CONFIG")
public class MLPSiteConfig extends MLPTimestampedEntity implements Serializable {

	private static final long serialVersionUID = 1213151198242327931L;

	// Alas the column name "KEY" isn't usable in most databases
	@Id
	@Column(name = "CONFIG_KEY", updatable = false, nullable = false, columnDefinition = "VARCHAR(50)")
	private String configKey;

	@Column(name = "CONFIG_VAL", nullable = false, columnDefinition = "VARCHAR(8192)")
	private String configValue;

	// Optional
	@Column(name = "USER_ID", columnDefinition = "CHAR(36)")
	private String userId;

	/**
	 * No-arg constructor.
	 */
	public MLPSiteConfig() {
		// no-arg constructor
	}

	/**
	 * This constructor accepts the required fields; i.e., the minimum that the user
	 * must supply to create a valid instance.
	 * 
	 * @param configKey
	 *            Row ID
	 * @param configValue
	 *            Configuration block, validated as JSON
	 * 
	 */
	public MLPSiteConfig(String configKey, String configValue) {
		this.configKey = configKey;
		this.configValue = configValue;
	}

	public String getConfigKey() {
		return configKey;
	}

	public void setConfigKey(String configKey) {
		this.configKey = configKey;
	}

	public String getConfigValue() {
		return configValue;
	}

	public void setConfigValue(String configValue) {
		this.configValue = configValue;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	@Override
	public boolean equals(Object that) {
		if (that == null)
			return false;
		if (!(that instanceof MLPSiteConfig))
			return false;
		MLPSiteConfig thatObj = (MLPSiteConfig) that;
		return Objects.equals(configKey, thatObj.configKey);
	}

	@Override
	public int hashCode() {
		return Objects.hash(configKey, configValue, userId);
	}

	@Override
	public String toString() {
		return this.getClass().getName() + "[key=" + configKey + ", value=" + configValue + ", user=" + userId
				+ ", created=" + getCreated() + ", ...]";
	}

}

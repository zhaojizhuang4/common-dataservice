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
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.GenericGenerator;

import io.swagger.annotations.ApiModelProperty;

/**
 * Model for a role, which defines what a user may do.
 * 
 * Participates in many-many relationship with users; annotations are on that
 * class.
 * 
 * Participates in one-many relationship with role function.
 */
@Entity
@Table(name = "C_ROLE")
public class MLPRole extends MLPTimestampedEntity implements Serializable {

	private static final long serialVersionUID = -2523194601671782097L;

	@Id
	@GeneratedValue(generator = "customUseOrGenerate")
	@GenericGenerator(name = "customUseOrGenerate", strategy = "org.acumos.cds.util.UseExistingOrNewUUIDGenerator")
	@Column(name = "ROLE_ID", nullable = false, updatable = false, columnDefinition = "CHAR(36)")
	@Size(max = 36)
	// Users MAY submit an ID; readOnly annotation must NOT be used
	@ApiModelProperty(value = "UUID; omit for system-generated value", example="12345678-abcd-90ab-cdef-1234567890ab")
	// Generated by DB; NotNull annotation not needed
	private String roleId;

	@Column(name = "NAME", nullable = false, columnDefinition = "VARCHAR(100)")
	@NotNull(message = "Name cannot be null")
	@Size(max = 100)
	@ApiModelProperty(required = true, example = "Role Name")
	private String name;

	@Column(name = "ACTIVE_YN", nullable = false, columnDefinition = "CHAR(1) DEFAULT 'Y'")
	@ApiModelProperty(required = true)
	private boolean active;

	/**
	 * No-arg constructor
	 */
	public MLPRole() {
		// no-arg constructor
	}

	/**
	 * This constructor accepts the required fields; i.e., the minimum that the user
	 * must supply to create a valid instance.
	 * 
	 * @param name
	 *            Role name
	 * @param active
	 *            True if active
	 */
	public MLPRole(String name, boolean active) {
		if (name == null)
			throw new IllegalArgumentException("Null not permitted");
		this.name = name;
		this.active = active;
	}

	/**
	 * Copy constructor
	 * 
	 * @param that
	 *            Instance to copy
	 */
	public MLPRole(MLPRole that) {
		super(that);
		this.active = that.active;
		this.name = that.name;
		this.roleId = that.roleId;
	}

	public String getRoleId() {
		return roleId;
	}

	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	@Override
	public boolean equals(Object that) {
		if (that == null)
			return false;
		if (!(that instanceof MLPRole))
			return false;
		MLPRole thatObj = (MLPRole) that;
		return Objects.equals(roleId, thatObj.roleId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(roleId, name);
	}

	@Override
	public String toString() {
		return this.getClass().getName() + "[roleId=" + roleId + ", name=" + name + ", created=" + getCreated()
				+ ", modified=" + getModified() + "]";
	}

}

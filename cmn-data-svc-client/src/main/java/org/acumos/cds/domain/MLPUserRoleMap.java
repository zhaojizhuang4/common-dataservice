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
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import org.acumos.cds.domain.MLPUserRoleMap.UserRoleMapPK;

/**
 * Model for a row in the user-role mapping table. This is in lieu of
 * many-to-one annotations.
 */
@Entity
@IdClass(UserRoleMapPK.class)
@Table(name = "C_USER_ROLE_MAP")
public class MLPUserRoleMap implements MLPEntity, Serializable {

	private static final long serialVersionUID = -1232519251083341808L;

	/**
	 * Embedded key for Hibernate
	 */
	@Embeddable
	public static class UserRoleMapPK implements Serializable {

		private static final long serialVersionUID = 1724447182888437934L;
		private String userId;
		private String roleId;

		public UserRoleMapPK() {
			// no-arg constructor
		}

		public UserRoleMapPK(String userId, String roleId) {
			this.userId = userId;
			this.roleId = roleId;
		}

		@Override
		public boolean equals(Object that) {
			if (that == null)
				return false;
			if (!(that instanceof UserRoleMapPK))
				return false;
			UserRoleMapPK thatPK = (UserRoleMapPK) that;
			return Objects.equals(userId, thatPK.userId) && Objects.equals(roleId, thatPK.roleId);
		}

		@Override
		public int hashCode() {
			return Objects.hash(userId, roleId);
		}

	}

	@Id
	@Column(name = "USER_ID", updatable = false, nullable = false, columnDefinition = "CHAR(36)")
	@Size(max = 36)
	private String userId;

	@Id
	@Column(name = "ROLE_ID", updatable = false, nullable = false, columnDefinition = "CHAR(36)")
	@Size(max = 36)
	private String roleId;

	/**
	 * No-arg constructor
	 */
	public MLPUserRoleMap() {
		// no-arg constructor
	}

	public MLPUserRoleMap(String userId, String roleId) {
		this.userId = userId;
		this.roleId = roleId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getRoleId() {
		return roleId;
	}

	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}

	@Override
	public boolean equals(Object that) {
		if (that == null)
			return false;
		if (!(that instanceof MLPUserRoleMap))
			return false;
		MLPUserRoleMap thatObj = (MLPUserRoleMap) that;
		return Objects.equals(userId, thatObj.userId) && Objects.equals(roleId, thatObj.roleId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(userId, roleId);
	}

	@Override
	public String toString() {
		return this.getClass().getName() + "[userId=" + userId + ", roleId=" + roleId + "]";
	}

}

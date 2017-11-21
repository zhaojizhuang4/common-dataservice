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

import org.acumos.cds.domain.MLPSolUserAccMap.SolUserAccessMapPK;

/**
 * Model for a row in the solution-user-access mapping table.
 */
@Entity
@IdClass(SolUserAccessMapPK.class)
@Table(name = "C_SOL_USER_ACCESS_MAP")
public class MLPSolUserAccMap implements MLPEntity, Serializable {

	private static final long serialVersionUID = 8809818075005891800L;

	/**
	 * Embedded key for Hibernate
	 */
	@Embeddable
	public static class SolUserAccessMapPK implements Serializable {

		private static final long serialVersionUID = 501173361575972604L;
		private String solutionId;
		private String userId;

		public SolUserAccessMapPK() {
			// no-arg constructor
		}

		public SolUserAccessMapPK(String solutionId, String userId) {
			this.solutionId = solutionId;
			this.userId = userId;
		}

		@Override
		public boolean equals(Object that) {
			if (that == null)
				return false;
			if (!(that instanceof SolUserAccessMapPK))
				return false;
			SolUserAccessMapPK thatPK = (SolUserAccessMapPK) that;
			return Objects.equals(solutionId, thatPK.solutionId) && Objects.equals(userId, thatPK.userId);
		}

		@Override
		public int hashCode() {
			return Objects.hash(solutionId, userId);
		}

	}

	@Id
	@Column(name = "SOLUTION_ID", updatable = false, nullable = false, columnDefinition = "CHAR(36)")
	@Size(max = 36)
	private String solutionId;

	@Id
	@Column(name = "USER_ID", updatable = false, nullable = false, columnDefinition = "CHAR(36)")
	@Size(max = 36)
	private String userId;

	public MLPSolUserAccMap() {
		// no-arg constructor
	}

	public MLPSolUserAccMap(String solutionId, String userId) {
		this.solutionId = solutionId;
		this.userId = userId;
	}

	public String getSolutionId() {
		return solutionId;
	}

	public void setSolutionId(String solutionId) {
		this.solutionId = solutionId;
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
		if (!(that instanceof MLPSolUserAccMap))
			return false;
		MLPSolUserAccMap thatPK = (MLPSolUserAccMap) that;
		return Objects.equals(solutionId, thatPK.solutionId) && Objects.equals(userId, thatPK.userId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(solutionId, userId);
	}

	@Override
	public String toString() {
		return this.getClass().getName() + "[solutionId=" + solutionId + ", userId=" + userId + "]";
	}

}

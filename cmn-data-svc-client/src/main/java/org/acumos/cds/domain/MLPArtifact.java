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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Model for an artifact. Inherits all simple field mappings from the abstract
 * superclass.
 * 
 * Participates in a many-to-many relationship with a solution revision via a
 * mapping table, but has no annotations for that.
 */
@Entity
@Table(name = MLPAbstractArtifact.TABLE_NAME)
public class MLPArtifact extends MLPAbstractArtifact implements Serializable {

	private static final long serialVersionUID = 814823907210569812L;

	@Column(name = "OWNER_ID", nullable = false, columnDefinition = "CHAR(36)")
	@NotNull(message = "OwnerID cannot be null")
	@Size(max = 36)
	private String ownerId;

	/**
	 * No-arg constructor.
	 */
	public MLPArtifact() {
		// no-arg constructor
	}

	/**
	 * This constructor accepts the required fields; i.e., the minimum that the user
	 * must supply to create a valid instance.
	 * 
	 * @param version
	 *            Version string
	 * @param artifactTypeCode
	 *            Valid artifact type code
	 * @param name
	 *            Name
	 * @param uri
	 *            URI
	 * @param ownerId
	 *            User ID for owner
	 * @param size
	 *            Length
	 */
	public MLPArtifact(String version, String artifactTypeCode, String name, String uri, String ownerId, int size) {
		super(version, artifactTypeCode, name, uri, size);
		if (ownerId == null)
			throw new IllegalArgumentException("Null not permitted");
		this.ownerId = ownerId;
	}

	public String getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}

	@Override
	public String toString() {
		return this.getClass().getName() + "[artifactId=" + getArtifactId() + ", ownerId=" + ownerId
				+ ", artifactTypeCode=" + getArtifactTypeCode() + ", description=" + getDescription() + ", version="
				+ getVersion() + ", uri=" + getUri() + ", created=" + getCreated() + ", modified=" + getModified()
				+ "]";
	}

}

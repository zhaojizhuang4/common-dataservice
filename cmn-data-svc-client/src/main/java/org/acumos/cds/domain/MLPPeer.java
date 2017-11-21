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
import org.hibernate.annotations.Type;

/**
 * Model for a peer - systems that communicate.
 */
@Entity
@Table(name = "C_PEER")
public class MLPPeer extends MLPTimestampedEntity implements Serializable {

	private static final long serialVersionUID = -8132835732122031289L;

	@Id
	@GeneratedValue(generator = "customUseOrGenerate")
	@GenericGenerator(name = "customUseOrGenerate", strategy = "org.acumos.cds.util.UseExistingOrNewUUIDGenerator")
	@Column(name = "PEER_ID", updatable = false, nullable = false, columnDefinition = "CHAR(36)")
	private String peerId;

	@Column(name = "NAME", nullable = false, columnDefinition = "VARCHAR(50)")
	@Size(max = 50)
	private String name;

	/**
	 * For x.509 certificate
	 */
	@Column(name = "SUBJECT_NAME", columnDefinition = "VARCHAR(100)")
	@Size(max = 100)
	private String subjectName;

	@Column(name = "DESCRIPTION", columnDefinition = "VARCHAR(512)")
	@Size(max = 512)
	private String description;

	@Column(name = "API_URL", columnDefinition = "VARCHAR(512)")
	@Size(max = 512)
	private String apiUrl;

	@Column(name = "WEB_URL", columnDefinition = "VARCHAR(512)")
	@Size(max = 512)
	private String webUrl;

	@Column(name = "IS_ACTIVE", nullable = false, columnDefinition = "CHAR(1) DEFAULT 'Y'")
	@Type(type = "yes_no")
	private boolean isActive;

	@Column(name = "IS_SELF", nullable = false, columnDefinition = "CHAR(1) DEFAULT 'N'")
	@Type(type = "yes_no")
	private boolean isSelf;

	@Column(name = "CONTACT1", nullable = false, columnDefinition = "VARCHAR(100)")
	@NotNull(message = "contact1 cannot be null")
	@Size(max = 100)
	private String contact1;

	@Column(name = "CONTACT2", nullable = false, columnDefinition = "VARCHAR(100)")
	@NotNull(message = "contact2 cannot be null")
	@Size(max = 100)
	private String contact2;

	/**
	 * No-arg constructor.
	 */
	public MLPPeer() {
		// no-arg constructor
	}

	/**
	 * This constructor accepts the required fields; i.e., the minimum that the user
	 * must supply to create a valid instance.
	 * 
	 * @param name
	 *            Peer name
	 * @param subjectName
	 *            X.509 subject name
	 * @param apiUrl
	 *            API URL
	 * @param webUrl
	 *            Web URL
	 * @param isActive
	 *            Is the entry active
	 * @param isSelf
	 *            Is the entry this site
	 * @param contact1
	 *            Primary contact details
	 * @param contact2
	 *            Alternate contact details
	 */
	public MLPPeer(String name, String subjectName, String apiUrl, String webUrl, boolean isActive, boolean isSelf,
			String contact1, String contact2) {
		this.name = name;
		this.subjectName = subjectName;
		this.apiUrl = apiUrl;
		this.webUrl = webUrl;
		this.isActive = isActive;
		this.isSelf = isSelf;
		this.contact1 = contact1;
		this.contact2 = contact2;
	}

	/**
	 * @return the peerId
	 */
	public String getPeerId() {
		return peerId;
	}

	/**
	 * @param peerId
	 *            the peerId to set
	 */
	public void setPeerId(String peerId) {
		this.peerId = peerId;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the subject name
	 */
	public String getSubjectName() {
		return subjectName;
	}

	/**
	 * @param subjectName
	 *            the name to set
	 */
	public void setSubjectName(String subjectName) {
		this.subjectName = subjectName;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the apiUrl
	 */
	public String getApiUrl() {
		return apiUrl;
	}

	/**
	 * @param apiUrl
	 *            the apiUrl to set
	 */
	public void setApiUrl(String apiUrl) {
		this.apiUrl = apiUrl;
	}

	/**
	 * @return the webUrl
	 */
	public String getWebUrl() {
		return webUrl;
	}

	/**
	 * @param webUrl
	 *            the webUrl to set
	 */
	public void setWebUrl(String webUrl) {
		this.webUrl = webUrl;
	}

	/**
	 * @return the isActive
	 */
	public boolean isActive() {
		return isActive;
	}

	/**
	 * @param isActive
	 *            the isActive to set
	 */
	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	/**
	 * @return the isSelf
	 */
	public boolean isSelf() {
		return isSelf;
	}

	/**
	 * @param isSelf
	 *            the isSelf to set
	 */
	public void setSelf(boolean isSelf) {
		this.isSelf = isSelf;
	}

	/**
	 * @return the contact1
	 */
	public String getContact1() {
		return contact1;
	}

	/**
	 * @param contact1
	 *            the contact1 to set
	 */
	public void setContact1(String contact1) {
		this.contact1 = contact1;
	}

	/**
	 * @return the contact2
	 */
	public String getContact2() {
		return contact2;
	}

	/**
	 * @param contact2
	 *            the contact2 to set
	 */
	public void setContact2(String contact2) {
		this.contact2 = contact2;
	}

	@Override
	public boolean equals(Object that) {
		if (that == null)
			return false;
		if (!(that instanceof MLPPeer))
			return false;
		MLPPeer thatObj = (MLPPeer) that;
		return Objects.equals(peerId, thatObj.peerId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(peerId, name, subjectName, webUrl);
	}

	@Override
	public String toString() {
		return this.getClass().getName() + "[peerId=" + peerId + ", name=" + name + ", subjectName=" + subjectName
				+ ", ...]";
	}

}

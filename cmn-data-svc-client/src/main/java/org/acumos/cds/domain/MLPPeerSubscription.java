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
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Size;

/**
 * Model for a peer subscription.
 */
@Entity
@Table(name = "C_PEER_SUB")
public class MLPPeerSubscription extends MLPTimestampedEntity implements Serializable {

	private static final long serialVersionUID = -250189208515313944L;

	// Hibernate is weak on the ID column generator, the method is specific to
	// the backing database. For portability, specify AUTO and define the column
	// appropriately in the database, which in MySQL requires "AUTO_INCREMENT".
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "SUB_ID", updatable = false, nullable = false, columnDefinition = "INT")
	private Long subId;

	@Column(name = "PEER_ID", nullable = false, columnDefinition = "CHAR(36)")
	@Size(max = 36)
	private String peerId;

	// JSON
	@Column(name = "SELECTOR", columnDefinition = "VARCHAR(1024)")
	@Size(max = 1024)
	private String selector;

	// JSON
	@Column(name = "OPTIONS", columnDefinition = "VARCHAR(1024)")
	@Size(max = 1024)
	private String options;

	@Column(name = "REFRESH_INTERVAL", columnDefinition = "INT")
	private Long refreshInterval;

	@Column(name = "MAX_ARTIFACT_SIZE", columnDefinition = "INT")
	private Long maxArtifactSize;

	/**
	 * No-arg constructor
	 */
	public MLPPeerSubscription() {
		// no-arg constructor
	}

	/**
	 * This constructor accepts the required fields; i.e., the minimum that the user
	 * must supply to create a valid instance.
	 * 
	 * @param peerId
	 *            Peer ID
	 */
	public MLPPeerSubscription(String peerId) {
		this.peerId = peerId;
	}

	public Long getSubId() {
		return subId;
	}

	public void setSubId(Long subId) {
		this.subId = subId;
	}

	public String getPeerId() {
		return peerId;
	}

	public void setPeerId(String peerId) {
		this.peerId = peerId;
	}

	public String getSelector() {
		return selector;
	}

	public void setSelector(String selector) {
		this.selector = selector;
	}

	public String getOptions() {
		return options;
	}

	public void setOptions(String options) {
		this.options = options;
	}

	/**
	 * @return seconds
	 */
	public Long getRefreshInterval() {
		return refreshInterval;
	}

	/**
	 * @param refreshInterval
	 *            seconds
	 */
	public void setRefreshInterval(Long refreshInterval) {
		this.refreshInterval = refreshInterval;
	}

	/**
	 * @return bytes
	 */
	public Long getMaxArtifactSize() {
		return maxArtifactSize;
	}

	/**
	 * @param maxArtifactSize
	 *            bytes
	 */
	public void setMaxArtifactSize(Long maxArtifactSize) {
		this.maxArtifactSize = maxArtifactSize;
	}

	@Override
	public boolean equals(Object that) {
		if (that == null)
			return false;
		if (!(that instanceof MLPPeerSubscription))
			return false;
		MLPPeerSubscription thatObj = (MLPPeerSubscription) that;
		return Objects.equals(subId, thatObj.subId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(subId, peerId, selector, options);
	}

	@Override
	public String toString() {
		return this.getClass().getName() + "[subId=" + subId + ", peerId=" + peerId + ", refreshInterval="
				+ refreshInterval + ", ...]";
	}

}

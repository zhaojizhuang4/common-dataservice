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
import javax.validation.constraints.Size;

import org.hibernate.annotations.GenericGenerator;

/**
 * A comment thread.
 */
@Entity
@Table(name = "C_THREAD")
public class MLPThread implements MLPEntity, Serializable {

	private static final long serialVersionUID = -8103629633174998747L;

	@Id
	@GeneratedValue(generator = "customUseOrGenerate")
	@GenericGenerator(name = "customUseOrGenerate", strategy = "org.acumos.cds.util.UseExistingOrNewUUIDGenerator")
	@Column(name = "THREAD_ID", updatable = false, nullable = false, columnDefinition = "CHAR(36)")
	@Size(max = 36)
	private String threadId;

	@Column(name = "TITLE", columnDefinition = "VARCHAR(100)")
	@Size(max = 100)
	private String title;

	@Column(name = "URL", columnDefinition = "VARCHAR(512)")
	@Size(max = 512)
	private String url;

	/**
	 * No-arg constructor
	 */
	public MLPThread() {
		// no-arg constructor
	}

	/**
	 * This constructor accepts the required fields; i.e., the minimum that the user
	 * must supply to create a valid instance.
	 * 
	 * @param url
	 *            The URL
	 */
	public MLPThread(String url) {
		this.url = url;
	}

	public String getThreadId() {
		return threadId;
	}

	public void setThreadId(String threadId) {
		this.threadId = threadId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public boolean equals(Object that) {
		if (that == null)
			return false;
		if (!(that instanceof MLPThread))
			return false;
		MLPThread thatObj = (MLPThread) that;
		return Objects.equals(threadId, thatObj.threadId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(threadId, title, url);
	}

	@Override
	public String toString() {
		return this.getClass().getName() + "[threadId=" + threadId + ", title=" + title + ", url=" + url + "]";
	}

}

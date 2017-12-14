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

package org.acumos.cds.transport;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/* https://blog.thecookinkitchen.com/how-to-consume-page-response-from-a-service-in-spring-boot-97293c18ba */

public class RestPageResponse<T> extends PageImpl<T> {

	private static final long serialVersionUID = 5835593096562217592L;

	private int number;
	private int size;
	private int totalPages;
	private int numberOfElements;
	private long totalElements;
	private boolean previousPage;
	private boolean first;
	private boolean nextPage;
	private boolean last;
	private Sort sort;

	/**
	 * Builds an object with an empty list.
	 */
	public RestPageResponse() {
		super(new ArrayList<T>());
	}

	/**
	 * Builds an object with the specified list.
	 * 
	 * @param content
	 *            List of content
	 */
	public RestPageResponse(List<T> content) {
		super(content);
		if (content != null)
			this.numberOfElements = content.size();
	}

	/**
	 * Builds an object with the specified values.
	 * 
	 * @param content
	 *            List of content
	 * @param pageable
	 *            Pageable
	 * @param total
	 *            Count
	 */
	public RestPageResponse(List<T> content, Pageable pageable, long total) {
		super(content, pageable, total);
		if (content != null)
			this.numberOfElements = content.size();
	}

	/**
	 * Builds a PageImpl
	 * 
	 * @return new PageImpl
	 */
	public PageImpl<T> pageImpl() {
		return new PageImpl<>(getContent(), new PageRequest(getNumber(), getSize(), getSort()), getTotalElements());
	}

	@Override
	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	@Override
	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	@Override
	public int getTotalPages() {
		return totalPages;
	}

	public void setTotalPages(int totalPages) {
		this.totalPages = totalPages;
	}

	@Override
	public int getNumberOfElements() {
		return numberOfElements;
	}

	public void setNumberOfElements(int numberOfElements) {
		this.numberOfElements = numberOfElements;
	}

	public boolean isPreviousPage() {
		return previousPage;
	}

	public void setPreviousPage(boolean previousPage) {
		this.previousPage = previousPage;
	}

	@Override
	public boolean isFirst() {
		return first;
	}

	public void setFirst(boolean first) {
		this.first = first;
	}

	public boolean isNextPage() {
		return nextPage;
	}

	public void setNextPage(boolean nextPage) {
		this.nextPage = nextPage;
	}

	@Override
	public boolean isLast() {
		return last;
	}

	public void setLast(boolean last) {
		this.last = last;
	}

	@Override
	public Sort getSort() {
		return sort;
	}

	@JsonDeserialize(using = CustomSortDeserializer.class)
	public void setSort(Sort sort) {
		this.sort = sort;
	}

	@Override
	public boolean equals(Object that) {
		if (that == null)
			return false;
		if (!(that instanceof RestPageResponse))
			return false;
		@SuppressWarnings("rawtypes")
		RestPageResponse thatPK = (RestPageResponse) that;
		return Objects.equals(getContent(), thatPK.getContent()) && Objects.equals(size, thatPK.size);
	}

	@Override
	public int hashCode() {
		return Objects.hash(getContent(), number, numberOfElements, size, totalElements, totalPages);
	}

}

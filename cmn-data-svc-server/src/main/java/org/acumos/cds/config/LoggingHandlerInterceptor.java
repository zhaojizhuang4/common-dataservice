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

package org.acumos.cds.config;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.acumos.cds.CCDSConstants;
import org.acumos.cds.logging.ONAPLogConstants;
import org.slf4j.MDC;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * Adds request details to the mapped diagnostic context (MDC) so they can be
 * logged. <BR>
 * http://www.devgrok.com/2017/04/adding-mdc-headers-to-every-spring-mvc.html
 */
public class LoggingHandlerInterceptor extends HandlerInterceptorAdapter {

	/**
	 * Tracks the set of keys added to MDC.
	 */
	private ThreadLocal<Set<String>> storedKeys = ThreadLocal.withInitial(() -> new HashSet<>());

	/**
	 * Copies key-value pairs from HTTP request to MDC context. Unfortunately they
	 * use different conventions for key naming.
	 */
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {

		addKey(ONAPLogConstants.MDCs.SERVER_FQDN, InetAddress.getLocalHost().getCanonicalHostName());
		addKey(ONAPLogConstants.MDCs.CLIENT_IP_ADDRESS, request.getRemoteAddr());
		addKey(ONAPLogConstants.MDCs.SERVICE_NAME, request.getRequestURI());
		final String requestId = request.getHeader(CCDSConstants.X_REQUEST_ID);
		if (requestId != null)
			addKey(ONAPLogConstants.MDCs.REQUEST_ID, requestId);
		return true;
	}

	private void addKey(String key, String value) {
		MDC.put(key, value);
		storedKeys.get().add(key);
	}

	// request ended on current thread remove properties
	@Override
	public void afterConcurrentHandlingStarted(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		removeKeys();
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		removeKeys();
	}

	private void removeKeys() {
		for (String key : storedKeys.get())
			MDC.remove(key);

		storedKeys.remove();
	}
}
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

package org.acumos.cds.controller;

import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.acumos.cds.CCDSConstants;
import org.acumos.cds.domain.MLPNotification;
import org.acumos.cds.repository.NotificationRepository;
import org.acumos.cds.transport.CountTransport;
import org.acumos.cds.transport.ErrorTransport;
import org.acumos.cds.transport.MLPTransportModel;
import org.acumos.cds.transport.SuccessTransport;
import org.acumos.cds.util.EELFLoggerDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.ApiOperation;

/**
 * Answers REST requests to get, add, update and delete notifications; to record
 * which users should receive a notification; to get notifications relevant for
 * a user; and to update when a user has viewed a notification.
 * 
 * https://stackoverflow.com/questions/942951/rest-api-error-return-good-practices
 */
@Controller
@RequestMapping("/" + CCDSConstants.NOTIFICATION_PATH)
public class NotificationController extends AbstractController {

	private static final EELFLoggerDelegate logger = EELFLoggerDelegate.getLogger(NotificationController.class);

	@Autowired
	private NotificationRepository notificationRepository;

	/**
	 * @return SuccessTransport object
	 */
	@ApiOperation(value = "Gets the count of notifications.", response = CountTransport.class)
	@RequestMapping(value = "/" + CCDSConstants.COUNT_PATH, method = RequestMethod.GET)
	@ResponseBody
	public CountTransport getNotificationCount() {
		Long count = notificationRepository.count();
		logger.debug(EELFLoggerDelegate.debugLogger, "getNotificationCount: result is {} ", count);
		return new CountTransport(count);
	}

	/**
	 * 
	 * @param pageable
	 *            Sort and page criteria
	 * @return Page of notifications
	 */
	@ApiOperation(value = "Gets a page of notifications, optionally sorted.", response = MLPNotification.class, responseContainer = "Page")
	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	public Page<MLPNotification> getNotifications(Pageable pageable) {
		return notificationRepository.findAll(pageable);
	}

	/**
	 * @param notif
	 *            Notification to save. If no ID is set a new one will be generated;
	 *            if an ID value is set, it will be used if valid and not in table.
	 * @param response
	 *            HttpServletResponse
	 * @return model to be serialized as JSON
	 */
	@ApiOperation(value = "Creates a new notification.", response = MLPNotification.class)
	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public Object createNotification(@RequestBody MLPNotification notif, HttpServletResponse response) {
		logger.debug(EELFLoggerDelegate.debugLogger, "create: received {} ", notif);
		Object result;
		try {
			String id = notif.getNotificationId();
			if (id != null) {
				UUID.fromString(id);
				if (notificationRepository.findOne(id) != null) {
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					result = new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "ID exists: " + id);
					return result;
				}
			}
			// Create a new row
			result = notificationRepository.save(notif);
			response.setStatus(HttpServletResponse.SC_CREATED);
			// This is a hack to create the location path.
			response.setHeader(HttpHeaders.LOCATION, CCDSConstants.NOTIFICATION_PATH + "/" + notif.getNotificationId());
		} catch (Exception ex) {
			Exception cve = findConstraintViolationException(ex);
			logger.warn(EELFLoggerDelegate.errorLogger, "createNotification", cve.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			result = new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "createNotification failed", cve);
		}
		return result;
	}

	/**
	 * @param rowId
	 *            Path parameter with the row ID
	 * @param notif
	 *            Notification to be updated
	 * @param response
	 *            HttpServletResponse
	 * @return Transport model with success or failure
	 */
	@ApiOperation(value = "Updates a notification.", response = SuccessTransport.class)
	@RequestMapping(value = "/{notificationId}", method = RequestMethod.PUT)
	@ResponseBody
	public Object updateNotification(@PathVariable("notificationId") String rowId, @RequestBody MLPNotification notif,
			HttpServletResponse response) {
		logger.debug(EELFLoggerDelegate.debugLogger, "update: received object {} ", notif);
		// Get the existing one
		MLPNotification existing = notificationRepository.findOne(rowId);
		if (existing == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + rowId, null);
		}
		MLPTransportModel result = null;
		try {
			// Use the path-parameter id; don't trust the one in the object
			notif.setNotificationId(rowId);
			// Update the existing row
			notificationRepository.save(notif);
			result = new SuccessTransport(HttpServletResponse.SC_OK, null);
		} catch (Exception ex) {
			Exception cve = findConstraintViolationException(ex);
			logger.warn(EELFLoggerDelegate.errorLogger, "updateNotification", cve.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			result = new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "updateNotification failed", cve);
		}
		return result;
	}

	/**
	 * @param rowId
	 *            Path parameter that identifies the instance
	 * @param response
	 *            HttpServletResponse
	 * @return Transport model with success or failure
	 */
	@ApiOperation(value = "Deletes a notification.", response = SuccessTransport.class)
	@RequestMapping(value = "/{notificationId}", method = RequestMethod.DELETE)
	@ResponseBody
	public MLPTransportModel deleteNotification(@PathVariable("notificationId") String rowId,
			HttpServletResponse response) {
		try {
			notificationRepository.delete(rowId);
			return new SuccessTransport(HttpServletResponse.SC_OK, null);
		} catch (Exception ex) {
			// e.g., EmptyResultDataAccessException is NOT an internal server error
			logger.warn(EELFLoggerDelegate.errorLogger, "deleteNotification", ex.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "deleteNotification failed", ex);
		}
	}

}

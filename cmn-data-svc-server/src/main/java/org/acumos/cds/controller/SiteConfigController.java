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

import javax.servlet.http.HttpServletResponse;

import org.acumos.cds.CCDSConstants;
import org.acumos.cds.domain.MLPSiteConfig;
import org.acumos.cds.repository.SiteConfigRepository;
import org.acumos.cds.repository.UserRepository;
import org.acumos.cds.transport.ErrorTransport;
import org.acumos.cds.transport.MLPTransportModel;
import org.acumos.cds.transport.SuccessTransport;
import org.acumos.cds.util.EELFLoggerDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.ApiOperation;

/**
 * Answers REST requests to manage site configuration entries.
 */
@Controller
@RequestMapping(value = "/" + CCDSConstants.CONFIG_PATH, produces = CCDSConstants.APPLICATION_JSON)
public class SiteConfigController extends AbstractController {

	private static final EELFLoggerDelegate logger = EELFLoggerDelegate.getLogger(SiteConfigController.class);

	@Autowired
	private SiteConfigRepository siteConfigRepository;
	@Autowired
	private UserRepository userRepository;

	/**
	 * @param configKey
	 *            Key to row with config value
	 * @param response
	 *            HttpServletResponse
	 * @return Success or Error transport model
	 */
	@ApiOperation(value = "Gets the site configuration for the specified key.", response = SuccessTransport.class)
	@RequestMapping(value = "/{configKey}", method = RequestMethod.GET)
	@ResponseBody
	public Object getSiteConfig(@PathVariable("configKey") String configKey, HttpServletResponse response) {
		MLPSiteConfig da = siteConfigRepository.findOne(configKey);
		if (da == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "No entry for ID " + configKey, null);
		}
		return da;
	}

	/**
	 * @param sc
	 *            SiteConfig model
	 * @param response
	 *            HttpServletResponse
	 * @return SiteConfig model
	 */
	@ApiOperation(value = "Creates a new site configuration.", response = MLPSiteConfig.class)
	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public Object createSiteConfig(@RequestBody MLPSiteConfig sc, HttpServletResponse response) {
		logger.debug(EELFLoggerDelegate.debugLogger, "createSiteConfig: received object: {} ", sc);
		if (siteConfigRepository.findOne(sc.getConfigKey()) != null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "Key exists: " + sc.getConfigKey(), null);
		}
		if (userRepository.findOne(sc.getUserId()) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "No user " + sc.getUserId(), null);
		}
		Object result;
		try {
			result = siteConfigRepository.save(sc);
			response.setStatus(HttpServletResponse.SC_CREATED);
			// This is a hack to create the location path.
			response.setHeader(HttpHeaders.LOCATION, CCDSConstants.CONFIG_PATH + "/" + sc.getConfigKey());
		} catch (Exception ex) {
			Exception cve = findConstraintViolationException(ex);
			logger.warn(EELFLoggerDelegate.errorLogger, "createSiteConfig", cve.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			result = new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "createSiteConfig failed", cve);
		}
		return result;
	}

	/**
	 * @param configKey
	 *            config key
	 * @param sc
	 *            SiteConfig model
	 * @param response
	 *            HttpServletResponse
	 * @return Success or error model
	 */
	@ApiOperation(value = "Updates a site configuration.", response = SuccessTransport.class)
	@RequestMapping(value = "/{configKey}", method = RequestMethod.PUT)
	@ResponseBody
	public Object updateSiteConfig(@PathVariable("configKey") String configKey, @RequestBody MLPSiteConfig sc,
			HttpServletResponse response) {
		logger.debug(EELFLoggerDelegate.debugLogger, "updateSiteConfig: received {} ", sc);
		// Get the existing one
		MLPSiteConfig existing = siteConfigRepository.findOne(configKey);
		if (existing == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "Failed to find object with id " + configKey,
					null);
		}
		MLPTransportModel result = null;
		try {
			// Use the path-parameter id; don't trust the one in the object
			sc.setConfigKey(configKey);
			// Update the existing row
			siteConfigRepository.save(sc);
			// Answer "OK"
			result = new SuccessTransport(HttpServletResponse.SC_OK, null);
		} catch (Exception ex) {
			Exception cve = findConstraintViolationException(ex);
			logger.warn(EELFLoggerDelegate.errorLogger, "updateSiteConfig", cve.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			result = new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "updateSiteConfig failed", cve);
		}
		return result;
	}

	/**
	 * @param configKey
	 *            config key
	 * @param response
	 *            HttpServletResponse
	 * @return Success or error model
	 */
	@ApiOperation(value = "Deletes a site configuration.", response = SuccessTransport.class)
	@RequestMapping(value = "/{configKey}", method = RequestMethod.DELETE)
	@ResponseBody
	public MLPTransportModel deleteSiteConfig(@PathVariable("configKey") String configKey,
			HttpServletResponse response) {
		try {
			siteConfigRepository.delete(configKey);
			return new SuccessTransport(HttpServletResponse.SC_OK, null);
		} catch (Exception ex) {
			// e.g., EmptyResultDataAccessException is NOT an internal server error
			logger.warn(EELFLoggerDelegate.errorLogger, "deleteSiteConfig", ex.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "deleteSiteConfig failed", ex);
		}
	}

}

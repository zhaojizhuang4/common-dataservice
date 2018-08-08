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

import java.lang.invoke.MethodHandles;

import javax.servlet.http.HttpServletResponse;

import org.acumos.cds.CCDSConstants;
import org.acumos.cds.domain.MLPSiteConfig;
import org.acumos.cds.repository.SiteConfigRepository;
import org.acumos.cds.repository.UserRepository;
import org.acumos.cds.transport.ErrorTransport;
import org.acumos.cds.transport.MLPTransportModel;
import org.acumos.cds.transport.SuccessTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
@RequestMapping(value = "/" + CCDSConstants.CONFIG_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
public class SiteConfigController extends AbstractController {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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
		logger.info("getSiteConfig key {}", configKey);
		MLPSiteConfig da = siteConfigRepository.findOne(configKey);
		if (da == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + configKey, null);
		}
		return da;
	}

	/**
	 * @param siteConfig
	 *            SiteConfig model
	 * @param response
	 *            HttpServletResponse
	 * @return SiteConfig model
	 */
	@ApiOperation(value = "Creates a new site configuration.", response = MLPSiteConfig.class)
	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public Object createSiteConfig(@RequestBody MLPSiteConfig siteConfig, HttpServletResponse response) {
		logger.info("createSiteConfig: config key {}", siteConfig.getConfigKey());
		if (siteConfigRepository.findOne(siteConfig.getConfigKey()) != null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "Key exists: " + siteConfig.getConfigKey(),
					null);
		}
		// UserID is optional
		if (siteConfig.getUserId() != null && userRepository.findOne(siteConfig.getUserId()) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + siteConfig.getUserId(),
					null);
		}
		try {
			Object result = siteConfigRepository.save(siteConfig);
			response.setStatus(HttpServletResponse.SC_CREATED);
			// This is a hack to create the location path.
			response.setHeader(HttpHeaders.LOCATION, CCDSConstants.CONFIG_PATH + "/" + siteConfig.getConfigKey());
			return result;
		} catch (Exception ex) {
			// e.g., EmptyResultDataAccessException is NOT an internal server error
			Exception cve = findConstraintViolationException(ex);
			logger.warn("createSiteConfig failed: {}", cve.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "createSiteConfig failed", cve);
		}
	}

	/**
	 * @param configKey
	 *            config key
	 * @param siteConfig
	 *            SiteConfig model
	 * @param response
	 *            HttpServletResponse
	 * @return Success or error model
	 */
	@ApiOperation(value = "Updates a site configuration.", response = SuccessTransport.class)
	@RequestMapping(value = "/{configKey}", method = RequestMethod.PUT)
	@ResponseBody
	public Object updateSiteConfig(@PathVariable("configKey") String configKey, @RequestBody MLPSiteConfig siteConfig,
			HttpServletResponse response) {
		logger.info("updateSiteConfig key {}", configKey);
		// Get the existing one
		MLPSiteConfig existing = siteConfigRepository.findOne(configKey);
		if (existing == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + configKey, null);
		}
		try {
			// Use the path-parameter id; don't trust the one in the object
			siteConfig.setConfigKey(configKey);
			siteConfigRepository.save(siteConfig);
			return new SuccessTransport(HttpServletResponse.SC_OK, null);
		} catch (Exception ex) {
			// e.g., EmptyResultDataAccessException is NOT an internal server error
			Exception cve = findConstraintViolationException(ex);
			logger.warn("updateSiteConfig failed: {}", cve.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "updateSiteConfig failed", cve);
		}
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
		logger.info("deleteSiteConfig key {}", configKey);
		try {
			siteConfigRepository.delete(configKey);
			return new SuccessTransport(HttpServletResponse.SC_OK, null);
		} catch (Exception ex) {
			// e.g., EmptyResultDataAccessException is NOT an internal server error
			logger.warn("deleteSiteConfig failed: {}", ex.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "deleteSiteConfig failed", ex);
		}
	}

}

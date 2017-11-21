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

import org.acumos.cds.CCDSConstants;
import org.acumos.cds.CdsApplication;
import org.acumos.cds.repository.ArtifactRepository;
import org.acumos.cds.transport.MLPTransportModel;
import org.acumos.cds.transport.SuccessTransport;
import org.acumos.cds.util.EELFLoggerDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.ApiOperation;

/**
 * Answers REST requests for the service health.
 */
@Controller
@RequestMapping(value = "/", produces = CCDSConstants.APPLICATION_JSON)
public class HealthcheckController extends AbstractController {

	private static final EELFLoggerDelegate logger = EELFLoggerDelegate.getLogger(HealthcheckController.class);

	@Autowired
	private ArtifactRepository artifactRepository;

	/**
	 * @return a JSON block with some status details.
	 */
	@ApiOperation(value = "Assesses the health of the application by querying the database.", response = SuccessTransport.class)
	@RequestMapping(value = CCDSConstants.HEALTHCHECK_PATH, method = RequestMethod.GET)
	@ResponseBody
	public MLPTransportModel getHealth() {
		long count = artifactRepository.count();
		logger.debug("getHealth: count is {}", count);
		return new SuccessTransport(200, "database reports artifact count is " + count);
	}

	/**
	 * @return A JSON block with version information as read from the
	 *         Implementation-Version property of the MANIFEST.MF.
	 */
	@ApiOperation(value = "Gets the value of the MANIFEST.MF property Implementation-Version as written by maven.", response = SuccessTransport.class)
	@RequestMapping(value = CCDSConstants.VERSION_PATH, method = RequestMethod.GET)
	@ResponseBody
	public MLPTransportModel getVersion() {
		String className = this.getClass().getSimpleName() + ".class";
		String classPath = this.getClass().getResource(className).toString();
		String version = classPath.startsWith("jar") ? CdsApplication.class.getPackage().getImplementationVersion()
				: "no version, classpath is not jar";
		return new SuccessTransport(200, version);
	}

}

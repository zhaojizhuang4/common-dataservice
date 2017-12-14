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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.acumos.cds.CCDSConstants;
import org.acumos.cds.domain.MLPAccessType;
import org.acumos.cds.domain.MLPArtifactType;
import org.acumos.cds.domain.MLPDeploymentStatus;
import org.acumos.cds.domain.MLPLoginProvider;
import org.acumos.cds.domain.MLPModelType;
import org.acumos.cds.domain.MLPToolkitType;
import org.acumos.cds.domain.MLPValidationSequence;
import org.acumos.cds.domain.MLPValidationSequence.ValidationSequencePK;
import org.acumos.cds.domain.MLPValidationStatus;
import org.acumos.cds.domain.MLPValidationType;
import org.acumos.cds.repository.AccessTypeRepository;
import org.acumos.cds.repository.ArtifactTypeRepository;
import org.acumos.cds.repository.DeploymentStatusRepository;
import org.acumos.cds.repository.LoginProviderRepository;
import org.acumos.cds.repository.ModelTypeRepository;
import org.acumos.cds.repository.ToolkitTypeRepository;
import org.acumos.cds.repository.ValidationSequenceRepository;
import org.acumos.cds.repository.ValidationStatusRepository;
import org.acumos.cds.repository.ValidationTypeRepository;
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
 * Provides getters for all code-name tables.
 */
@Controller
public class CodeTableController extends AbstractController {

	private static final EELFLoggerDelegate logger = EELFLoggerDelegate.getLogger(CodeTableController.class);

	@Autowired
	private AccessTypeRepository accessTypeRepository;
	@Autowired
	private ArtifactTypeRepository artifactTypeRepository;
	@Autowired
	private LoginProviderRepository loginProviderRepository;
	@Autowired
	private ModelTypeRepository modelTypeRepository;
	@Autowired
	private ToolkitTypeRepository toolkitTypeRepository;
	@Autowired
	private ValidationStatusRepository validationStatusRepository;
	@Autowired
	private ValidationTypeRepository validationTypeRepository;
	@Autowired
	private ValidationSequenceRepository validationSequenceRepository;
	@Autowired
	private DeploymentStatusRepository deploymentStatusRepository;

	/**
	 * @return List of MLPAccessType objects
	 * @throws ServletException
	 *             in case of unrecoverable failure
	 */
	@ApiOperation(value = "Gets the list of access types.", response = MLPAccessType.class, responseContainer = "List")
	@RequestMapping(value = "/" + CCDSConstants.ACCESS_PATH + "/" + CCDSConstants.TYPE_PATH, method = RequestMethod.GET)
	@ResponseBody
	public Iterable<MLPAccessType> getAccessTypeList() throws ServletException {
		return accessTypeRepository.findAll();
	}

	/**
	 * @return List of MLPArtifactType objects
	 * @throws ServletException
	 *             in case of unrecoverable failure
	 */
	@ApiOperation(value = "Gets the list of artifact types.", response = MLPArtifactType.class, responseContainer = "List")
	@RequestMapping(value = "/" + CCDSConstants.ARTIFACT_PATH + "/"
			+ CCDSConstants.TYPE_PATH, method = RequestMethod.GET)
	@ResponseBody
	public Iterable<MLPArtifactType> getArtifactTypeList() throws ServletException {
		return artifactTypeRepository.findAll();
	}

	/**
	 * @return List of MLPLoginProvider objects
	 * @throws ServletException
	 *             in case of unrecoverable failure
	 */
	@ApiOperation(value = "Gets the list of login providers.", response = MLPLoginProvider.class, responseContainer = "List")
	@RequestMapping(value = "/" + CCDSConstants.LOGIN_PROVIDER_PATH, method = RequestMethod.GET)
	@ResponseBody
	public Iterable<MLPLoginProvider> getLoginProviderList() throws ServletException {
		return loginProviderRepository.findAll();
	}

	/**
	 * @return List of MLPToolkitType objects
	 * @throws ServletException
	 *             in case of unrecoverable failure
	 */
	@ApiOperation(value = "Gets the list of model types.", response = MLPToolkitType.class, responseContainer = "List")
	@RequestMapping(value = "/" + CCDSConstants.MODEL_PATH + "/" + CCDSConstants.TYPE_PATH, method = RequestMethod.GET)
	@ResponseBody
	public Iterable<MLPModelType> getModelTypeList() throws ServletException {
		return modelTypeRepository.findAll();
	}

	/**
	 * @return List of MLPToolkitType objects
	 * @throws ServletException
	 *             in case of unrecoverable failure
	 */
	@ApiOperation(value = "Gets the list of toolkit types.", response = MLPToolkitType.class, responseContainer = "List")
	@RequestMapping(value = "/" + CCDSConstants.TOOLKIT_PATH + "/"
			+ CCDSConstants.TYPE_PATH, method = RequestMethod.GET)
	@ResponseBody
	public Iterable<MLPToolkitType> getToolkitTypeList() throws ServletException {
		return toolkitTypeRepository.findAll();
	}

	/**
	 * @return List of MLPValidationStatus objects
	 * @throws ServletException
	 *             in case of unrecoverable failure
	 */
	@ApiOperation(value = "Gets the list of validation status codes.", response = MLPValidationStatus.class, responseContainer = "Iterable")
	@RequestMapping(value = "/" + CCDSConstants.VAL_STAT_PATH, method = RequestMethod.GET)
	@ResponseBody
	public Iterable<MLPValidationStatus> getValidationStatusList() throws ServletException {
		return validationStatusRepository.findAll();
	}

	/**
	 * @return List of MLPValidationType objects
	 * @throws ServletException
	 *             in case of unrecoverable failure
	 */
	@ApiOperation(value = "Gets the list of validation type codes.", response = MLPValidationType.class, responseContainer = "Iterable")
	@RequestMapping(value = "/" + CCDSConstants.VAL_TYPE_PATH, method = RequestMethod.GET)
	@ResponseBody
	public Iterable<MLPValidationType> getValidationTypeList() throws ServletException {
		return validationTypeRepository.findAll();
	}

	/**
	 * @return List of MLPDeploymentStatus objects
	 * @throws ServletException
	 *             in case of unrecoverable failure
	 */
	@ApiOperation(value = "Gets the list of deployment status codes.", response = MLPDeploymentStatus.class, responseContainer = "Iterable")
	@RequestMapping(value = "/" + CCDSConstants.DEP_STAT_PATH, method = RequestMethod.GET)
	@ResponseBody
	public Iterable<MLPDeploymentStatus> getDeploymentStatusList() throws ServletException {
		return deploymentStatusRepository.findAll();
	}

	/**
	 * @return List of MLPSolValSeq objects
	 * @throws ServletException
	 *             in case of unrecoverable failure
	 */
	@ApiOperation(value = "Gets the list of validation sequence records.", response = MLPValidationSequence.class, responseContainer = "Iterable")
	@RequestMapping(value = "/" + CCDSConstants.VAL_SEQ_PATH, method = RequestMethod.GET)
	@ResponseBody
	public Iterable<MLPValidationSequence> getValidationSequenceList() throws ServletException {
		return validationSequenceRepository.findAll();
	}

	/**
	 * @param sequence
	 *            Instance to save (redundant; the path parameters have all the
	 *            required data)
	 * @param response
	 *            HttpServletResponse
	 * @return solution model for serialization as JSON
	 */
	@ApiOperation(value = "Creates a new validation sequence record.", response = MLPValidationSequence.class)
	@RequestMapping(value = "/" + CCDSConstants.VAL_SEQ_PATH + "/{sequence}/" + CCDSConstants.VAL_TYPE_PATH
			+ "/{valTypeCode}", method = RequestMethod.POST)
	@ResponseBody
	public Object createValidationSequence(@RequestBody MLPValidationSequence sequence, HttpServletResponse response) {
		logger.debug(EELFLoggerDelegate.debugLogger, "createValidationSequence: received object: {} ", sequence);
		Object result;
		try {
			// Create a new row
			result = validationSequenceRepository.save(sequence);
			response.setStatus(HttpServletResponse.SC_CREATED);
			// This is a hack to create the location path.
			response.setHeader(HttpHeaders.LOCATION, CCDSConstants.VAL_SEQ_PATH);
		} catch (Exception ex) {
			Exception cve = findConstraintViolationException(ex);
			logger.warn(EELFLoggerDelegate.errorLogger, "createValidationSequence", cve.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			result = new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "createValidationSequence failed", cve);
		}
		return result;
	}

	/**
	 * @param sequence
	 *            sequence number
	 * @param valTypeCode
	 *            validation type code
	 * @param response
	 *            HttpServletResponse
	 * @return Transport model with success
	 */
	@ApiOperation(value = "Deletes the specified validation sequence record.", response = SuccessTransport.class)
	@RequestMapping(value = "/" + CCDSConstants.VAL_SEQ_PATH + "/{sequence}/" + CCDSConstants.VAL_TYPE_PATH
			+ "/{valTypeCode}", method = RequestMethod.DELETE)
	@ResponseBody
	public MLPTransportModel deleteValidationSequence(@PathVariable("sequence") Integer sequence,
			@PathVariable("valTypeCode") String valTypeCode, HttpServletResponse response) {
		try {
			// Build a key for fetch
			ValidationSequencePK pk = new ValidationSequencePK(sequence, valTypeCode);
			validationSequenceRepository.delete(pk);
			// Answer "OK"
			return new SuccessTransport(HttpServletResponse.SC_OK, null);
		} catch (Exception ex) {
			// e.g., EmptyResultDataAccessException is NOT an internal server error
			logger.warn(EELFLoggerDelegate.errorLogger, "deleteValidationSequence", ex.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "deleteValidationSequence failed", ex);
		}
	}

}

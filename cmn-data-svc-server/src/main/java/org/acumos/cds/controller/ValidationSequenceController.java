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
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import org.acumos.cds.CCDSConstants;
import org.acumos.cds.CodeNameType;
import org.acumos.cds.domain.MLPValidationSequence;
import org.acumos.cds.domain.MLPValidationSequence.ValidationSequencePK;
import org.acumos.cds.repository.ValidationSequenceRepository;
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
 * Provides getters for all code-name value sets. Fixed value sets are
 * implemented as Java enums, so these controllers are only of interest to
 * clients that don't use the provided Java client.
 */
@Controller
@RequestMapping("/" + CCDSConstants.VAL_SEQ_PATH)
public class ValidationSequenceController extends AbstractController {

	private static final EELFLoggerDelegate logger = EELFLoggerDelegate.getLogger(MethodHandles.lookup().lookupClass());

	@Autowired
	private ValidationSequenceRepository validationSequenceRepository;

	/**
	 * @return List of MLPValidationSequence objects
	 */
	@ApiOperation(value = "Gets the list of validation sequence records.", response = MLPValidationSequence.class, responseContainer = "Iterable")
	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	public Iterable<MLPValidationSequence> getValidationSequenceList() {
		Date beginDate = new Date();
		Iterable<MLPValidationSequence> result = validationSequenceRepository.findAll();
		logger.audit(beginDate, "getValidationSequenceList");
		return result;
	}

	/**
	 * @param sequence
	 *            sequence number
	 * @param valTypeCode
	 *            validation type code
	 * @param valSeq
	 *            Instance to save (redundant; the path parameters have all the
	 *            required data)
	 * @param response
	 *            HttpServletResponse
	 * @return solution model for serialization as JSON
	 */
	@ApiOperation(value = "Creates a new validation sequence record.", response = MLPValidationSequence.class)
	@RequestMapping(value = "/{sequence}/" + CCDSConstants.VAL_TYPE_PATH
			+ "/{valTypeCode}", method = RequestMethod.POST)
	@ResponseBody
	public Object createValidationSequence(@PathVariable("sequence") Integer sequence,
			@PathVariable("valTypeCode") String valTypeCode, @RequestBody MLPValidationSequence valSeq,
			HttpServletResponse response) {
		Date beginDate = new Date();
		try {
			// Validate enum code
			super.validateCode(valTypeCode, CodeNameType.VALIDATION_TYPE);
			// Use path parameters
			valSeq.setSequence(sequence);
			valSeq.setValTypeCode(valTypeCode);
			// Create a new row
			Object result = validationSequenceRepository.save(valSeq);
			response.setStatus(HttpServletResponse.SC_CREATED);
			// This is a hack to create the location path.
			response.setHeader(HttpHeaders.LOCATION, CCDSConstants.VAL_SEQ_PATH);
			logger.audit(beginDate, "createValidationSequence: sequence {} valTypeCode {}", sequence, valTypeCode);
			return result;
		} catch (Exception ex) {
			Exception cve = findConstraintViolationException(ex);
			logger.warn(EELFLoggerDelegate.errorLogger, "createValidationSequence", cve.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "createValidationSequence failed", cve);
		}
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
	@RequestMapping(value = "/{sequence}/" + CCDSConstants.VAL_TYPE_PATH
			+ "/{valTypeCode}", method = RequestMethod.DELETE)
	@ResponseBody
	public MLPTransportModel deleteValidationSequence(@PathVariable("sequence") Integer sequence,
			@PathVariable("valTypeCode") String valTypeCode, HttpServletResponse response) {
		Date beginDate = new Date();
		try {
			// Build a key for fetch
			ValidationSequencePK pk = new ValidationSequencePK(sequence, valTypeCode);
			validationSequenceRepository.delete(pk);
			logger.audit(beginDate, "deleteValidationSequence: sequence {} valTypeCode {}", sequence, valTypeCode);
			return new SuccessTransport(HttpServletResponse.SC_OK, null);
		} catch (Exception ex) {
			// e.g., EmptyResultDataAccessException is NOT an internal server error
			logger.warn(EELFLoggerDelegate.errorLogger, "deleteValidationSequence", ex.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "deleteValidationSequence failed", ex);
		}
	}

}

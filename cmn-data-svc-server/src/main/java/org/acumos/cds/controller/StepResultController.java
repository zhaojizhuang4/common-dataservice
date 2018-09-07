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
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.acumos.cds.CCDSConstants;
import org.acumos.cds.CodeNameType;
import org.acumos.cds.domain.MLPPublishRequest;
import org.acumos.cds.domain.MLPStepResult;
import org.acumos.cds.repository.StepResultRepository;
import org.acumos.cds.service.StepResultSearchService;
import org.acumos.cds.transport.ErrorTransport;
import org.acumos.cds.transport.MLPTransportModel;
import org.acumos.cds.transport.SuccessTransport;
import org.acumos.cds.util.ApiPageable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Controller
@RequestMapping(value = "/" + CCDSConstants.STEP_RESULT_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
public class StepResultController extends AbstractController {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Autowired
	private StepResultRepository stepResultRepository;
	@Autowired
	private StepResultSearchService stepResultSearchService;

	@ApiOperation(value = "Gets a page of step results, optionally sorted on fields.", //
			response = MLPStepResult.class, responseContainer = "Page")
	@ApiPageable
	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	public Page<MLPStepResult> getStepResults(Pageable pageRequest) {
		logger.info("getStepResults {}", pageRequest);
		return stepResultRepository.findAll(pageRequest);
	}

	@ApiOperation(value = "Gets the step result for the specified ID. Returns bad request if the ID is not found.", //
			response = MLPStepResult.class)
	@ApiResponses({ @ApiResponse(code = 400, message = "Bad request", response = ErrorTransport.class) })
	@RequestMapping(value = "/{stepResultId}", method = RequestMethod.GET)
	@ResponseBody
	public Object getStepResult(@PathVariable("stepResultId") Long stepResultId, HttpServletResponse response) {
		logger.info("getStepResult: stepResultId {}", stepResultId);
		MLPStepResult sr = stepResultRepository.findOne(stepResultId);
		if (sr == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + stepResultId, null);
		}
		return sr;
	}

	@ApiOperation(value = "Searches for entities with attribute values matching the field name - field value pairs specified as query parameters. " //
			+ "Defaults to and (conjunction); send junction query parameter '_j=o' for or (disjunction).", //
			response = MLPPublishRequest.class, responseContainer = "Page")
	@ApiPageable
	@ApiResponses({ @ApiResponse(code = 400, message = "Bad request", response = ErrorTransport.class) })
	@RequestMapping(value = "/" + CCDSConstants.SEARCH_PATH, method = RequestMethod.GET)
	@ResponseBody
	public Object searchStepResults(
			// This actually IS required; set flag to false for swagger UI
			@ApiParam(value = "Field name - field value pairs as request parameters in the format name=value, minimum 1; repeats allowed. " //
					+ "Not supported by Swagger web UI.", allowMultiple = true, type = "Array[string]", required = false) //
			@RequestParam MultiValueMap<String, String> queryParameters, HttpServletResponse response,
			Pageable pageRequest) {
		logger.info("searchStepResults {}", queryParameters);
		cleanPageableParameters(queryParameters);
		List<String> junction = queryParameters.remove(CCDSConstants.JUNCTION_QUERY_PARAM);
		boolean isOr = junction != null && junction.size() == 1 && "o".equals(junction.get(0));
		if (queryParameters.size() == 0) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "Missing query", null);
		}
		try {
			Map<String, Object> convertedQryParm = convertQueryParameters(MLPStepResult.class, queryParameters);
			return stepResultSearchService.findStepResults(convertedQryParm, isOr, pageRequest);
		} catch (Exception ex) {
			logger.warn("searchStepResults failed: {}", ex.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST,
					ex.getCause() != null ? ex.getCause().getMessage() : "searchStepResults failed", ex);
		}
	}

	@ApiOperation(value = "Creates a new entity with a generated ID. Returns bad request on constraint violation etc.", //
			response = MLPStepResult.class)
	@ApiResponses({ @ApiResponse(code = 400, message = "Bad request", response = ErrorTransport.class) })
	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public Object createStepResult(@RequestBody MLPStepResult stepResult, HttpServletResponse response) {
		logger.info("createStepResult: enter");
		try {
			// Validate enum codes
			super.validateCode(stepResult.getStepCode(), CodeNameType.STEP_TYPE);
			super.validateCode(stepResult.getStatusCode(), CodeNameType.STEP_STATUS);
			// Force creation of new ID
			stepResult.setStepResultId(null);
			MLPStepResult result = stepResultRepository.save(stepResult);
			response.setStatus(HttpServletResponse.SC_CREATED);
			// This is a hack to create the location path.
			response.setHeader(HttpHeaders.LOCATION, CCDSConstants.STEP_RESULT_PATH + "/" + result.getStepResultId());
			return result;
		} catch (Exception ex) {
			// e.g., EmptyResultDataAccessException is NOT an internal server error
			Exception cve = findConstraintViolationException(ex);
			logger.warn("createStepResult failed: {}", cve.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "createStepResult failed", cve);
		}
	}

	@ApiOperation(value = "Updates an existing entity with the supplied data. Returns bad request on constraint violation etc.", //
			response = SuccessTransport.class)
	@ApiResponses({ @ApiResponse(code = 400, message = "Bad request", response = ErrorTransport.class) })
	@RequestMapping(value = "/{stepResultId}", method = RequestMethod.PUT)
	@ResponseBody
	public Object updateStepResult(@PathVariable("stepResultId") Long stepResultId,
			@RequestBody MLPStepResult stepResult, HttpServletResponse response) {
		logger.info("updateStepResult: stepResultId {}", stepResultId);
		// Get the existing one
		MLPStepResult existing = stepResultRepository.findOne(stepResultId);
		if (existing == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + stepResultId, null);
		}
		try {
			// Validate enum codes
			super.validateCode(stepResult.getStepCode(), CodeNameType.STEP_TYPE);
			super.validateCode(stepResult.getStatusCode(), CodeNameType.STEP_STATUS);
			// Use the path-parameter id; don't trust the one in the object
			stepResult.setStepResultId(stepResultId);
			// Update the existing row
			stepResultRepository.save(stepResult);
			return new SuccessTransport(HttpServletResponse.SC_OK, null);
		} catch (Exception ex) {
			// e.g., EmptyResultDataAccessException is NOT an internal server error
			Exception cve = findConstraintViolationException(ex);
			logger.warn("updateStepResult failed: {}", cve.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "updateStepResult failed", cve);
		}
	}

	@ApiOperation(value = "Deletes the entity with the specified ID. Returns bad request if the ID is not found.", //
			response = SuccessTransport.class)
	@ApiResponses({ @ApiResponse(code = 400, message = "Bad request", response = ErrorTransport.class) })
	@RequestMapping(value = "/{stepResultId}", method = RequestMethod.DELETE)
	@ResponseBody
	public MLPTransportModel deleteStepResult(@PathVariable("stepResultId") Long stepResultId,
			HttpServletResponse response) {
		logger.info("deleteStepResult: stepResultId {}", stepResultId);
		try {
			stepResultRepository.delete(stepResultId);
			return new SuccessTransport(HttpServletResponse.SC_OK, null);
		} catch (Exception ex) {
			// e.g., EmptyResultDataAccessException is NOT an internal server error
			logger.warn("deleteStepResult failed: {}", ex.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "deleteStepResult failed", ex);
		}
	}

}

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
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.acumos.cds.CCDSConstants;
import org.acumos.cds.domain.MLPArtifact;
import org.acumos.cds.domain.MLPSolutionRevision;
import org.acumos.cds.repository.ArtifactRepository;
import org.acumos.cds.repository.SolutionRevisionRepository;
import org.acumos.cds.service.ArtifactSearchService;
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
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.ApiOperation;

/**
 * Answers REST requests to get, search, create, update and delete artifacts.
 */
@Controller
@RequestMapping(value = "/" + CCDSConstants.ARTIFACT_PATH, produces = CCDSConstants.APPLICATION_JSON)
public class ArtifactController extends AbstractController {

	private static final EELFLoggerDelegate logger = EELFLoggerDelegate.getLogger(MethodHandles.lookup().lookupClass());

	@Autowired
	private ArtifactRepository artifactRepository;
	@Autowired
	private ArtifactSearchService artifactService;
	@Autowired
	private SolutionRevisionRepository solutionRevisionRepository;

	/**
	 * @return SuccessTransport object
	 */
	@ApiOperation(value = "Gets the count of artifacts.", response = CountTransport.class)
	@RequestMapping(value = "/" + CCDSConstants.COUNT_PATH, method = RequestMethod.GET)
	@ResponseBody
	public CountTransport getArtifactCount() {
		Date beginDate = new Date();
		Long count = artifactRepository.count();
		logger.audit(beginDate, "getArtifactCount");
		return new CountTransport(count);
	}

	/**
	 * @param pageRequest
	 *            Page and sort criteria
	 * @return List of artifacts, for serialization as JSON
	 */
	@ApiOperation(value = "Gets a page of artifacts, optionally sorted on fields.", response = MLPArtifact.class, responseContainer = "Page")
	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	public Page<MLPArtifact> getArtifacts(Pageable pageRequest) {
		Date beginDate = new Date();
		Page<MLPArtifact> page = artifactRepository.findAll(pageRequest);
		logger.audit(beginDate, "getArtifacts pageRequest {}", pageRequest);
		return page;
	}

	/**
	 * @param term
	 *            Search term used for partial match ("like")
	 * @param pageRequest
	 *            Page and sort criteria
	 * @return List of artifacts
	 */
	@ApiOperation(value = "Searches for artifacts with names or descriptions that contain the search term", response = MLPArtifact.class, responseContainer = "List")
	@RequestMapping(value = "/" + CCDSConstants.LIKE_PATH, method = RequestMethod.GET)
	@ResponseBody
	public Iterable<MLPArtifact> like(@RequestParam(CCDSConstants.TERM_PATH) String term, Pageable pageRequest) {
		Date beginDate = new Date();
		Iterable<MLPArtifact> i = artifactRepository.findBySearchTerm(term, pageRequest);
		logger.audit(beginDate, "like pageRequest {}", pageRequest);
		return i;
	}

	/**
	 * @param queryParameters
	 *            Map of String (field name) to String (value) for restricting the
	 *            query
	 * @param pageRequest
	 *            Page and sort criteria
	 * @param response
	 *            HttpServletResponse
	 * @return Page of artifacts, for serialization as JSON
	 */
	@ApiOperation(value = "Searches for artifacts using the field name - field value pairs specified as query parameters. Defaults to and (conjunction); send junction query parameter = o for or (disjunction).", response = MLPArtifact.class, responseContainer = "Page")
	@RequestMapping(value = "/" + CCDSConstants.SEARCH_PATH, method = RequestMethod.GET)
	@ResponseBody
	public Object searchArtifacts(@RequestParam MultiValueMap<String, String> queryParameters, Pageable pageRequest,
			HttpServletResponse response) {
		Date beginDate = new Date();
		cleanPageableParameters(queryParameters);
		List<String> junction = queryParameters.remove(CCDSConstants.JUNCTION_QUERY_PARAM);
		boolean isOr = junction != null && junction.size() == 1 && "o".equals(junction.get(0));
		if (queryParameters.size() == 0) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "Missing query", null);
		}
		try {
			Map<String, Object> convertedQryParm = convertQueryParameters(MLPArtifact.class, queryParameters);
			Object result = artifactService.findArtifacts(convertedQryParm, isOr, pageRequest);
			logger.audit(beginDate, "searchArtifacts query {}", queryParameters);
			return result;
		} catch (Exception ex) {
			logger.warn(EELFLoggerDelegate.errorLogger, "searchArtifacts", ex.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST,
					ex.getCause() != null ? ex.getCause().getMessage() : "searchArtifacts failed", ex);
		}
	}

	/**
	 * @param artifactId
	 *            Path parameter with row ID
	 * @param response
	 *            HttpServletResponse
	 * @return An artifact if found, an error otherwise.
	 */
	@ApiOperation(value = "Gets the artifact for the specified ID.", response = MLPArtifact.class)
	@RequestMapping(value = "/{artifactId}", method = RequestMethod.GET)
	@ResponseBody
	public Object getArtifact(@PathVariable("artifactId") String artifactId, HttpServletResponse response) {
		Date beginDate = new Date();
		MLPArtifact da = artifactRepository.findOne(artifactId);
		logger.audit(beginDate, "getArtifact ID {}", artifactId);
		if (da == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + artifactId, null);
		}
		return da;
	}

	/**
	 * @param artifactId
	 *            Path parameter with row ID
	 * @param response
	 *            HttpServletResponse
	 * @return A list of revisions, possibly empty
	 */
	@ApiOperation(value = "Gets the solution revisions that use the specified artifact ID.", response = MLPSolutionRevision.class, responseContainer = "List")
	@RequestMapping(value = "/{artifactId}/" + CCDSConstants.REVISION_PATH, method = RequestMethod.GET)
	@ResponseBody
	public Object getRevisionsForArtifact(@PathVariable("artifactId") String artifactId, HttpServletResponse response) {
		Date beginDate = new Date();
		// Validate the artifact ID because an empty result is ambiguous.
		MLPArtifact da = artifactRepository.findOne(artifactId);
		if (da == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + artifactId, null);
		}
		Object result = solutionRevisionRepository.findByArtifactId(artifactId);
		logger.audit(beginDate, "getRevisionsForArtifact ID {}", artifactId);
		return result;
	}

	/**
	 * @param artifact
	 *            artifact to save. If no ID is set a new one will be generated; if
	 *            an ID value is set, it will be used if valid and not in table.
	 * @param response
	 *            HttpServletResponse
	 * @return Entity on success; error on failure.
	 */
	@ApiOperation(value = "Creates a new artifact.", response = MLPArtifact.class)
	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public Object createArtifact(@RequestBody MLPArtifact artifact, HttpServletResponse response) {
		Date beginDate = new Date();
		try {
			String id = artifact.getArtifactId();
			if (id != null) {
				UUID.fromString(id);
				if (artifactRepository.findOne(id) != null) {
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "ID exists: " + id);
				}
			}
			// Create a new row
			Object result = artifactRepository.save(artifact);
			response.setStatus(HttpServletResponse.SC_CREATED);
			// This is a hack to create the location path.
			response.setHeader(HttpHeaders.LOCATION, CCDSConstants.ARTIFACT_PATH + "/" + artifact.getArtifactId());
			logger.audit(beginDate, "createArtifact ID {}", artifact.getArtifactId());
			return result;
		} catch (Exception ex) {
			Exception cve = findConstraintViolationException(ex);
			logger.warn(EELFLoggerDelegate.errorLogger, "createArtifact", cve.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "createArtifact failed", cve);
		}
	}

	/**
	 * @param artifactId
	 *            Path parameter with the row ID
	 * @param artifact
	 *            artifact data to be updated
	 * @param response
	 *            HttpServletResponse
	 * @return Artifact that maps String to Object, for serialization as JSON
	 */
	@ApiOperation(value = "Updates an artifact.", response = SuccessTransport.class)
	@RequestMapping(value = "/{artifactId}", method = RequestMethod.PUT)
	@ResponseBody
	public Object updateArtifact(@PathVariable("artifactId") String artifactId, @RequestBody MLPArtifact artifact,
			HttpServletResponse response) {
		Date beginDate = new Date();
		// Check for existing because the Hibernate save() method doesn't distinguish
		MLPArtifact existing = artifactRepository.findOne(artifactId);
		if (existing == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + artifactId, null);
		}
		try {
			// Use the path-parameter id; don't trust the one in the object
			artifact.setArtifactId(artifactId);
			// Update the existing row
			artifactRepository.save(artifact);
			Object result = new SuccessTransport(HttpServletResponse.SC_OK, null);
			logger.audit(beginDate, "updateArtifact ID {}", artifactId);
			return result;
		} catch (Exception ex) {
			Exception cve = findConstraintViolationException(ex);
			logger.warn(EELFLoggerDelegate.errorLogger, "updateArtifact", cve.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "updateArtifact failed", cve);
		}
	}

	/**
	 * 
	 * @param artifactId
	 *            Path parameter that identifies the instance
	 * @param response
	 *            HttpServletResponse
	 * @return Artifact that maps String to Object, for serialization as JSON
	 */
	@ApiOperation(value = "Deletes the artifact with the specified ID.", response = SuccessTransport.class)
	@RequestMapping(value = "/{artifactId}", method = RequestMethod.DELETE)
	@ResponseBody
	public MLPTransportModel deleteArtifact(@PathVariable("artifactId") String artifactId,
			HttpServletResponse response) {
		Date beginDate = new Date();
		try {
			artifactRepository.delete(artifactId);
			logger.audit(beginDate, "deleteArtifact ID {}", artifactId);
			return new SuccessTransport(HttpServletResponse.SC_OK, null);
		} catch (Exception ex) {
			// e.g., EmptyResultDataAccessException is NOT an internal server error
			logger.warn(EELFLoggerDelegate.errorLogger, "deleteArtifact", ex.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "deleteArtifact failed", ex);
		}
	}

}

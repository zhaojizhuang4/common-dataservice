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
import org.acumos.cds.domain.MLPArtifact;
import org.acumos.cds.domain.MLPRevisionDescription;
import org.acumos.cds.domain.MLPSolRevArtMap;
import org.acumos.cds.repository.ArtifactRepository;
import org.acumos.cds.repository.RevisionDescriptionRepository;
import org.acumos.cds.repository.SolRevArtMapRepository;
import org.acumos.cds.repository.SolutionRevisionRepository;
import org.acumos.cds.transport.ErrorTransport;
import org.acumos.cds.transport.SuccessTransport;
import org.acumos.cds.util.EELFLoggerDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.ApiOperation;

/**
 * A revision is a collection of artifacts. A revision cannot exist without a
 * solution, but an artifact can exist without a revision.
 */
@Controller
@RequestMapping("/" + CCDSConstants.REVISION_PATH)
public class RevisionController extends AbstractController {

	private static final EELFLoggerDelegate logger = EELFLoggerDelegate.getLogger(MethodHandles.lookup().lookupClass());

	@Autowired
	private ArtifactRepository artifactRepository;
	@Autowired
	private SolutionRevisionRepository revisionRepository;
	@Autowired
	private RevisionDescriptionRepository revisionDescRepository;
	@Autowired
	private SolRevArtMapRepository solRevArtMapRepository;

	/**
	 * @param revisionId
	 *            revision ID
	 * @param response
	 *            HttpServletResponse
	 * @return Success indicator
	 */
	@ApiOperation(value = "Gets the artifacts for the revision.", response = MLPArtifact.class, responseContainer = "List")
	@RequestMapping(value = "/{revisionId}/" + CCDSConstants.ARTIFACT_PATH, method = RequestMethod.GET)
	@ResponseBody
	public Iterable<MLPArtifact> getRevisionArtifacts(@PathVariable("revisionId") String revisionId,
			HttpServletResponse response) {
		Date beginDate = new Date();
		Iterable<MLPArtifact> result = artifactRepository.findByRevision(revisionId);
		logger.audit(beginDate, "getSolRevArtifacts: revisionId {}", revisionId);
		return result;
	}

	/**
	 * @param revisionId
	 *            revision ID
	 * @param artifactId
	 *            artifact ID
	 * @param response
	 *            HttpServletResponse
	 * @return Success indicator
	 */
	@ApiOperation(value = "Adds an artifact to the revision.", response = SuccessTransport.class)
	@RequestMapping(value = "/{revisionId}/" + CCDSConstants.ARTIFACT_PATH
			+ "/{artifactId}", method = RequestMethod.POST)
	@ResponseBody
	public SuccessTransport addRevisionArtifact(@PathVariable("revisionId") String revisionId,
			@PathVariable("artifactId") String artifactId, HttpServletResponse response) {
		Date beginDate = new Date();
		MLPSolRevArtMap map = new MLPSolRevArtMap(revisionId, artifactId);
		solRevArtMapRepository.save(map);
		logger.audit(beginDate, "addRevArtifact: revisionId {} artifactId {}", revisionId, artifactId);
		return new SuccessTransport(HttpServletResponse.SC_OK, null);
	}

	/**
	 * @param revisionId
	 *            revision ID
	 * @param artifactId
	 *            artifact ID
	 * @param response
	 *            HttpServletResponse
	 * @return Success indicator
	 */
	@ApiOperation(value = "Removes an artifact from the revision.", response = SuccessTransport.class)
	@RequestMapping(value = "/{revisionId}/" + CCDSConstants.ARTIFACT_PATH
			+ "/{artifactId}", method = RequestMethod.DELETE)
	@ResponseBody
	public SuccessTransport dropRevisionArtifact(@PathVariable("revisionId") String revisionId,
			@PathVariable("artifactId") String artifactId, HttpServletResponse response) {
		Date beginDate = new Date();
		solRevArtMapRepository.delete(new MLPSolRevArtMap.SolRevArtMapPK(revisionId, artifactId));
		logger.audit(beginDate, "dropRevArtifact: revisionId {} artifactId {}", revisionId, artifactId);
		return new SuccessTransport(HttpServletResponse.SC_OK, null);
	}

	/**
	 * @param revisionId
	 *            Path parameter with revision ID
	 * @param accessTypeCode
	 *            Path parameter with access type code
	 * @param response
	 *            HttpServletResponse
	 * @return A description if found, an error otherwise.
	 */
	@ApiOperation(value = "Gets the revision description for the specified access type.", response = MLPRevisionDescription.class)
	@RequestMapping(value = "/{revisionId}/" + CCDSConstants.ACCESS_PATH + "/{accessTypeCode}/"
			+ CCDSConstants.DESCRIPTION_PATH, method = RequestMethod.GET)
	@ResponseBody
	public Object getRevisionDescription(@PathVariable("revisionId") String revisionId,
			@PathVariable("accessTypeCode") String accessTypeCode, HttpServletResponse response) {
		Date beginDate = new Date();
		MLPRevisionDescription da = revisionDescRepository
				.findOne(new MLPRevisionDescription.RevDescPK(revisionId, accessTypeCode));
		if (da == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST,
					NO_ENTRY_WITH_ID + revisionId + "/" + accessTypeCode, null);
		}
		logger.audit(beginDate, "getRevisionDescription: revisionId {} accessTypeCode ", revisionId, accessTypeCode);
		return da;
	}

	/**
	 * @param revisionId
	 *            Path parameter with revision ID
	 * @param accessTypeCode
	 *            Path parameter with access type code
	 * @param description
	 *            description to create
	 * @param response
	 *            HttpServletResponse
	 * @return Description model for serialization as JSON
	 */
	@ApiOperation(value = "Creates a new description for the specified revision and access type.", response = MLPRevisionDescription.class)
	@RequestMapping(value = "/{revisionId}/" + CCDSConstants.ACCESS_PATH + "/{accessTypeCode}/"
			+ CCDSConstants.DESCRIPTION_PATH, method = RequestMethod.POST)
	@ResponseBody
	public Object createRevisionDescription(@PathVariable("revisionId") String revisionId,
			@PathVariable("accessTypeCode") String accessTypeCode, @RequestBody MLPRevisionDescription description,
			HttpServletResponse response) {
		Date beginDate = new Date();
		if (revisionRepository.findOne(revisionId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + revisionId, null);
		}
		try {
			// Validate enum codes
			super.validateCode(description.getAccessTypeCode(), CodeNameType.ACCESS_TYPE);
			// Use the validated values
			description.setRevisionId(revisionId);
			description.setAccessTypeCode(accessTypeCode);
			// Create a new row
			MLPRevisionDescription result = revisionDescRepository.save(description);
			logger.audit(beginDate, "createRevisionDescription: revisionId {} accessTypeCode {}", revisionId,
					accessTypeCode);
			return result;
		} catch (Exception ex) {
			Exception cve = findConstraintViolationException(ex);
			logger.warn(EELFLoggerDelegate.errorLogger, "createRevisionDescription failed: {}", cve.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "createRevisionDescription failed", cve);
		}
	}

	/**
	 * @param revisionId
	 *            Path parameter with revision ID
	 * @param accessTypeCode
	 *            Path parameter with access type code
	 * @param description
	 *            description to update
	 * @param response
	 *            HttpServletResponse
	 * @return Status message
	 */
	@ApiOperation(value = "Updates an existing description for the specified revision and access type.", response = SuccessTransport.class)
	@RequestMapping(value = "/{revisionId}/" + CCDSConstants.ACCESS_PATH + "/{accessTypeCode}/"
			+ CCDSConstants.DESCRIPTION_PATH, method = RequestMethod.PUT)
	@ResponseBody
	public Object updateRevisionDescription(@PathVariable("revisionId") String revisionId,
			@PathVariable("accessTypeCode") String accessTypeCode, @RequestBody MLPRevisionDescription description,
			HttpServletResponse response) {
		Date beginDate = new Date();
		// Get the existing one
		MLPRevisionDescription existing = revisionDescRepository
				.findOne(new MLPRevisionDescription.RevDescPK(revisionId, accessTypeCode));
		if (existing == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST,
					NO_ENTRY_WITH_ID + revisionId + "/" + accessTypeCode, null);
		}
		try {
			super.validateCode(description.getAccessTypeCode(), CodeNameType.ACCESS_TYPE);
			// Use the validated values
			description.setRevisionId(revisionId);
			description.setAccessTypeCode(accessTypeCode);
			revisionDescRepository.save(description);
			logger.audit(beginDate, "updateRevisionDescription: revisionId {} accessTypeCode {}", revisionId,
					accessTypeCode);
			return new SuccessTransport(HttpServletResponse.SC_OK, null);
		} catch (Exception ex) {
			Exception cve = findConstraintViolationException(ex);
			logger.warn(EELFLoggerDelegate.errorLogger, "updateRevisionDescription failed: {}", cve.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "updateRevisionDescription failed", cve);
		}
	}

	/**
	 * @param revisionId
	 *            Path parameter with revision ID
	 * @param accessTypeCode
	 *            Path parameter with access type code
	 * @param response
	 *            HttpServletResponse
	 * @return Status message
	 */
	@ApiOperation(value = "Deletes the description for the specified revision and access type.", response = SuccessTransport.class)
	@RequestMapping(value = "/{revisionId}/" + CCDSConstants.ACCESS_PATH + "/{accessTypeCode}/"
			+ CCDSConstants.DESCRIPTION_PATH, method = RequestMethod.DELETE)
	@ResponseBody
	public Object deleteRevisionDescription(@PathVariable("revisionId") String revisionId,
			@PathVariable("accessTypeCode") String accessTypeCode, HttpServletResponse response) {
		Date beginDate = new Date();
		try {
			revisionDescRepository.delete(new MLPRevisionDescription.RevDescPK(revisionId, accessTypeCode));
			logger.audit(beginDate, "deleteRevisionDescription: revisionId {} accessTypeCode {}", revisionId,
					accessTypeCode);
			return new SuccessTransport(HttpServletResponse.SC_OK, null);
		} catch (Exception ex) {
			// e.g., EmptyResultDataAccessException is NOT an internal server error
			logger.warn(EELFLoggerDelegate.errorLogger, "deleteRevisionDescription failed: {}", ex.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "deleteRevisionDescription failed", ex);
		}
	}

}
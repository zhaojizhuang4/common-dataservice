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

import java.util.Date;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.acumos.cds.CCDSConstants;
import org.acumos.cds.domain.MLPArtifact;
import org.acumos.cds.domain.MLPSolRevArtMap;
import org.acumos.cds.domain.MLPSolTagMap;
import org.acumos.cds.domain.MLPSolUserAccMap;
import org.acumos.cds.domain.MLPSolution;
import org.acumos.cds.domain.MLPSolutionDeployment;
import org.acumos.cds.domain.MLPSolutionDownload;
import org.acumos.cds.domain.MLPSolutionRating;
import org.acumos.cds.domain.MLPSolutionRating.SolutionRatingPK;
import org.acumos.cds.domain.MLPSolutionRevision;
import org.acumos.cds.domain.MLPSolutionValidation;
import org.acumos.cds.domain.MLPSolutionValidation.SolutionValidationPK;
import org.acumos.cds.domain.MLPSolutionWeb;
import org.acumos.cds.domain.MLPTag;
import org.acumos.cds.domain.MLPUser;
import org.acumos.cds.repository.ArtifactRepository;
import org.acumos.cds.repository.SolRevArtMapRepository;
import org.acumos.cds.repository.SolTagMapRepository;
import org.acumos.cds.repository.SolUserAccMapRepository;
import org.acumos.cds.repository.SolutionDeploymentRepository;
import org.acumos.cds.repository.SolutionDownloadRepository;
import org.acumos.cds.repository.SolutionFavoriteRepository;
import org.acumos.cds.repository.SolutionRatingRepository;
import org.acumos.cds.repository.SolutionRepository;
import org.acumos.cds.repository.SolutionRevisionRepository;
import org.acumos.cds.repository.SolutionValidationRepository;
import org.acumos.cds.repository.SolutionWebRepository;
import org.acumos.cds.repository.TagRepository;
import org.acumos.cds.repository.UserRepository;
import org.acumos.cds.service.SolutionSearchService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.ApiOperation;

/**
 * A solution is a collection of revisions. A revision points to a collection of
 * artifacts. A solution revision cannot exist without a solution, but an
 * artifact can exist without a revision.
 */
@Controller
@RequestMapping("/" + CCDSConstants.SOLUTION_PATH)
public class SolutionController extends AbstractController {

	private static final EELFLoggerDelegate logger = EELFLoggerDelegate.getLogger(SolutionController.class);

	@Autowired
	private ArtifactRepository artifactRepository;
	@Autowired
	private SolRevArtMapRepository solRevArtMapRepository;
	@Autowired
	private SolTagMapRepository solTagMapRepository;
	@Autowired
	private SolUserAccMapRepository solUserAccMapRepository;
	@Autowired
	private SolutionDeploymentRepository solutionDeploymentRepository;
	@Autowired
	private SolutionDownloadRepository solutionDownloadRepository;
	@Autowired
	private SolutionFavoriteRepository solutionFavoriteRepository;
	@Autowired
	private SolutionRatingRepository solutionRatingRepository;
	@Autowired
	private SolutionRepository solutionRepository;
	@Autowired
	private SolutionRevisionRepository solutionRevisionRepository;
	@Autowired
	private SolutionSearchService solutionSearchService;
	@Autowired
	private TagRepository solutionTagRepository;
	@Autowired
	private SolutionValidationRepository solutionValidationRepository;
	@Autowired
	private SolutionWebRepository solutionWebRepository;
	@Autowired
	private UserRepository userRepository;

	/**
	 * Updates the cached value(s) for solution downloads.
	 * 
	 * Is extra concurrency control required here? A spring controller is a
	 * singleton, but what about threads?
	 * 
	 * @param solutionId
	 *            Solution ID
	 */
	private void updateSolutionDownloadStats(String solutionId) {
		Long count = solutionDownloadRepository.getSolutionDownloadCount(solutionId);
		if (count != null) {
			MLPSolutionWeb stats = solutionWebRepository.findOne(solutionId);
			stats.setDownloadCount(count);
			stats.setLastDownload(new Date());
			solutionWebRepository.save(stats);
		}
	}

	/**
	 * Updates the cached value(s) for solution ratings.
	 * 
	 * Is extra concurrency control required here? A spring controller is a
	 * singleton, but what about threads?
	 * 
	 * @param solutionId
	 *            Solution ID
	 */
	private void updateSolutionRatingStats(String solutionId) {
		Long count = solutionRatingRepository.getSolutionRatingCount(solutionId);
		Double avg = solutionRatingRepository.getSolutionRatingAverage(solutionId);
		if (count != null && avg != null) {
			MLPSolutionWeb stats = solutionWebRepository.findOne(solutionId);
			stats.setRatingCount(count);
			stats.setRatingAverageTenths(Math.round(10 * avg));
			solutionWebRepository.save(stats);
		}
	}

	/**
	 * @param response
	 *            HttpServletResponse
	 * @return SuccessTransport object
	 */
	@ApiOperation(value = "Gets the count of solutions.", response = CountTransport.class)
	@RequestMapping(value = CCDSConstants.COUNT_PATH, method = RequestMethod.GET)
	@ResponseBody
	public CountTransport getSolutionCount(HttpServletResponse response) {
		Long count = solutionRepository.count();
		return new CountTransport(count);
	}

	/**
	 * @param pageable
	 *            Sort and page criteria
	 * @param response
	 *            HttpServletResponse
	 * @return List of artifacts, for serialization as JSON
	 */
	@ApiOperation(value = "Gets a page of solutions, optionally sorted on fields.", response = MLPSolution.class, responseContainer = "Page")
	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	public Page<MLPSolution> getPageOfSolutions(Pageable pageable, HttpServletResponse response) {
		return solutionRepository.findAll(pageable);
	}

	/**
	 * @param term
	 *            Search term used for partial match ("like")
	 * @param pageRequest
	 *            Page and sort criteria
	 * @param response
	 *            HttpServletResponse
	 * @return List of solutions
	 */
	@ApiOperation(value = "Searches for solutions with names or descriptions that contain the search term", response = MLPSolution.class, responseContainer = "Page")
	@RequestMapping(value = "/" + CCDSConstants.SEARCH_PATH + "/" + CCDSConstants.LIKE_PATH, method = RequestMethod.GET)
	@ResponseBody
	public Page<MLPSolution> findByLikeKeyword(@RequestParam(CCDSConstants.TERM_PATH) String term, Pageable pageRequest,
			HttpServletResponse response) {
		return solutionRepository.findBySearchTerm(term, pageRequest);
	}

	/**
	 * 
	 * @param tag
	 *            Tag string to find
	 * @param pageRequest
	 *            Page and sort criteria
	 * @param response
	 *            HttpServletResponse
	 * @return List of solutions
	 */
	@ApiOperation(value = "Gets a page of solutions matching the specified tag.", response = MLPSolution.class, responseContainer = "Page")
	@RequestMapping(value = "/" + CCDSConstants.SEARCH_PATH + "/" + CCDSConstants.TAG_PATH, method = RequestMethod.GET)
	@ResponseBody
	public Object findByTag(@RequestParam("tag") String tag, Pageable pageRequest, HttpServletResponse response) {
		MLPTag existing = solutionTagRepository.findOne(tag);
		if (existing == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "Failed to find tag " + tag, null);
		}
		return solutionRepository.findByTag(tag, pageRequest);
	}

	/**
	 * Fetches the value, splits on comma, and converts the four-letter sequence
	 * "null" to the null value.
	 * 
	 * @param parmName
	 *            Map key
	 * @param queryParameters
	 *            Map of parameters
	 * @return String array; null if key is not present
	 */
	private String[] getOptCsvParameter(String parmName, Map<String, String> queryParameters) {
		String val = queryParameters.get(parmName);
		if (val == null)
			return null;
		String[] vals = val.split(",");
		for (int i = 0; i < vals.length; ++i)
			if ("null".equals(vals[i]))
				vals[i] = null;
		return vals;
	}

	/**
	 * @param queryParameters
	 *            Map of String (field name) to String (value) for restricting the
	 *            query
	 * @param response
	 *            HttpServletResponse
	 * @return List of solutions
	 */
	@ApiOperation(value = "Gets a list of solutions, optionally restricted by field name - field value pairs specified as query parameters.", response = MLPSolution.class, responseContainer = "List")
	@RequestMapping(value = "/" + CCDSConstants.SEARCH_PATH, method = RequestMethod.GET)
	@ResponseBody
	public Object searchSolutions(@RequestParam Map<String, String> queryParameters, HttpServletResponse response) {
		try {
			Map<String, Object> convertedQryParm = null;
			boolean isOr = false;
			if (queryParameters != null && queryParameters.size() > 0) {
				String junction = queryParameters.remove(CCDSConstants.JUNCTION_QUERY_PARAM);
				isOr = junction != null && "o".equals(junction);
				convertedQryParm = convertQueryParameters(MLPSolution.class, queryParameters);
			}
			return solutionSearchService.getSolutions(convertedQryParm, isOr);
		} catch (Exception ex) {
			logger.warn(EELFLoggerDelegate.errorLogger, "searchSolutions failed", ex);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST,
					ex.getCause() != null ? ex.getCause().getMessage() : "searchSolutions failed", ex);
		}
	}

	/**
	 * Dynamic query.
	 * 
	 * @param queryParameters
	 *            Field names-value pairs, see below for names. Some values can be
	 *            comma-separated lists.
	 * @param pageRequest
	 *            Page and sort criteria
	 * @param response
	 *            HttpServletResponse
	 * @return Page of solutions
	 */
	@ApiOperation(value = "Gets a page of solutions for populating Portal screens.", response = MLPSolution.class, responseContainer = "Page")
	@RequestMapping(value = "/" + CCDSConstants.SEARCH_PATH + "/"
			+ CCDSConstants.PORTAL_PATH, method = RequestMethod.GET)
	@ResponseBody
	public Object findPortalSolutions(@RequestParam Map<String, String> queryParameters, Pageable pageRequest,
			HttpServletResponse response) {
		try {
			// This parameter is required
			Boolean active = new Boolean(queryParameters.get(CCDSConstants.SEARCH_ACTIVE));
			// All remaining parameters are optional
			String[] nameKws = getOptCsvParameter(CCDSConstants.SEARCH_NAME, queryParameters);
			String[] descKws = getOptCsvParameter(CCDSConstants.SEARCH_DESC, queryParameters);
			String[] ownerIds = getOptCsvParameter(CCDSConstants.SEARCH_OWNERS, queryParameters);
			String[] accessTypeCodes = getOptCsvParameter(CCDSConstants.SEARCH_ACCESS_TYPES, queryParameters);
			String[] modelTypeCodes = getOptCsvParameter(CCDSConstants.SEARCH_MODEL_TYPES, queryParameters);
			String[] valStatusCodes = getOptCsvParameter(CCDSConstants.SEARCH_VAL_STATUSES, queryParameters);
			String[] tags = getOptCsvParameter(CCDSConstants.SEARCH_TAGS, queryParameters);
			return solutionSearchService.findPortalSolutions(nameKws, descKws, active, ownerIds, accessTypeCodes,
					modelTypeCodes, valStatusCodes, tags, pageRequest);
		} catch (Exception ex) {
			logger.warn(EELFLoggerDelegate.errorLogger, "findPortalSolutions failed", ex);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST,
					ex.getCause() != null ? ex.getCause().getMessage() : "findPortalSolutions failed", ex);
		}
	}

	/**
	 * @param solutionId
	 *            Path parameter with row ID
	 * @param response
	 *            HttpServletResponse
	 * @return A solution if found, an error otherwise.
	 */
	@ApiOperation(value = "Gets the solution for the specified ID.", response = MLPSolution.class)
	@RequestMapping(value = "/{solutionId}", method = RequestMethod.GET)
	@ResponseBody
	public Object getSolution(@PathVariable("solutionId") String solutionId, HttpServletResponse response) {
		MLPSolution da = solutionRepository.findOne(solutionId);
		if (da == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "No entry for row ID " + solutionId, null);
		}
		return da;
	}

	/**
	 * @param solution
	 *            Solution to save. If no ID is set a new one will be generated; if
	 *            an ID value is set, it will be used if valid and not in table.
	 * @param response
	 *            HttpServletResponse
	 * @return solution model for serialization as JSON
	 */
	@ApiOperation(value = "Creates a new solution.", response = MLPSolution.class)
	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public Object createSolution(@RequestBody MLPSolution solution, HttpServletResponse response) {
		logger.debug(EELFLoggerDelegate.debugLogger, "createSolution: received object: {} ", solution);
		Object result;
		try {
			String id = solution.getSolutionId();
			if (id != null) {
				UUID.fromString(id);
				if (solutionRepository.findOne(id) != null) {
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					result = new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "ID exists: " + id);
					return result;
				}
			}
			// Ensure web stat object is empty
			solution.setWebStats(null);
			// Create a new row
			// ALSO send back the model for client convenience
			MLPSolution persisted = solutionRepository.save(solution);
			// Cascade manually - create an empty web stats entry.
			solutionWebRepository.save(new MLPSolutionWeb(persisted.getSolutionId()));
			// This is a hack to create the location path.
			response.setStatus(HttpServletResponse.SC_CREATED);
			response.setHeader(HttpHeaders.LOCATION, CCDSConstants.SOLUTION_PATH + "/" + solution.getSolutionId());
			result = persisted;
		} catch (Exception ex) {
			Exception cve = findConstraintViolationException(ex);
			logger.warn(EELFLoggerDelegate.errorLogger, "createSolution", cve.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			result = new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "createSolution failed", cve);
		}
		return result;
	}

	/**
	 * @param solutionId
	 *            Path parameter with the row ID
	 * @param solution
	 *            solution data to be updated
	 * @param response
	 *            HttpServletResponse
	 * @return Status message
	 */
	@ApiOperation(value = "Updates a solution.", response = SuccessTransport.class)
	@RequestMapping(value = "/{solutionId}", method = RequestMethod.PUT)
	@ResponseBody
	public Object updateSolution(@PathVariable("solutionId") String solutionId, @RequestBody MLPSolution solution,
			HttpServletResponse response) {
		logger.debug(EELFLoggerDelegate.debugLogger, "putSolution: received {} ", solution);
		// Get the existing one
		MLPSolution existing = solutionRepository.findOne(solutionId);
		if (existing == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "Failed to find object with id " + solutionId,
					null);
		}
		MLPTransportModel result = null;
		try {
			// Use the path-parameter id; don't trust the one in the object
			solution.setSolutionId(solutionId);
			// Discard any stats object; updates don't happen via this interface
			// solution.setWebStats(null);
			// Update the existing row
			solutionRepository.save(solution);
			// Answer "OK"
			result = new SuccessTransport(HttpServletResponse.SC_OK, null);
		} catch (Exception ex) {
			Exception cve = findConstraintViolationException(ex);
			logger.warn(EELFLoggerDelegate.errorLogger, "updateSolution", cve.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			result = new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "updateSolution failed", cve);
		}
		return result;
	}

	/**
	 * Special case of update that increments a solution view count.
	 * 
	 * @param solutionId
	 *            Path parameter with the row ID
	 * @param response
	 *            HttpServletResponse
	 * @return Status message
	 */
	@ApiOperation(value = "Increments view count of the specified solution.", response = SuccessTransport.class)
	@RequestMapping(value = "/{solutionId}/" + CCDSConstants.VIEW_PATH, method = RequestMethod.PUT)
	@ResponseBody
	public Object incrementViewCount(@PathVariable("solutionId") String solutionId, HttpServletResponse response) {
		logger.debug(EELFLoggerDelegate.debugLogger, "incrementViewCount: id {} ", solutionId);
		// Get the existing one
		MLPSolutionWeb existing = solutionWebRepository.findOne(solutionId);
		if (existing == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "No solution with id " + solutionId, null);
		}
		MLPTransportModel result = null;
		try {
			// Have the database do the increment to avoid race conditions
			solutionWebRepository.incrementViewCount(solutionId);
			result = new SuccessTransport(HttpServletResponse.SC_OK, null);
		} catch (Exception ex) {
			logger.error(EELFLoggerDelegate.errorLogger, "incrementViewCount failed", ex);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			result = new ErrorTransport(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "incrementViewCount failed", ex);
		}
		return result;
	}

	/**
	 * Originally this was declared void and accordingly returned nothing. But when
	 * used in SpringBoot, after invoking the method it would look for a ThymeLeaf
	 * template, fail to find it, then throw internal server error.
	 * 
	 * @param solutionId
	 *            Path parameter that identifies the instance
	 * @param response
	 *            HttpServletResponse
	 * @return Solution that maps String to Object, for serialization as JSON
	 */
	@ApiOperation(value = "Deletes a solution.", response = SuccessTransport.class)
	@RequestMapping(value = "/{solutionId}", method = RequestMethod.DELETE)
	@ResponseBody
	public MLPTransportModel deleteSolution(@PathVariable("solutionId") String solutionId,
			HttpServletResponse response) {
		MLPSolution existing = solutionRepository.findOne(solutionId);
		if (existing == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "No solution with id " + solutionId, null);
		}
		try {
			// Manually cascade the delete
			// what about composite solutions?
			solutionDeploymentRepository.deleteDeploymentsForSolution(solutionId);
			solTagMapRepository.deleteTagsForSolution(solutionId);
			solutionDownloadRepository.deleteBySolutionId(solutionId);
			solutionRatingRepository.deleteBySolutionId(solutionId);
			solutionValidationRepository.deleteBySolutionId(solutionId);
			solUserAccMapRepository.deleteUsersForSolution(solutionId);
			solutionFavoriteRepository.deleteBySolutionId(solutionId);
			existing.setWebStats(null);
			// The web stats are annotated as optional, so be cautious when deleting
			MLPSolutionWeb webStats = solutionWebRepository.findOne(solutionId);
			if (webStats != null)
				solutionWebRepository.delete(solutionId);
			for (MLPSolutionRevision r : solutionRevisionRepository.findBySolution(new String[] { solutionId })) {
				for (MLPArtifact a : artifactRepository.findByRevision(r.getRevisionId()))
					solRevArtMapRepository
							.delete(new MLPSolRevArtMap.SolRevArtMapPK(r.getRevisionId(), a.getArtifactId()));
				// do NOT delete artifacts!
				solutionRevisionRepository.delete(r);
			}
			solutionRepository.delete(solutionId);
			return new SuccessTransport(HttpServletResponse.SC_OK, null);
		} catch (Exception ex) {
			// e.g., EmptyResultDataAccessException is NOT an internal server error
			logger.warn(EELFLoggerDelegate.errorLogger, "deleteSolution failed", ex.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "deleteSolution failed", ex);
		}
	}

	/**
	 * @param solutionId
	 *            Array of solution IDs (comma-separated values - the name should be
	 *            plural but it's declared above). Spring will split the list if the
	 *            path variable is declared as String array or List of String.
	 * @return List of solutions
	 */
	@ApiOperation(value = "Gets a list of revisions for the specified solution IDs.", response = MLPSolutionRevision.class, responseContainer = "List")
	@RequestMapping(value = "/{solutionId}/" + CCDSConstants.REVISION_PATH, method = RequestMethod.GET)
	@ResponseBody
	public Iterable<MLPSolutionRevision> getListOfRevisions(@PathVariable("solutionId") String[] solutionId) {
		return solutionRevisionRepository.findBySolution(solutionId);
	}

	/**
	 * @param solutionId
	 *            Path parameter with solution ID; ignored
	 * @param revisionId
	 *            Path parameter with revision ID
	 * @param response
	 *            HttpServletResponse
	 * @return A solution if found, an error otherwise.
	 */
	@ApiOperation(value = "Gets the revision for the specified ID.", response = MLPSolution.class)
	@RequestMapping(value = "/{solutionId}/" + CCDSConstants.REVISION_PATH
			+ "/{revisionId}", method = RequestMethod.GET)
	@ResponseBody
	public Object getSolutionRevision(@PathVariable("solutionId") String solutionId,
			@PathVariable("revisionId") String revisionId, HttpServletResponse response) {
		MLPSolutionRevision da = solutionRevisionRepository.findOne(revisionId);
		if (da == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "No entry for ID " + revisionId, null);
		}
		return da;
	}

	/**
	 * @param solutionId
	 *            solution ID
	 * @param revision
	 *            Revision to save. If no ID is set a new one will be generated; if
	 *            an ID value is set, it will be used if valid and not in table.
	 * @param response
	 *            HttpServletResponse
	 * @return Solution revision model for serialization as JSON
	 */
	@ApiOperation(value = "Creates a new solution revision.", response = MLPSolutionRevision.class)
	@RequestMapping(value = "/{solutionId}/" + CCDSConstants.REVISION_PATH, method = RequestMethod.POST)
	@ResponseBody
	public Object createSolutionRevision(@PathVariable("solutionId") String solutionId,
			@RequestBody MLPSolutionRevision revision, HttpServletResponse response) {
		logger.debug(EELFLoggerDelegate.debugLogger, "create: solution ID {}", solutionId);
		if (solutionRepository.findOne(solutionId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "No solution for ID " + solutionId, null);
		}
		Object result;
		try {
			String id = revision.getRevisionId();
			if (id != null) {
				UUID.fromString(id);
				if (solutionRevisionRepository.findOne(id) != null) {
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					result = new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "ID exists: " + id);
					return result;
				}
			}
			// Add the solution, which the client cannot provide
			revision.setSolutionId(solutionId);
			// Create a new row
			result = solutionRevisionRepository.save(revision);
		} catch (Exception ex) {
			Exception cve = findConstraintViolationException(ex);
			logger.warn(EELFLoggerDelegate.errorLogger, "createSolutionRevision", cve.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			result = new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "createSolutionRevision failed", cve);
		}
		return result;
	}

	/**
	 * @param solutionId
	 *            solution ID
	 * @param revisionId
	 *            revision ID
	 * @param revision
	 *            revision to update
	 * @param response
	 *            HttpServletResponse
	 * @return Status message
	 */
	@ApiOperation(value = "Updates an existing solution revision.", response = SuccessTransport.class)
	@RequestMapping(value = "/{solutionId}/" + CCDSConstants.REVISION_PATH
			+ "/{revisionId}", method = RequestMethod.PUT)
	@ResponseBody
	public Object updateSolutionRevision(@PathVariable("solutionId") String solutionId,
			@PathVariable("revisionId") String revisionId, @RequestBody MLPSolutionRevision revision,
			HttpServletResponse response) {
		logger.debug(EELFLoggerDelegate.debugLogger, "updateSolutionRevision: solution ID {}, revision ID {}",
				solutionId, revisionId);
		if (solutionRepository.findOne(solutionId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "No solution for ID " + solutionId, null);
		}
		if (solutionRevisionRepository.findOne(revisionId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "No revision for ID " + revisionId, null);
		}
		Object result;
		try {
			// Use the validated values
			revision.setRevisionId(revisionId);
			revision.setSolutionId(solutionId);
			// Update
			solutionRevisionRepository.save(revision);
			// Answer "OK"
			result = new SuccessTransport(HttpServletResponse.SC_OK, null);
		} catch (Exception ex) {
			Exception cve = findConstraintViolationException(ex);
			logger.warn(EELFLoggerDelegate.errorLogger, "updateSolutionRevision", cve.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			result = new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "updateSolutionRevision failed", cve);
		}
		return result;
	}

	/**
	 * @param solutionId
	 *            solution ID
	 * @param revisionId
	 *            revision ID
	 * @param response
	 *            HttpServletResponse
	 * @return Status message
	 */
	@ApiOperation(value = "Deletes a solution revision.", response = SuccessTransport.class)
	@RequestMapping(value = "/{solutionId}/" + CCDSConstants.REVISION_PATH
			+ "/{revisionId}", method = RequestMethod.DELETE)
	@ResponseBody
	public Object deleteSolutionRevision(@PathVariable("solutionId") String solutionId,
			@PathVariable("revisionId") String revisionId, HttpServletResponse response) {
		logger.debug(EELFLoggerDelegate.debugLogger, "delete: solution ID {}, revision ID {}", solutionId, revisionId);
		try {
			solutionRevisionRepository.delete(revisionId);
			// Answer "OK"
			return new SuccessTransport(HttpServletResponse.SC_OK, null);
		} catch (Exception ex) {
			// e.g., EmptyResultDataAccessException is NOT an internal server error
			logger.warn(EELFLoggerDelegate.errorLogger, "deleteSolutionRevision failed", ex.getMessage());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "deleteRevision failed", ex);
		}
	}

	/**
	 * @param solutionId
	 *            solution ID
	 * @param revisionId
	 *            revision ID
	 * @param response
	 *            HttpServletResponse
	 * @return Success indicator
	 */
	@ApiOperation(value = "Gets the artifacts for the solution revision.", response = MLPArtifact.class, responseContainer = "List")
	@RequestMapping(value = "/{solutionId}/" + CCDSConstants.REVISION_PATH + "/{revisionId}/"
			+ CCDSConstants.ARTIFACT_PATH, method = RequestMethod.GET)
	@ResponseBody
	public Iterable<MLPArtifact> getSolRevArtifacts(@PathVariable("solutionId") String solutionId,
			@PathVariable("revisionId") String revisionId, HttpServletResponse response) {
		logger.debug(EELFLoggerDelegate.debugLogger, "getSolRevArtifacts: solution {}, revision {} ", solutionId,
				revisionId);
		return artifactRepository.findByRevision(revisionId);
	}

	/**
	 * @param solutionId
	 *            solution ID
	 * @param revisionId
	 *            revision ID
	 * @param artifactId
	 *            artifact ID
	 * @param response
	 *            HttpServletResponse
	 * @return Success indicator
	 */
	@ApiOperation(value = "Adds an artifact to the solution revision.", response = SuccessTransport.class)
	@RequestMapping(value = "/{solutionId}/" + CCDSConstants.REVISION_PATH + "/{revisionId}/"
			+ CCDSConstants.ARTIFACT_PATH + "/{artifactId}", method = RequestMethod.POST)
	@ResponseBody
	public SuccessTransport addRevArtifact(@PathVariable("solutionId") String solutionId,
			@PathVariable("revisionId") String revisionId, @PathVariable("artifactId") String artifactId,
			HttpServletResponse response) {
		logger.debug(EELFLoggerDelegate.debugLogger, "addArtifact: solution {}, revision {}, artifact {} ", solutionId,
				revisionId, artifactId);
		MLPSolRevArtMap map = new MLPSolRevArtMap(revisionId, artifactId);
		solRevArtMapRepository.save(map);
		return new SuccessTransport(HttpServletResponse.SC_OK, null);
	}

	/**
	 * @param solutionId
	 *            solution ID
	 * @param revisionId
	 *            revision ID
	 * @param artifactId
	 *            artifact ID
	 * @param response
	 *            HttpServletResponse
	 * @return Success indicator
	 */
	@ApiOperation(value = "Removes an artifact from the solution revision.", response = SuccessTransport.class)
	@RequestMapping(value = "/{solutionId}/" + CCDSConstants.REVISION_PATH + "/{revisionId}/"
			+ CCDSConstants.ARTIFACT_PATH + "/{artifactId}", method = RequestMethod.DELETE)
	@ResponseBody
	public SuccessTransport dropRevArtifact(@PathVariable("solutionId") String solutionId,
			@PathVariable("revisionId") String revisionId, @PathVariable("artifactId") String artifactId,
			HttpServletResponse response) {
		logger.debug(EELFLoggerDelegate.debugLogger, "dropArtifact: solution {}, revision {}, artifact {} ", solutionId,
				revisionId, artifactId);
		solRevArtMapRepository.delete(new MLPSolRevArtMap.SolRevArtMapPK(revisionId, artifactId));
		return new SuccessTransport(HttpServletResponse.SC_OK, null);
	}

	/**
	 * @param solutionId
	 *            Solution ID
	 * @return List of MLPTag
	 */
	@ApiOperation(value = "Gets a list of tags for the specified solution.", response = MLPTag.class, responseContainer = "List")
	@RequestMapping(value = "/{solutionId}/" + CCDSConstants.TAG_PATH, method = RequestMethod.GET)
	@ResponseBody
	public Iterable<MLPTag> getTagsForSolution(@PathVariable("solutionId") String solutionId) {
		return solutionTagRepository.findBySolution(solutionId);
	}

	/**
	 * @param solutionId
	 *            solution ID
	 * @param tag
	 *            tag to add
	 * @param response
	 *            HttpServletResponse
	 * @return Success indicator
	 */
	@ApiOperation(value = "Adds a tag to the solution.", response = SuccessTransport.class)
	@RequestMapping(value = "/{solutionId}/" + CCDSConstants.TAG_PATH + "/{tag}", method = RequestMethod.POST)
	@ResponseBody
	public Object addTag(@PathVariable("solutionId") String solutionId, @PathVariable("tag") String tag,
			HttpServletResponse response) {
		logger.debug(EELFLoggerDelegate.debugLogger, "addTag: solution {}, tag {}", solutionId, tag);
		if (solutionTagRepository.findOne(tag) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "No tag " + tag, null);
		} else if (solutionRepository.findOne(solutionId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "No solution " + solutionId, null);
		} else {
			solTagMapRepository.save(new MLPSolTagMap(solutionId, tag));
			return new SuccessTransport(HttpServletResponse.SC_OK, null);
		}
	}

	/**
	 * @param solutionId
	 *            solution ID
	 * @param tag
	 *            tag to add
	 * @param response
	 *            HttpServletResponse
	 * @return Success indicator
	 */
	@ApiOperation(value = "Drops a tag from the solution.", response = SuccessTransport.class)
	@RequestMapping(value = "/{solutionId}/" + CCDSConstants.TAG_PATH + "/{tag}", method = RequestMethod.DELETE)
	@ResponseBody
	public Object dropTag(@PathVariable("solutionId") String solutionId, @PathVariable("tag") String tag,
			HttpServletResponse response) {
		logger.debug(EELFLoggerDelegate.debugLogger, "dropTag: solution {}, tag {}", solutionId, tag);
		if (solutionTagRepository.findOne(tag) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "No tag " + tag, null);
		} else if (solutionRepository.findOne(solutionId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "No solution " + solutionId, null);
		} else {
			solTagMapRepository.delete(new MLPSolTagMap.SolTagMapPK(solutionId, tag));
			return new SuccessTransport(HttpServletResponse.SC_OK, null);
		}
	}

	/**
	 * @param solutionId
	 *            Path parameter with solution ID
	 * @param pageRequest
	 *            Page and sort criteria
	 * @param response
	 *            HttpServletResponse
	 * @return A page of download records
	 */
	@ApiOperation(value = "Gets a page of download records for the specified solution ID.", response = MLPSolutionDownload.class, responseContainer = "Page")
	@RequestMapping(value = "/{solutionId}/" + CCDSConstants.DOWNLOAD_PATH, method = RequestMethod.GET)
	@ResponseBody
	public Object getPageOfSolutionDownloads(@PathVariable("solutionId") String solutionId, Pageable pageRequest,
			HttpServletResponse response) {
		Iterable<MLPSolutionDownload> da = solutionDownloadRepository.findBySolutionId(solutionId, pageRequest);
		if (da == null || !da.iterator().hasNext()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "No entries for solution ID " + solutionId,
					null);
		}
		return da;
	}

	/**
	 * @param solutionId
	 *            Solution ID
	 * @param artifactId
	 *            Artifact ID
	 * @param userId
	 *            User ID
	 * @param sd
	 *            solution download object
	 * @param response
	 *            HttpServletResponse
	 * @return solution model for serialization as JSON
	 */
	@ApiOperation(value = "Creates a new solution download record.", response = MLPSolutionDownload.class)
	@RequestMapping(value = "/{solutionId}/" + CCDSConstants.DOWNLOAD_PATH + "/" + CCDSConstants.ARTIFACT_PATH
			+ "/{artifactId}/" + CCDSConstants.USER_PATH + "/{userId}", method = RequestMethod.POST)
	@ResponseBody
	public Object createSolutionDownload(@PathVariable("solutionId") String solutionId,
			@PathVariable("userId") String userId, @PathVariable("artifactId") String artifactId,
			@RequestBody MLPSolutionDownload sd, HttpServletResponse response) {
		logger.debug(EELFLoggerDelegate.debugLogger, "create: received object: {} ", sd);
		// These validations duplicate the constraints but are much user friendlier.
		if (solutionRepository.findOne(solutionId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "No solution " + solutionId, null);
		}
		if (artifactRepository.findOne(artifactId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "No artifact" + artifactId, null);
		}
		if (userRepository.findOne(userId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "No user " + userId, null);
		}
		Object result;
		try {
			// Create a new row
			// Use path IDs
			sd.setSolutionId(solutionId);
			sd.setUserId(userId);
			sd.setArtifactId(artifactId);
			result = solutionDownloadRepository.save(sd);
			response.setStatus(HttpServletResponse.SC_CREATED);
			response.setHeader(HttpHeaders.LOCATION, CCDSConstants.SOLUTION_PATH + "/" + sd.getSolutionId() + "/"
					+ CCDSConstants.DOWNLOAD_PATH + sd.getDownloadId());
			// Update cache
			updateSolutionDownloadStats(solutionId);
		} catch (Exception ex) {
			logger.error(EELFLoggerDelegate.errorLogger, "createSolutionDownload", ex.toString());
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			result = new ErrorTransport(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					ex.getCause() != null ? ex.getCause().getMessage() : "createSolutionDownload failed", ex);
		}
		return result;
	}

	/**
	 * @param solutionId
	 *            solution ID
	 * @param downloadId
	 *            ID of record
	 * @param response
	 *            HttpServletResponse
	 * @return Transport model with success
	 */
	@ApiOperation(value = "Deletes the specified solution download record.", response = SuccessTransport.class)
	@RequestMapping(value = "/{solutionId}/" + CCDSConstants.DOWNLOAD_PATH
			+ "/{downloadId}", method = RequestMethod.DELETE)
	@ResponseBody
	public MLPTransportModel deleteSolutionDownload(@PathVariable("solutionId") String solutionId,
			@PathVariable("downloadId") Long downloadId, HttpServletResponse response) {
		try { // Build a key for fetch
			solutionDownloadRepository.delete(downloadId);
			// Update cache
			updateSolutionDownloadStats(solutionId);
			// Answer "OK"
			return new SuccessTransport(HttpServletResponse.SC_OK, null);
		} catch (Exception ex) {
			// e.g., EmptyResultDataAccessException is NOT an internal server error
			logger.warn(EELFLoggerDelegate.errorLogger, "deleteSolutionDownload failed", ex.getMessage());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "deleteSolutionDownload failed", ex);
		}
	}

	/**
	 * @param solutionId
	 *            Path parameter with ID
	 * @param pageRequest
	 *            Page and sort criteria
	 * @param response
	 *            HttpServletResponse
	 * @return A list of solution ratings
	 */
	@ApiOperation(value = "Gets all user ratings for the specified solution.", response = MLPSolutionRating.class, responseContainer = "List")
	@RequestMapping(value = "/{solutionId}/" + CCDSConstants.RATING_PATH, method = RequestMethod.GET)
	@ResponseBody
	public Object getListOfSolutionRating(@PathVariable("solutionId") String solutionId, Pageable pageRequest,
			HttpServletResponse response) {
		Iterable<MLPSolutionRating> sr = solutionRatingRepository.findBySolutionId(solutionId, pageRequest);
		if (sr == null || !sr.iterator().hasNext()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "No entries for solution " + solutionId,
					null);
		}
		return sr;
	}

	/**
	 * @param solutionId
	 *            Path parameter with solution ID
	 * @param userId
	 *            Path parameter with user ID
	 * @param response
	 *            HttpServletResponse
	 * @return MLPSolutionRating
	 */
	@ApiOperation(value = "Gets an existing solution rating.", response = MLPSolutionRating.class)
	@RequestMapping(value = "/{solutionId}/" + CCDSConstants.RATING_PATH + "/" + CCDSConstants.USER_PATH
			+ "/{userId}", method = RequestMethod.GET)
	@ResponseBody
	public Object getSolutionRating(@PathVariable("solutionId") String solutionId,
			@PathVariable("userId") String userId, HttpServletResponse response) {
		if (solutionRepository.findOne(solutionId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "No solution " + solutionId, null);
		}
		if (userRepository.findOne(userId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "No user " + userId, null);
		}
		MLPSolutionRating da = solutionRatingRepository.findOne(new SolutionRatingPK(solutionId, userId));
		if (da == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST,
					"No rating for solution ID " + solutionId + " by user " + userId, null);
		}
		return da;
	}

	/**
	 * @param solutionId
	 *            Solution ID
	 * @param userId
	 *            User who gave the rating
	 * @param sr
	 *            Solution rating object
	 * @param response
	 *            HttpServletResponse
	 * @return solution model for serialization as JSON
	 */
	@ApiOperation(value = "Creates a new solution rating.", response = MLPSolutionRating.class)
	@RequestMapping(value = "/{solutionId}/" + CCDSConstants.RATING_PATH + "/" + CCDSConstants.USER_PATH
			+ "/{userId}", method = RequestMethod.POST)
	@ResponseBody
	public Object createSolutionRating(@PathVariable("solutionId") String solutionId,
			@PathVariable("userId") String userId, @RequestBody MLPSolutionRating sr, HttpServletResponse response) {
		logger.debug(EELFLoggerDelegate.debugLogger, "create: received object: {} ", sr);
		if (solutionRepository.findOne(solutionId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "No solution " + solutionId, null);
		}
		if (userRepository.findOne(userId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "No user " + userId, null);
		}
		Object result;
		try {
			// Use path IDs
			sr.setSolutionId(solutionId);
			sr.setUserId(userId);
			result = solutionRatingRepository.save(sr);
			response.setStatus(HttpServletResponse.SC_CREATED);
			response.setHeader(HttpHeaders.LOCATION, CCDSConstants.SOLUTION_PATH + "/" + solutionId + "/"
					+ CCDSConstants.RATING_PATH + "/" + CCDSConstants.USER_PATH + "/" + userId);
			// Update cache
			updateSolutionRatingStats(solutionId);
		} catch (Exception ex) {
			Exception cve = findConstraintViolationException(ex);
			logger.warn(EELFLoggerDelegate.errorLogger, "createSolutionRating", cve.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			result = new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "createSolutionRating failed", cve);
		}
		return result;
	}

	/**
	 * @param solutionId
	 *            Solution ID
	 * @param userId
	 *            User who gave the rating
	 * @param sr
	 *            Solution rating object
	 * @param response
	 *            HttpServletResponse
	 * @return Transport model with success or failure
	 */
	@ApiOperation(value = "Updates the specified solution rating.", response = SuccessTransport.class)
	@RequestMapping(value = "/{solutionId}/" + CCDSConstants.RATING_PATH + "/" + CCDSConstants.USER_PATH
			+ "/{userId}", method = RequestMethod.PUT)
	@ResponseBody
	public Object updateSolutionRating(@PathVariable("solutionId") String solutionId,
			@PathVariable("userId") String userId, @RequestBody MLPSolutionRating sr, HttpServletResponse response) {
		logger.debug(EELFLoggerDelegate.debugLogger, "update: received {} ", sr);
		// Get the existing one
		SolutionRatingPK pk = new SolutionRatingPK(solutionId, userId);
		MLPSolutionRating existing = solutionRatingRepository.findOne(pk);
		if (existing == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "Failed to find object with id " + pk, null);
		}
		MLPTransportModel result = null;
		try {
			// Use path IDs
			sr.setSolutionId(solutionId);
			sr.setUserId(userId);
			solutionRatingRepository.save(sr);
			// Answer "OK"
			result = new SuccessTransport(HttpServletResponse.SC_OK, null);
			// Update cache
			updateSolutionRatingStats(solutionId);
		} catch (Exception ex) {
			Exception cve = findConstraintViolationException(ex);
			logger.warn(EELFLoggerDelegate.errorLogger, "updateSolutionRating", cve.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			result = new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "updateSolutionRating failed", cve);
		}
		return result;
	}

	/**
	 * @param solutionId
	 *            solution ID
	 * @param userId
	 *            User who gave the rating
	 * @param response
	 *            HttpServletResponse
	 * @return Transport model with success
	 */
	@ApiOperation(value = "Deletes the specified solution rating.", response = SuccessTransport.class)
	@RequestMapping(value = "/{solutionId}/" + CCDSConstants.RATING_PATH + "/" + CCDSConstants.USER_PATH
			+ "/{userId}", method = RequestMethod.DELETE)
	@ResponseBody
	public MLPTransportModel deleteSolutionRating(@PathVariable("solutionId") String solutionId,
			@PathVariable("userId") String userId, HttpServletResponse response) {
		try {
			// Build a key for fetch
			SolutionRatingPK pk = new SolutionRatingPK(solutionId, userId);
			solutionRatingRepository.delete(pk);
			// Update cache
			updateSolutionRatingStats(solutionId);
			// Answer "OK"
			return new SuccessTransport(HttpServletResponse.SC_OK, null);
		} catch (Exception ex) {
			// e.g., EmptyResultDataAccessException is NOT an internal server error
			logger.warn(EELFLoggerDelegate.errorLogger, "deleteSolutionRating failed", ex.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "deleteSolutionRating failed", ex);
		}
	}

	/**
	 * @param solutionId
	 *            Solution ID
	 * @param response
	 *            HttpServletResponse
	 * @return Web site metadata about the specified solution including view count,
	 *         download count, rating count and such.
	 */
	@ApiOperation(value = "Gets web metadata for the specified solution including average rating and total download count.", response = MLPSolutionWeb.class)
	@RequestMapping(value = "/{solutionId}/" + CCDSConstants.WEB_PATH, method = RequestMethod.GET)
	@ResponseBody
	public Object getSolutionWebStats(@PathVariable("solutionId") String solutionId, HttpServletResponse response) {
		MLPSolutionWeb stats = solutionWebRepository.findOne(solutionId);
		if (stats == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "Failed to find object with id " + solutionId,
					null);
		}
		return stats;
	}

	/**
	 * @param solutionId
	 *            Solution ID
	 * @return List of users
	 */
	@ApiOperation(value = "Gets ACL of users for the specified solution.", response = MLPUser.class, responseContainer = "List")
	@RequestMapping(value = "/{solutionId}/" + CCDSConstants.USER_PATH + "/"
			+ CCDSConstants.ACCESS_PATH, method = RequestMethod.GET)
	@ResponseBody
	public Iterable<MLPUser> getSolutionACL(@PathVariable("solutionId") String solutionId) {
		return solUserAccMapRepository.getUsersForSolution(solutionId);
	}

	/**
	 * @param solutionId
	 *            solution ID
	 * @param userId
	 *            user ID to add
	 * @param response
	 *            HttpServletResponse
	 * @return Success indicator
	 */
	@ApiOperation(value = "Adds a user to the ACL for the specified solution.", response = SuccessTransport.class)
	@RequestMapping(value = "/{solutionId}/" + CCDSConstants.USER_PATH + "/{userId}/"
			+ CCDSConstants.ACCESS_PATH, method = RequestMethod.POST)
	@ResponseBody
	public Object addUserToSolutionACL(@PathVariable("solutionId") String solutionId,
			@PathVariable("userId") String userId, HttpServletResponse response) {
		logger.debug(EELFLoggerDelegate.debugLogger, "addUserToSolutionACL: solution {}, user {}", solutionId, userId);
		if (solutionRepository.findOne(solutionId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "No solution " + solutionId, null);
		} else if (userRepository.findOne(userId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "No user " + userId, null);
		} else {
			solUserAccMapRepository.save(new MLPSolUserAccMap(solutionId, userId));
			return new SuccessTransport(HttpServletResponse.SC_OK, null);
		}
	}

	/**
	 * @param solutionId
	 *            solution ID
	 * @param userId
	 *            user ID to drop
	 * @param response
	 *            HttpServletResponse
	 * @return Success indicator
	 */
	@ApiOperation(value = "Drops a user from the ACL for the specified solution.", response = SuccessTransport.class)
	@RequestMapping(value = "/{solutionId}/" + CCDSConstants.USER_PATH + "/{userId}/"
			+ CCDSConstants.ACCESS_PATH, method = RequestMethod.DELETE)
	@ResponseBody
	public Object dropUserFromSolutionACL(@PathVariable("solutionId") String solutionId,
			@PathVariable("userId") String userId, HttpServletResponse response) {
		logger.debug(EELFLoggerDelegate.debugLogger, "dropUserFromSolutionACL: solution {}, user {}", solutionId,
				userId);
		if (solutionRepository.findOne(solutionId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "No solution " + solutionId, null);
		} else if (userRepository.findOne(userId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "No user " + userId, null);
		} else {
			solUserAccMapRepository.delete(new MLPSolUserAccMap.SolUserAccessMapPK(solutionId, userId));
			return new SuccessTransport(HttpServletResponse.SC_OK, null);
		}
	}

	/**
	 * @param userId
	 *            Path parameter with user ID
	 * @param pageable
	 *            Page and sort criteria
	 * @param response
	 *            HttpServletResponse
	 * @return A usage if found, an error otherwise.
	 */
	@ApiOperation(value = "Gets a page of solutions with the specified user in the ACL, optionally sorted on fields.", response = MLPSolution.class, responseContainer = "List")
	@RequestMapping(value = CCDSConstants.USER_PATH + "/{userId}/"
			+ CCDSConstants.ACCESS_PATH, method = RequestMethod.GET)
	@ResponseBody
	public Object getPageOfAccessibleSolutions(@PathVariable("userId") String userId, Pageable pageable,
			HttpServletResponse response) {
		return solUserAccMapRepository.getSolutionsForUser(userId, pageable);
	}

	/**
	 * @param solutionId
	 *            Path parameter with solution ID
	 * @param revisionId
	 *            Path parameter with revision ID
	 * @param response
	 *            HttpServletResponse
	 * @return A list of solution ratings
	 */
	@ApiOperation(value = "Gets validation results for the specified solution and revision.", response = MLPSolutionValidation.class, responseContainer = "List")
	@RequestMapping(value = "/{solutionId}/" + CCDSConstants.REVISION_PATH + "/{revisionId}/"
			+ CCDSConstants.VALIDATION_PATH, method = RequestMethod.GET)
	@ResponseBody
	public Object getListOfSolutionValidations(@PathVariable("solutionId") String solutionId,
			@PathVariable("revisionId") String revisionId, HttpServletResponse response) {
		Iterable<MLPSolutionValidation> items = solutionValidationRepository.findBySolutionIdRevisionId(solutionId,
				revisionId);
		if (items == null || !items.iterator().hasNext()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST,
					"No entries for solution " + solutionId + ", revision " + revisionId, null);
		}
		return items;
	}

	/**
	 * @param solutionId
	 *            Path parameter with solution ID
	 * @param revisionId
	 *            Path parameter with revision ID
	 * @param taskId
	 *            Path parameter with task ID
	 * @param sv
	 *            Solution validation object
	 * @param response
	 *            HttpServletResponse
	 * @return solution model for serialization as JSON
	 */
	@ApiOperation(value = "Creates a new solution validation record.", response = MLPSolutionValidation.class)
	@RequestMapping(value = "/{solutionId}/" + CCDSConstants.REVISION_PATH + "/{revisionId}/"
			+ CCDSConstants.VALIDATION_PATH + "/{taskId}", method = RequestMethod.POST)
	@ResponseBody
	public Object createSolutionValidation(@PathVariable("solutionId") String solutionId,
			@PathVariable("revisionId") String revisionId, @PathVariable("taskId") String taskId,
			@RequestBody MLPSolutionValidation sv, HttpServletResponse response) {
		logger.debug(EELFLoggerDelegate.debugLogger, "createSolutionValidation: received object: {} ", sv);
		if (solutionRepository.findOne(solutionId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "No solution " + solutionId, null);
		}
		if (solutionRevisionRepository.findOne(revisionId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "No revision " + revisionId, null);
		}
		Object result;
		try {
			// Use path IDs
			sv.setSolutionId(solutionId);
			sv.setRevisionId(revisionId);
			sv.setTaskId(taskId);
			result = solutionValidationRepository.save(sv);
			response.setStatus(HttpServletResponse.SC_CREATED);
			response.setHeader(HttpHeaders.LOCATION,
					CCDSConstants.SOLUTION_PATH + "/" + solutionId + "/" + CCDSConstants.REVISION_PATH + "/"
							+ revisionId + "/" + CCDSConstants.VALIDATION_PATH + "/" + taskId);
		} catch (Exception ex) {
			Exception cve = findConstraintViolationException(ex);
			logger.warn(EELFLoggerDelegate.errorLogger, "createSolutionValidation", cve.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			result = new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "createSolutionValidation failed", cve);
		}
		return result;
	}

	/**
	 * @param solutionId
	 *            Path parameter with solution ID
	 * @param revisionId
	 *            Path parameter with revision ID
	 * @param taskId
	 *            Path parameter with task ID
	 * @param sv
	 *            Solution validation object
	 * @param response
	 *            HttpServletResponse
	 * @return Transport model with success or failure
	 */
	@ApiOperation(value = "Updates the specified solution validation.", response = SuccessTransport.class)
	@RequestMapping(value = "/{solutionId}/" + CCDSConstants.REVISION_PATH + "/{revisionId}/"
			+ CCDSConstants.VALIDATION_PATH + "/{taskId}", method = RequestMethod.PUT)
	@ResponseBody
	public Object updateSolutionValidation(@PathVariable("solutionId") String solutionId,
			@PathVariable("revisionId") String revisionId, @PathVariable("taskId") String taskId,
			@RequestBody MLPSolutionValidation sv, HttpServletResponse response) {
		logger.debug(EELFLoggerDelegate.debugLogger, "updateSolutionValidation: received {} ", sv);
		// Get the existing one
		SolutionValidationPK pk = new SolutionValidationPK(solutionId, revisionId, taskId);
		MLPSolutionValidation existing = solutionValidationRepository.findOne(pk);
		if (existing == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "Failed to find object with id " + pk, null);
		}
		MLPTransportModel result = null;
		try {
			// Use path IDs
			sv.setSolutionId(solutionId);
			sv.setRevisionId(revisionId);
			sv.setTaskId(taskId);
			solutionValidationRepository.save(sv);
			// Answer "OK"
			result = new SuccessTransport(HttpServletResponse.SC_OK, null);
		} catch (Exception ex) {
			Exception cve = findConstraintViolationException(ex);
			logger.warn(EELFLoggerDelegate.errorLogger, "updateSolutionValidation", cve.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			result = new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "updateSolutionValidation failed", cve);
		}
		return result;
	}

	/**
	 * @param solutionId
	 *            Path parameter with solution ID
	 * @param revisionId
	 *            Path parameter with revision ID
	 * @param taskId
	 *            Path parameter with task ID
	 * @param response
	 *            HttpServletResponse
	 * @return Transport model with success
	 */
	@ApiOperation(value = "Deletes the specified solution validation record.", response = SuccessTransport.class)
	@RequestMapping(value = "/{solutionId}/" + CCDSConstants.REVISION_PATH + "/{revisionId}/"
			+ CCDSConstants.VALIDATION_PATH + "/{taskId}", method = RequestMethod.DELETE)
	@ResponseBody
	public MLPTransportModel deleteSolutionValidation(@PathVariable("solutionId") String solutionId,
			@PathVariable("revisionId") String revisionId, @PathVariable("taskId") String taskId,
			HttpServletResponse response) {
		try {
			SolutionValidationPK pk = new SolutionValidationPK(solutionId, revisionId, taskId);
			solutionValidationRepository.delete(pk);
			return new SuccessTransport(HttpServletResponse.SC_OK, null);
		} catch (Exception ex) {
			// e.g., EmptyResultDataAccessException is NOT an internal server error
			logger.warn(EELFLoggerDelegate.errorLogger, "deleteSolutionValidation failed", ex.getMessage());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "deleteSolutionValidation failed", ex);
		}
	}

	/**
	 * @param solutionId
	 *            Path parameter with solution ID
	 * @param revisionId
	 *            Path parameter with revision ID
	 * @param pageRequest
	 *            Page and sort criteria
	 * @param response
	 *            HttpServletResponse
	 * @return Page of deployments
	 */
	@ApiOperation(value = "Gets the deployments for the specified solution and revision IDs.", response = MLPSolutionDeployment.class, responseContainer = "Page")
	@RequestMapping(value = "/{solutionId}/" + CCDSConstants.REVISION_PATH + "/{revisionId}/"
			+ CCDSConstants.DEPLOYMENT_PATH, method = RequestMethod.GET)
	@ResponseBody
	public Object getSolutionDeployments(@PathVariable("solutionId") String solutionId,
			@PathVariable("revisionId") String revisionId, Pageable pageRequest, HttpServletResponse response) {
		Page<MLPSolutionDeployment> da = solutionDeploymentRepository.findBySolutionRevisionIds(solutionId, revisionId,
				pageRequest);
		if (da == null || !da.iterator().hasNext()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST,
					"No entries for solution ID " + solutionId + ", revision ID " + revisionId, null);
		}
		return da;
	}

	/**
	 * @param solutionId
	 *            Path parameter with solution ID
	 * @param revisionId
	 *            Path parameter with revision ID
	 * @param userId
	 *            Path parameter with user ID
	 * @param pageRequest
	 *            Page and sort criteria
	 * @param response
	 *            HttpServletResponse
	 * @return Page of deployments
	 */
	@ApiOperation(value = "Gets the deployments for the specified solution, revision and user IDs.", response = MLPSolutionDeployment.class, responseContainer = "Page")
	@RequestMapping(value = "/{solutionId}/" + CCDSConstants.REVISION_PATH + "/{revisionId}/" + CCDSConstants.USER_PATH
			+ "/{userId}/" + CCDSConstants.DEPLOYMENT_PATH, method = RequestMethod.GET)
	@ResponseBody
	public Object getUserSolutionRevisionDeployments(@PathVariable("solutionId") String solutionId,
			@PathVariable("revisionId") String revisionId, @PathVariable("userId") String userId, Pageable pageRequest,
			HttpServletResponse response) {
		Page<MLPSolutionDeployment> da = solutionDeploymentRepository.findBySolutionRevisionUserIds(solutionId,
				revisionId, userId, pageRequest);
		if (da == null || !da.iterator().hasNext()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST,
					"No entries for solution ID " + solutionId + ", revision ID " + revisionId + ", user ID " + userId,
					null);
		}
		return da;
	}

	/**
	 * @param solutionId
	 *            Solution ID
	 * @param revisionId
	 *            Path parameter with revision ID
	 * @param sd
	 *            Solution Deployment model
	 * @param response
	 *            HttpServletResponse
	 * @return model for serialization as JSON
	 */
	@ApiOperation(value = "Creates a new deployment record for the specified solution and revision.", response = MLPSolutionDeployment.class)
	@RequestMapping(value = "/{solutionId}/" + CCDSConstants.REVISION_PATH + "/{revisionId}/"
			+ CCDSConstants.DEPLOYMENT_PATH, method = RequestMethod.POST)
	@ResponseBody
	public Object createSolutionDeployment(@PathVariable("solutionId") String solutionId,
			@PathVariable("revisionId") String revisionId, @RequestBody MLPSolutionDeployment sd,
			HttpServletResponse response) {
		logger.debug(EELFLoggerDelegate.debugLogger, "create: received object: {} ", sd);
		Object result;
		if (solutionRepository.findOne(solutionId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "No solution " + solutionId, null);
		}
		if (solutionRevisionRepository.findOne(revisionId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "No revision " + revisionId, null);
		}
		if (userRepository.findOne(sd.getUserId()) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "No user " + sd.getUserId(), null);
		}
		try {
			// Validate ID if present
			String id = sd.getDeploymentId();
			if (id != null) {
				UUID.fromString(id);
				if (solutionDeploymentRepository.findOne(id) != null) {
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "ID exists: " + id);
				}
			}
			// Create a new row
			// Use path IDs
			sd.setSolutionId(solutionId);
			sd.setRevisionId(revisionId);
			// do NOT null out the deployment ID
			result = solutionDeploymentRepository.save(sd);
			response.setStatus(HttpServletResponse.SC_CREATED);
			response.setHeader(HttpHeaders.LOCATION,
					CCDSConstants.SOLUTION_PATH + "/" + sd.getSolutionId() + "/" + CCDSConstants.REVISION_PATH
							+ revisionId + CCDSConstants.DEPLOYMENT_PATH + "/" + sd.getDeploymentId());
		} catch (Exception ex) {
			logger.error(EELFLoggerDelegate.errorLogger, "createSolutionDeployment failed", ex);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			result = new ErrorTransport(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					ex.getCause() != null ? ex.getCause().getMessage() : "createSolutionDeployment failed", ex);
		}
		return result;
	}

	/**
	 * @param solutionId
	 *            Path parameter with Solution ID
	 * @param revisionId
	 *            Path parameter with revision ID
	 * @param deploymentId
	 *            Path parameter with deployment ID
	 * @param sd
	 *            Solution Deployment model
	 * @param response
	 *            HttpServletResponse
	 * @return model for serialization as JSON
	 */
	@ApiOperation(value = "Updates the deployment record for the specified ID.", response = MLPSolutionDeployment.class)
	@RequestMapping(value = "/{solutionId}/" + CCDSConstants.REVISION_PATH + "/{revisionId}/"
			+ CCDSConstants.DEPLOYMENT_PATH + "/{deploymentId}", method = RequestMethod.PUT)
	@ResponseBody
	public Object updateSolutionDeployment(@PathVariable("solutionId") String solutionId,
			@PathVariable("revisionId") String revisionId, @PathVariable("deploymentId") String deploymentId,
			@RequestBody MLPSolutionDeployment sd, HttpServletResponse response) {
		logger.debug(EELFLoggerDelegate.debugLogger, "update: received object: {} ", sd);
		if (solutionDeploymentRepository.findOne(deploymentId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "No deployment " + deploymentId, null);
		}
		Object result;
		try {
			// Create a new row
			// Use path IDs
			sd.setSolutionId(solutionId);
			sd.setRevisionId(revisionId);
			sd.setDeploymentId(deploymentId);
			solutionDeploymentRepository.save(sd);
			result = new SuccessTransport(HttpServletResponse.SC_OK, null);
		} catch (Exception ex) {
			Exception cve = findConstraintViolationException(ex);
			logger.warn(EELFLoggerDelegate.errorLogger, "updateSolutionDeployment", cve.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			result = new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "updateSolutionDeployment failed", cve);
		}
		return result;
	}

	/**
	 * @param solutionId
	 *            Solution ID; actually not required
	 * @param revisionId
	 *            Revision ID; actually not required
	 * @param deploymentId
	 *            Deployment ID; the primary key
	 * @param response
	 *            HttpServletResponse
	 * @return Transport model with success
	 */
	@ApiOperation(value = "Deletes the specified deployment record.", response = SuccessTransport.class)
	@RequestMapping(value = "/{solutionId}/" + CCDSConstants.REVISION_PATH + "/{revisionId}/"
			+ CCDSConstants.DEPLOYMENT_PATH + "/{deploymentId}", method = RequestMethod.DELETE)
	@ResponseBody
	public MLPTransportModel deleteSolutionDeployment(@PathVariable("solutionId") String solutionId,
			@PathVariable("revisionId") String revisionId, @PathVariable("deploymentId") String deploymentId,
			HttpServletResponse response) {
		try {
			solutionDeploymentRepository.delete(deploymentId);
			return new SuccessTransport(HttpServletResponse.SC_OK, null);
		} catch (Exception ex) {
			// e.g., EmptyResultDataAccessException is NOT an internal server error
			logger.warn(EELFLoggerDelegate.errorLogger, "deleteSolutionDeployment failed", ex.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "deleteSolutionDeployment failed", ex);
		}
	}

}

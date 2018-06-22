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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.acumos.cds.CCDSConstants;
import org.acumos.cds.CodeNameType;
import org.acumos.cds.domain.MLPArtifact;
import org.acumos.cds.domain.MLPCompSolMap;
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
import org.acumos.cds.repository.CompSolMapRepository;
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
import org.acumos.cds.repository.StepResultRepository;
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
import org.springframework.util.MultiValueMap;
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

	private static final EELFLoggerDelegate logger = EELFLoggerDelegate.getLogger(MethodHandles.lookup().lookupClass());

	@Autowired
	private ArtifactRepository artifactRepository;
	@Autowired
	private CompSolMapRepository compSolMapRepository;
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
	private TagRepository tagRepository;
	@Autowired
	private SolutionValidationRepository solutionValidationRepository;
	@Autowired
	private SolutionWebRepository solutionWebRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private StepResultRepository stepResultRepository;

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
	 * @return SuccessTransport object
	 */
	@ApiOperation(value = "Gets the count of solutions.", response = CountTransport.class)
	@RequestMapping(value = CCDSConstants.COUNT_PATH, method = RequestMethod.GET)
	@ResponseBody
	public CountTransport getSolutionCount() {
		Date beginDate = new Date();
		Long count = solutionRepository.count();
		logger.audit(beginDate, "getSolutionCount");
		return new CountTransport(count);
	}

	/**
	 * @param pageable
	 *            Sort and page criteria
	 * @param response
	 *            HttpServletResponse
	 * @return Page of solutions
	 */
	@ApiOperation(value = "Gets a page of solutions, optionally sorted on fields.", response = MLPSolution.class, responseContainer = "Page")
	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	public Page<MLPSolution> getSolutions(Pageable pageable, HttpServletResponse response) {
		Date beginDate = new Date();
		Page<MLPSolution> result = solutionRepository.findAll(pageable);
		logger.audit(beginDate, "getSolutions {}", pageable);
		return result;
	}

	/**
	 * @param term
	 *            Search term used for partial match ("like")
	 * @param pageRequest
	 *            Page and sort criteria. Spring sets to page 0 of size 20 if client
	 *            sends nothing.
	 * @param response
	 *            HttpServletResponse
	 * @return Page of solutions
	 */
	@ApiOperation(value = "Searches for solutions with names or descriptions that contain the search term.", response = MLPSolution.class, responseContainer = "Page")
	@RequestMapping(value = "/" + CCDSConstants.SEARCH_PATH + "/" + CCDSConstants.LIKE_PATH, method = RequestMethod.GET)
	@ResponseBody
	public Page<MLPSolution> findSolutionsByLikeKeyword(@RequestParam(CCDSConstants.TERM_PATH) String term,
			Pageable pageRequest, HttpServletResponse response) {
		Date beginDate = new Date();
		Page<MLPSolution> result = solutionRepository.findBySearchTerm(term, pageRequest);
		logger.audit(beginDate, "findSolutionsByLikeKeyword {}", term);
		return result;
	}

	/**
	 * @param tag
	 *            Tag string to find
	 * @param pageRequest
	 *            Page and sort criteria. Spring sets to page 0 of size 20 if client
	 *            sends nothing.
	 * @param response
	 *            HttpServletResponse
	 * @return Page of solutions
	 */
	@ApiOperation(value = "Gets a page of solutions matching the specified tag.", response = MLPSolution.class, responseContainer = "Page")
	@RequestMapping(value = "/" + CCDSConstants.SEARCH_PATH + "/" + CCDSConstants.TAG_PATH, method = RequestMethod.GET)
	@ResponseBody
	public Object findSolutionsByTag(@RequestParam("tag") String tag, Pageable pageRequest,
			HttpServletResponse response) {
		Date beginDate = new Date();
		MLPTag existing = tagRepository.findOne(tag);
		if (existing == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + tag, null);
		}
		Page<MLPSolution> result = solutionRepository.findByTag(tag, pageRequest);
		logger.audit(beginDate, "findSolutionsByTag {}", tag);
		return result;
	}

	/**
	 * Fetches the value, and converts the four-letter sequence "null" to the null
	 * value.
	 * 
	 * @param parmName
	 *            Map key
	 * @param queryParameters
	 *            Map of parameters
	 * @return String array; empty if key is not present
	 */
	private String[] getOptStringArray(String parmName, MultiValueMap<String, String> queryParameters) {
		List<String> val = queryParameters.get(parmName);
		if (val == null)
			return new String[0];
		String[] vals = new String[val.size()];
		vals = val.toArray(vals);
		for (int i = 0; i < vals.length; ++i)
			if ("null".equals(vals[i]))
				vals[i] = null;
		return vals;
	}

	/**
	 * @param queryParameters
	 *            Map of String (field name) to String (value) for restricting the
	 *            query
	 * @param pageRequest
	 *            Page and sort criteria. Spring sets to page 0 of size 20 if client
	 *            sends nothing.
	 * @param response
	 *            HttpServletResponse
	 * @return Page of solutions
	 */
	@ApiOperation(value = "Searches for solutions using the field name - field value pairs specified as query parameters. Defaults to and (conjunction); send junction query parameter = o for or (disjunction). With no limit, defaults to size 20.", response = MLPSolution.class, responseContainer = "Page")
	@RequestMapping(value = "/" + CCDSConstants.SEARCH_PATH, method = RequestMethod.GET)
	@ResponseBody
	public Object searchSolutions(@RequestParam MultiValueMap<String, String> queryParameters, Pageable pageRequest,
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
			Map<String, Object> convertedQryParm = convertQueryParameters(MLPSolution.class, queryParameters);
			Object result = solutionSearchService.findSolutions(convertedQryParm, isOr, pageRequest);
			logger.audit(beginDate, "searchSolutions: query {}", queryParameters);
			return result;
		} catch (Exception ex) {
			logger.warn(EELFLoggerDelegate.errorLogger, "searchSolutions failed", ex);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST,
					ex.getCause() != null ? ex.getCause().getMessage() : "searchSolutions failed", ex);
		}
	}

	/**
	 * Supports a dynamic query by user on Portal screen.
	 * 
	 * @param queryParameters
	 *            Field names-value pairs, see below for names. Some values can be
	 *            comma-separated lists.
	 * @param pageRequest
	 *            Page and sort criteria. Spring sets to page 0 of size 20 if client
	 *            sends nothing.
	 * @param response
	 *            HttpServletResponse
	 * @return Page of solutions
	 */
	@ApiOperation(value = "Gets a page of solutions for populating Portal screens.", response = MLPSolution.class, responseContainer = "Page")
	@RequestMapping(value = "/" + CCDSConstants.SEARCH_PATH + "/"
			+ CCDSConstants.PORTAL_PATH, method = RequestMethod.GET)
	@ResponseBody
	public Object findPortalSolutions(@RequestParam MultiValueMap<String, String> queryParameters, Pageable pageRequest,
			HttpServletResponse response) {
		Date beginDate = new Date();
		try {
			// This parameter is required
			Boolean active = new Boolean(queryParameters.getFirst(CCDSConstants.SEARCH_ACTIVE));
			// All remaining parameters are optional
			String[] nameKws = getOptStringArray(CCDSConstants.SEARCH_NAME, queryParameters);
			String[] descKws = getOptStringArray(CCDSConstants.SEARCH_DESC, queryParameters);
			String[] ownerIds = getOptStringArray(CCDSConstants.SEARCH_OWNERS, queryParameters);
			String[] modelTypeCodes = getOptStringArray(CCDSConstants.SEARCH_MODEL_TYPES, queryParameters);
			String[] accTypeCodes = getOptStringArray(CCDSConstants.SEARCH_ACCESS_TYPES, queryParameters);
			String[] valStatusCodes = getOptStringArray(CCDSConstants.SEARCH_VAL_STATUSES, queryParameters);
			String[] tags = getOptStringArray(CCDSConstants.SEARCH_TAGS, queryParameters);
			Object result = solutionSearchService.findPortalSolutions(nameKws, descKws, active, ownerIds,
					modelTypeCodes, accTypeCodes, valStatusCodes, tags, pageRequest);
			logger.audit(beginDate, "findPortalSolutions: query {}", queryParameters);
			return result;
		} catch (Exception ex) {
			logger.warn(EELFLoggerDelegate.errorLogger, "findPortalSolutions failed", ex);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST,
					ex.getCause() != null ? ex.getCause().getMessage() : "findPortalSolutions failed", ex);
		}
	}

	/**
	 * @param queryParameters
	 *            Map of String (field name) to String (value) for restricting the
	 *            query. Expects access type codes (optional), validation status
	 *            codes (optional), and date (required).
	 * @param pageRequest
	 *            Page and sort criteria
	 * @param response
	 *            HttpServletResponse
	 * @return Page of solutions
	 */
	@ApiOperation(value = "Gets a page of solutions modified after the specified time, expressed in milliseconds since the Epoch.", response = MLPSolution.class, responseContainer = "Page")
	@RequestMapping(value = "/" + CCDSConstants.SEARCH_PATH + "/" + CCDSConstants.DATE_PATH, method = RequestMethod.GET)
	@ResponseBody
	public Object findSolutionsByDate(@RequestParam MultiValueMap<String, String> queryParameters, Pageable pageRequest,
			HttpServletResponse response) {
		Date beginDate = new Date();
		Boolean active = new Boolean(queryParameters.getFirst(CCDSConstants.SEARCH_ACTIVE));
		String[] accessTypeCodes = getOptStringArray(CCDSConstants.SEARCH_ACCESS_TYPES, queryParameters);
		String[] valStatusCodes = getOptStringArray(CCDSConstants.SEARCH_VAL_STATUSES, queryParameters);
		String[] dateMillis = getOptStringArray(CCDSConstants.SEARCH_DATE, queryParameters);
		if (dateMillis.length != 1) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "Missing date parameter in query", null);
		}
		Date date = new Date(Long.parseLong(dateMillis[0]));
		Object result = solutionSearchService.findSolutionsByModifiedDate(active, accessTypeCodes, valStatusCodes, date,
				pageRequest);
		logger.audit(beginDate, "findSolutionsByDate: query {}", queryParameters);
		return result;
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
		Date beginDate = new Date();
		MLPSolution da = solutionRepository.findOne(solutionId);
		if (da == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + solutionId, null);
		}
		logger.audit(beginDate, "getSolution: ID {}", solutionId);
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
		Date beginDate = new Date();
		try {
			// Validate enum codes
			if (solution.getModelTypeCode() != null)
				super.validateCode(solution.getModelTypeCode(), CodeNameType.MODEL_TYPE);
			if (solution.getToolkitTypeCode() != null)
				super.validateCode(solution.getToolkitTypeCode(), CodeNameType.TOOLKIT_TYPE);
			String id = solution.getSolutionId();
			if (id != null) {
				UUID.fromString(id);
				if (solutionRepository.findOne(id) != null) {
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "Solution exists with ID " + id);
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
			response.setHeader(HttpHeaders.LOCATION, CCDSConstants.SOLUTION_PATH + "/" + persisted.getSolutionId());
			logger.audit(beginDate, "createSolution: ID {}", persisted.getSolutionId());
			return persisted;
		} catch (Exception ex) {
			Exception cve = findConstraintViolationException(ex);
			logger.warn(EELFLoggerDelegate.errorLogger, "createSolution", cve.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "createSolution failed", cve);
		}
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
		Date beginDate = new Date();
		// Get the existing one
		MLPSolution existing = solutionRepository.findOne(solutionId);
		if (existing == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + solutionId, null);
		}
		try {
			// Validate enum codes
			if (solution.getModelTypeCode() != null)
				super.validateCode(solution.getModelTypeCode(), CodeNameType.MODEL_TYPE);
			if (solution.getToolkitTypeCode() != null)
				super.validateCode(solution.getToolkitTypeCode(), CodeNameType.TOOLKIT_TYPE);
			// Use the path-parameter id; don't trust the one in the object
			solution.setSolutionId(solutionId);
			// Discard any stats object; updates don't happen via this interface
			solution.setWebStats(null);
			solutionRepository.save(solution);
			logger.audit(beginDate, "updateSolution: ID {}", solutionId);
			return new SuccessTransport(HttpServletResponse.SC_OK, null);
		} catch (Exception ex) {
			Exception cve = findConstraintViolationException(ex);
			logger.warn(EELFLoggerDelegate.errorLogger, "updateSolution", cve.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "updateSolution failed", cve);
		}
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
		Date beginDate = new Date();
		// Get the existing one
		MLPSolutionWeb existing = solutionWebRepository.findOne(solutionId);
		if (existing == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + solutionId, null);
		}
		try {
			// Have the database do the increment to avoid race conditions
			solutionWebRepository.incrementViewCount(solutionId);
			logger.audit(beginDate, "incrementViewCount: ID {}", solutionId);
			return new SuccessTransport(HttpServletResponse.SC_OK, null);
		} catch (Exception ex) {
			logger.error(EELFLoggerDelegate.errorLogger, "incrementViewCount failed", ex);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return new ErrorTransport(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "incrementViewCount failed", ex);
		}
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
		Date beginDate = new Date();
		try {
			// Manually cascade the delete
			solutionDeploymentRepository.deleteBySolutionId(solutionId);
			compSolMapRepository.deleteByParentId(solutionId);
			solTagMapRepository.deleteBySolutionId(solutionId);
			solutionDownloadRepository.deleteBySolutionId(solutionId);
			solutionRatingRepository.deleteBySolutionId(solutionId);
			solutionValidationRepository.deleteBySolutionId(solutionId);
			solUserAccMapRepository.deleteBySolutionId(solutionId);
			solutionFavoriteRepository.deleteBySolutionId(solutionId);
			stepResultRepository.deleteBySolutionId(solutionId);
			// The web stats are annotated as optional, so be cautious when deleting
			MLPSolutionWeb webStats = solutionWebRepository.findOne(solutionId);
			if (webStats != null)
				solutionWebRepository.delete(solutionId);
			for (MLPSolutionRevision r : solutionRevisionRepository.findBySolutionIdIn(new String[] { solutionId })) {
				for (MLPArtifact a : artifactRepository.findByRevision(r.getRevisionId()))
					solRevArtMapRepository
							.delete(new MLPSolRevArtMap.SolRevArtMapPK(r.getRevisionId(), a.getArtifactId()));
				// do NOT delete artifacts!
				solutionRevisionRepository.delete(r);
			}
			solutionRepository.delete(solutionId);
			logger.audit(beginDate, "deleteSolution: ID {}", solutionId);
			return new SuccessTransport(HttpServletResponse.SC_OK, null);
		} catch (Exception ex) {
			// e.g., EmptyResultDataAccessException is NOT an internal server error
			logger.warn(EELFLoggerDelegate.errorLogger, "deleteSolution failed", ex.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "deleteSolution failed", ex);
		}
	}

	/**
	 * @param solutionIds
	 *            Array of solution IDs (comma-separated values - the name should be
	 *            plural but it's declared above). Spring will split the list if the
	 *            path variable is declared as String array or List of String.
	 * @return List of revisions
	 */
	@ApiOperation(value = "Gets a list of revisions for the specified solution IDs.", response = MLPSolutionRevision.class, responseContainer = "List")
	@RequestMapping(value = "/{solutionId}/" + CCDSConstants.REVISION_PATH, method = RequestMethod.GET)
	@ResponseBody
	public Iterable<MLPSolutionRevision> getListOfRevisions(@PathVariable("solutionId") String[] solutionIds) {
		Date beginDate = new Date();
		Iterable<MLPSolutionRevision> result = solutionRevisionRepository.findBySolutionIdIn(solutionIds);
		logger.audit(beginDate, "getListOfRevisions: solution IDs {}", Arrays.toString(solutionIds));
		return result;
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
		Date beginDate = new Date();
		MLPSolutionRevision da = solutionRevisionRepository.findOne(revisionId);
		if (da == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + revisionId, null);
		}
		logger.audit(beginDate, "getSolutionRevision: solutionId {} revisionId {}", solutionId, revisionId);
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
		Date beginDate = new Date();
		if (solutionRepository.findOne(solutionId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + solutionId, null);
		}
		try {
			// Validate enum codes
			if (revision.getAccessTypeCode() != null)
				super.validateCode(revision.getAccessTypeCode(), CodeNameType.ACCESS_TYPE);
			if (revision.getValidationStatusCode() != null)
				super.validateCode(revision.getValidationStatusCode(), CodeNameType.VALIDATION_STATUS);
			String id = revision.getRevisionId();
			if (id != null) {
				UUID.fromString(id);
				if (solutionRevisionRepository.findOne(id) != null) {
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "Revision exists with ID " + id);
				}
			}
			// Ensure correct solution ID
			revision.setSolutionId(solutionId);
			// Create a new row
			MLPSolutionRevision result = solutionRevisionRepository.save(revision);
			logger.audit(beginDate, "createSolutionRevision: solutionId {} revisionId {}", solutionId,
					result.getRevisionId());
			return result;
		} catch (Exception ex) {
			Exception cve = findConstraintViolationException(ex);
			logger.warn(EELFLoggerDelegate.errorLogger, "createSolutionRevision", cve.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "createSolutionRevision failed", cve);
		}
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
		Date beginDate = new Date();
		if (solutionRepository.findOne(solutionId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + solutionId, null);
		}
		if (solutionRevisionRepository.findOne(revisionId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + revisionId, null);
		}
		try {
			// Validate enum codes
			if (revision.getAccessTypeCode() != null)
				super.validateCode(revision.getAccessTypeCode(), CodeNameType.ACCESS_TYPE);
			if (revision.getValidationStatusCode() != null)
				super.validateCode(revision.getValidationStatusCode(), CodeNameType.VALIDATION_STATUS);
			// Use the validated values
			revision.setRevisionId(revisionId);
			revision.setSolutionId(solutionId);
			solutionRevisionRepository.save(revision);
			logger.audit(beginDate, "updateSolutionRevision: solution ID {}, revision ID {}", solutionId, revisionId);
			return new SuccessTransport(HttpServletResponse.SC_OK, null);
		} catch (Exception ex) {
			Exception cve = findConstraintViolationException(ex);
			logger.warn(EELFLoggerDelegate.errorLogger, "updateSolutionRevision", cve.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "updateSolutionRevision failed", cve);
		}
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
		Date beginDate = new Date();
		try {
			solutionRevisionRepository.delete(revisionId);
			logger.audit(beginDate, "deleteSolutionRevision: solutionId {} revisionId {}", solutionId, revisionId);
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
		Date beginDate = new Date();
		Iterable<MLPArtifact> result = artifactRepository.findByRevision(revisionId);
		logger.audit(beginDate, "getSolRevArtifacts: solutionId {} revisionId {}", solutionId, revisionId);
		return result;
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
		Date beginDate = new Date();
		MLPSolRevArtMap map = new MLPSolRevArtMap(revisionId, artifactId);
		solRevArtMapRepository.save(map);
		logger.audit(beginDate, "addRevArtifact: solutionId {} revisionId {} artifactId {}", solutionId, revisionId,
				artifactId);
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
		Date beginDate = new Date();
		solRevArtMapRepository.delete(new MLPSolRevArtMap.SolRevArtMapPK(revisionId, artifactId));
		logger.audit(beginDate, "dropRevArtifact: solutionId {} revisionId {} artifactId {}", solutionId, revisionId,
				artifactId);
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
		Date beginDate = new Date();
		Iterable<MLPTag> result = tagRepository.findBySolution(solutionId);
		logger.audit(beginDate, "getTagsForSolution: solutionId {}", solutionId);
		return result;
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
		Date beginDate = new Date();
		if (tagRepository.findOne(tag) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + tag, null);
		} else if (solutionRepository.findOne(solutionId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + solutionId, null);
		} else {
			solTagMapRepository.save(new MLPSolTagMap(solutionId, tag));
			logger.audit(beginDate, "addTag: solutionId {} tag {}", solutionId, tag);
			return new SuccessTransport(HttpServletResponse.SC_OK, null);
		}
	}

	/**
	 * @param solutionId
	 *            solution ID
	 * @param tag
	 *            tag to remove
	 * @param response
	 *            HttpServletResponse
	 * @return Success indicator
	 */
	@ApiOperation(value = "Drops a tag from the solution.", response = SuccessTransport.class)
	@RequestMapping(value = "/{solutionId}/" + CCDSConstants.TAG_PATH + "/{tag}", method = RequestMethod.DELETE)
	@ResponseBody
	public Object dropTag(@PathVariable("solutionId") String solutionId, @PathVariable("tag") String tag,
			HttpServletResponse response) {
		Date beginDate = new Date();
		if (tagRepository.findOne(tag) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + tag, null);
		} else if (solutionRepository.findOne(solutionId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + solutionId, null);
		} else {
			solTagMapRepository.delete(new MLPSolTagMap.SolTagMapPK(solutionId, tag));
			logger.audit(beginDate, "dropTag: solutionId {} tag {}", solutionId, tag);
			return new SuccessTransport(HttpServletResponse.SC_OK, null);
		}
	}

	/**
	 * @param solutionId
	 *            Path parameter with solution ID
	 * @param pageRequest
	 *            Page and sort criteria. Spring sets to page 0 of size 20 if client
	 *            sends nothing.
	 * @param response
	 *            HttpServletResponse
	 * @return A page of download records
	 */
	@ApiOperation(value = "Gets a page of download records for the specified solution ID.", response = MLPSolutionDownload.class, responseContainer = "Page")
	@RequestMapping(value = "/{solutionId}/" + CCDSConstants.DOWNLOAD_PATH, method = RequestMethod.GET)
	@ResponseBody
	public Object getSolutionDownloads(@PathVariable("solutionId") String solutionId, Pageable pageRequest,
			HttpServletResponse response) {
		Date beginDate = new Date();
		if (solutionRepository.findOne(solutionId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + solutionId, null);
		}
		Iterable<MLPSolutionDownload> da = solutionDownloadRepository.findBySolutionId(solutionId, pageRequest);
		logger.audit(beginDate, "getSolutionDownloads: solutionId {}", solutionId);
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
		Date beginDate = new Date();
		// These validations duplicate the constraints but are much user friendlier.
		if (solutionRepository.findOne(solutionId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + solutionId, null);
		}
		if (artifactRepository.findOne(artifactId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + artifactId, null);
		}
		if (userRepository.findOne(userId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + userId, null);
		}
		try {
			// Create a new row using path IDs
			sd.setSolutionId(solutionId);
			sd.setUserId(userId);
			sd.setArtifactId(artifactId);
			Object result = solutionDownloadRepository.save(sd);
			response.setStatus(HttpServletResponse.SC_CREATED);
			response.setHeader(HttpHeaders.LOCATION, CCDSConstants.SOLUTION_PATH + "/" + sd.getSolutionId() + "/"
					+ CCDSConstants.DOWNLOAD_PATH + sd.getDownloadId());
			// Update cache
			updateSolutionDownloadStats(solutionId);
			logger.audit(beginDate, "createSolutionDownload: solutionId {} userId {} artifactId {}", solutionId, userId,
					artifactId);
			return result;
		} catch (Exception ex) {
			logger.error(EELFLoggerDelegate.errorLogger, "createSolutionDownload", ex.toString());
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return new ErrorTransport(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					ex.getCause() != null ? ex.getCause().getMessage() : "createSolutionDownload failed", ex);
		}
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
		Date beginDate = new Date();
		try { // Build a key for fetch
			solutionDownloadRepository.delete(downloadId);
			// Update cache!
			updateSolutionDownloadStats(solutionId);
			logger.audit(beginDate, "deleteSolutionDownload: solutionId {} downloadId {}", solutionId, downloadId);
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
	 *            Page and sort criteria. Spring sets to page 0 of size 20 if client
	 *            sends nothing.
	 * @param response
	 *            HttpServletResponse
	 * @return A list of solution ratings
	 */
	@ApiOperation(value = "Gets all user ratings for the specified solution.", response = MLPSolutionRating.class, responseContainer = "List")
	@RequestMapping(value = "/{solutionId}/" + CCDSConstants.RATING_PATH, method = RequestMethod.GET)
	@ResponseBody
	public Object getListOfSolutionRating(@PathVariable("solutionId") String solutionId, Pageable pageRequest,
			HttpServletResponse response) {
		Date beginDate = new Date();
		if (solutionRepository.findOne(solutionId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + solutionId, null);
		}
		Iterable<MLPSolutionRating> sr = solutionRatingRepository.findBySolutionId(solutionId, pageRequest);
		logger.audit(beginDate, "getListOfSolutionRating: solutionId {}", solutionId);
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
		Date beginDate = new Date();
		SolutionRatingPK pk = new SolutionRatingPK(solutionId, userId);
		MLPSolutionRating da = solutionRatingRepository.findOne(pk);
		if (da == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + pk);
		}
		logger.audit(beginDate, "getSolutionRating: solutionId {} userId {}", solutionId, userId);
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
		Date beginDate = new Date();
		if (solutionRepository.findOne(solutionId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + solutionId, null);
		}
		if (userRepository.findOne(userId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + userId, null);
		}
		try {
			// Use path IDs
			sr.setSolutionId(solutionId);
			sr.setUserId(userId);
			Object result = solutionRatingRepository.save(sr);
			response.setStatus(HttpServletResponse.SC_CREATED);
			response.setHeader(HttpHeaders.LOCATION, CCDSConstants.SOLUTION_PATH + "/" + solutionId + "/"
					+ CCDSConstants.RATING_PATH + "/" + CCDSConstants.USER_PATH + "/" + userId);
			// Update cache
			updateSolutionRatingStats(solutionId);
			logger.audit(beginDate, "createSolutionRating: solutionId {} userId {}", solutionId, userId);
			return result;
		} catch (Exception ex) {
			Exception cve = findConstraintViolationException(ex);
			logger.warn(EELFLoggerDelegate.errorLogger, "createSolutionRating", cve.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "createSolutionRating failed", cve);
		}
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
		Date beginDate = new Date();
		// Get the existing one
		SolutionRatingPK pk = new SolutionRatingPK(solutionId, userId);
		MLPSolutionRating existing = solutionRatingRepository.findOne(pk);
		if (existing == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + pk, null);
		}
		try {
			// Use path IDs
			sr.setSolutionId(solutionId);
			sr.setUserId(userId);
			solutionRatingRepository.save(sr);
			// Update cache!
			updateSolutionRatingStats(solutionId);
			logger.audit(beginDate, "updateSolutionRating: solutionId {} userId {}", solutionId, userId);
			return new SuccessTransport(HttpServletResponse.SC_OK, null);
		} catch (Exception ex) {
			Exception cve = findConstraintViolationException(ex);
			logger.warn(EELFLoggerDelegate.errorLogger, "updateSolutionRating", cve.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "updateSolutionRating failed", cve);
		}
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
		Date beginDate = new Date();
		try {
			// Build a key for fetch
			SolutionRatingPK pk = new SolutionRatingPK(solutionId, userId);
			solutionRatingRepository.delete(pk);
			// Update cache!
			updateSolutionRatingStats(solutionId);
			logger.audit(beginDate, "deleteSolutionRating: solutionId {} userId {}", solutionId, userId);
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
		Date beginDate = new Date();
		MLPSolutionWeb stats = solutionWebRepository.findOne(solutionId);
		if (stats == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + solutionId, null);
		}
		logger.audit(beginDate, "getSolutionWebStats: solutionId {}", solutionId);
		return stats;
	}

	/**
	 * @param solutionId
	 *            Solution ID
	 * @return List of users
	 */
	@ApiOperation(value = "Gets access-control list of users for the specified solution.", response = MLPUser.class, responseContainer = "List")
	@RequestMapping(value = "/{solutionId}/" + CCDSConstants.USER_PATH + "/"
			+ CCDSConstants.ACCESS_PATH, method = RequestMethod.GET)
	@ResponseBody
	public Iterable<MLPUser> getSolutionACL(@PathVariable("solutionId") String solutionId) {
		Date beginDate = new Date();
		Iterable<MLPUser> result = solUserAccMapRepository.getUsersForSolution(solutionId);
		logger.audit(beginDate, "getSolutionACL: solutionId {}", solutionId);
		return result;
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
		Date beginDate = new Date();
		if (solutionRepository.findOne(solutionId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + solutionId, null);
		} else if (userRepository.findOne(userId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + userId, null);
		} else {
			solUserAccMapRepository.save(new MLPSolUserAccMap(solutionId, userId));
			logger.audit(beginDate, "addUserToSolutionACL: solution {}, user {}", solutionId, userId);
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
		Date beginDate = new Date();
		if (solutionRepository.findOne(solutionId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + solutionId, null);
		} else if (userRepository.findOne(userId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + userId, null);
		} else {
			solUserAccMapRepository.delete(new MLPSolUserAccMap.SolUserAccessMapPK(solutionId, userId));
			logger.audit(beginDate, "dropUserFromSolutionACL: solution {}, user {}", solutionId, userId);
			return new SuccessTransport(HttpServletResponse.SC_OK, null);
		}
	}

	/**
	 * @param userId
	 *            Path parameter with user ID
	 * @param pageable
	 *            Page and sort criteria. Spring sets to page 0 of size 20 if client
	 *            sends nothing.
	 * @param response
	 *            HttpServletResponse
	 * @return A usage if found, an error otherwise.
	 */
	@ApiOperation(value = "Gets a page of solutions with the specified user in the ACL, optionally sorted on fields.", response = MLPSolution.class, responseContainer = "List")
	@RequestMapping(value = CCDSConstants.USER_PATH + "/{userId}/"
			+ CCDSConstants.ACCESS_PATH, method = RequestMethod.GET)
	@ResponseBody
	public Page<MLPSolution> getAccessibleSolutions(@PathVariable("userId") String userId, Pageable pageable,
			HttpServletResponse response) {
		Date beginDate = new Date();
		Page<MLPSolution> result = solUserAccMapRepository.getSolutionsForUser(userId, pageable);
		logger.audit(beginDate, "getAccessibleSolutions: user {}", userId);
		return result;
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
		Date beginDate = new Date();
		Iterable<MLPSolutionValidation> items = solutionValidationRepository.findBySolutionIdAndRevisionId(solutionId,
				revisionId);
		if (items == null || !items.iterator().hasNext()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST,
					NO_ENTRY_WITH_ID + solutionId + ", " + revisionId, null);
		}
		logger.audit(beginDate, "getListOfSolutionValidations: solutionId {} revisionId {}", solutionId, revisionId);
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
		Date beginDate = new Date();
		if (solutionRepository.findOne(solutionId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + solutionId, null);
		}
		if (solutionRevisionRepository.findOne(revisionId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + revisionId, null);
		}
		try {
			// Validate enum codes
			if (sv.getValidationStatusCode() != null)
				super.validateCode(sv.getValidationStatusCode(), CodeNameType.VALIDATION_STATUS);
			// type is required
			super.validateCode(sv.getValidationTypeCode(), CodeNameType.VALIDATION_TYPE);
			// Use path IDs
			sv.setSolutionId(solutionId);
			sv.setRevisionId(revisionId);
			sv.setTaskId(taskId);
			Object result = solutionValidationRepository.save(sv);
			response.setStatus(HttpServletResponse.SC_CREATED);
			response.setHeader(HttpHeaders.LOCATION,
					CCDSConstants.SOLUTION_PATH + "/" + solutionId + "/" + CCDSConstants.REVISION_PATH + "/"
							+ revisionId + "/" + CCDSConstants.VALIDATION_PATH + "/" + taskId);
			logger.audit(beginDate, "createSolutionValidation: solutionId {} revisionId {} taskId {}", solutionId,
					revisionId, taskId);
			return result;
		} catch (Exception ex) {
			Exception cve = findConstraintViolationException(ex);
			logger.warn(EELFLoggerDelegate.errorLogger, "createSolutionValidation", cve.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "createSolutionValidation failed", cve);
		}
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
		Date beginDate = new Date();
		// Get the existing one
		SolutionValidationPK pk = new SolutionValidationPK(solutionId, revisionId, taskId);
		MLPSolutionValidation existing = solutionValidationRepository.findOne(pk);
		if (existing == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + pk, null);
		}
		try {
			// Validate enum codes
			if (sv.getValidationStatusCode() != null)
				super.validateCode(sv.getValidationStatusCode(), CodeNameType.VALIDATION_STATUS);
			// type is required
			super.validateCode(sv.getValidationTypeCode(), CodeNameType.VALIDATION_TYPE);
			// Use path IDs
			sv.setSolutionId(solutionId);
			sv.setRevisionId(revisionId);
			sv.setTaskId(taskId);
			solutionValidationRepository.save(sv);
			logger.audit(beginDate, "updateSolutionValidation: solutionId {} revisionId {} taskId {}", solutionId,
					revisionId, taskId);
			return new SuccessTransport(HttpServletResponse.SC_OK, null);
		} catch (Exception ex) {
			Exception cve = findConstraintViolationException(ex);
			logger.warn(EELFLoggerDelegate.errorLogger, "updateSolutionValidation", cve.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "updateSolutionValidation failed", cve);
		}
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
		Date beginDate = new Date();
		try {
			SolutionValidationPK pk = new SolutionValidationPK(solutionId, revisionId, taskId);
			solutionValidationRepository.delete(pk);
			logger.audit(beginDate, "deleteSolutionValidation: solutionId {} revisionId {} taskId {}", solutionId,
					revisionId, taskId);
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
	 *            Page and sort criteria. Spring sets to page 0 of size 20 if client
	 *            sends nothing.
	 * @param response
	 *            HttpServletResponse
	 * @return Page of deployments
	 */
	@ApiOperation(value = "Gets the deployments for the specified solution and revision IDs.", response = MLPSolutionDeployment.class, responseContainer = "Page")
	@RequestMapping(value = "/{solutionId}/" + CCDSConstants.REVISION_PATH + "/{revisionId}/"
			+ CCDSConstants.DEPLOY_PATH, method = RequestMethod.GET)
	@ResponseBody
	public Object getSolutionDeployments(@PathVariable("solutionId") String solutionId,
			@PathVariable("revisionId") String revisionId, Pageable pageRequest, HttpServletResponse response) {
		Date beginDate = new Date();
		if (solutionRepository.findOne(solutionId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + solutionId, null);
		}
		if (solutionRevisionRepository.findOne(revisionId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + revisionId, null);
		}
		Page<MLPSolutionDeployment> da = solutionDeploymentRepository.findBySolutionIdAndRevisionId(solutionId,
				revisionId, pageRequest);
		logger.audit(beginDate, "getSolutionDeployments: solutionId {} revisionId {}", solutionId, revisionId);
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
	 *            Page and sort criteria. Spring sets to page 0 of size 20 if client
	 *            sends nothing.
	 * @param response
	 *            HttpServletResponse
	 * @return Page of deployments
	 */
	@ApiOperation(value = "Gets the deployments for the specified solution, revision and user IDs.", response = MLPSolutionDeployment.class, responseContainer = "Page")
	@RequestMapping(value = "/{solutionId}/" + CCDSConstants.REVISION_PATH + "/{revisionId}/" + CCDSConstants.USER_PATH
			+ "/{userId}/" + CCDSConstants.DEPLOY_PATH, method = RequestMethod.GET)
	@ResponseBody
	public Object getUserSolutionRevisionDeployments(@PathVariable("solutionId") String solutionId,
			@PathVariable("revisionId") String revisionId, @PathVariable("userId") String userId, Pageable pageRequest,
			HttpServletResponse response) {
		Date beginDate = new Date();
		if (solutionRepository.findOne(solutionId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + solutionId, null);
		}
		if (solutionRevisionRepository.findOne(revisionId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + revisionId, null);
		}
		if (userRepository.findOne(userId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + userId, null);
		}
		Page<MLPSolutionDeployment> da = solutionDeploymentRepository.findBySolutionIdAndRevisionIdAndUserId(solutionId,
				revisionId, userId, pageRequest);
		logger.audit(beginDate, "getUserSolutionRevisionDeployments: solutionId {} revisionId {} userId {}", solutionId,
				revisionId, userId);
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
			+ CCDSConstants.DEPLOY_PATH, method = RequestMethod.POST)
	@ResponseBody
	public Object createSolutionDeployment(@PathVariable("solutionId") String solutionId,
			@PathVariable("revisionId") String revisionId, @RequestBody MLPSolutionDeployment sd,
			HttpServletResponse response) {
		Date beginDate = new Date();
		if (solutionRepository.findOne(solutionId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + solutionId, null);
		}
		if (solutionRevisionRepository.findOne(revisionId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + revisionId, null);
		}
		if (userRepository.findOne(sd.getUserId()) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + sd.getUserId(), null);
		}
		try {
			// Validate enum code
			super.validateCode(sd.getDeploymentStatusCode(), CodeNameType.DEPLOYMENT_STATUS);
			// Validate ID if present
			String id = sd.getDeploymentId();
			if (id != null) {
				UUID.fromString(id);
				if (solutionDeploymentRepository.findOne(id) != null) {
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "Deployment exists with ID " + id);
				}
			}
			// Create a new row
			// Use path IDs
			sd.setSolutionId(solutionId);
			sd.setRevisionId(revisionId);
			// do NOT null out the deployment ID
			Object result = solutionDeploymentRepository.save(sd);
			response.setStatus(HttpServletResponse.SC_CREATED);
			response.setHeader(HttpHeaders.LOCATION,
					CCDSConstants.SOLUTION_PATH + "/" + sd.getSolutionId() + "/" + CCDSConstants.REVISION_PATH
							+ revisionId + CCDSConstants.DEPLOY_PATH + "/" + sd.getDeploymentId());
			logger.audit(beginDate, "createSolutionDeployment: solutionId {} revisionId {}", solutionId, revisionId);
			return result;
		} catch (Exception ex) {
			logger.error(EELFLoggerDelegate.errorLogger, "createSolutionDeployment failed", ex);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return new ErrorTransport(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					ex.getCause() != null ? ex.getCause().getMessage() : "createSolutionDeployment failed", ex);
		}
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
			+ CCDSConstants.DEPLOY_PATH + "/{deploymentId}", method = RequestMethod.PUT)
	@ResponseBody
	public Object updateSolutionDeployment(@PathVariable("solutionId") String solutionId,
			@PathVariable("revisionId") String revisionId, @PathVariable("deploymentId") String deploymentId,
			@RequestBody MLPSolutionDeployment sd, HttpServletResponse response) {
		Date beginDate = new Date();
		if (solutionDeploymentRepository.findOne(deploymentId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + deploymentId, null);
		}
		try {
			// Create a new row
			// Use path IDs
			sd.setSolutionId(solutionId);
			sd.setRevisionId(revisionId);
			sd.setDeploymentId(deploymentId);
			solutionDeploymentRepository.save(sd);
			logger.audit(beginDate, "updateSolutionDeployment: solutionId {} revisionId {} deploymentId {}", solutionId,
					revisionId, deploymentId);
			return new SuccessTransport(HttpServletResponse.SC_OK, null);
		} catch (Exception ex) {
			Exception cve = findConstraintViolationException(ex);
			logger.warn(EELFLoggerDelegate.errorLogger, "updateSolutionDeployment", cve.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "updateSolutionDeployment failed", cve);
		}
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
			+ CCDSConstants.DEPLOY_PATH + "/{deploymentId}", method = RequestMethod.DELETE)
	@ResponseBody
	public MLPTransportModel deleteSolutionDeployment(@PathVariable("solutionId") String solutionId,
			@PathVariable("revisionId") String revisionId, @PathVariable("deploymentId") String deploymentId,
			HttpServletResponse response) {
		Date beginDate = new Date();
		try {
			solutionDeploymentRepository.delete(deploymentId);
			logger.audit(beginDate, "updateSolutionDeployment: solutionId {} revisionId {} deploymentId {}", solutionId,
					revisionId, deploymentId);
			return new SuccessTransport(HttpServletResponse.SC_OK, null);
		} catch (Exception ex) {
			// e.g., EmptyResultDataAccessException is NOT an internal server error
			logger.warn(EELFLoggerDelegate.errorLogger, "deleteSolutionDeployment failed", ex.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "deleteSolutionDeployment failed", ex);
		}
	}

	/**
	 * @param parentId
	 *            Parent solution ID
	 * @return List of child solution IDs
	 */
	@ApiOperation(value = "Gets a list of child solution IDs used in the specified composite solution.", response = String.class, responseContainer = "List")
	@RequestMapping(value = "/{parentId}/" + CCDSConstants.COMP_PATH, method = RequestMethod.GET)
	@ResponseBody
	public Iterable<String> getCompositeSolutionMembers(@PathVariable("parentId") String parentId) {
		Date beginDate = new Date();
		Iterable<MLPCompSolMap> result = compSolMapRepository.findByParentId(parentId);
		List<String> children = new ArrayList<>();
		Iterator<MLPCompSolMap> kids = result.iterator();
		while (kids.hasNext())
			children.add(kids.next().getChildId());
		logger.audit(beginDate, "getCompositeSolutionMembers: parentId {}", parentId);
		return children;
	}

	/**
	 * @param parentId
	 *            parent solution ID
	 * @param childId
	 *            child solution ID to add
	 * @param response
	 *            HttpServletResponse
	 * @return Success indicator
	 */
	@ApiOperation(value = "Adds a child to the parent composite solution.", response = SuccessTransport.class)
	@RequestMapping(value = "/{parentId}/" + CCDSConstants.COMP_PATH + "/{childId}", method = RequestMethod.POST)
	@ResponseBody
	public Object addCompositeSolutionMember(@PathVariable("parentId") String parentId,
			@PathVariable("childId") String childId, HttpServletResponse response) {
		Date beginDate = new Date();
		if (solutionRepository.findOne(parentId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + parentId, null);
		} else if (solutionRepository.findOne(childId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + childId, null);
		} else {
			compSolMapRepository.save(new MLPCompSolMap(parentId, childId));
			logger.audit(beginDate, "addCompositeSolutionMember: parentId {} childId {}", parentId, childId);
			return new SuccessTransport(HttpServletResponse.SC_OK, null);
		}
	}

	/**
	 * @param parentId
	 *            parent solution ID
	 * @param childId
	 *            child solution ID to remove
	 * @param response
	 *            HttpServletResponse
	 * @return Success indicator
	 */
	@ApiOperation(value = "Drops a child from the parent composite solution.", response = SuccessTransport.class)
	@RequestMapping(value = "/{parentId}/" + CCDSConstants.COMP_PATH + "/{childId}", method = RequestMethod.DELETE)
	@ResponseBody
	public Object dropCompositeSolutionMember(@PathVariable("parentId") String parentId,
			@PathVariable("childId") String childId, HttpServletResponse response) {
		Date beginDate = new Date();
		if (solutionRepository.findOne(parentId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + parentId, null);
		} else if (solutionRepository.findOne(childId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + childId, null);
		} else {
			compSolMapRepository.delete(new MLPCompSolMap(parentId, childId));
			logger.audit(beginDate, "dropCompositeSolutionMember: parentId {} childId {}", parentId, childId);
			return new SuccessTransport(HttpServletResponse.SC_OK, null);
		}
	}

}
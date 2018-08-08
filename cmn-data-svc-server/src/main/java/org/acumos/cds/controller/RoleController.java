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
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.acumos.cds.CCDSConstants;
import org.acumos.cds.domain.MLPRole;
import org.acumos.cds.domain.MLPRoleFunction;
import org.acumos.cds.repository.RoleFunctionRepository;
import org.acumos.cds.repository.RoleRepository;
import org.acumos.cds.service.RoleSearchService;
import org.acumos.cds.transport.CountTransport;
import org.acumos.cds.transport.ErrorTransport;
import org.acumos.cds.transport.MLPTransportModel;
import org.acumos.cds.transport.SuccessTransport;
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

/**
 * Answers REST requests to get, add, update and delete roles.
 * 
 * https://stackoverflow.com/questions/942951/rest-api-error-return-good-practices
 */
@Controller
@RequestMapping(value = "/" + CCDSConstants.ROLE_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
public class RoleController extends AbstractController {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Autowired
	private RoleRepository roleRepository;
	@Autowired
	private RoleSearchService roleSearchService;
	@Autowired
	private RoleFunctionRepository roleFunctionRepository;

	/**
	 * @return CountTransport with count of roles
	 */
	@ApiOperation(value = "Gets the count of roles.", response = CountTransport.class)
	@RequestMapping(value = CCDSConstants.COUNT_PATH, method = RequestMethod.GET)
	@ResponseBody
	public CountTransport getRoleCount() {
		logger.info("getRoleCount");
		Long count = roleRepository.count();
		return new CountTransport(count);
	}

	/**
	 * @param pageable
	 *            Sort and page criteria
	 * @return List of artifacts, for serialization as JSON
	 */
	@ApiOperation(value = "Gets a page of roles, optionally sorted on fields.", response = MLPRole.class, responseContainer = "Page")
	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	public Page<MLPRole> getRoles(Pageable pageable) {
		logger.info("getRoles query {}", pageable);
		Page<MLPRole> result = roleRepository.findAll(pageable);
		return result;
	}

	/**
	 * @param queryParameters
	 *            Map of String (field name) to String (value) for restricting the
	 *            query
	 * @param pageRequest
	 *            Sort and page criteria
	 * @param response
	 *            HttpServletResponse
	 * @return List of roles, for serialization as JSON.
	 */
	@ApiOperation(value = "Searches for roles using the field name - field value pairs specified as query parameters. Defaults to and (conjunction); send junction query parameter = o for or (disjunction).", response = MLPRole.class, responseContainer = "Page")
	@RequestMapping(value = "/" + CCDSConstants.SEARCH_PATH, method = RequestMethod.GET)
	@ResponseBody
	public Object searchRoles(@RequestParam MultiValueMap<String, String> queryParameters, Pageable pageRequest,
			HttpServletResponse response) {
		logger.info("searchRoles query {}", queryParameters);
		cleanPageableParameters(queryParameters);
		List<String> junction = queryParameters.remove(CCDSConstants.JUNCTION_QUERY_PARAM);
		boolean isOr = junction != null && junction.size() == 1 && "o".equals(junction.get(0));
		if (queryParameters.size() == 0) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "Missing query", null);
		}
		try {
			Map<String, Object> convertedQryParm = convertQueryParameters(MLPRole.class, queryParameters);
			Object result = roleSearchService.findRoles(convertedQryParm, isOr, pageRequest);
			return result;
		} catch (Exception ex) {
			// e.g., EmptyResultDataAccessException is NOT an internal server error
			logger.warn("searchRoles failed: {}", ex.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST,
					ex.getCause() != null ? ex.getCause().getMessage() : "searchRoles failed", ex);
		}
	}

	/**
	 * @param roleId
	 *            Path parameter with row ID
	 * @param response
	 *            HttpServletResponse
	 * @return A role if found, an error otherwise.
	 */
	@ApiOperation(value = "Gets the role for the specified ID.", response = MLPRole.class)
	@RequestMapping(value = "/{roleId}", method = RequestMethod.GET)
	@ResponseBody
	public Object getRole(@PathVariable("roleId") String roleId, HttpServletResponse response) {
		logger.info("getRole roleId {}", roleId);
		MLPRole da = roleRepository.findOne(roleId);
		if (da == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + roleId, null);
		}
		return da;
	}

	/**
	 * @param role
	 *            Role to save. If no ID is set a new one will be generated; if an
	 *            ID value is set, it will be used if valid and not in table.
	 * @param response
	 *            HttpServletResponse
	 * @return model to be serialized as JSON
	 */
	@ApiOperation(value = "Creates a new role.", response = MLPRole.class)
	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public Object createRole(@RequestBody MLPRole role, HttpServletResponse response) {
		logger.info("createRole role {}", role);
		try {
			String id = role.getRoleId();
			if (id != null) {
				UUID.fromString(id);
				if (roleRepository.findOne(id) != null) {
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "ID exists: " + id);
				}
			}
			// Create a new row
			Object result = roleRepository.save(role);
			response.setStatus(HttpServletResponse.SC_CREATED);
			// This is a hack to create the location path.
			response.setHeader(HttpHeaders.LOCATION, CCDSConstants.ROLE_PATH + "/" + role.getRoleId());
			return result;
		} catch (Exception ex) {
			// e.g., EmptyResultDataAccessException is NOT an internal server error
			Exception cve = findConstraintViolationException(ex);
			logger.warn("createRole failed: {}", cve.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "createRole failed", cve);
		}
	}

	/**
	 * @param roleId
	 *            Path parameter with the row ID
	 * @param role
	 *            Role to be updated
	 * @param response
	 *            HttpServletResponse
	 * @return Transport model with success or failure
	 */
	@ApiOperation(value = "Updates a role.", response = SuccessTransport.class)
	@RequestMapping(value = "/{roleId}", method = RequestMethod.PUT)
	@ResponseBody
	public Object updateRole(@PathVariable("roleId") String roleId, @RequestBody MLPRole role,
			HttpServletResponse response) {
		logger.info("updateRole roleId {}", roleId);
		// Get the existing one
		MLPRole existing = roleRepository.findOne(roleId);
		if (existing == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + roleId, null);
		}
		try {
			// Use the path-parameter id; don't trust the one in the object
			role.setRoleId(roleId);
			// Update the existing row
			roleRepository.save(role);
			return new SuccessTransport(HttpServletResponse.SC_OK, null);
		} catch (Exception ex) {
			// e.g., EmptyResultDataAccessException is NOT an internal server error
			Exception cve = findConstraintViolationException(ex);
			logger.warn("updateRole failed: {}", cve.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "updateRole failed", cve);
		}
	}

	/**
	 * @param roleId
	 *            Path parameter that identifies the instance
	 * @param response
	 *            HttpServletResponse
	 * @return Transport model with success or failure
	 */
	@ApiOperation(value = "Deletes a role.", response = SuccessTransport.class)
	@RequestMapping(value = "/{roleId}", method = RequestMethod.DELETE)
	@ResponseBody
	public MLPTransportModel deleteRole(@PathVariable("roleId") String roleId, HttpServletResponse response) {
		logger.info("deleteRole roleId {}", roleId);
		try {
			Iterable<MLPRoleFunction> fns = roleFunctionRepository.findByRoleId(roleId);
			if (fns != null)
				roleFunctionRepository.delete(fns);
			roleRepository.delete(roleId);
			return new SuccessTransport(HttpServletResponse.SC_OK, null);
		} catch (Exception ex) {
			// e.g., EmptyResultDataAccessException is NOT an internal server error
			logger.warn("deleteRole failed: {}", ex.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "deleteRole failed", ex);
		}
	}

	/**
	 * @param roleId
	 *            Path parameter with role ID
	 * @param response
	 *            HttpServletResponse
	 * @return List of role functions if the specified role is found; an error
	 *         otherwise.
	 */
	@ApiOperation(value = "Gets the functions for the specified role.", response = MLPRoleFunction.class, responseContainer = "List")
	@RequestMapping(value = "/{roleId}/" + CCDSConstants.FUNCTION_PATH, method = RequestMethod.GET)
	@ResponseBody
	public Object getListOfRoleFunc(@PathVariable("roleId") String roleId, HttpServletResponse response) {
		logger.info("getListOfRoleFunc roleId {}", roleId);
		if (roleRepository.findOne(roleId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + roleId, null);
		}
		Object result = roleFunctionRepository.findByRoleId(roleId);
		return result;
	}

	/**
	 * @param roleId
	 *            Path parameter with role ID, which is ignored.
	 * @param functionId
	 *            Path parameter with role function ID
	 * @param response
	 *            HttpServletResponse
	 * @return A role if found, an error otherwise.
	 */
	@ApiOperation(value = "Gets the role function for the specified role and function IDs.", response = MLPRoleFunction.class)
	@RequestMapping(value = "/{roleId}/" + CCDSConstants.FUNCTION_PATH + "/{functionId}", method = RequestMethod.GET)
	@ResponseBody
	public Object getRoleFunc(@PathVariable("roleId") String roleId, @PathVariable("functionId") String functionId,
			HttpServletResponse response) {
		logger.info("getRoleFunc roleId {} functionId {}", roleId, functionId);
		MLPRoleFunction rf = roleFunctionRepository.findOne(functionId);
		if (rf == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + functionId, null);
		}
		return rf;
	}

	/**
	 * @param roleId
	 *            role ID
	 * @param roleFunction
	 *            role function to save
	 * @param response
	 *            HttpServletResponse
	 * @return Solution revision model for serialization as JSON
	 */
	@ApiOperation(value = "Creates a new role function.", response = MLPRoleFunction.class)
	@RequestMapping(value = "/{roleId}/" + CCDSConstants.FUNCTION_PATH, method = RequestMethod.POST)
	@ResponseBody
	public Object createRoleFunc(@PathVariable("roleId") String roleId, @RequestBody MLPRoleFunction roleFunction,
			HttpServletResponse response) {
		logger.info("createRoleFunc: function {}", roleFunction);
		if (roleRepository.findOne(roleId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + roleId, null);
		}
		Object result;
		try {
			// Null out any existing ID to get an auto-generated ID
			roleFunction.setRoleFunctionId(null);
			// Add the solution, which the client cannot provide
			roleFunction.setRoleId(roleId);
			// Create a new row
			result = roleFunctionRepository.save(roleFunction);
		} catch (Exception ex) {
			// e.g., EmptyResultDataAccessException is NOT an internal server error
			Exception cve = findConstraintViolationException(ex);
			logger.warn("createRoleFunc failed: {}", cve.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			result = new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "createRoleFunc failed", cve);
		}
		return result;
	}

	/**
	 * @param roleId
	 *            role ID
	 * @param functionId
	 *            function ID
	 * @param roleFunction
	 *            item to update
	 * @param response
	 *            HttpServletResponse
	 * @return Status message
	 */
	@ApiOperation(value = "Updates an existing role function.", response = SuccessTransport.class)
	@RequestMapping(value = "/{roleId}/" + CCDSConstants.FUNCTION_PATH + "/{functionId}", method = RequestMethod.PUT)
	@ResponseBody
	public Object updateRoleFunc(@PathVariable("roleId") String roleId, @PathVariable("functionId") String functionId,
			@RequestBody MLPRoleFunction roleFunction, HttpServletResponse response) {
		logger.info("updateRoleFunc roleId {} functionId {}", roleId, functionId);
		if (roleRepository.findOne(roleId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + roleId, null);
		}
		if (roleFunctionRepository.findOne(functionId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + functionId, null);
		}
		try {
			// Use the validated values
			roleFunction.setRoleFunctionId(functionId);
			roleFunction.setRoleId(roleId);
			// Update
			roleFunctionRepository.save(roleFunction);
			return new SuccessTransport(HttpServletResponse.SC_OK, null);
		} catch (Exception ex) {
			// e.g., EmptyResultDataAccessException is NOT an internal server error
			Exception cve = findConstraintViolationException(ex);
			logger.warn("updateRoleFunc failed: {}", cve.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "updateRoleFunc failed", cve);
		}
	}

	/**
	 * 
	 * @param roleId
	 *            Path parameter that identifies the role
	 * @param functionId
	 *            Path parameter that identifies the role function
	 * @param response
	 *            HttpServletResponse
	 * @return Transport model with success or failure
	 */
	@ApiOperation(value = "Deletes a role function.", response = SuccessTransport.class)
	@RequestMapping(value = "/{roleId}/" + CCDSConstants.FUNCTION_PATH + "/{functionId}", method = RequestMethod.DELETE)
	@ResponseBody
	public MLPTransportModel deleteRoleFunc(@PathVariable("roleId") String roleId,
			@PathVariable("functionId") String functionId, HttpServletResponse response) {
		logger.info("deleteRoleFunc roleId {} funcId {}", roleId, functionId);
		try {
			roleFunctionRepository.delete(functionId);
			return new SuccessTransport(HttpServletResponse.SC_OK, null);
		} catch (Exception ex) {
			// e.g., EmptyResultDataAccessException is NOT an internal server error
			logger.warn("deleteRoleFunc failed: {}", ex.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "deleteRoleFunc failed", ex);
		}
	}

}

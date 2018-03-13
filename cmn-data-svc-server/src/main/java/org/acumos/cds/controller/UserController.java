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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletResponse;

import org.acumos.cds.CCDSConstants;
import org.acumos.cds.CodeNameType;
import org.acumos.cds.domain.MLPNotifUserMap;
import org.acumos.cds.domain.MLPPasswordChangeRequest;
import org.acumos.cds.domain.MLPRole;
import org.acumos.cds.domain.MLPSolution;
import org.acumos.cds.domain.MLPSolutionDeployment;
import org.acumos.cds.domain.MLPSolutionFavorite;
import org.acumos.cds.domain.MLPSolutionFavorite.SolutionFavoritePK;
import org.acumos.cds.domain.MLPUser;
import org.acumos.cds.domain.MLPUserLoginProvider;
import org.acumos.cds.domain.MLPUserLoginProvider.UserLoginProviderPK;
import org.acumos.cds.domain.MLPUserNotifPref;
import org.acumos.cds.domain.MLPUserNotification;
import org.acumos.cds.domain.MLPUserRoleMap;
import org.acumos.cds.repository.NotifUserMapRepository;
import org.acumos.cds.repository.NotificationRepository;
import org.acumos.cds.repository.RoleRepository;
import org.acumos.cds.repository.SolutionDeploymentRepository;
import org.acumos.cds.repository.SolutionFavoriteRepository;
import org.acumos.cds.repository.SolutionRepository;
import org.acumos.cds.repository.UserLoginProviderRepository;
import org.acumos.cds.repository.UserNotificationPreferenceRepository;
import org.acumos.cds.repository.UserRepository;
import org.acumos.cds.repository.UserRoleMapRepository;
import org.acumos.cds.service.UserSearchService;
import org.acumos.cds.transport.CountTransport;
import org.acumos.cds.transport.ErrorTransport;
import org.acumos.cds.transport.LoginTransport;
import org.acumos.cds.transport.MLPTransportModel;
import org.acumos.cds.transport.SuccessTransport;
import org.acumos.cds.transport.UsersRoleRequest;
import org.acumos.cds.util.EELFLoggerDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.bcrypt.BCrypt;
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
 * Answers REST requests to get, add, update and delete user instances.
 * 
 * Data at rest is hashed; data in flight is not. So an inbound JSON request has
 * a clear-text password.
 * 
 * https://stackoverflow.com/questions/942951/rest-api-error-return-good-
 * practices
 */
@Controller
@RequestMapping("/" + CCDSConstants.USER_PATH)
public class UserController extends AbstractController {

	private static final EELFLoggerDelegate logger = EELFLoggerDelegate.getLogger(UserController.class);

	@Autowired
	private EntityManager entityManager;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private UserSearchService userSearchService;
	@Autowired
	private RoleRepository roleRepository;
	@Autowired
	private UserRoleMapRepository userRoleMapRepository;
	@Autowired
	private UserLoginProviderRepository userLoginProviderRepository;
	@Autowired
	private NotificationRepository notificationRepository;
	@Autowired
	private NotifUserMapRepository notifUserMapRepository;
	@Autowired
	private UserNotificationPreferenceRepository notificationPreferenceRepository;
	@Autowired
	private SolutionFavoriteRepository solutionFavoriteRepository;
	@Autowired
	private SolutionRepository solutionRepository;
	@Autowired
	private SolutionDeploymentRepository solutionDeploymentRepository;

	/**
	 * @return Model that maps String to Object, for serialization as JSON
	 */
	@ApiOperation(value = "Gets the count of users.", response = CountTransport.class)
	@RequestMapping(value = CCDSConstants.COUNT_PATH, method = RequestMethod.GET)
	@ResponseBody
	public CountTransport getUserCount() {
		Long count = userRepository.count();
		return new CountTransport(count);
	}

	/**
	 * @param login
	 *            Body with name and clear-text password
	 * @param response
	 *            HttpServletResponse
	 * @return A user if found, an error otherwise.
	 */
	@ApiOperation(value = "Checks the specified credentials.  The supplied login name is matched against the user's login name and email fields.  Returns the user object if found; bad request if no match is found.", response = MLPUser.class)
	@RequestMapping(value = "/" + CCDSConstants.LOGIN_PATH, method = RequestMethod.POST)
	@ResponseBody
	public Object login(@RequestBody LoginTransport login, HttpServletResponse response) {
		if (login.getName() == null || login.getName().trim().length() == 0 || login.getPass() == null
				|| login.getPass().trim().length() == 0) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "Missing login name and/or password");
		}
		Object result;
		MLPUser user = userRepository.findByLoginOrEmail(login.getName());
		boolean passwordMatches = user != null //
				&& BCrypt.checkpw(login.getPass(), user.getLoginHash());
		if (user == null || !passwordMatches) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			result = new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "No match for credentials", null);
		} else {
			// detach from Hibernate and wipe hash
			entityManager.detach(user);
			user.setLoginHash(null);
			result = user;
		}
		return result;
	}

	/**
	 * @param pageable
	 *            Sort and page criteria
	 * @return List of artifacts, for serialization as JSON
	 */
	@ApiOperation(value = "Gets a page of users, optionally sorted on fields.", response = MLPUser.class, responseContainer = "Page")
	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	public Page<MLPUser> getUsers(Pageable pageable) {
		Page<MLPUser> page = userRepository.findAll(pageable);
		for (MLPUser user : page.getContent()) {
			// detach from Hibernate
			entityManager.detach(user);
			// wipe password
			user.setLoginHash(null);
		}
		return page;
	}

	/**
	 * @param term
	 *            Search term used for partial match ("like")
	 * @param pageable
	 *            Sort and page criteria
	 * @return List of users
	 */
	@ApiOperation(value = "Searches for users with names that contain the search term.", response = MLPUser.class, responseContainer = "Page")
	@RequestMapping(value = "/" + CCDSConstants.LIKE_PATH, method = RequestMethod.GET)
	@ResponseBody
	public Page<MLPUser> likeUsers(@RequestParam(CCDSConstants.TERM_PATH) String term, Pageable pageable) {
		Page<MLPUser> page = userRepository.findBySearchTerm(term, pageable);
		for (MLPUser user : page.getContent()) {
			// detach from Hibernate
			entityManager.detach(user);
			// wipe password
			user.setLoginHash(null);
		}
		return page;
	}

	/**
	 * @param queryParameters
	 *            Map of String (field name) to String (value) for restricting the
	 *            query.
	 * @param pageable
	 *            Sort and page criteria
	 * @param response
	 *            HttpServletResponse
	 * @return Page of users, for serialization as JSON.
	 */
	@ApiOperation(value = "Searches for users using the field name - field value pairs specified as query parameters. Defaults to and (conjunction); send junction query parameter = o for or (disjunction).", response = MLPUser.class, responseContainer = "Page")
	@RequestMapping(value = "/" + CCDSConstants.SEARCH_PATH, method = RequestMethod.GET)
	@ResponseBody
	public Object searchUsers(@RequestParam MultiValueMap<String, String> queryParameters, Pageable pageable,
			HttpServletResponse response) {
		cleanPageableParameters(queryParameters);
		List<String> junction = queryParameters.remove(CCDSConstants.JUNCTION_QUERY_PARAM);
		boolean isOr = junction != null && junction.size() == 1 && "o".equals(junction.get(0));
		if (queryParameters.size() == 0) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "Missing query", null);
		}
		try {
			Map<String, Object> convertedQryParm = convertQueryParameters(MLPUser.class, queryParameters);
			Page<MLPUser> userPage = userSearchService.findUsers(convertedQryParm, isOr, pageable);
			logger.debug(EELFLoggerDelegate.debugLogger, "searchUsers: size is {} ", userPage.getNumberOfElements());
			// Wipe login hash values
			Iterator<MLPUser> userIter = userPage.iterator();
			while (userIter.hasNext()) {
				MLPUser user = userIter.next();
				entityManager.detach(user);
				user.setLoginHash(null);
			}
			return userPage;
		} catch (Exception ex) {
			logger.warn(EELFLoggerDelegate.errorLogger, "searchUsers failed", ex);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST,
					ex.getCause() != null ? ex.getCause().getMessage() : "searchUsers failed", ex);
		}
	}

	/**
	 * @param userId
	 *            Path parameter with row ID
	 * @param response
	 *            HttpServletResponse
	 * @return A user if found, an error otherwise.
	 */
	@ApiOperation(value = "Gets the user for the specified ID.", response = MLPUser.class)
	@RequestMapping(value = "/{userId}", method = RequestMethod.GET)
	@ResponseBody
	public Object getUser(@PathVariable("userId") String userId, HttpServletResponse response) {
		MLPUser user = userRepository.findOne(userId);
		if (user == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + userId, null);
		}
		// detach from Hibernate and wipe password
		entityManager.detach(user);
		user.setLoginHash(null);
		return user;
	}

	/**
	 * @param user
	 *            User object to save. If no ID is set a new one will be generated;
	 *            if an ID value is set, it will be used if valid and not in table.
	 * @param response
	 *            HttpServletResponse
	 * @return user model to be serialized as JSON
	 */
	@ApiOperation(value = "Creates a new user.", response = MLPUser.class)
	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public Object createUser(@RequestBody MLPUser user, HttpServletResponse response) {
		logger.debug(EELFLoggerDelegate.debugLogger, "createUser: received object: {} ", user);
		Object result;
		try {
			String id = user.getUserId();
			if (id != null) {
				UUID.fromString(id);
				if (userRepository.findOne(id) != null) {
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					result = new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "ID exists: " + id);
					return result;
				}
			}
			// Password arrives in the clear in the hash field, so hash if
			// present
			if (user.getLoginHash() != null) {
				String pwHash = BCrypt.hashpw(user.getLoginHash(), BCrypt.gensalt());
				user.setLoginHash(pwHash);
			}
			// Create a new row
			MLPUser newUser = userRepository.save(user);
			response.setStatus(HttpServletResponse.SC_CREATED);
			// This is a hack to create the location path.
			response.setHeader(HttpHeaders.LOCATION, CCDSConstants.USER_PATH + "/" + newUser.getUserId());
			// ALSO send back the model for client convenience,
			// but first detach from Hibernate and wipe the hash
			entityManager.detach(newUser);
			newUser.setLoginHash(null);
			result = newUser;
		} catch (Exception ex) {
			Exception cve = findConstraintViolationException(ex);
			logger.warn(EELFLoggerDelegate.errorLogger, "createUser", cve.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			result = new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "createUser failed", cve);
		}
		return result;
	}

	/**
	 * @param userId
	 *            Path parameter with the row ID
	 * @param user
	 *            User data to be updated
	 * @param response
	 *            HttpServletResponse
	 * @return Transport model with success or failure
	 */
	@ApiOperation(value = "Updates a user.", response = SuccessTransport.class)
	@RequestMapping(value = "/{userId}", method = RequestMethod.PUT)
	@ResponseBody
	public Object updateUser(@PathVariable("userId") String userId, @RequestBody MLPUser user,
			HttpServletResponse response) {
		logger.debug(EELFLoggerDelegate.debugLogger, "update: received user {} ", user);
		// Get the existing one
		MLPUser existingUser = userRepository.findOne(userId);
		if (existingUser == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + userId, null);
		}
		MLPTransportModel result = null;
		try {
			// Use the path-parameter id; don't trust the one in the object
			user.setUserId(userId);
			// Hash password if present in request
			if (user.getLoginHash() != null) {
				String pwHash = BCrypt.hashpw(user.getLoginHash(), BCrypt.gensalt());
				user.setLoginHash(pwHash);
			} else {
				user.setLoginHash(existingUser.getLoginHash());
			}
			userRepository.save(user);
			result = new SuccessTransport(HttpServletResponse.SC_OK, null);
		} catch (Exception ex) {
			Exception cve = findConstraintViolationException(ex);
			logger.warn(EELFLoggerDelegate.errorLogger, "updateUser", cve.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			result = new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "updateUser failed", cve);
		}
		return result;
	}

	/**
	 * @param userId
	 *            Path parameter with the row ID
	 * @param changeRequest
	 *            contains old and new password
	 * @param response
	 *            HttpServletResponse
	 * @return Transport model with success or failure
	 */
	@ApiOperation(value = "Sets the user's password to the new value if the old value matches.", response = SuccessTransport.class)
	@RequestMapping(value = "/{userId}/" + CCDSConstants.CHPASS_PATH, method = RequestMethod.PUT)
	@ResponseBody
	public Object updatePassword(@PathVariable("userId") String userId,
			@RequestBody MLPPasswordChangeRequest changeRequest, HttpServletResponse response) {
		// Do not log the old/new password values!
		logger.debug(EELFLoggerDelegate.debugLogger, "updatePassword: received request for user {}", userId);
		// Get the existing user
		MLPUser existingUser = userRepository.findOne(userId);
		if (existingUser == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + userId, null);
		}
		// Reject empty passwords
		if (changeRequest.getNewLoginPass() == null || changeRequest.getNewLoginPass().length() == 0) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "Cannot set the password to empty", null);
		}
		MLPTransportModel result = null;
		try {
			final boolean bothNull = existingUser.getLoginHash() == null && changeRequest.getOldLoginPass() == null;
			final boolean notNullAndMatch = existingUser.getLoginHash() != null
					&& changeRequest.getOldLoginPass() != null
					&& BCrypt.checkpw(changeRequest.getOldLoginPass(), existingUser.getLoginHash());
			if (bothNull || notNullAndMatch) {
				String pwHash = BCrypt.hashpw(changeRequest.getNewLoginPass(), BCrypt.gensalt());
				existingUser.setLoginHash(pwHash);
			} else {
				// no match
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "The old password did not match", null);
			}
			userRepository.save(existingUser);
			result = new SuccessTransport(HttpServletResponse.SC_OK, null);
		} catch (Exception ex) {
			logger.error(EELFLoggerDelegate.errorLogger, "updatePassword failed", ex);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			result = new ErrorTransport(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "updatePassword failed", ex);
		}
		return result;
	}

	/**
	 * Originally this was declared void and accordingly returned nothing. But when
	 * used in SpringBoot, after invoking the method it would look for a ThymeLeaf
	 * template, fail to find it, then throw internal server error.
	 * 
	 * @param userId
	 *            Path parameter that identifies the user
	 * @param response
	 *            HttpServletResponse
	 * @return Transport model with success or failure
	 */
	@ApiOperation(value = "Deletes a user.", response = SuccessTransport.class)
	@RequestMapping(value = "/{userId}", method = RequestMethod.DELETE)
	@ResponseBody
	public MLPTransportModel deleteUser(@PathVariable("userId") String userId, HttpServletResponse response) {
		try {
			Iterable<MLPUserRoleMap> roles = userRoleMapRepository.findByUserId(userId);
			if (roles != null)
				userRoleMapRepository.delete(roles);
			Iterable<MLPUserLoginProvider> logins = userLoginProviderRepository.findByUserId(userId);
			if (logins != null)
				userLoginProviderRepository.delete(logins);
			Iterable<MLPNotifUserMap> notifs = notifUserMapRepository.findByUserId(userId);
			if (notifs != null)
				notifUserMapRepository.delete(notifs);
			userRepository.delete(userId);
			return new SuccessTransport(HttpServletResponse.SC_OK, null);
		} catch (Exception ex) {
			// e.g., EmptyResultDataAccessException is NOT an internal server
			// error
			logger.warn(EELFLoggerDelegate.errorLogger, "deleteUser failed", ex.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "deleteUser failed", ex);
		}
	}

	/**
	 * @param roleId
	 *            Role ID
	 * @return Model that maps String to Object, for serialization as JSON
	 */
	@ApiOperation(value = "Gets the count of users in a role.", response = CountTransport.class)
	@RequestMapping(value = CCDSConstants.ROLE_PATH + "/{roleId}/"
			+ CCDSConstants.COUNT_PATH, method = RequestMethod.GET)
	@ResponseBody
	public CountTransport getRoleUsersCount(@PathVariable("roleId") String roleId) {
		Long count = userRoleMapRepository.getRoleUsersCount(roleId);
		return new CountTransport(count);
	}

	/**
	 * @param userId
	 *            User ID
	 * @return List of roles for the specified user
	 */
	@ApiOperation(value = "Gets roles for the specified user ID.", response = MLPRole.class, responseContainer = "List")
	@RequestMapping(value = "/{userId}/" + CCDSConstants.ROLE_PATH, method = RequestMethod.GET)
	@ResponseBody
	public Iterable<MLPRole> getRolesForUser(@PathVariable("userId") String userId) {
		return roleRepository.findByUser(userId);
	}

	/**
	 * @param userId
	 *            user ID
	 * @param roleId
	 *            role to add
	 * @param response
	 *            HttpServletResponse
	 * @return Success indicator
	 */
	@ApiOperation(value = "Adds a role to the user.", response = SuccessTransport.class)
	@RequestMapping(value = "/{userId}/" + CCDSConstants.ROLE_PATH + "/{roleId}", method = RequestMethod.POST)
	@ResponseBody
	public Object addUserRole(@PathVariable("userId") String userId, @PathVariable("roleId") String roleId,
			HttpServletResponse response) {
		logger.debug(EELFLoggerDelegate.debugLogger, "addUserRole: user {}, role {}", userId, roleId);
		if (userRepository.findOne(userId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + userId, null);
		} else if (roleRepository.findOne(roleId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + roleId, null);
		} else {
			userRoleMapRepository.save(new MLPUserRoleMap(userId, roleId));
			return new SuccessTransport(HttpServletResponse.SC_OK, null);
		}
	}

	/**
	 * @param userId
	 *            user ID
	 * @param roleIds
	 *            roles to assign
	 * @param response
	 *            HttpServletResponse
	 * @return Success indicator
	 */
	@ApiOperation(value = "Assigns the specified roles to the user after dropping any existing assignments.", response = SuccessTransport.class)
	@RequestMapping(value = "/{userId}/" + CCDSConstants.ROLE_PATH, method = RequestMethod.PUT)
	@ResponseBody
	public Object updateUserRoles(@PathVariable("userId") String userId, @RequestBody List<String> roleIds,
			HttpServletResponse response) {
		logger.debug(EELFLoggerDelegate.debugLogger, "updateUserRoles: user {}, roles {}", userId, roleIds);

		if (userRepository.findOne(userId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + userId, null);
		}
		for (String roleId : roleIds) {
			if (roleRepository.findOne(roleId) == null) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + roleId, null);
			}
		}

		// Remove all existing role assignments
		Iterable<MLPUserRoleMap> existing = userRoleMapRepository.findByUserId(userId);
		userRoleMapRepository.delete(existing);
		// Create new ones
		for (String roleId : roleIds)
			userRoleMapRepository.save(new MLPUserRoleMap(userId, roleId));

		return new SuccessTransport(HttpServletResponse.SC_OK, null);
	}

	/**
	 * @param userId
	 *            user ID
	 * @param roleId
	 *            role to drop
	 * @param response
	 *            HttpServletResponse
	 * @return Success indicator
	 */
	@ApiOperation(value = "Drops a role from the user.", response = SuccessTransport.class)
	@RequestMapping(value = "/{userId}/" + CCDSConstants.ROLE_PATH + "/{roleId}", method = RequestMethod.DELETE)
	@ResponseBody
	public Object dropUserRole(@PathVariable("userId") String userId, @PathVariable("roleId") String roleId,
			HttpServletResponse response) {
		logger.debug(EELFLoggerDelegate.debugLogger, "dropUserRole: user {}, role {}", userId, roleId);

		if (userRepository.findOne(userId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + userId, null);
		} else if (roleRepository.findOne(roleId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + roleId, null);
		} else {
			userRoleMapRepository.delete(new MLPUserRoleMap(userId, roleId));
			return new SuccessTransport(HttpServletResponse.SC_OK, null);
		}
	}

	/**
	 * @param roleId
	 *            role to add
	 * @param usersRoleRequest
	 *            UsersRoleRequest with action and list of user ID
	 * @param response
	 *            HttpServletResponse
	 * @return Success indicator
	 */
	@ApiOperation(value = "Adds or removes the specified role for multiple users.", response = SuccessTransport.class)
	@RequestMapping(value = CCDSConstants.ROLE_PATH + "/{roleId}", method = RequestMethod.PUT)
	@ResponseBody
	public Object addOrDropUsersInRole(@PathVariable("roleId") String roleId,
			@RequestBody UsersRoleRequest usersRoleRequest, HttpServletResponse response) {
		logger.debug(EELFLoggerDelegate.debugLogger, "addOrDropUsersInRole: role {}", roleId);
		// Validate entire request before making any change to avoid failing
		// midway
		if (roleRepository.findOne(roleId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + roleId, null);
		}
		if (usersRoleRequest.getUserIds().isEmpty()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "No users", null);
		}
		for (String userId : usersRoleRequest.getUserIds()) {
			if (userRepository.findOne(userId) == null) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + userId, null);
			}
		}
		for (String userId : usersRoleRequest.getUserIds()) {
			MLPUserRoleMap.UserRoleMapPK pk = new MLPUserRoleMap.UserRoleMapPK(userId, roleId);
			boolean exists = userRoleMapRepository.findOne(pk) != null;
			if (exists && usersRoleRequest.isAdd()) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "User already in role " + userId, null);
			} else if (!exists && !usersRoleRequest.isAdd()) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "User not in role " + userId, null);
			}
		}

		// Make the changes
		for (String userId : usersRoleRequest.getUserIds()) {
			if (usersRoleRequest.isAdd())
				userRoleMapRepository.save(new MLPUserRoleMap(userId, roleId));
			else
				userRoleMapRepository.delete(new MLPUserRoleMap.UserRoleMapPK(userId, roleId));
		}

		// Done
		return new SuccessTransport(HttpServletResponse.SC_OK, null);
	}

	/**
	 * @param userId
	 *            Path parameter with row ID
	 * @param providerCode
	 *            Provider code
	 * @param providerUserId
	 *            Provider user ID
	 * @param response
	 *            HttpServletResponse
	 * @return A user if found, an error otherwise.
	 */
	@ApiOperation(value = "Gets the login provider for the specified user, provider code and provider login.", response = MLPUserLoginProvider.class)
	@RequestMapping(value = "/{userId}/" + CCDSConstants.LOGIN_PROVIDER_PATH + "/{providerCode}/"
			+ CCDSConstants.LOGIN_PATH + "/{providerUserId}", method = RequestMethod.GET)
	@ResponseBody
	public Object getUserLoginProvider(@PathVariable("userId") String userId,
			@PathVariable("providerCode") String providerCode, @PathVariable("providerUserId") String providerUserId,
			HttpServletResponse response) {
		if (userRepository.findOne(userId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + userId, null);
		}
		// Build a key for fetch
		UserLoginProviderPK pk = new UserLoginProviderPK(userId, providerCode, providerUserId);
		MLPUserLoginProvider ulp = userLoginProviderRepository.findOne(pk);
		if (ulp == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + pk, null);
		}
		return ulp;
	}

	/**
	 * @param userId
	 *            Path parameter with row ID
	 * @param response
	 *            HttpServletResponse
	 * @return A user if found, an error otherwise.
	 */
	@ApiOperation(value = "Gets all login providers for the specified user.", response = MLPUserLoginProvider.class, responseContainer = "List")
	@RequestMapping(value = "/{userId}/" + CCDSConstants.LOGIN_PROVIDER_PATH, method = RequestMethod.GET)
	@ResponseBody
	public Object getAllLoginProviders(@PathVariable("userId") String userId, HttpServletResponse response) {
		if (userRepository.findOne(userId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + userId, null);
		}
		// this might be an empty list, which is ok.
		return userLoginProviderRepository.findByUserId(userId);
	}

	/**
	 * @param userId
	 *            User ID
	 * @param providerCode
	 *            Provider code
	 * @param providerUserId
	 *            Provider user ID
	 * @param ulp
	 *            user login provider object to save
	 * @param response
	 *            HttpServletResponse
	 * @return user login provider
	 */
	@ApiOperation(value = "Creates a user login provider entry.", response = MLPUserLoginProvider.class)
	@RequestMapping(value = "/{userId}/" + CCDSConstants.LOGIN_PROVIDER_PATH + "/{providerCode}/"
			+ CCDSConstants.LOGIN_PATH + "/{providerUserId}", method = RequestMethod.POST)
	@ResponseBody
	public Object createUserLoginProvider(@PathVariable("userId") String userId,
			@PathVariable("providerCode") String providerCode, @PathVariable("providerUserId") String providerUserId,
			@RequestBody MLPUserLoginProvider ulp, HttpServletResponse response) {
		logger.debug(EELFLoggerDelegate.debugLogger, "createUserLoginProvider: received object: {} ", ulp);
		// Validate args
		if (userRepository.findOne(userId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + userId, null);
		}
		Object result;
		try {
			// Validate enum code
			super.validateCode(ulp.getProviderCode(), CodeNameType.LOGIN_PROVIDER);
			// Use path IDs
			ulp.setUserId(userId);
			ulp.setProviderCode(providerCode);
			ulp.setProviderUserId(providerUserId);
			result = userLoginProviderRepository.save(ulp);
			response.setStatus(HttpServletResponse.SC_CREATED);
			// This is a hack to create the location path.
			response.setHeader(HttpHeaders.LOCATION,
					"/" + CCDSConstants.USER_PATH + "/" + userId + "/" + CCDSConstants.LOGIN_PROVIDER_PATH + "/"
							+ providerCode + "/" + CCDSConstants.LOGIN_PATH + "/" + providerUserId);
		} catch (Exception ex) {
			Exception cve = findConstraintViolationException(ex);
			logger.warn(EELFLoggerDelegate.errorLogger, "createUserLoginProvider", cve.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			result = new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "createUserLoginProvider failed", cve);
		}
		return result;
	}

	/**
	 * @param userId
	 *            User ID
	 * @param providerCode
	 *            Provider code
	 * @param providerUserId
	 *            Provider user ID
	 * @param ulp
	 *            user login provider object to save
	 * @param response
	 *            HttpServletResponse
	 * @return Transport model with success or failure
	 */
	@ApiOperation(value = "Updates the specified user login provider entry.", response = SuccessTransport.class)
	@RequestMapping(value = "/{userId}/" + CCDSConstants.LOGIN_PROVIDER_PATH + "/{providerCode}/"
			+ CCDSConstants.LOGIN_PATH + "/{providerUserId}", method = RequestMethod.PUT)
	@ResponseBody
	public Object updateUserLoginProvider(@PathVariable("userId") String userId,
			@PathVariable("providerCode") String providerCode, @PathVariable("providerUserId") String providerUserId,
			@RequestBody MLPUserLoginProvider ulp, HttpServletResponse response) {
		logger.debug(EELFLoggerDelegate.debugLogger, "update: received {} ", ulp);
		// Validate args
		if (userRepository.findOne(userId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + userId, null);
		}
		// Get the existing one
		// Build a key for fetch
		UserLoginProviderPK pk = new UserLoginProviderPK(userId, providerCode, providerUserId);
		MLPUserLoginProvider existing = userLoginProviderRepository.findOne(pk);
		if (existing == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + pk, null);
		}
		MLPTransportModel result = null;
		try {
			// Validate enum code
			super.validateCode(ulp.getProviderCode(), CodeNameType.LOGIN_PROVIDER);
			// Use path IDs
			ulp.setUserId(userId);
			ulp.setProviderCode(providerCode);
			ulp.setProviderUserId(providerUserId);
			userLoginProviderRepository.save(ulp);
			result = new SuccessTransport(HttpServletResponse.SC_OK, null);
		} catch (Exception ex) {
			Exception cve = findConstraintViolationException(ex);
			logger.warn(EELFLoggerDelegate.errorLogger, "updateUserLoginProvider", cve.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			result = new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "updateUserLoginProvider failed", cve);
		}
		return result;
	}

	/**
	 * @param userId
	 *            User ID
	 * @param providerCode
	 *            Provider code
	 * @param providerUserId
	 *            Provider user ID
	 * @param response
	 *            HttpServletResponse
	 * @return Transport model with success or failure
	 */
	@ApiOperation(value = "Deletes the specified user login provider entry.", response = SuccessTransport.class)
	@RequestMapping(value = "/{userId}/" + CCDSConstants.LOGIN_PROVIDER_PATH + "/{providerCode}/"
			+ CCDSConstants.LOGIN_PATH + "/{providerUserId}", method = RequestMethod.DELETE)
	@ResponseBody
	public MLPTransportModel deleteLoginProvider(@PathVariable("userId") String userId,
			@PathVariable("providerCode") String providerCode, @PathVariable("providerUserId") String providerUserId,
			HttpServletResponse response) {
		try {
			// Build a key for fetch
			UserLoginProviderPK pk = new UserLoginProviderPK(userId, providerCode, providerUserId);
			userLoginProviderRepository.delete(pk);
			return new SuccessTransport(HttpServletResponse.SC_OK, null);
		} catch (Exception ex) {
			// e.g., EmptyResultDataAccessException is NOT an internal server
			// error
			logger.warn(EELFLoggerDelegate.errorLogger, "deleteLoginProvider failed", ex.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "deleteLoginProvider failed", ex);
		}
	}

	/**
	 * @param userId
	 *            Path parameter with row ID
	 * @param pageRequest
	 *            Page and sort criteria
	 * @param response
	 *            HttpServletResponse
	 * @return A usage if found, an error otherwise.
	 */
	@ApiOperation(value = "Gets a page of solutions which are favorites for the specified user ID.", response = MLPSolution.class, responseContainer = "Page")
	@RequestMapping(value = "/{userId}/" + CCDSConstants.FAVORITE_PATH + "/"
			+ CCDSConstants.SOLUTION_PATH, method = RequestMethod.GET)
	@ResponseBody
	public Object getFavoriteSolutions(@PathVariable("userId") String userId, Pageable pageRequest,
			HttpServletResponse response) {
		if (userRepository.findOne(userId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + userId, null);
		}
		// this might be empty, which is ok.
		return solutionFavoriteRepository.findByUserId(userId, pageRequest);
	}

	/**
	 * @param solutionId
	 *            Solution ID
	 * @param userId
	 *            User ID
	 * @param sfv
	 *            solution favorite object
	 * @param response
	 *            HttpServletResponse
	 * @return solution favorite
	 */
	@ApiOperation(value = "Creates a new solution favorite record.", response = MLPSolutionFavorite.class)
	@RequestMapping(value = "/{userId}/" + CCDSConstants.FAVORITE_PATH + "/" + CCDSConstants.SOLUTION_PATH
			+ "/{solutionId}", method = RequestMethod.POST)
	@ResponseBody
	public Object createSolutionFavorite(@PathVariable("solutionId") String solutionId,
			@PathVariable("userId") String userId, @RequestBody MLPSolutionFavorite sfv, HttpServletResponse response) {
		logger.debug(EELFLoggerDelegate.debugLogger, "createSolutionFavorite: received object: {} ", sfv);
		if (solutionRepository.findOne(solutionId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + solutionId, null);
		}
		if (userRepository.findOne(userId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + userId, null);
		}
		Object result;
		try {
			// Create a new row
			// Use path IDs
			sfv.setSolutionId(solutionId);
			sfv.setUserId(userId);
			result = solutionFavoriteRepository.save(sfv);
			response.setStatus(HttpServletResponse.SC_CREATED);
			response.setHeader(HttpHeaders.LOCATION, CCDSConstants.USER_PATH + "/" + sfv.getUserId() + "/"
					+ CCDSConstants.FAVORITE_PATH + "/" + CCDSConstants.SOLUTION_PATH + "/" + sfv.getSolutionId());
		} catch (Exception ex) {
			logger.error(EELFLoggerDelegate.errorLogger, "createSolutionFavorite failed", ex);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			result = new ErrorTransport(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					ex.getCause() != null ? ex.getCause().getMessage() : "createSolutionFavorite failed", ex);
		}
		return result;
	}

	/**
	 * @param solutionId
	 *            solution ID
	 * @param userId
	 *            User ID
	 * @param response
	 *            HttpServletResponse
	 * @return Transport model with success
	 */
	@ApiOperation(value = "Deletes the specified solution favorite record.", response = SuccessTransport.class)
	@RequestMapping(value = "/{userId}/" + CCDSConstants.FAVORITE_PATH + "/" + CCDSConstants.SOLUTION_PATH
			+ "/{solutionId}", method = RequestMethod.DELETE)
	@ResponseBody
	public MLPTransportModel deleteSolutionFavorite(@PathVariable("solutionId") String solutionId,
			@PathVariable("userId") String userId, HttpServletResponse response) {
		try {
			// Build a key for fetch
			SolutionFavoritePK pk = new SolutionFavoritePK(solutionId, userId);
			solutionFavoriteRepository.delete(pk);
			return new SuccessTransport(HttpServletResponse.SC_OK, null);
		} catch (Exception ex) {
			// e.g., EmptyResultDataAccessException is NOT an internal server
			// error
			logger.warn(EELFLoggerDelegate.errorLogger, "deleteSolutionFavorite failed", ex.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "deleteSolutionFavorite failed", ex);
		}
	}

	/**
	 * @param userId
	 *            User ID
	 * @param pageable
	 *            Page request
	 * @return List of active notifications for the specified user, ignoring viewed
	 *         status
	 */
	@ApiOperation(value = "Gets active notifications for the specified user ID.", response = MLPUserNotification.class, responseContainer = "List")
	@RequestMapping(value = "/{userId}/" + CCDSConstants.NOTIFICATION_PATH, method = RequestMethod.GET)
	@ResponseBody
	public Iterable<MLPUserNotification> getActiveNotificationsForUser(@PathVariable("userId") String userId,
			Pageable pageable) {
		return notificationRepository.findActiveByUser(userId, pageable);
	}

	/**
	 * @param userId
	 *            User ID
	 * @param notificationId
	 *            Notification ID
	 * @param notifUserMap
	 *            map object
	 * @param response
	 *            HttpServletResponse
	 * @return Success indicator
	 */
	@ApiOperation(value = "Adds a user as a recipient of the notification.", response = SuccessTransport.class)
	@RequestMapping(value = "/{userId}/" + CCDSConstants.NOTIFICATION_PATH
			+ "/{notificationId}", method = RequestMethod.POST)
	@ResponseBody
	public Object addUserNotification(@PathVariable("userId") String userId,
			@PathVariable("notificationId") String notificationId, @RequestBody MLPNotifUserMap notifUserMap,
			HttpServletResponse response) {
		logger.debug(EELFLoggerDelegate.debugLogger, "addUserNotification: user {}, notif {}", userId, notificationId);
		if (userRepository.findOne(userId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + userId, null);
		} else if (notificationRepository.findOne(notificationId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + notificationId, null);
		} else {
			notifUserMap.setUserId(userId);
			notifUserMap.setNotificationId(notificationId);
			notifUserMapRepository.save(notifUserMap);
			return new SuccessTransport(HttpServletResponse.SC_OK, null);
		}
	}

	/**
	 * @param notificationId
	 *            Notification ID
	 * @param userId
	 *            User ID
	 * @param notifUserMap
	 *            map object
	 * @param response
	 *            HttpServletResponse
	 * @return Success indicator
	 */
	@ApiOperation(value = "Updates the notification-user map; e.g., to record the date when the user viewed the notification.", response = SuccessTransport.class)
	@RequestMapping(value = "/{userId}/" + CCDSConstants.NOTIFICATION_PATH
			+ "/{notificationId}", method = RequestMethod.PUT)
	@ResponseBody
	public Object updateUserNotification(@PathVariable("userId") String userId,
			@PathVariable("notificationId") String notificationId, @RequestBody MLPNotifUserMap notifUserMap,
			HttpServletResponse response) {
		logger.debug(EELFLoggerDelegate.debugLogger, "updateUserNotification: user {}, notif {}", userId,
				notificationId);
		if (userRepository.findOne(userId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + userId, null);
		}
		if (notificationRepository.findOne(notificationId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + notificationId, null);
		}
		Object result;
		try {
			notifUserMap.setNotificationId(notificationId);
			notifUserMap.setUserId(userId);
			notifUserMapRepository.save(notifUserMap);
			result = new SuccessTransport(HttpServletResponse.SC_OK, null);
		} catch (Exception ex) {
			logger.error(EELFLoggerDelegate.errorLogger, "updateUserNotification failed", ex);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			result = new ErrorTransport(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					ex.getCause() != null ? ex.getCause().getMessage() : "updateUserNotification failed", ex);
		}
		return result;
	}

	/**
	 * @param notificationId
	 *            Notification ID
	 * @param userId
	 *            User ID
	 * @param response
	 *            HttpServletResponse
	 * @return Success indicator
	 */
	@ApiOperation(value = "Drops a user as a recipient of the notification.", response = SuccessTransport.class)
	@RequestMapping(value = "/{userId}/" + CCDSConstants.NOTIFICATION_PATH
			+ "/{notificationId}", method = RequestMethod.DELETE)
	@ResponseBody
	public Object dropUserRecipient(@PathVariable("userId") String userId,
			@PathVariable("notificationId") String notificationId, HttpServletResponse response) {
		logger.debug(EELFLoggerDelegate.debugLogger, "dropUserRecipient: user {}, notif{}", userId, notificationId);
		if (userRepository.findOne(userId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + userId, null);
		} else if (notificationRepository.findOne(notificationId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + notificationId, null);
		} else {
			MLPNotifUserMap map = new MLPNotifUserMap(notificationId, userId);
			notifUserMapRepository.delete(map);
			return new SuccessTransport(HttpServletResponse.SC_OK, null);
		}
	}

	/**
	 * @param userNotifPrefId
	 *            Path parameter with row ID
	 * @param response
	 *            HttpServletResponse
	 * @return a user notification preference if found, an error otherwise.
	 */
	@ApiOperation(value = "Gets the user notification preference for the specified ID.", response = MLPUserNotifPref.class)
	@RequestMapping(value = CCDSConstants.NOTIFICATION_PREF_PATH + "/{userNotifPrefId}", method = RequestMethod.GET)
	@ResponseBody
	public Object getUserNotificationPreference(@PathVariable("userNotifPrefId") Long userNotifPrefId,
			HttpServletResponse response) {
		MLPUserNotifPref usrnp = notificationPreferenceRepository.findOne(userNotifPrefId);
		if (usrnp == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + userNotifPrefId, null);
		}
		return usrnp;
	}

	/**
	 * @param userId
	 *            User ID
	 * @return List of notification preferences for the specified user
	 */
	@ApiOperation(value = "Gets notification preferences for the specified user ID.", response = MLPUserNotifPref.class, responseContainer = "List")
	@RequestMapping(value = "/{userId}/" + CCDSConstants.NOTIFICATION_PREF_PATH, method = RequestMethod.GET)
	@ResponseBody
	public Iterable<MLPUserNotifPref> getNotificationPreferencesForUser(@PathVariable("userId") String userId) {
		return notificationPreferenceRepository.findByUserId(userId);
	}

	/**
	 * @param usrNotifPref
	 *            user notification preference to save. A new one will be generated;
	 * @param response
	 *            HttpServletResponse
	 * @return Entity on success; error on failure.
	 */
	@ApiOperation(value = "Creates a new user notification preference", response = MLPUserNotifPref.class)
	@RequestMapping(value = CCDSConstants.NOTIFICATION_PREF_PATH, method = RequestMethod.POST)
	@ResponseBody
	public Object createUserNotificationPreference(@RequestBody MLPUserNotifPref usrNotifPref,
			HttpServletResponse response) {
		logger.debug(EELFLoggerDelegate.debugLogger, "createUserNotificationPreference: received {} ", usrNotifPref);
		Object result;
		try {
			// Validate enum codes
			super.validateCode(usrNotifPref.getMsgSeverityCode(), CodeNameType.MESSAGE_SEVERITY);
			super.validateCode(usrNotifPref.getNotfDelvMechCode(), CodeNameType.NOTIFICATION_DELIVERY_MECHANISM);
			// Create a new row
			result = notificationPreferenceRepository.save(usrNotifPref);
			response.setStatus(HttpServletResponse.SC_CREATED);
			// This is a hack to create the location path.
			response.setHeader(HttpHeaders.LOCATION, CCDSConstants.USER_PATH + CCDSConstants.NOTIFICATION_PREF_PATH);
		} catch (Exception ex) {
			Exception cve = findConstraintViolationException(ex);
			logger.warn(EELFLoggerDelegate.errorLogger, "createUserNotificationPreference", cve.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			result = new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "createUserNotificationPreference failed",
					cve);
		}
		return result;
	}

	/**
	 * @param userNotifPrefId
	 *            Path parameter with the row ID
	 * @param usrNotifPref
	 *            stepResult data to be updated
	 * @param response
	 *            HttpServletResponse
	 * @return user notification preference that maps String to Object, for
	 *         serialization as JSON
	 */
	@ApiOperation(value = "Updates a user notification preference.", response = SuccessTransport.class)
	@RequestMapping(value = CCDSConstants.NOTIFICATION_PREF_PATH + "/{userNotifPrefId}", method = RequestMethod.PUT)
	@ResponseBody
	public Object updateUserNotificationPreference(@PathVariable("userNotifPrefId") Long userNotifPrefId,
			@RequestBody MLPUserNotifPref usrNotifPref, HttpServletResponse response) {
		logger.debug(EELFLoggerDelegate.debugLogger, "updateUserNotificationPreference: received {} ", usrNotifPref);
		// Get the existing one
		MLPUserNotifPref existing = notificationPreferenceRepository.findOne(userNotifPrefId);
		if (existing == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + userNotifPrefId, null);
		}
		MLPTransportModel result = null;
		try {
			// Validate enum codes
			super.validateCode(usrNotifPref.getMsgSeverityCode(), CodeNameType.MESSAGE_SEVERITY);
			super.validateCode(usrNotifPref.getNotfDelvMechCode(), CodeNameType.NOTIFICATION_DELIVERY_MECHANISM);
			// Use the path-parameter id; don't trust the one in the object
			usrNotifPref.setUserNotifPrefId(userNotifPrefId);
			// Update the existing row
			notificationPreferenceRepository.save(usrNotifPref);
			result = new SuccessTransport(HttpServletResponse.SC_OK, null);
		} catch (Exception ex) {
			Exception cve = findConstraintViolationException(ex);
			logger.warn(EELFLoggerDelegate.errorLogger, "updateUserNotificationPreference", cve.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			result = new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "updateUserNotificationPreference failed",
					cve);
		}
		return result;
	}

	/**
	 * 
	 * @param userNotifPrefId
	 *            Path parameter that identifies the instance
	 * @param response
	 *            HttpServletResponse
	 * @return Transport model with success or failure
	 */
	@ApiOperation(value = "Deletes the user notification preference with the specified ID.", response = SuccessTransport.class)
	@RequestMapping(value = CCDSConstants.NOTIFICATION_PREF_PATH + "/{userNotifPrefId}", method = RequestMethod.DELETE)
	@ResponseBody
	public MLPTransportModel deleteUserNotificationPreference(@PathVariable("userNotifPrefId") Long userNotifPrefId,
			HttpServletResponse response) {
		logger.debug(EELFLoggerDelegate.debugLogger, "deleteUserNotificationPreference: received {} ", userNotifPrefId);
		// Get the existing one
		try {
			notificationPreferenceRepository.delete(userNotifPrefId);
			return new SuccessTransport(HttpServletResponse.SC_OK, null);
		} catch (Exception ex) {
			// e.g., EmptyResultDataAccessException is NOT an internal server
			// error
			logger.warn(EELFLoggerDelegate.errorLogger, "deleteUserNotificationPreference", ex.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "deleteUserNotificationPreference failed",
					ex);
		}
	}

	/**
	 * @param userId
	 *            Path parameter with user ID
	 * @param pageRequest
	 *            Page and sort criteria
	 * @param response
	 *            HttpServletResponse
	 * @return Page of deployments
	 */
	@ApiOperation(value = "Gets the deployments for the specified user ID.", response = MLPSolutionDeployment.class, responseContainer = "Page")
	@RequestMapping(value = "/{userId}/" + CCDSConstants.DEPLOY_PATH, method = RequestMethod.GET)
	@ResponseBody
	public Object getUserDeployments(@PathVariable("userId") String userId, Pageable pageRequest,
			HttpServletResponse response) {
		Page<MLPSolutionDeployment> da = solutionDeploymentRepository.findByUserId(userId, pageRequest);
		if (da == null || !da.iterator().hasNext()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + userId, null);
		}
		return da;
	}

}

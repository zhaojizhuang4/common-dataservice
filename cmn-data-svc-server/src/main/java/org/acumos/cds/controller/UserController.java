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
import java.util.HashMap;
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
import org.acumos.cds.domain.MLPTag;
import org.acumos.cds.domain.MLPUser;
import org.acumos.cds.domain.MLPUserLoginProvider;
import org.acumos.cds.domain.MLPUserLoginProvider.UserLoginProviderPK;
import org.acumos.cds.domain.MLPUserRoleMap;
import org.acumos.cds.domain.MLPUserTagMap;
import org.acumos.cds.repository.NotifUserMapRepository;
import org.acumos.cds.repository.RoleRepository;
import org.acumos.cds.repository.SolutionDeploymentRepository;
import org.acumos.cds.repository.SolutionFavoriteRepository;
import org.acumos.cds.repository.SolutionRepository;
import org.acumos.cds.repository.UserLoginProviderRepository;
import org.acumos.cds.repository.UserRepository;
import org.acumos.cds.repository.UserRoleMapRepository;
import org.acumos.cds.repository.UserTagMapRepository;
import org.acumos.cds.service.UserSearchService;
import org.acumos.cds.transport.CountTransport;
import org.acumos.cds.transport.ErrorTransport;
import org.acumos.cds.transport.LoginTransport;
import org.acumos.cds.transport.MLPTransportModel;
import org.acumos.cds.transport.SuccessTransport;
import org.acumos.cds.transport.UsersRoleRequest;
import org.acumos.cds.util.ApiPageable;
import org.jasypt.util.text.BasicTextEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Controller;
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

/**
 * Answers REST requests to get, add, update and delete user instances.
 * 
 * Data at rest is hashed; data in flight is not. So an inbound JSON request has
 * a clear-text password.
 * 
 * https://stackoverflow.com/questions/942951/rest-api-error-return-good-practices
 */
@Controller
@RequestMapping(value = "/" + CCDSConstants.USER_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
public class UserController extends AbstractController {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	// Use reasonable defaults in case configuration is missing
	/**
	 * Number of failures to allow before blocking temporarily
	 */
	@Value("${login.failure.count:3}")
	private Integer loginFailureCount;
	/**
	 * Duration of temporary block, in seconds
	 */
	@Value("${login.failure.block.time:90}")
	private Integer loginFailureBlockTimeSec;
	/**
	 * Rudimentary encryption of API token
	 */
	@Value("${jasypt.encryptor.password:change-me-should-never-be-used}")
	private String jasyptEncryptorPassword;

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
	private NotifUserMapRepository notifUserMapRepository;
	@Autowired
	private SolutionFavoriteRepository solutionFavoriteRepository;
	@Autowired
	private SolutionRepository solutionRepository;
	@Autowired
	private SolutionDeploymentRepository solutionDeploymentRepository;
	@Autowired
	private UserTagMapRepository userTagMapRepository;

	@ApiOperation(value = "Gets the count of users.", response = CountTransport.class)
	@RequestMapping(value = CCDSConstants.COUNT_PATH, method = RequestMethod.GET)
	@ResponseBody
	public CountTransport getUserCount() {
		logger.info("getUserCount");
		Long count = userRepository.count();
		return new CountTransport(count);
	}

	/**
	 * Only for method checkUserCredentials
	 */
	private enum CredentialType {
		PASSWORD, API_TOKEN, VERIFY_TOKEN;
	}

	/**
	 * Checks specified user credentials against values in the database, which has
	 * hashes (not clear text) of sensitive information like password or token.
	 * Updates the record in all cases -- last login on success, failure count
	 * otherwise. Temporarily blocks user if more than a configurable number of
	 * login failures happen. Error messages reveal information to clients like
	 * existence of user; clients should NOT pass on to users. Reports much detail
	 * to the audit logger.
	 * 
	 * Should this track failures for non-existent users to avoid revealing user
	 * existence? Is keeping an in-memory hash of limited size a reasonable
	 * approach?
	 * 
	 * @param credentials
	 *            User name and authentication token
	 * @param credentialType
	 *            type of credential to check
	 * @param response
	 *            HttpServletResponse
	 */
	private Object checkUserCredentials(LoginTransport credentials, CredentialType credentialType,
			HttpServletResponse response) {
		if (credentials == null || credentials.getName() == null || credentials.getName().trim().isEmpty()
				|| credentials.getPass() == null || credentials.getPass().trim().isEmpty()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "Missing or empty credential(s)");
		}
		MLPUser user = userRepository.findByLoginOrEmail(credentials.getName());
		if (user == null || !user.isActive()) {
			logger.info("checkUserCredentials: unknown or inactve: {}", credentials.getName());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			// This reveals that the username does not exist
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST,
					"Failed to find active user " + credentials.getName());
		}
		if (user.getLoginFailCount() != null) {
			// This is a second or subsequent failure
			if (user.getLoginFailCount() < this.loginFailureCount) {
				logger.info("checkUserCredentials: user {} attempt after failure {}", user.getLoginName(),
						Integer.toString(user.getLoginFailCount()));
			} else {
				// Exceeds threshold. Defend against null fail date in db.
				long lastFailureTime = user.getLoginFailDate() == null ? new Date().getTime()
						: user.getLoginFailDate().getTime();
				long elapsedTimeSec = (new Date().getTime() - lastFailureTime) / 1000;
				long blockedTimeSec = this.loginFailureBlockTimeSec - elapsedTimeSec;
				if (blockedTimeSec > 0) {
					logger.info("checkUserCredentials: user {} blocked for {} sec", user.getLoginName(),
							Long.toString(blockedTimeSec));
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					// This reveals that the username exists
					return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST,
							"Repeated login failures, user blocked for " + Long.toString(blockedTimeSec) + " sec");
				} else {
					logger.info("checkUserCredentials: user {} block expired", user.getLoginName());
				}
			}
		}

		boolean match = false;
		if (credentialType == CredentialType.PASSWORD || credentialType == CredentialType.VERIFY_TOKEN) {
			String hash = credentialType == CredentialType.PASSWORD ? user.getLoginHash() : user.getVerifyTokenHash();
			match = BCrypt.checkpw(credentials.getPass(), hash);
		} else if (credentialType == CredentialType.API_TOKEN) {
			match = credentials.getPass().equals(decryptWithJasypt(user.getApiToken()));
		} else {
			throw new IllegalArgumentException("Unexpected credential type: " + credentialType);
		}

		if (!match) {
			// Record the failure
			logger.info("checkUserCredentials: user {} failed auth type {}", user.getLoginName(),
					credentialType.name());
			user.setLoginFailCount((short) (user.getLoginFailCount() == null ? 1 : user.getLoginFailCount() + 1));
			user.setLoginFailDate(new Date());
			userRepository.save(user);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "Failed to authenticate user", null);
		}
		// Success!
		if (user.getLoginFailCount() != null) {
			logger.info("checkUserCredentials: clearing login failures for user {}", user.getLoginName());
			user.setLoginFailCount(null);
			user.setLoginFailDate(null);
		}
		user.setLastLogin(new Date());
		userRepository.save(user);
		logger.info("checkUserCredentials: authenticated user {}", user.getLoginName());
		entityManager.detach(user);
		user.clearHashes();
		if (user.getApiToken() != null)
			user.setApiToken(decryptWithJasypt(user.getApiToken()));
		return user;
	}

	@ApiOperation(value = "Checks the specified credentials for full access. " //
			+ "Searches both login name and email fields for the specified name. " //
			+ "Returns the user object if an active user exists with the specified credentials; " //
			+ "returns bad request if no match is found. Imposes a delay on repeated failures.", //
			response = MLPUser.class)
	@ApiResponses({ @ApiResponse(code = 400, message = "Bad request", response = ErrorTransport.class) })
	@RequestMapping(value = "/" + CCDSConstants.LOGIN_PATH, method = RequestMethod.POST)
	@ResponseBody
	public Object loginUser(@RequestBody LoginTransport login, HttpServletResponse response) {
		logger.info("loginUser: user name {}", login.getName());
		return checkUserCredentials(login, CredentialType.PASSWORD, response);
	}

	@ApiOperation(value = "Checks the specified credentials for API access. "
			+ "Searches both login name and email fields for the specified name. "
			+ "Returns the user object if an active user exists with the specified credentials; "
			+ "returns bad request if no match is found. Imposes a delay on repeated failures.", //
			response = MLPUser.class)
	@ApiResponses({ @ApiResponse(code = 400, message = "Bad request", response = ErrorTransport.class) })
	@RequestMapping(value = "/" + CCDSConstants.LOGIN_API_PATH, method = RequestMethod.POST)
	@ResponseBody
	public Object loginApi(@RequestBody LoginTransport login, HttpServletResponse response) {
		logger.info("loginApi: user name {}", login.getName());
		return checkUserCredentials(login, CredentialType.API_TOKEN, response);
	}

	@ApiOperation(value = "Checks the specified credentials for verification. "
			+ "Searches both login name and email fields for the specified name. "
			+ "Returns the user object if an active user exists with the specified credentials; "
			+ "returns bad request if no match is found. Imposes a delay on repeated failures.", //
			response = MLPUser.class)
	@ApiResponses({ @ApiResponse(code = 400, message = "Bad request", response = ErrorTransport.class) })
	@RequestMapping(value = "/" + CCDSConstants.VERIFY_PATH, method = RequestMethod.POST)
	@ResponseBody
	public Object verifyUser(@RequestBody LoginTransport login, HttpServletResponse response) {
		logger.info("verifyUser: user name {}", login.getName());
		return checkUserCredentials(login, CredentialType.VERIFY_TOKEN, response);
	}

	@ApiOperation(value = "Changes the user's password to the new value if the user exists, is active, "
			+ "and the old password matches. Returns bad request if not found or not matched.", //
			response = SuccessTransport.class)
	@ApiResponses({ @ApiResponse(code = 400, message = "Bad request", response = ErrorTransport.class) })
	@RequestMapping(value = "/{userId}/" + CCDSConstants.CHPASS_PATH, method = RequestMethod.PUT)
	@ResponseBody
	public MLPTransportModel updatePassword(@PathVariable("userId") String userId,
			@RequestBody MLPPasswordChangeRequest changeRequest, HttpServletResponse response) {
		logger.info("updatePassword: userId {}", userId);
		// Existing password may be null, but reject empty new password
		if (changeRequest.getNewLoginPass() == null || changeRequest.getNewLoginPass().length() == 0) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "Missing or null new password", null);
		}
		// Get the existing user
		MLPUser user = userRepository.findOne(userId);
		if (user == null || !user.isActive()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST,
					"Failed to find active user with ID " + userId, null);
		}
		try {
			final boolean bothNull = user.getLoginHash() == null && changeRequest.getOldLoginPass() == null;
			final boolean notNullAndMatch = user.getLoginHash() != null && changeRequest.getOldLoginPass() != null
					&& BCrypt.checkpw(changeRequest.getOldLoginPass(), user.getLoginHash());
			if (bothNull || notNullAndMatch) {
				logger.info("updatePassword: Change password for user {}", user.getLoginName());
				final String pwHash = BCrypt.hashpw(changeRequest.getNewLoginPass(), BCrypt.gensalt());
				user.setLoginHash(pwHash);
				userRepository.save(user);
				logger.info("updatePassword: updated user {}", user.getLoginName());
				return new SuccessTransport(HttpServletResponse.SC_OK, null);
			} else {
				final String notMatched = "The old password did not match";
				logger.info("updatePassword: failed to update user {}", user.getLoginName());
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, notMatched, null);
			}
		} catch (Exception ex) {
			logger.error("updatePassword failed: {}", ex.toString());
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return new ErrorTransport(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "updatePassword failed", ex);
		}
	}

	@ApiOperation(value = "Gets a page of users, optionally sorted on fields.", //
			response = MLPUser.class, responseContainer = "Page")
	@ApiPageable
	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	public Page<MLPUser> getUsers(Pageable pageable) {
		logger.info("getUsers {}", pageable);
		Page<MLPUser> page = userRepository.findAll(pageable);
		for (MLPUser user : page.getContent()) {
			// detach from Hibernate and clear sensitive data
			entityManager.detach(user);
			user.clearHashes();
			if (user.getApiToken() != null)
				user.setApiToken(decryptWithJasypt(user.getApiToken()));
		}
		return page;
	}

	@ApiOperation(value = "Searches for users with names that contain the search term using a like operator.", //
			response = MLPUser.class, responseContainer = "Page")
	@ApiPageable
	@RequestMapping(value = "/" + CCDSConstants.LIKE_PATH, method = RequestMethod.GET)
	@ResponseBody
	public Page<MLPUser> likeUsers(@RequestParam(CCDSConstants.TERM_PATH) String term, Pageable pageable) {
		logger.info("likeUsers: term {}", term);
		Page<MLPUser> page = userRepository.findBySearchTerm(term, pageable);
		for (MLPUser user : page.getContent()) {
			// detach from Hibernate and clear sensitive data
			entityManager.detach(user);
			user.clearHashes();
			if (user.getApiToken() != null)
				user.setApiToken(decryptWithJasypt(user.getApiToken()));
		}
		return page;
	}

	/*
	 * This method was an early attempt to provide a search feature. Originally
	 * written with a generic map request parameter to avoid binding field names,
	 * but that is not supported by Swagger web UI. Now allows use from that web UI
	 * at the cost of hard-coding many class field names.
	 */
	private static final String firstNameField = "firstName";
	private static final String middleNameField = "middleName";
	private static final String lastNameField = "lastName";
	private static final String orgNameField = "orgName";
	private static final String emailField = "email";
	private static final String loginNameField = "loginName";
	private static final String activeField = "active";

	@ApiOperation(value = "Searches for users with attributes matching the values specified as query parameters. " //
			+ "Defaults to match all (conjunction); send junction query parameter '_j=o' to match any (disjunction).", //
			response = MLPUser.class, responseContainer = "Page")
	@ApiPageable
	@ApiResponses({ @ApiResponse(code = 400, message = "Bad request", response = ErrorTransport.class) })
	@RequestMapping(value = "/" + CCDSConstants.SEARCH_PATH, method = RequestMethod.GET)
	@ResponseBody
	public Object searchUsers(@ApiParam(value = "Junction", allowableValues = "a,o") //
	@RequestParam(name = CCDSConstants.JUNCTION_QUERY_PARAM, required = false) String junction, //
			@ApiParam(value = "First name") //
			@RequestParam(name = firstNameField, required = false) String firstName, //
			@ApiParam(value = "Middle name") //
			@RequestParam(name = middleNameField, required = false) String middleName, //
			@ApiParam(value = "Last name") //
			@RequestParam(name = lastNameField, required = false) String lastName, //
			@ApiParam(value = "Org name") //
			@RequestParam(name = orgNameField, required = false) String orgName, //
			@ApiParam(value = "Email") //
			@RequestParam(name = emailField, required = false) String email, //
			@ApiParam(value = "Login name") //
			@RequestParam(name = loginNameField, required = false) String loginName, //
			@ApiParam(value = "Active") //
			@RequestParam(name = activeField, required = false) Boolean active, //
			Pageable pageRequest, HttpServletResponse response) {
		logger.info("searchUsers enter");
		boolean isOr = junction != null && "o".equals(junction);
		Map<String, Object> queryParameters = new HashMap<>();
		if (firstName != null)
			queryParameters.put(firstNameField, firstName);
		if (middleName != null)
			queryParameters.put(middleNameField, middleName);
		if (lastName != null)
			queryParameters.put(lastNameField, lastName);
		if (orgName != null)
			queryParameters.put(orgNameField, orgName);
		if (email != null)
			queryParameters.put(emailField, email);
		if (loginName != null)
			queryParameters.put(loginNameField, loginName);
		if (active != null)
			queryParameters.put(activeField, active);
		if (queryParameters.size() == 0) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "Missing query", null);
		}
		try {
			Page<MLPUser> userPage = userSearchService.findUsers(queryParameters, isOr, pageRequest);
			// Wipe hash values
			Iterator<MLPUser> userIter = userPage.iterator();
			while (userIter.hasNext()) {
				MLPUser user = userIter.next();
				entityManager.detach(user);
				user.clearHashes();
				if (user.getApiToken() != null)
					user.setApiToken(decryptWithJasypt(user.getApiToken()));
			}
			return userPage;
		} catch (Exception ex) {
			logger.error("searchUsers failed: {}", ex.toString());
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return new ErrorTransport(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					ex.getCause() != null ? ex.getCause().getMessage() : "searchUsers failed", ex);
		}
	}

	@ApiOperation(value = "Gets the user for the specified ID. Returns bad request if the ID is not found.", //
			response = MLPUser.class)
	@ApiResponses({ @ApiResponse(code = 400, message = "Bad request", response = ErrorTransport.class) })
	@RequestMapping(value = "/{userId}", method = RequestMethod.GET)
	@ResponseBody
	public Object getUser(@PathVariable("userId") String userId, HttpServletResponse response) {
		logger.info("getUser: userId {}", userId);
		MLPUser user = userRepository.findOne(userId);
		if (user == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + userId, null);
		}
		// detach from Hibernate and wipe hashes
		entityManager.detach(user);
		user.clearHashes();
		if (user.getApiToken() != null)
			user.setApiToken(decryptWithJasypt(user.getApiToken()));
		return user;
	}

	/**
	 * Supports rudimentary decryption to avoid storing clear text in database.
	 * 
	 * @param encryptedMessage
	 *            Cipher text
	 * @return clear text
	 */
	private String decryptWithJasypt(String encryptedMessage) {
		BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
		textEncryptor.setPassword(jasyptEncryptorPassword);
		return textEncryptor.decrypt(encryptedMessage);
	}

	/**
	 * Supports rudimentary encryption to avoid storing clear text in database.
	 * 
	 * @param clearText
	 *            Clear text
	 * @return cipher text
	 */
	private String encryptWithJasypt(String clearText) {
		BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
		textEncryptor.setPassword(jasyptEncryptorPassword);
		return textEncryptor.encrypt(clearText);
	}

	@ApiOperation(value = "Creates a new entity and generates an ID if needed. Returns bad request on constraint violation etc.", //
			response = MLPUser.class)
	@ApiResponses({ @ApiResponse(code = 400, message = "Bad request", response = ErrorTransport.class) })
	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public Object createUser(@RequestBody MLPUser user, HttpServletResponse response) {
		// Do not log clear-text passwords or tokens!
		logger.info("createUser: loginName {}", user.getLoginName());
		Object result;
		try {
			String id = user.getUserId();
			if (id != null) {
				UUID.fromString(id);
				if (userRepository.findOne(id) != null) {
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "ID exists: " + id);
				}
			}
			// Hash any clear-text sensitive user credentials
			if (user.getLoginHash() != null)
				user.setLoginHash(BCrypt.hashpw(user.getLoginHash(), BCrypt.gensalt()));
			if (user.getVerifyTokenHash() != null)
				user.setVerifyTokenHash(BCrypt.hashpw(user.getVerifyTokenHash(), BCrypt.gensalt()));
			// Encrypt any API token
			if (user.getApiToken() != null)
				user.setApiToken(encryptWithJasypt(user.getApiToken()));
			// Create a new row
			MLPUser newUser = userRepository.save(user);
			response.setStatus(HttpServletResponse.SC_CREATED);
			// This is a hack to create the location path.
			response.setHeader(HttpHeaders.LOCATION, CCDSConstants.USER_PATH + "/" + newUser.getUserId());
			// ALSO send back the model for client convenience,
			// but first detach from Hibernate and wipe all hashes
			entityManager.detach(newUser);
			newUser.clearHashes();
			result = newUser;
			return result;
		} catch (Exception ex) {
			Exception cve = findConstraintViolationException(ex);
			logger.warn("createUser failed: {}", cve.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "createUser failed", cve);
		}
	}

	@ApiOperation(value = "Updates an existing entity with the supplied data. Returns bad request on constraint violation etc.", //
			response = SuccessTransport.class)
	@ApiResponses({ @ApiResponse(code = 400, message = "Bad request", response = ErrorTransport.class) })
	@RequestMapping(value = "/{userId}", method = RequestMethod.PUT)
	@ResponseBody
	public Object updateUser(@PathVariable("userId") String userId, @RequestBody MLPUser user,
			HttpServletResponse response) {
		logger.info("updateUser: userId {}", userId);
		// Get the existing one
		MLPUser existingUser = userRepository.findOne(userId);
		if (existingUser == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + userId, null);
		}
		try {
			// Use the path-parameter id; don't trust the one in the object
			user.setUserId(userId);
			// Hash any clear-text sensitive user credentials; otherwise use old value
			if (user.getLoginHash() != null)
				user.setLoginHash(BCrypt.hashpw(user.getLoginHash(), BCrypt.gensalt()));
			else
				user.setLoginHash(existingUser.getLoginHash());
			if (user.getVerifyTokenHash() != null)
				user.setVerifyTokenHash(BCrypt.hashpw(user.getVerifyTokenHash(), BCrypt.gensalt()));
			else
				user.setVerifyTokenHash(existingUser.getVerifyTokenHash());
			// Encrypt any API token.
			// But unlike password, allow caller to null it out.
			if (user.getApiToken() != null)
				user.setApiToken(encryptWithJasypt(user.getApiToken()));
			userRepository.save(user);
			return new SuccessTransport(HttpServletResponse.SC_OK, null);
		} catch (Exception ex) {
			Exception cve = findConstraintViolationException(ex);
			logger.warn("updateUser failed: {}", cve.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "updateUser failed", cve);
		}
	}

	/*
	 * Originally this was declared void and accordingly returned nothing. But when
	 * used in SpringBoot, after invoking the method it would look for a ThymeLeaf
	 * template, fail to find it, then throw internal server error.
	 */
	@ApiOperation(value = "Deletes the entity with the specified ID. Cascades to related entities. Returns bad request if the ID is not found.", //
			response = SuccessTransport.class)
	@ApiResponses({ @ApiResponse(code = 400, message = "Bad request", response = ErrorTransport.class) })
	@RequestMapping(value = "/{userId}", method = RequestMethod.DELETE)
	@ResponseBody
	public MLPTransportModel deleteUser(@PathVariable("userId") String userId, HttpServletResponse response) {
		logger.info("deleteUser: userId {}", userId);
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
			// e.g., EmptyResultDataAccessException is NOT an internal server error
			logger.warn("deleteUser failed: {}", ex.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "deleteUser failed", ex);
		}
	}

	@ApiOperation(value = "Gets the count of users in a role.", response = CountTransport.class)
	@RequestMapping(value = CCDSConstants.ROLE_PATH + "/{roleId}/"
			+ CCDSConstants.COUNT_PATH, method = RequestMethod.GET)
	@ResponseBody
	public CountTransport getRoleUsersCount(@PathVariable("roleId") String roleId) {
		logger.info("getRoleUsersCount: roleId {}", roleId);
		Long count = userRoleMapRepository.getRoleUsersCount(roleId);
		return new CountTransport(count);
	}

	@ApiOperation(value = "Gets all roles assigned to the specified user ID.", response = MLPRole.class, responseContainer = "List")
	@RequestMapping(value = "/{userId}/" + CCDSConstants.ROLE_PATH, method = RequestMethod.GET)
	@ResponseBody
	public Iterable<MLPRole> getRolesForUser(@PathVariable("userId") String userId) {
		logger.info("getRolesForUser: userId {}", userId);
		return roleRepository.findByUser(userId);
	}

	@ApiOperation(value = "Adds a role to the user. Returns bad request if an ID is not found.", //
			response = SuccessTransport.class)
	@ApiResponses({ @ApiResponse(code = 400, message = "Bad request", response = ErrorTransport.class) })
	@RequestMapping(value = "/{userId}/" + CCDSConstants.ROLE_PATH + "/{roleId}", method = RequestMethod.POST)
	@ResponseBody
	public Object addUserRole(@PathVariable("userId") String userId, @PathVariable("roleId") String roleId,
			HttpServletResponse response) {
		logger.info("addUserRole: userId {}, roleId {}", userId, roleId);
		if (userRepository.findOne(userId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + userId, null);
		} else if (roleRepository.findOne(roleId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + roleId, null);
		}
		userRoleMapRepository.save(new MLPUserRoleMap(userId, roleId));
		return new SuccessTransport(HttpServletResponse.SC_OK, null);
	}

	@ApiOperation(value = "Assigns the specified roles to the user after dropping any existing assignments. Returns bad request if an Id is not found", //
			response = SuccessTransport.class)
	@ApiResponses({ @ApiResponse(code = 400, message = "Bad request", response = ErrorTransport.class) })
	@RequestMapping(value = "/{userId}/" + CCDSConstants.ROLE_PATH, method = RequestMethod.PUT)
	@ResponseBody
	public Object updateUserRoles(@PathVariable("userId") String userId, @RequestBody List<String> roleIds,
			HttpServletResponse response) {
		logger.info("updateUserRoles: user {}, roles {}", userId, roleIds);
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
		for (String roleId : roleIds) {
			userRoleMapRepository.save(new MLPUserRoleMap(userId, roleId));
		}
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
	@ApiOperation(value = "Drops a role from the user. Returns bad request if an ID is not found.", response = SuccessTransport.class)
	@ApiResponses({ @ApiResponse(code = 400, message = "Bad request", response = ErrorTransport.class) })
	@RequestMapping(value = "/{userId}/" + CCDSConstants.ROLE_PATH + "/{roleId}", method = RequestMethod.DELETE)
	@ResponseBody
	public Object dropUserRole(@PathVariable("userId") String userId, @PathVariable("roleId") String roleId,
			HttpServletResponse response) {
		logger.info("dropUserRole: userId {} roleId {}", userId, roleId);
		try {
			userRoleMapRepository.delete(new MLPUserRoleMap(userId, roleId));
			return new SuccessTransport(HttpServletResponse.SC_OK, null);
		} catch (Exception ex) {
			// e.g., EmptyResultDataAccessException is NOT an internal server error
			logger.warn("dropUserRole failed: {}", ex.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "dropUserRole failed", ex);
		}
	}

	@ApiOperation(value = "Adds or removes the specified role for multiple users. Returns bad request if an ID is not found.", //
			response = SuccessTransport.class)
	@ApiResponses({ @ApiResponse(code = 400, message = "Bad request", response = ErrorTransport.class) })
	@RequestMapping(value = CCDSConstants.ROLE_PATH + "/{roleId}", method = RequestMethod.PUT)
	@ResponseBody
	public Object addOrDropUsersInRole(@PathVariable("roleId") String roleId,
			@RequestBody UsersRoleRequest usersRoleRequest, HttpServletResponse response) {
		logger.info("addOrDropUsersInRole: role {} users {}", roleId, String.join(", ", usersRoleRequest.getUserIds()));
		// Validate entire request before making any change
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
		for (String userId : usersRoleRequest.getUserIds()) {
			if (usersRoleRequest.isAdd())
				userRoleMapRepository.save(new MLPUserRoleMap(userId, roleId));
			else
				userRoleMapRepository.delete(new MLPUserRoleMap.UserRoleMapPK(userId, roleId));
		}
		return new SuccessTransport(HttpServletResponse.SC_OK, null);
	}

	@ApiOperation(value = "Gets the login provider for the specified user, provider code and provider login. "
			+ "Returns bad request if an ID is not found.", //
			response = MLPUserLoginProvider.class)
	@ApiResponses({ @ApiResponse(code = 400, message = "Bad request", response = ErrorTransport.class) })
	@RequestMapping(value = "/{userId}/" + CCDSConstants.LOGIN_PROVIDER_PATH + "/{providerCode}/"
			+ CCDSConstants.LOGIN_PATH + "/{providerUserId}", method = RequestMethod.GET)
	@ResponseBody
	public Object getUserLoginProvider(@PathVariable("userId") String userId,
			@PathVariable("providerCode") String providerCode, @PathVariable("providerUserId") String providerUserId,
			HttpServletResponse response) {
		logger.info("getUserLoginProvider: userId {} providerCode {} providerUserId {}", userId, providerCode,
				providerUserId);
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

	@ApiOperation(value = "Gets all login providers for the specified user.", //
			response = MLPUserLoginProvider.class, responseContainer = "List")
	@RequestMapping(value = "/{userId}/" + CCDSConstants.LOGIN_PROVIDER_PATH, method = RequestMethod.GET)
	@ResponseBody
	public Object getAllLoginProviders(@PathVariable("userId") String userId, HttpServletResponse response) {
		logger.info("getAllLoginProviders: userId {}", userId);
		return userLoginProviderRepository.findByUserId(userId);
	}

	@ApiOperation(value = "Creates a new entity. Returns bad request on constraint violation etc.", //
			response = MLPUserLoginProvider.class)
	@ApiResponses({ @ApiResponse(code = 400, message = "Bad request", response = ErrorTransport.class) })
	@RequestMapping(value = "/{userId}/" + CCDSConstants.LOGIN_PROVIDER_PATH + "/{providerCode}/"
			+ CCDSConstants.LOGIN_PATH + "/{providerUserId}", method = RequestMethod.POST)
	@ResponseBody
	public Object createUserLoginProvider(@PathVariable("userId") String userId,
			@PathVariable("providerCode") String providerCode, @PathVariable("providerUserId") String providerUserId,
			@RequestBody MLPUserLoginProvider ulp, HttpServletResponse response) {
		logger.info("createUserLoginProvider: userId {} providerCode {} providerUserId {}", userId, providerCode,
				providerUserId);
		// Validate args
		if (userRepository.findOne(userId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + userId, null);
		}
		try {
			// Validate enum code
			super.validateCode(ulp.getProviderCode(), CodeNameType.LOGIN_PROVIDER);
			// Use path IDs
			ulp.setUserId(userId);
			ulp.setProviderCode(providerCode);
			ulp.setProviderUserId(providerUserId);
			Object result = userLoginProviderRepository.save(ulp);
			response.setStatus(HttpServletResponse.SC_CREATED);
			// This is a hack to create the location path.
			response.setHeader(HttpHeaders.LOCATION,
					"/" + CCDSConstants.USER_PATH + "/" + userId + "/" + CCDSConstants.LOGIN_PROVIDER_PATH + "/"
							+ providerCode + "/" + CCDSConstants.LOGIN_PATH + "/" + providerUserId);
			return result;
		} catch (Exception ex) {
			// e.g., EmptyResultDataAccessException is NOT an internal server error
			Exception cve = findConstraintViolationException(ex);
			logger.warn("createUserLoginProvider failed: {}", cve.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "createUserLoginProvider failed", cve);
		}
	}

	@ApiOperation(value = "Updates an existing entity with the supplied data. Returns bad request on constraint violation etc.", //
			response = SuccessTransport.class)
	@ApiResponses({ @ApiResponse(code = 400, message = "Bad request", response = ErrorTransport.class) })
	@RequestMapping(value = "/{userId}/" + CCDSConstants.LOGIN_PROVIDER_PATH + "/{providerCode}/"
			+ CCDSConstants.LOGIN_PATH + "/{providerUserId}", method = RequestMethod.PUT)
	@ResponseBody
	public Object updateUserLoginProvider(@PathVariable("userId") String userId,
			@PathVariable("providerCode") String providerCode, @PathVariable("providerUserId") String providerUserId,
			@RequestBody MLPUserLoginProvider ulp, HttpServletResponse response) {
		logger.info("updateUserLoginProvider: userId {} providerCode {} providerUserId {}", userId, providerCode,
				providerUserId);
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
		try {
			// Validate enum code
			super.validateCode(ulp.getProviderCode(), CodeNameType.LOGIN_PROVIDER);
			// Use path IDs
			ulp.setUserId(userId);
			ulp.setProviderCode(providerCode);
			ulp.setProviderUserId(providerUserId);
			userLoginProviderRepository.save(ulp);
			return new SuccessTransport(HttpServletResponse.SC_OK, null);
		} catch (Exception ex) {
			// e.g., EmptyResultDataAccessException is NOT an internal server error
			Exception cve = findConstraintViolationException(ex);
			logger.warn("updateUserLoginProvider failed: {}", cve.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "updateUserLoginProvider failed", cve);
		}
	}

	@ApiOperation(value = "Deletes the entity with the specified ID. Returns bad request if the ID is not found.", //
			response = SuccessTransport.class)
	@ApiResponses({ @ApiResponse(code = 400, message = "Bad request", response = ErrorTransport.class) })
	@RequestMapping(value = "/{userId}/" + CCDSConstants.LOGIN_PROVIDER_PATH + "/{providerCode}/"
			+ CCDSConstants.LOGIN_PATH + "/{providerUserId}", method = RequestMethod.DELETE)
	@ResponseBody
	public MLPTransportModel deleteLoginProvider(@PathVariable("userId") String userId,
			@PathVariable("providerCode") String providerCode, @PathVariable("providerUserId") String providerUserId,
			HttpServletResponse response) {
		logger.info("deleteLoginProvider: userId {} providerCode {} providerUserId {}", userId, providerCode,
				providerUserId);
		try {
			// Build a key for fetch
			UserLoginProviderPK pk = new UserLoginProviderPK(userId, providerCode, providerUserId);
			userLoginProviderRepository.delete(pk);
			return new SuccessTransport(HttpServletResponse.SC_OK, null);
		} catch (Exception ex) {
			// e.g., EmptyResultDataAccessException is NOT an internal server error
			logger.warn("deleteLoginProvider failed: {}", ex.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "deleteLoginProvider failed", ex);
		}
	}

	@ApiOperation(value = "Gets a page of solutions which are favorites for the specified user ID.", //
			response = MLPSolution.class, responseContainer = "Page")
	@ApiPageable
	@ApiResponses({ @ApiResponse(code = 400, message = "Bad request", response = ErrorTransport.class) })
	@RequestMapping(value = "/{userId}/" + CCDSConstants.FAVORITE_PATH + "/"
			+ CCDSConstants.SOLUTION_PATH, method = RequestMethod.GET)
	@ResponseBody
	public Object getFavoriteSolutions(@PathVariable("userId") String userId, Pageable pageRequest,
			HttpServletResponse response) {
		logger.info("getFavoriteSolutions: userId {}", userId);
		return solutionFavoriteRepository.findByUserId(userId, pageRequest);
	}

	@ApiOperation(value = "Creates a new solution favorite record. Returns bad request on constraint violation etc.", //
			response = MLPSolutionFavorite.class)
	@ApiResponses({ @ApiResponse(code = 400, message = "Bad request", response = ErrorTransport.class) })
	@RequestMapping(value = "/{userId}/" + CCDSConstants.FAVORITE_PATH + "/" + CCDSConstants.SOLUTION_PATH
			+ "/{solutionId}", method = RequestMethod.POST)
	@ResponseBody
	public Object createSolutionFavorite(@PathVariable("solutionId") String solutionId,
			@PathVariable("userId") String userId, @RequestBody MLPSolutionFavorite sfv, HttpServletResponse response) {
		logger.info("createSolutionFavorite: solutionId {} userId {}", solutionId, userId);
		if (solutionRepository.findOne(solutionId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + solutionId, null);
		}
		if (userRepository.findOne(userId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + userId, null);
		}
		try {
			// Create a new row
			// Use path IDs
			sfv.setSolutionId(solutionId);
			sfv.setUserId(userId);
			Object result = solutionFavoriteRepository.save(sfv);
			response.setStatus(HttpServletResponse.SC_CREATED);
			response.setHeader(HttpHeaders.LOCATION, CCDSConstants.USER_PATH + "/" + sfv.getUserId() + "/"
					+ CCDSConstants.FAVORITE_PATH + "/" + CCDSConstants.SOLUTION_PATH + "/" + sfv.getSolutionId());
			return result;
		} catch (Exception ex) {
			// e.g., EmptyResultDataAccessException is NOT an internal server error
			Exception cve = findConstraintViolationException(ex);
			logger.error("createSolutionFavorite failed: {}", cve.toString());
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return new ErrorTransport(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					ex.getCause() != null ? ex.getCause().getMessage() : "createSolutionFavorite failed", cve);
		}
	}

	@ApiOperation(value = "Deletes the entity with the specified IDs. Returns bad request if the entity is not found.", //
			response = SuccessTransport.class)
	@ApiResponses({ @ApiResponse(code = 400, message = "Bad request", response = ErrorTransport.class) })
	@RequestMapping(value = "/{userId}/" + CCDSConstants.FAVORITE_PATH + "/" + CCDSConstants.SOLUTION_PATH
			+ "/{solutionId}", method = RequestMethod.DELETE)
	@ResponseBody
	public MLPTransportModel deleteSolutionFavorite(@PathVariable("solutionId") String solutionId,
			@PathVariable("userId") String userId, HttpServletResponse response) {
		logger.info("deleteSolutionFavorite: solutionId {} userId {}", solutionId, userId);
		try {
			// Build a key for fetch
			SolutionFavoritePK pk = new SolutionFavoritePK(solutionId, userId);
			solutionFavoriteRepository.delete(pk);
			return new SuccessTransport(HttpServletResponse.SC_OK, null);
		} catch (Exception ex) {
			// e.g., EmptyResultDataAccessException is NOT an internal server error
			logger.warn("deleteSolutionFavorite failed: {}", ex.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "deleteSolutionFavorite failed", ex);
		}
	}

	@ApiOperation(value = "Gets the deployments for the specified user ID.", //
			response = MLPSolutionDeployment.class, responseContainer = "Page")
	@ApiPageable
	@ApiResponses({ @ApiResponse(code = 400, message = "Bad request", response = ErrorTransport.class) })
	@RequestMapping(value = "/{userId}/" + CCDSConstants.DEPLOY_PATH, method = RequestMethod.GET)
	@ResponseBody
	public Object getUserDeployments(@PathVariable("userId") String userId, Pageable pageRequest,
			HttpServletResponse response) {
		logger.info("getUserDeployments: userId {} ", userId);
		return solutionDeploymentRepository.findByUserId(userId, pageRequest);
	}

	@ApiOperation(value = "Adds a tag to the user. Returns bad request on constraint violation etc.", //
			response = SuccessTransport.class)
	@ApiResponses({ @ApiResponse(code = 400, message = "Bad request", response = ErrorTransport.class) })
	@RequestMapping(value = "/{userId}/" + CCDSConstants.TAG_PATH + "/{tag}", method = RequestMethod.POST)
	@ResponseBody
	public Object addUserTag(@PathVariable("userId") String userId, @PathVariable("tag") String tag,
			HttpServletResponse response) {
		logger.info("addUserTag: userId {} tag {}", userId, tag);
		if (userRepository.findOne(userId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + userId, null);
		} else if (userTagMapRepository.findOne(new MLPUserTagMap.UserTagMapPK(userId, tag)) != null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "Already has tag " + tag, null);
		}
		if (tagRepository.findOne(tag) == null) {
			// Tags are cheap & easy to create, so make life easy for client
			tagRepository.save(new MLPTag(tag));
			logger.info("addUserTag: created tag {}", tag);
		}
		userTagMapRepository.save(new MLPUserTagMap(userId, tag));
		return new SuccessTransport(HttpServletResponse.SC_OK, null);
	}

	@ApiOperation(value = "Drops a tag from the user. Returns bad request if not found.", //
			response = SuccessTransport.class)
	@ApiResponses({ @ApiResponse(code = 400, message = "Bad request", response = ErrorTransport.class) })
	@RequestMapping(value = "/{userId}/" + CCDSConstants.TAG_PATH + "/{tag}", method = RequestMethod.DELETE)
	@ResponseBody
	public Object dropUserTag(@PathVariable("userId") String userId, @PathVariable("tag") String tag,
			HttpServletResponse response) {
		logger.info("dropTag: userId {} tag {}", userId, tag);
		try {
			userTagMapRepository.delete(new MLPUserTagMap.UserTagMapPK(userId, tag));
			return new SuccessTransport(HttpServletResponse.SC_OK, null);
		} catch (Exception ex) {
			// e.g., EmptyResultDataAccessException is NOT an internal server error
			logger.warn("dropTag failed: {}", ex.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "dropTag failed", ex);
		}
	}

}

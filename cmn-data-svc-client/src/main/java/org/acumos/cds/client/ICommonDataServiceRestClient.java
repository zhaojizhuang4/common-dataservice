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

package org.acumos.cds.client;

import java.util.List;
import java.util.Map;

import org.acumos.cds.domain.MLPAccessType;
import org.acumos.cds.domain.MLPArtifact;
import org.acumos.cds.domain.MLPArtifactType;
import org.acumos.cds.domain.MLPComment;
import org.acumos.cds.domain.MLPDeploymentStatus;
import org.acumos.cds.domain.MLPLoginProvider;
import org.acumos.cds.domain.MLPModelType;
import org.acumos.cds.domain.MLPNotification;
import org.acumos.cds.domain.MLPPasswordChangeRequest;
import org.acumos.cds.domain.MLPPeer;
import org.acumos.cds.domain.MLPPeerSubscription;
import org.acumos.cds.domain.MLPRole;
import org.acumos.cds.domain.MLPRoleFunction;
import org.acumos.cds.domain.MLPSiteConfig;
import org.acumos.cds.domain.MLPSolution;
import org.acumos.cds.domain.MLPSolutionDeployment;
import org.acumos.cds.domain.MLPSolutionDownload;
import org.acumos.cds.domain.MLPSolutionFavorite;
import org.acumos.cds.domain.MLPSolutionRating;
import org.acumos.cds.domain.MLPSolutionRevision;
import org.acumos.cds.domain.MLPSolutionValidation;
import org.acumos.cds.domain.MLPSolutionWeb;
import org.acumos.cds.domain.MLPTag;
import org.acumos.cds.domain.MLPThread;
import org.acumos.cds.domain.MLPToolkitType;
import org.acumos.cds.domain.MLPUser;
import org.acumos.cds.domain.MLPUserLoginProvider;
import org.acumos.cds.domain.MLPUserNotification;
import org.acumos.cds.domain.MLPValidationSequence;
import org.acumos.cds.domain.MLPValidationStatus;
import org.acumos.cds.domain.MLPValidationType;
import org.acumos.cds.transport.RestPageRequest;
import org.acumos.cds.transport.RestPageResponse;
import org.acumos.cds.transport.SuccessTransport;

/**
 * Defines the interface of the Controller REST client. On a request that is
 * missing required data the response code is 400 (bad request); on a request
 * for an entity that does not exist the response code is 404 (not found).
 * 
 * Callers are STRONGLY advised to catch the runtime (unchecked) exception
 * HttpStatusCodeException and call its method
 * {@link org.springframework.web.client.HttpStatusCodeException#getResponseBodyAsString()}
 * to obtain the detailed error message sent by the server.
 */
public interface ICommonDataServiceRestClient {

	/**
	 * Checks the health of the server.
	 * 
	 * @return Object with health string
	 */
	SuccessTransport getHealth();

	/**
	 * Gets the version of the server.
	 * 
	 * @return Object with version string
	 */
	SuccessTransport getVersion();

	/**
	 * Gets all access types.
	 * 
	 * @return List of access type objects.
	 */
	List<MLPAccessType> getAccessTypes();

	/**
	 * Gets all artifact types.
	 * 
	 * @return List of artifact type objects.
	 */
	List<MLPArtifactType> getArtifactTypes();

	/**
	 * Gets all login providers.
	 * 
	 * @return List of login provider objects.
	 */
	List<MLPLoginProvider> getLoginProviders();

	/**
	 * Gets all model types.
	 * 
	 * @return List of model type objects.
	 */
	List<MLPModelType> getModelTypes();

	/**
	 * Gets all toolkit types.
	 * 
	 * @return List of tookit type objects.
	 */
	List<MLPToolkitType> getToolkitTypes();

	/**
	 * Gets all validation status codes
	 * 
	 * @return List of validation status objects.
	 */
	List<MLPValidationStatus> getValidationStatuses();

	/**
	 * Gets all validation type codes.
	 * 
	 * @return List of validation type objects.
	 */
	List<MLPValidationType> getValidationTypes();

	/**
	 * Gets all deployment status codes
	 * 
	 * @return List of deployment status objects.
	 */
	List<MLPDeploymentStatus> getDeploymentStatuses();

	/**
	 * Gets count of solutions.
	 * 
	 * @return Count of solutions.
	 */
	long getSolutionCount();

	/**
	 * Gets a page of solutions.
	 * 
	 * @param pageRequest
	 *            Page index, page size, sort information; ignored if null.
	 * @return List of objects.
	 */
	RestPageResponse<MLPSolution> getSolutions(RestPageRequest pageRequest);

	/**
	 * Returns solutions with a name or description that contains the search term.
	 * 
	 * @param searchTerm
	 *            String to find
	 * @param pageRequest
	 *            Page index, page size, sort information; ignored if null.
	 * @return Page of solution objects.
	 */
	RestPageResponse<MLPSolution> findSolutionsBySearchTerm(String searchTerm, RestPageRequest pageRequest);

	/**
	 * Returns solutions tagged with the specified string.
	 * 
	 * @param tag
	 *            Tag to find
	 * @param pageRequest
	 *            Page index, page size, sort information; ignored if null.
	 * @return Page of solution objects.
	 */
	RestPageResponse<MLPSolution> findSolutionsByTag(String tag, RestPageRequest pageRequest);

	/**
	 * Searches solutions for matches on every specified condition. Special-purpose
	 * method to support the dynamic search on the portal interface.
	 * 
	 * TODO: Return a solution DTO with related information such as download count.
	 * 
	 * @param nameKeyword
	 *            Keyword to perform "LIKE" search in Name field; ignored if null or
	 *            empty
	 * @param descriptionKeyword
	 *            Keyword to perform "LIKE" search in Description field; ignored if
	 *            null or empty
	 * @param authorKeyword
	 *            Keyword to perform "LIKE" search in user name; ignored if null or
	 *            empty; TODO not implemented yet
	 * @param active
	 *            Solution active status (required)
	 * @param accessTypeCodes
	 *            Access type codes; use four-letter sequence "null" to match a null
	 *            value; ignored if null or empty
	 * @param modelTypeCodes
	 *            Model type codes; use four-letter sequence "null" to match a null
	 *            value; ignored if null or empty
	 * @param validationStatusCodes
	 *            Validation status codes; use four-letter sequence "null" to match
	 *            a null value; ignored if null or empty
	 * @param tags
	 *            Solution tag names; ignored if null or empty
	 * @param pageRequest
	 *            Page index, page size, sort information; ignored if null.
	 * @return Page of solution objects.
	 */
	RestPageResponse<MLPSolution> findPortalSolutions(String nameKeyword, String descriptionKeyword,
			String authorKeyword, boolean active, String[] accessTypeCodes, String[] modelTypeCodes,
			String[] validationStatusCodes, String[] tags, RestPageRequest pageRequest);

	/**
	 * Searches the solutions.
	 * 
	 * @param queryParameters
	 *            Map of field-name, field-value pairs to use as query criteria.
	 *            Accepts Boolean, Date, Integer, Long, String.
	 * @param isOr
	 *            If true, finds matches on any field-value pair (conditions are
	 *            OR-ed together); otherwise finds matches on all field-value pairs
	 *            (conditions are AND-ed together).
	 * @return List of solution objects.
	 */
	List<MLPSolution> searchSolutions(Map<String, Object> queryParameters, boolean isOr);

	/**
	 * Gets the solution with the specified ID.
	 * 
	 * @param solutionId
	 *            solution ID
	 * @return Solution object
	 */
	MLPSolution getSolution(String solutionId);

	/**
	 * Creates a solution.
	 * 
	 * @param solution
	 *            Solution data. If the ID field is null a new value is generated;
	 *            otherwise the ID value is used if valid and not already known.
	 * @return Complete object, with generated information such as ID
	 */
	MLPSolution createSolution(MLPSolution solution);

	/**
	 * Updates a solution.
	 * 
	 * @param solution
	 *            Solution data
	 */
	void updateSolution(MLPSolution solution);

	/**
	 * A convenience method that increments the view count of a solution by 1.
	 * 
	 * This requires only one database access, instead of two to fetch the solution
	 * entity and save it again.
	 * 
	 * @param solutionId
	 *            solution ID
	 */
	void incrementSolutionViewCount(String solutionId);

	/**
	 * Deletes a solution. A solution can be deleted if is not associated with any
	 * revisions; if associations remain the delete will fail.
	 * 
	 * @param solutionId
	 *            solution ID
	 */
	void deleteSolution(String solutionId);

	/**
	 * Gets the solution revisions for the specified solution ID.
	 * 
	 * @param solutionId
	 *            solution ID.
	 * @return List of Solution revision objects for the specified solution.
	 */
	List<MLPSolutionRevision> getSolutionRevisions(String solutionId);

	/**
	 * Gets the solution revisions for the specified solution IDs.
	 * 
	 * @param solutionIds
	 *            solution IDs. Caveat: the number of possible entries in this list
	 *            is constrained by client/server limitations on URL length.
	 * @return List of Solution revision objects for any of the specified solutions.
	 */
	List<MLPSolutionRevision> getSolutionRevisions(String[] solutionIds);

	/**
	 * Gets the solution revision with the specified ID.
	 * 
	 * @param solutionId
	 *            solution ID
	 * @param revisionId
	 *            revision ID
	 * @return Solution revision object
	 */
	MLPSolutionRevision getSolutionRevision(String solutionId, String revisionId);

	/**
	 * Gets the solution revisions for the specified artifact ID.
	 * 
	 * @param artifactId
	 *            artifact ID
	 * @return List of Solution revision objects for the specified artifact.
	 */
	List<MLPSolutionRevision> getSolutionRevisionsForArtifact(String artifactId);

	/**
	 * Creates a solution revision.
	 * 
	 * @param revision
	 *            Solution revision data. If the ID field is null a new value is
	 *            generated; otherwise the ID value is used if valid and not already
	 *            known.
	 * @return Complete object, with generated information such as ID
	 */
	MLPSolutionRevision createSolutionRevision(MLPSolutionRevision revision);

	/**
	 * Updates a solution revision.
	 * 
	 * @param revision
	 *            Solution revision data
	 */
	void updateSolutionRevision(MLPSolutionRevision revision);

	/**
	 * Deletes a solution revision. A solution revision can be deleted if is not
	 * associated with any artifacts; if associations remain the delete will fail.
	 * 
	 * @param solutionId
	 *            solution ID
	 * @param revisionId
	 *            revision ID
	 */
	void deleteSolutionRevision(String solutionId, String revisionId);

	/**
	 * Gets the artifacts for a solution revision
	 * 
	 * @param solutionId
	 *            solution ID
	 * @param revisionId
	 *            revision ID
	 * @return List of MLPArtifact
	 */
	List<MLPArtifact> getSolutionRevisionArtifacts(String solutionId, String revisionId);

	/**
	 * Adds an artifact to a solution revision
	 * 
	 * @param solutionId
	 *            solution ID
	 * @param revisionId
	 *            revision ID
	 * @param artifactId
	 *            artifact Id
	 */
	void addSolutionRevisionArtifact(String solutionId, String revisionId, String artifactId);

	/**
	 * Removes an artifact from a solution revision
	 * 
	 * @param solutionId
	 *            solution ID
	 * @param revisionId
	 *            revision ID
	 * @param artifactId
	 *            artifact Id
	 */
	void dropSolutionRevisionArtifact(String solutionId, String revisionId, String artifactId);

	/**
	 * Gets a page of solution tags.
	 *
	 * @param pageRequest
	 *            Page index, page size, sort information; ignored if null.
	 * @return Page of solution tag objects
	 */
	RestPageResponse<MLPTag> getTags(RestPageRequest pageRequest);

	/**
	 * Creates a solution tag.
	 * 
	 * @param tag
	 *            tag object
	 * @return Complete object which wraps the tag
	 */
	MLPTag createTag(MLPTag tag);

	/**
	 * Deletes a solution tag. A tag can be deleted if is not associated with any
	 * solutions; if associations remain the delete will fail.
	 * 
	 * @param tag
	 *            tag object
	 */
	void deleteTag(MLPTag tag);

	/**
	 * Gets the solution tags for the specified solution ID.
	 * 
	 * @param solutionId
	 *            solution ID.
	 * @return List of Solution tag objects for the specified solution.
	 */
	List<MLPTag> getSolutionTags(String solutionId);

	/**
	 * Adds the specified tag to the specified solution.
	 * 
	 * @param tag
	 *            tag string
	 * @param solutionId
	 *            solution ID
	 */
	void addSolutionTag(String solutionId, String tag);

	/**
	 * Removes the specified tag from the specified solution.
	 * 
	 * @param tag
	 *            tag string
	 * @param solutionId
	 *            solution ID
	 */
	void dropSolutionTag(String solutionId, String tag);

	/**
	 * Gets the count of artifacts.
	 * 
	 * @return Count of artifacts.
	 */
	long getArtifactCount();

	/**
	 * Gets a page of artifacts.
	 * 
	 * @param pageRequest
	 *            Page index, page size, sort information; ignored if null.
	 * @return Page of artifact objects.
	 */
	RestPageResponse<MLPArtifact> getArtifacts(RestPageRequest pageRequest);

	/**
	 * Returns artifacts with a name or description that contains the search term.
	 * 
	 * @param searchTerm
	 *            String to find
	 * @param pageRequest
	 *            Page index, page size, sort information; ignored if null.
	 * @return Page of artifact objects.
	 */
	RestPageResponse<MLPArtifact> findArtifactsBySearchTerm(String searchTerm, RestPageRequest pageRequest);

	/**
	 * Searches artifacts.
	 * 
	 * @param queryParameters
	 *            Map of field-name, field-value pairs to use as query criteria.
	 *            Accepts Boolean, Date, Integer, Long, String.
	 * @param isOr
	 *            If true, finds matches on any field-value pair (conditions are
	 *            OR-ed together); otherwise finds matches on all field-value pairs
	 *            (conditions are AND-ed together).
	 * @return List of artifact objects.
	 */
	List<MLPArtifact> searchArtifacts(Map<String, Object> queryParameters, boolean isOr);

	/**
	 * Gets the artifact with the specified ID.
	 * 
	 * @param artifactId
	 *            artifact ID
	 * @return Artifact object
	 */
	MLPArtifact getArtifact(String artifactId);

	/**
	 * Creates a artifact.
	 * 
	 * @param artifact
	 *            Artifact data. If the ID field is null a new value is generated;
	 *            otherwise the ID value is used if valid and not already known.
	 * @return Complete object, with generated information such as ID
	 */
	MLPArtifact createArtifact(MLPArtifact artifact);

	/**
	 * Updates an artifact.
	 * 
	 * @param artifact
	 *            Artifact data
	 */
	void updateArtifact(MLPArtifact artifact);

	/**
	 * Deletes an artifact. An artifact can be deleted if is not associated with any
	 * solution revisions; if associations remain the delete will fail.
	 * 
	 * @param artifactId
	 *            artifact ID
	 */
	void deleteArtifact(String artifactId);

	/**
	 * Gets count of users.
	 * 
	 * @return Count of users.
	 */
	long getUserCount();

	/**
	 * Gets a page of users.
	 * 
	 * @param pageRequest
	 *            Page index, page size, sort information; ignored if null.
	 * @return Page of objects.
	 */
	RestPageResponse<MLPUser> getUsers(RestPageRequest pageRequest);

	/**
	 * Returns users with a first, middle, last or login name that contains the
	 * search term.
	 * 
	 * @param searchTerm
	 *            String to find
	 * @param pageRequest
	 *            Page index, page size, sort information; ignored if null.
	 * @return List of user objects.
	 */
	RestPageResponse<MLPUser> findUsersBySearchTerm(String searchTerm, RestPageRequest pageRequest);

	/**
	 * Searches users for exact matches.
	 * 
	 * @param queryParameters
	 *            Map of field-name, field-value pairs to use as query criteria.
	 *            Accepts Boolean, Date, Integer, Long, String.
	 * @param isOr
	 *            If true, finds matches on any field-value pair (conditions are
	 *            OR-ed together); otherwise finds matches on all field-value pairs
	 *            (conditions are AND-ed together).
	 * @return List of user objects
	 */
	List<MLPUser> searchUsers(Map<String, Object> queryParameters, boolean isOr);

	/**
	 * Searches for user with the specified credentials.
	 * 
	 * @param name
	 *            loginname OR email address; both fields are checked
	 * @param pass
	 *            clear-text password
	 * @return User object if a match is found
	 * 
	 *         If no match is found
	 */
	MLPUser loginUser(String name, String pass);

	/**
	 * Gets the user with the specified ID.
	 * 
	 * @param userId
	 *            user ID
	 * @return User object
	 */
	MLPUser getUser(String userId);

	/**
	 * Creates a user.
	 * 
	 * @param user
	 *            User data. If the ID field is null a new value is generated;
	 *            otherwise the ID value is used if valid and not already known.
	 * @return Complete object, with generated information such as ID
	 */
	MLPUser createUser(MLPUser user);

	/**
	 * Updates a user.
	 * 
	 * @param user
	 *            User data
	 */
	void updateUser(MLPUser user);

	/**
	 * Deletes a user. Cascades the delete to login-provider, notification and role
	 * associations. If associations remain with artifacts such as solutions the
	 * delete will fail.
	 * 
	 * @param userId
	 *            user ID
	 */
	void deleteUser(String userId);

	/**
	 * Gets the roles for the specified user ID.
	 * 
	 * @param userId
	 *            user ID.
	 * @return List of Role objects for the specified user.
	 */
	List<MLPRole> getUserRoles(String userId);

	/**
	 * Adds the specified role to the specified user.
	 * 
	 * @param userId
	 *            user ID
	 * @param roleId
	 *            role ID
	 */
	void addUserRole(String userId, String roleId);

	/**
	 * Updates the user to have exactly the specified roles only; i.e., remove any
	 * roles not in the list.
	 * 
	 * @param userId
	 *            user ID
	 * @param roleIds
	 *            List of role IDs
	 */
	void updateUserRoles(String userId, List<String> roleIds);

	/**
	 * Removes the specified role from the specified user.
	 * 
	 * @param userId
	 *            user ID
	 * @param roleId
	 *            role ID
	 */
	void dropUserRole(String userId, String roleId);

	/**
	 * Assigns the specified role to each user in the specified list.
	 * 
	 * @param userIds
	 *            List of user IDs
	 * @param roleId
	 *            role ID
	 */
	void addUsersInRole(List<String> userIds, String roleId);

	/**
	 * Removes the specified role from each user in the specified list.
	 * 
	 * @param userIds
	 *            List of user IDs
	 * @param roleId
	 *            role ID
	 */
	void dropUsersInRole(List<String> userIds, String roleId);

	/**
	 * Gets count of users with the specified role.
	 * 
	 * @param roleId
	 *            role ID
	 * @return Count of users in that role
	 */
	long getRoleUsersCount(String roleId);

	/**
	 * Gets the specified user login provider.
	 * 
	 * @param userId
	 *            user ID
	 * @param providerCode
	 *            Provider code
	 * @param providerLogin
	 *            User login at the provider
	 * @return user login provider
	 */
	MLPUserLoginProvider getUserLoginProvider(String userId, String providerCode, String providerLogin);

	/**
	 * Gets the user's login providers.
	 * 
	 * @param userId
	 *            user ID
	 * @return List of user login providers
	 */
	List<MLPUserLoginProvider> getUserLoginProviders(String userId);

	/**
	 * Creates a user login provider.
	 * 
	 * @param provider
	 *            data to populate new entry
	 * @return Complete object, with generated information such as ID
	 */
	MLPUserLoginProvider createUserLoginProvider(MLPUserLoginProvider provider);

	/**
	 * Updates a user login provider
	 * 
	 * @param provider
	 *            data to update
	 */
	void updateUserLoginProvider(MLPUserLoginProvider provider);

	/**
	 * Deletes a user login provider.
	 * 
	 * @param provider
	 *            data to delete
	 */
	void deleteUserLoginProvider(MLPUserLoginProvider provider);

	/**
	 * Gets count of roles.
	 * 
	 * @return Count of roles.
	 */
	long getRoleCount();

	/**
	 * Searches roles for exact matches.
	 * 
	 * @param queryParameters
	 *            Map of field-name, field-value pairs to use as query criteria.
	 *            Accepts Boolean, Date, Integer, Long, String.
	 * @param isOr
	 *            If true, finds matches on any field-value pair (conditions are
	 *            OR-ed together); otherwise finds matches on all field-value pairs
	 *            (conditions are AND-ed together).
	 * @return List of instances, which may be empty.
	 */
	List<MLPRole> searchRoles(Map<String, Object> queryParameters, boolean isOr);

	/**
	 * Gets the roles.
	 * 
	 * @param pageRequest
	 *            Page index, page size, sort information; ignored if null.
	 * @return List of MLPRoles
	 */
	RestPageResponse<MLPRole> getRoles(RestPageRequest pageRequest);

	/**
	 * Gets the object with the specified ID.
	 * 
	 * @param roleId
	 *            role ID
	 * @return instance with the specified ID; null if none exists.
	 */
	MLPRole getRole(String roleId);

	/**
	 * Writes the specified role.
	 * 
	 * @param role
	 *            Role data. If the ID field is null a new value is generated;
	 *            otherwise the ID value is used if valid and not already known.
	 * @return Complete object, with generated information such as ID
	 */
	MLPRole createRole(MLPRole role);

	/**
	 * Updates the specified role.
	 * 
	 * @param role
	 *            instance to save
	 */
	void updateRole(MLPRole role);

	/**
	 * Deletes a role. A role can be deleted if is not associated with any users.
	 * Cascades the delete to associated role functions.
	 * 
	 * @param roleId
	 *            Role ID
	 */
	void deleteRole(String roleId);

	/**
	 * Gets the role functions for the specified role
	 * 
	 * @param roleId
	 *            role ID
	 * @return List of RoleFunctions;
	 */
	List<MLPRoleFunction> getRoleFunctions(String roleId);

	/**
	 * Gets the role function with the specified ID.
	 * 
	 * @param roleId
	 *            role ID
	 * @param roleFunctionId
	 *            role function ID
	 * @return instance with the specified ID; null if none exists.
	 */
	MLPRoleFunction getRoleFunction(String roleId, String roleFunctionId);

	/**
	 * Creates the specified role function.
	 * 
	 * @param roleFunction
	 *            instance to save
	 * @return Complete object, with generated information such as ID
	 */
	MLPRoleFunction createRoleFunction(MLPRoleFunction roleFunction);

	/**
	 * Creates the specified role function.
	 * 
	 * @param roleFunction
	 *            instance to save
	 */
	void updateRoleFunction(MLPRoleFunction roleFunction);

	/**
	 * Deletes a role function.
	 * 
	 * @param roleId
	 *            role ID
	 * @param roleFunctionId
	 *            role function ID
	 */
	void deleteRoleFunction(String roleId, String roleFunctionId);

	/**
	 * Gets a page of peers.
	 * 
	 * @param pageRequest
	 *            Page index, page size, sort information; ignored if null.
	 * @return Page of peer objects.
	 */
	RestPageResponse<MLPPeer> getPeers(RestPageRequest pageRequest);

	/**
	 * Searches peers for exact matches.
	 * 
	 * @param queryParameters
	 *            Map of field-name, field-value pairs to use as query criteria.
	 *            Accepts Boolean, Date, Integer, Long, String.
	 * @param isOr
	 *            If true, finds matches on any field-value pair (conditions are
	 *            OR-ed together); otherwise finds matches on all field-value pairs
	 *            (conditions are AND-ed together).
	 * @return List of peer objects
	 */
	List<MLPPeer> searchPeers(Map<String, Object> queryParameters, boolean isOr);

	/**
	 * Gets the peer with the specified ID.
	 * 
	 * @param peerId
	 *            Instance ID
	 * @return User object
	 */
	MLPPeer getPeer(String peerId);

	/**
	 * Creates a peer.
	 * 
	 * @param peer
	 *            Peer data. If the ID field is null a new value is generated;
	 *            otherwise the ID value is used if valid and not already known.
	 * @return Complete object, with generated information such as ID
	 */
	MLPPeer createPeer(MLPPeer peer);

	/**
	 * Updates a peer.
	 * 
	 * @param user
	 *            Peer data
	 */
	void updatePeer(MLPPeer user);

	/**
	 * Deletes a peer. Cascades the delete to peer subscriptions. If other
	 * associations remain the delete will fail.
	 * 
	 * @param peerId
	 *            Instance ID
	 */
	void deletePeer(String peerId);

	/**
	 * Gets all subscriptions for the specified peer.
	 * 
	 * @param peerId
	 *            Peer ID
	 * @return List of peer objects
	 */
	List<MLPPeerSubscription> getPeerSubscriptions(String peerId);

	/**
	 * Gets the peer subscription with the specified ID.
	 * 
	 * @param subscriptionId
	 *            Subscription ID
	 * @return Peer subscription object
	 */
	MLPPeerSubscription getPeerSubscription(Long subscriptionId);

	/**
	 * Creates a peer subscription
	 * 
	 * @param peerSub
	 *            subscription to create
	 * @return Complete object, with generated information such as ID
	 */
	MLPPeerSubscription createPeerSubscription(MLPPeerSubscription peerSub);

	/**
	 * Updates a peer subscription
	 * 
	 * @param peerSub
	 *            subscription to update
	 */
	void updatePeerSubscription(MLPPeerSubscription peerSub);

	/**
	 * Deletes a peer subscription.
	 *
	 * @param subscriptionId
	 *            Peer subscription ID
	 */
	void deletePeerSubscription(Long subscriptionId);

	/**
	 * Gets the artifact download details for the specified solution.
	 * 
	 * @param solutionId
	 *            Instance ID
	 * @param pageRequest
	 *            Page index, page size, sort information; ignored if null.
	 * @return List of solution downloads
	 */
	RestPageResponse<MLPSolutionDownload> getSolutionDownloads(String solutionId, RestPageRequest pageRequest);

	/**
	 * Creates a solution-artifact download record.
	 * 
	 * @param download
	 *            Instance to save
	 * @return Complete object.
	 */
	MLPSolutionDownload createSolutionDownload(MLPSolutionDownload download);

	/**
	 * Deletes a solution-artifact download record.
	 * 
	 * @param download
	 *            Instance to delete
	 */
	void deleteSolutionDownload(MLPSolutionDownload download);

	/**
	 * Gets a page of solutions that the specified user has marked as favorite.
	 * <P>
	 * (This does NOT return MLPSolutionFavorite objects!)
	 * 
	 * @param userId
	 *            Instance ID
	 * @param pageRequest
	 *            Page index, page size, sort information; ignored if null.
	 * @return Page of solutions that are favorites of the user; might be empty.
	 */
	RestPageResponse<MLPSolution> getFavoriteSolutions(String userId, RestPageRequest pageRequest);

	/**
	 * Creates a solution favorite record; i.e., marks a solution as a favorite of a
	 * specified user
	 * 
	 * @param fs
	 *            favorite solution model
	 * @return Complete object
	 */
	MLPSolutionFavorite createSolutionFavorite(MLPSolutionFavorite fs);

	/**
	 * Deletes a solution favorite record; i.e., unmarks a solution as a favorite of
	 * a specified user
	 * 
	 * @param fs
	 *            favorite solution model
	 */
	void deleteSolutionFavorite(MLPSolutionFavorite fs);

	/**
	 * Gets the user ratings for the specified solution.
	 * 
	 * @param solutionId
	 *            Instance ID
	 * @param pageRequest
	 *            Page index, page size, sort information; ignored if null.
	 * @return List of solution ratings
	 */
	RestPageResponse<MLPSolutionRating> getSolutionRatings(String solutionId, RestPageRequest pageRequest);

	/**
	 * Gets a rating for the specified solution and user.
	 * 
	 * @param solutionId
	 *            Solution ID
	 * @param userId
	 *            User ID
	 * @return Solution rating
	 */
	MLPSolutionRating getSolutionRating(String solutionId, String userId);

	/**
	 * Creates a solution rating.
	 * 
	 * @param rating
	 *            Instance to save
	 * @return Complete object, with generated information such as ID
	 */
	MLPSolutionRating createSolutionRating(MLPSolutionRating rating);

	/**
	 * Updates a solution rating.
	 * 
	 * @param rating
	 *            Instance to update
	 */
	void updateSolutionRating(MLPSolutionRating rating);

	/**
	 * Deletes a solution rating.
	 * 
	 * @param rating
	 *            Instance to delete
	 */
	void deleteSolutionRating(MLPSolutionRating rating);

	/**
	 * Gets the count of notifications.
	 * 
	 * @return Count of notifications.
	 */
	long getNotificationCount();

	/**
	 * Gets a page of notifications.
	 * 
	 * @param pageRequest
	 *            Page index, page size, sort information; ignored if null.
	 * @return Page of objects.
	 */
	RestPageResponse<MLPNotification> getNotifications(RestPageRequest pageRequest);

	/**
	 * Creates a notification.
	 * 
	 * @param notification
	 *            Notification data. If the ID field is null a new value is
	 *            generated; otherwise the ID value is used if valid and not already
	 *            known.
	 * @return Complete object, with generated information such as ID
	 */
	MLPNotification createNotification(MLPNotification notification);

	/**
	 * Updates a notification.
	 * 
	 * @param notification
	 *            Instance to update
	 */
	void updateNotification(MLPNotification notification);

	/**
	 * Deletes a notification. A notification can be deleted if is not associated
	 * with any user recipients; if associations remain the delete will fail.
	 * 
	 * @param notificationId
	 *            ID of instance to delete
	 */
	void deleteNotification(String notificationId);

	/**
	 * Gets a page of active notifications for the specified user, both viewed and
	 * unviewed. "Active" means the current date/time falls within the
	 * notification's begin and end timestamps.
	 * 
	 * @param userId
	 *            User ID
	 * @param pageRequest
	 *            Page index, page size, sort information; ignored if null.
	 * @return Page of objects.
	 */
	RestPageResponse<MLPUserNotification> getUserNotifications(String userId, RestPageRequest pageRequest);

	/**
	 * Adds the specified user as a recipient of the specified notification.
	 * 
	 * @param notificationId
	 *            notification ID
	 * @param userId
	 *            user ID
	 */
	void addUserToNotification(String notificationId, String userId);

	/**
	 * Drops the specified user as a recipient of the specified notification.
	 * 
	 * @param notificationId
	 *            notification ID
	 * @param userId
	 *            user ID
	 */
	void dropUserFromNotification(String notificationId, String userId);

	/**
	 * Store that the user has viewed the notification.
	 * 
	 * @param notificationId
	 *            notification ID
	 * @param userId
	 *            user ID
	 */
	void setUserViewedNotification(String notificationId, String userId);

	/**
	 * Gets website metadata about the specified solution including average rating
	 * and total download count.
	 * 
	 * @param solutionId
	 *            Solution ID
	 * @return Object with metadata
	 */
	MLPSolutionWeb getSolutionWebMetadata(String solutionId);

	/**
	 * Gets the users with access to the specified solution.
	 * 
	 * @param solutionId
	 *            Solution ID
	 * @return List of users
	 */
	List<MLPUser> getSolutionAccessUsers(String solutionId);

	/**
	 * Gets the solutions accessible to the specified user.
	 * 
	 * @param userId
	 *            User ID
	 * @param pageRequest
	 *            Page index, page size, sort information; ignored if null.
	 * @return Page of solutions
	 */
	RestPageResponse<MLPSolution> getUserAccessSolutions(String userId, RestPageRequest pageRequest);

	/**
	 * Grants access to the specified solution for the specified user.
	 * 
	 * @param solutionId
	 *            solution ID
	 * @param userId
	 *            user ID
	 */
	void addSolutionUserAccess(String solutionId, String userId);

	/**
	 * Removes access to the specified solution for the specified user.
	 * 
	 * @param solutionId
	 *            solution ID
	 * @param userId
	 *            user ID
	 */
	void dropSolutionUserAccess(String solutionId, String userId);

	/**
	 * Updates the password for the specified user. Returns an error if the old
	 * password does not match.
	 * 
	 * @param user
	 *            User object
	 * @param changeRequest
	 *            Old and new passwords
	 */
	void updatePassword(MLPUser user, MLPPasswordChangeRequest changeRequest);

	/**
	 * Gets the validation results for the specified solution revision.
	 * 
	 * @param solutionId
	 *            Instance ID
	 * @param revisionId
	 *            Instance ID
	 * @return List of solution validations
	 */
	List<MLPSolutionValidation> getSolutionValidations(String solutionId, String revisionId);

	/**
	 * Creates a solution validation record.
	 * 
	 * @param validation
	 *            Instance to save
	 * @return Complete object, with generated information such as ID
	 */
	MLPSolutionValidation createSolutionValidation(MLPSolutionValidation validation);

	/**
	 * Updates a solution validation record.
	 * 
	 * @param validation
	 *            Instance to update
	 */
	void updateSolutionValidation(MLPSolutionValidation validation);

	/**
	 * Deletes a solution validation record.
	 * 
	 * @param validation
	 *            Instance to delete
	 */
	void deleteSolutionValidation(MLPSolutionValidation validation);

	/**
	 * Gets the validation sequence records.
	 * 
	 * @return List of validation seqeuence
	 */
	List<MLPValidationSequence> getValidationSequences();

	/**
	 * Creates a validation sequence record.
	 * 
	 * @param sequence
	 *            Instance to save
	 * @return Complete object
	 */
	MLPValidationSequence createValidationSequence(MLPValidationSequence sequence);

	/**
	 * Deletes a validation sequence record.
	 * 
	 * @param sequence
	 *            Instance to delete
	 */
	void deleteValidationSequence(MLPValidationSequence sequence);

	/**
	 * Gets a page of deployments for the specified user.
	 * 
	 * @param userId
	 *            User ID
	 * @param pageRequest
	 *            Page index, page size, sort information; ignored if null.
	 * @return Page of solution deployments
	 */
	RestPageResponse<MLPSolutionDeployment> getUserDeployments(String userId, RestPageRequest pageRequest);

	/**
	 * Gets a page of deployments for the specified solution revision.
	 * 
	 * @param solutionId
	 *            Solution ID
	 * @param revisionId
	 *            Revision ID
	 * @param pageRequest
	 *            Page index, page size, sort information; ignored if null.
	 * @return Page of solution deployments
	 */
	RestPageResponse<MLPSolutionDeployment> getSolutionDeployments(String solutionId, String revisionId,
			RestPageRequest pageRequest);

	/**
	 * Gets a page of deployments for the specified solution revision and user.
	 * 
	 * @param solutionId
	 *            Solution ID
	 * @param revisionId
	 *            Revision ID
	 * @param userId
	 *            User ID
	 * @param pageRequest
	 *            Page index, page size, sort information; ignored if null.
	 * @return Page of solution deployments
	 */
	RestPageResponse<MLPSolutionDeployment> getUserSolutionDeployments(String solutionId, String revisionId,
			String userId, RestPageRequest pageRequest);

	/**
	 * Creates a solution deployment record.
	 * 
	 * @param deployment
	 *            Instance to save
	 * @return Complete object, with generated information such as ID
	 */
	MLPSolutionDeployment createSolutionDeployment(MLPSolutionDeployment deployment);

	/**
	 * Updates a solution validation record.
	 * 
	 * @param deployment
	 *            Instance to update
	 */
	void updateSolutionDeployment(MLPSolutionDeployment deployment);

	/**
	 * Deletes a solution deployment record.
	 * 
	 * @param deployment
	 *            Instance to delete
	 */
	void deleteSolutionDeployment(MLPSolutionDeployment deployment);

	/**
	 * Gets one site configuration entry.
	 * 
	 * @param configKey
	 *            Config key
	 * @return Site configuration
	 */
	MLPSiteConfig getSiteConfig(String configKey);

	/**
	 * Creates a site configuration entry.
	 * 
	 * @param config
	 *            Instance to save
	 * @return Complete object
	 */
	MLPSiteConfig createSiteConfig(MLPSiteConfig config);

	/**
	 * Updates a site configuration entry.
	 * 
	 * @param config
	 *            Instance to update
	 */
	void updateSiteConfig(MLPSiteConfig config);

	/**
	 * Deletes a site configuration entry.
	 * 
	 * @param configKey
	 *            key of instance to delete
	 */
	void deleteSiteConfig(String configKey);

	/**
	 * Gets count of threads.
	 * 
	 * @return Count of threads.
	 */
	long getThreadCount();

	/**
	 * Gets a page of threads
	 * 
	 * @param pageRequest
	 *            Page index, page size, sort information; ignored if null.
	 * @return Page of objects.
	 */
	RestPageResponse<MLPThread> getThreads(RestPageRequest pageRequest);

	/**
	 * Gets the thread with the specified ID.
	 * 
	 * @param threadId
	 *            thread ID
	 * @return Thread object
	 */
	MLPThread getThread(String threadId);

	/**
	 * Creates a thread
	 * 
	 * @param thread
	 *            Thread data. If the ID field is null a new value is generated;
	 *            otherwise the ID value is used if valid and not already known.
	 * @return Complete object, with generated information such as ID
	 */
	MLPThread createThread(MLPThread thread);

	/**
	 * Updates a thread
	 * 
	 * @param thread
	 *            Thread data
	 */
	void updateThread(MLPThread thread);

	/**
	 * Deletes a thread. Cascades the delete to comment associations.
	 * 
	 * @param threadId
	 *            thread ID
	 */
	void deleteThread(String threadId);

	/**
	 * Gets count of comments in a thread.
	 * 
	 * @param threadId
	 *            Thread ID
	 * @return Count of comments
	 */
	long getThreadCommentCount(String threadId);

	/**
	 * Gets one page of a thread of comments.
	 * 
	 * @param threadId
	 *            Thread ID
	 * @param pageRequest
	 *            Page index, page size, sort information; ignored if null.
	 * @return One page of comments in the thread, sorted as specified.
	 */
	RestPageResponse<MLPComment> getThreadComments(String threadId, RestPageRequest pageRequest);

	/**
	 * Gets the comment with the specified IDs.
	 * 
	 * @param threadId
	 *            Thread ID
	 * @param commentId
	 *            comment ID
	 * @return Comment object
	 */
	MLPComment getComment(String threadId, String commentId);

	/**
	 * Creates a comment
	 * 
	 * @param comment
	 *            Comment data. If the ID field is null a new value is generated;
	 *            otherwise the ID value is used if valid and not already known.
	 * @return Complete object, with generated information such as ID
	 */
	MLPComment createComment(MLPComment comment);

	/**
	 * Updates a comment
	 * 
	 * @param comment
	 *            Comment data
	 */
	void updateComment(MLPComment comment);

	/**
	 * Deletes a comment.
	 * 
	 * @param threadId
	 *            Thread ID
	 * @param commentId
	 *            comment ID
	 */
	void deleteComment(String threadId, String commentId);

}

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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.acumos.cds.CCDSConstants;
import org.acumos.cds.domain.MLPAccessType;
import org.acumos.cds.domain.MLPArtifact;
import org.acumos.cds.domain.MLPArtifactType;
import org.acumos.cds.domain.MLPComment;
import org.acumos.cds.domain.MLPDeploymentStatus;
import org.acumos.cds.domain.MLPLoginProvider;
import org.acumos.cds.domain.MLPModelType;
import org.acumos.cds.domain.MLPNotifUserMap;
import org.acumos.cds.domain.MLPNotification;
import org.acumos.cds.domain.MLPPasswordChangeRequest;
import org.acumos.cds.domain.MLPPeer;
import org.acumos.cds.domain.MLPPeerGroup;
import org.acumos.cds.domain.MLPPeerGrpMemMap;
import org.acumos.cds.domain.MLPPeerPeerAccMap;
import org.acumos.cds.domain.MLPPeerSolAccMap;
import org.acumos.cds.domain.MLPPeerSubscription;
import org.acumos.cds.domain.MLPRole;
import org.acumos.cds.domain.MLPRoleFunction;
import org.acumos.cds.domain.MLPSiteConfig;
import org.acumos.cds.domain.MLPSolUserAccMap;
import org.acumos.cds.domain.MLPSolution;
import org.acumos.cds.domain.MLPSolutionDeployment;
import org.acumos.cds.domain.MLPSolutionDownload;
import org.acumos.cds.domain.MLPSolutionFavorite;
import org.acumos.cds.domain.MLPSolutionGroup;
import org.acumos.cds.domain.MLPSolutionRating;
import org.acumos.cds.domain.MLPSolutionRevision;
import org.acumos.cds.domain.MLPSolutionValidation;
import org.acumos.cds.domain.MLPSolutionWeb;
import org.acumos.cds.domain.MLPStepResult;
import org.acumos.cds.domain.MLPStepStatus;
import org.acumos.cds.domain.MLPStepType;
import org.acumos.cds.domain.MLPTag;
import org.acumos.cds.domain.MLPThread;
import org.acumos.cds.domain.MLPToolkitType;
import org.acumos.cds.domain.MLPUser;
import org.acumos.cds.domain.MLPUserLoginProvider;
import org.acumos.cds.domain.MLPUserNotification;
import org.acumos.cds.domain.MLPUserNotifPref;
import org.acumos.cds.domain.MLPUserRoleMap;
import org.acumos.cds.domain.MLPValidationSequence;
import org.acumos.cds.domain.MLPValidationStatus;
import org.acumos.cds.domain.MLPValidationType;
import org.acumos.cds.transport.CountTransport;
import org.acumos.cds.transport.LoginTransport;
import org.acumos.cds.transport.RestPageRequest;
import org.acumos.cds.transport.RestPageResponse;
import org.acumos.cds.transport.SuccessTransport;
import org.acumos.cds.transport.UsersRoleRequest;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * <P>
 * Provides methods for accessing the Common Data Service API via REST. Supports
 * basic HTTP authentication. Clients should use the
 * {@link #getInstance(String, String, String)} method.
 * </P>
 *
 * <P>
 * The server sets an HTTP error code on any failure and sends the details of
 * the failure. The Spring RestTemplate behavior on receiving a non-200-class
 * response is to throw
 * {@link org.springframework.web.client.HttpStatusCodeException}. The error
 * details from the server can be fetched by calling that class's
 * getResponseBodyAsString() method.
 * </P>
 */
public class CommonDataServiceRestClientImpl implements ICommonDataServiceRestClient {

	private static Logger logger = LoggerFactory.getLogger(CommonDataServiceRestClientImpl.class);

	private final String baseUrl;
	private final RestTemplate restTemplate;

	/**
	 * Creates an instance to access the remote endpoint using the specified
	 * credentials.
	 * 
	 * If user and pass are both supplied, uses basic HTTP authentication; if either
	 * one is missing, no authentication is used.
	 * 
	 * Clients should use the static method
	 * {@link #getInstance(String, String, String)} instead of this constructor.
	 * 
	 * @param webapiUrl
	 *            URL of the web endpoint
	 * @param user
	 *            user name; ignored if null
	 * @param pass
	 *            password; ignored if null
	 */
	public CommonDataServiceRestClientImpl(final String webapiUrl, final String user, final String pass) {
		if (webapiUrl == null)
			throw new IllegalArgumentException("Null URL not permitted");

		URL url = null;
		try {
			url = new URL(webapiUrl);
			baseUrl = url.toExternalForm();
		} catch (MalformedURLException ex) {
			throw new IllegalArgumentException("Failed to parse URL", ex);
		}
		final HttpHost httpHost = new HttpHost(url.getHost(), url.getPort());

		// Build a client with a credentials provider
		CloseableHttpClient httpClient = null;
		if (user != null && pass != null) {
			CredentialsProvider credsProvider = new BasicCredentialsProvider();
			credsProvider.setCredentials(new AuthScope(httpHost), new UsernamePasswordCredentials(user, pass));
			httpClient = HttpClientBuilder.create().setDefaultCredentialsProvider(credsProvider).build();
		} else {
			httpClient = HttpClientBuilder.create().build();
		}
		// Create request factory
		HttpComponentsClientHttpRequestFactoryBasicAuth requestFactory = new HttpComponentsClientHttpRequestFactoryBasicAuth(
				httpHost);
		requestFactory.setHttpClient(httpClient);

		// Put the factory in the template
		restTemplate = new RestTemplate();
		restTemplate.setRequestFactory(requestFactory);
	}

	/**
	 * Creates an instance to access the remote endpoint using the specified
	 * template, which allows HTTP credentials, choice of route, etc.
	 * 
	 * Clients should use the static method
	 * {@link #getInstance(String, RestTemplate)} instead of this constructor.
	 * 
	 * @param webapiUrl
	 *            URL of the web endpoint
	 * @param restTemplate
	 *            REST template to use for connections
	 */
	public CommonDataServiceRestClientImpl(final String webapiUrl, final RestTemplate restTemplate) {
		if (webapiUrl == null || restTemplate == null)
			throw new IllegalArgumentException("Null not permitted");
		URL url = null;
		try {
			url = new URL(webapiUrl);
			baseUrl = url.toExternalForm();
		} catch (MalformedURLException ex) {
			throw new IllegalArgumentException("Failed to parse URL", ex);
		}
		this.restTemplate = restTemplate;
	}

	/**
	 * Gets an instance to access a remote endpoint using the specified template.
	 * This factory method is the preferred usage.
	 * 
	 * @param webapiUrl
	 *            URL of the web endpoint
	 * @param user
	 *            user name; ignored if null
	 * @param pass
	 *            password; ignored if null
	 * @return Instance of ICommonDataServiceRestClient
	 */
	public static ICommonDataServiceRestClient getInstance(String webapiUrl, String user, String pass) {
		return new CommonDataServiceRestClientImpl(webapiUrl, user, pass);
	}

	/**
	 * Gets an instance to access a remote endpoint using the specified template.
	 * 
	 * @param webapiUrl
	 *            URL of the web endpoint
	 * @param restTemplate
	 *            REST template
	 * @return Instance of ICommonDataServiceRestClient
	 */
	public static ICommonDataServiceRestClient getInstance(String webapiUrl, RestTemplate restTemplate) {
		return new CommonDataServiceRestClientImpl(webapiUrl, restTemplate);
	}

	/**
	 * Privileged access for subclasses.
	 * 
	 * @return RestTemplate configured for access to remote CDS server.
	 */
	protected RestTemplate getRestTemplate() {
		return restTemplate;
	}

	/**
	 * Builds URI by adding specified path segments and query parameters to the base
	 * URL. Converts an array of values to a series of parameters with the same
	 * name; e.g., "find foo in list [a,b]" becomes request parameters
	 * "foo=a&foo=b".
	 * 
	 * @param path
	 *            Array of path segments
	 * @param queryParams
	 *            key-value pairs; ignored if null or empty. Gives special treatment
	 *            to Date-type values, Array values, and null values inside arrays.
	 * @param restPageRequest
	 *            page, size and sort specification; ignored if null.
	 * @return
	 */
	private URI buildUri(final String[] path, final Map<String, Object> queryParams, RestPageRequest pageRequest) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(this.baseUrl);
		for (int p = 0; p < path.length; ++p)
			builder.pathSegment(path[p]);
		if (queryParams != null && queryParams.size() > 0) {
			for (Map.Entry<String, ? extends Object> entry : queryParams.entrySet()) {
				if (entry.getValue() instanceof Date) {
					// Server expects Date type as Long (not String)
					builder.queryParam(entry.getKey(), ((Date) entry.getValue()).getTime());
				} else if (entry.getValue().getClass().isArray()) {
					Object[] array = (Object[]) entry.getValue();
					for (Object o : array) {
						if (o == null)
							builder.queryParam(entry.getKey(), "null");
						else if (o instanceof Date)
							builder.queryParam(entry.getKey(), ((Date) o).getTime());
						else
							builder.queryParam(entry.getKey(), o.toString());
					}
				} else {
					builder.queryParam(entry.getKey(), entry.getValue().toString());
				}
			}
		}
		if (pageRequest != null) {
			if (pageRequest.getSize() != null)
				builder.queryParam("page", Integer.toString(pageRequest.getPage()));
			if (pageRequest.getPage() != null)
				builder.queryParam("size", Integer.toString(pageRequest.getSize()));
			if (pageRequest.getFieldToDirectionMap() != null && pageRequest.getFieldToDirectionMap().size() > 0) {
				for (Map.Entry<String, String> entry : pageRequest.getFieldToDirectionMap().entrySet()) {
					String value = entry.getKey() + (entry.getValue() == null ? "" : ("," + entry.getValue()));
					builder.queryParam("sort", value);
				}
			}
		}
		return builder.build().encode().toUri();
	}

	@Override
	public SuccessTransport getHealth() {
		URI uri = buildUri(new String[] { CCDSConstants.HEALTHCHECK_PATH }, null, null);
		logger.debug("getHealth: uri {}", uri);
		ResponseEntity<SuccessTransport> response = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<SuccessTransport>() {
				});
		return response.getBody();
	}

	@Override
	public SuccessTransport getVersion() {
		URI uri = buildUri(new String[] { CCDSConstants.VERSION_PATH }, null, null);
		logger.debug("getVersion: uri {}", uri);
		ResponseEntity<SuccessTransport> response = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<SuccessTransport>() {
				});
		return response.getBody();
	}

	@Override
	public List<MLPAccessType> getAccessTypes() {
		URI uri = buildUri(new String[] { CCDSConstants.ACCESS_PATH, CCDSConstants.TYPE_PATH }, null, null);
		logger.debug("getAccessTypes: uri {}", uri);
		ResponseEntity<List<MLPAccessType>> response = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<List<MLPAccessType>>() {
				});
		return response.getBody();
	}

	@Override
	public List<MLPArtifactType> getArtifactTypes() {
		URI uri = buildUri(new String[] { CCDSConstants.ARTIFACT_PATH, CCDSConstants.TYPE_PATH }, null, null);
		logger.debug("getArtifactTypes: uri {}", uri);
		ResponseEntity<List<MLPArtifactType>> response = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<List<MLPArtifactType>>() {
				});
		return response.getBody();
	}

	@Override
	public List<MLPLoginProvider> getLoginProviders() {
		URI uri = buildUri(new String[] { CCDSConstants.LOGIN_PROVIDER_PATH }, null, null);
		logger.debug("getLoginProviders: uri {}", uri);
		ResponseEntity<List<MLPLoginProvider>> response = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<List<MLPLoginProvider>>() {
				});
		return response.getBody();
	}

	@Override
	public List<MLPModelType> getModelTypes() {
		URI uri = buildUri(new String[] { CCDSConstants.MODEL_PATH, CCDSConstants.TYPE_PATH }, null, null);
		logger.debug("getModelTypes: uri {}", uri);
		ResponseEntity<List<MLPModelType>> response = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<List<MLPModelType>>() {
				});
		return response.getBody();
	}

	@Override
	public List<MLPToolkitType> getToolkitTypes() {
		URI uri = buildUri(new String[] { CCDSConstants.TOOLKIT_PATH, CCDSConstants.TYPE_PATH }, null, null);
		logger.debug("getTookitTypes: uri {}", uri);
		ResponseEntity<List<MLPToolkitType>> response = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<List<MLPToolkitType>>() {
				});
		return response.getBody();
	}

	@Override
	public List<MLPValidationStatus> getValidationStatuses() {
		URI uri = buildUri(new String[] { CCDSConstants.VAL_STAT_PATH }, null, null);
		logger.debug("getValidationStatuses: uri {}", uri);
		ResponseEntity<List<MLPValidationStatus>> response = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<List<MLPValidationStatus>>() {
				});
		return response.getBody();
	}

	@Override
	public List<MLPValidationType> getValidationTypes() {
		URI uri = buildUri(new String[] { CCDSConstants.VAL_TYPE_PATH }, null, null);
		logger.debug("getValidationTypes: uri {}", uri);
		ResponseEntity<List<MLPValidationType>> response = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<List<MLPValidationType>>() {
				});
		return response.getBody();
	}

	@Override
	public List<MLPDeploymentStatus> getDeploymentStatuses() {
		URI uri = buildUri(new String[] { CCDSConstants.DEP_STAT_PATH }, null, null);
		logger.debug("getDeploymentStatuses: uri {}", uri);
		ResponseEntity<List<MLPDeploymentStatus>> response = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<List<MLPDeploymentStatus>>() {
				});
		return response.getBody();
	}

	@Override
	public List<MLPStepStatus> getStepStatuses() {
		URI uri = buildUri(new String[] { CCDSConstants.STEP_STAT_PATH }, null, null);
		logger.debug("getStepStatuses: uri {}", uri);
		ResponseEntity<List<MLPStepStatus>> response = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<List<MLPStepStatus>>() {
				});
		return response.getBody();
	}

	@Override
	public List<MLPStepType> getStepTypes() {
		URI uri = buildUri(new String[] { CCDSConstants.STEP_TYPE_PATH }, null, null);
		logger.debug("getStepTypes: uri {}", uri);
		ResponseEntity<List<MLPStepType>> response = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<List<MLPStepType>>() {
				});
		return response.getBody();
	}

	@Override
	public long getSolutionCount() {
		URI uri = buildUri(new String[] { CCDSConstants.SOLUTION_PATH, CCDSConstants.COUNT_PATH }, null, null);
		logger.debug("getSolutionCount: uri {}", uri);
		ResponseEntity<CountTransport> response = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<CountTransport>() {
				});
		return response.getBody().getCount();
	}

	@Override
	public RestPageResponse<MLPSolution> getSolutions(RestPageRequest pageRequest) {
		URI uri = buildUri(new String[] { CCDSConstants.SOLUTION_PATH }, null, pageRequest);
		logger.debug("getSolutions: uri {}", uri);
		ResponseEntity<RestPageResponse<MLPSolution>> response = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<RestPageResponse<MLPSolution>>() {
				});
		return response.getBody();
	}

	@Override
	public RestPageResponse<MLPSolution> findSolutionsBySearchTerm(String searchTerm, RestPageRequest pageRequest) {
		HashMap<String, Object> parms = new HashMap<>();
		parms.put(CCDSConstants.TERM_PATH, searchTerm);
		URI uri = buildUri(
				new String[] { CCDSConstants.SOLUTION_PATH, CCDSConstants.SEARCH_PATH, CCDSConstants.LIKE_PATH }, parms,
				pageRequest);
		logger.debug("findSolutionsBySearchTerm: uri {}", uri);
		ResponseEntity<RestPageResponse<MLPSolution>> response = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<RestPageResponse<MLPSolution>>() {
				});
		return response.getBody();
	}

	@Override
	public RestPageResponse<MLPSolution> findSolutionsByTag(String tag, RestPageRequest pageRequest) {
		HashMap<String, Object> parms = new HashMap<>();
		parms.put(CCDSConstants.TAG_PATH, tag);
		URI uri = buildUri(
				new String[] { CCDSConstants.SOLUTION_PATH, CCDSConstants.SEARCH_PATH, CCDSConstants.TAG_PATH }, parms,
				pageRequest);
		logger.debug("findSolutionsByTag: uri {}", uri);
		ResponseEntity<RestPageResponse<MLPSolution>> response = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<RestPageResponse<MLPSolution>>() {
				});
		return response.getBody();
	}

	@Override
	public RestPageResponse<MLPSolution> findPortalSolutions(String[] nameKeywords, String[] descriptionKeywords,
			boolean active, String[] ownerIds, String[] accessTypeCodes, String[] modelTypeCodes,
			String[] validationStatusCodes, String[] tags, RestPageRequest pageRequest) {
		HashMap<String, Object> parms = new HashMap<>();
		// This is required
		parms.put(CCDSConstants.SEARCH_ACTIVE, active);
		if (nameKeywords != null && nameKeywords.length > 0)
			parms.put(CCDSConstants.SEARCH_NAME, nameKeywords);
		if (descriptionKeywords != null && descriptionKeywords.length > 0)
			parms.put(CCDSConstants.SEARCH_DESC, descriptionKeywords);
		if (ownerIds != null && ownerIds.length > 0)
			parms.put(CCDSConstants.SEARCH_OWNERS, ownerIds);
		if (accessTypeCodes != null && accessTypeCodes.length > 0)
			parms.put(CCDSConstants.SEARCH_ACCESS_TYPES, accessTypeCodes);
		if (modelTypeCodes != null && modelTypeCodes.length > 0)
			parms.put(CCDSConstants.SEARCH_MODEL_TYPES, modelTypeCodes);
		if (tags != null && tags.length > 0)
			parms.put(CCDSConstants.SEARCH_TAGS, tags);
		if (validationStatusCodes != null && validationStatusCodes.length > 0)
			parms.put(CCDSConstants.SEARCH_VAL_STATUSES, validationStatusCodes);
		URI uri = buildUri(
				new String[] { CCDSConstants.SOLUTION_PATH, CCDSConstants.SEARCH_PATH, CCDSConstants.PORTAL_PATH },
				parms, pageRequest);
		logger.debug("findPortalSolutions: uri {}", uri);
		ResponseEntity<RestPageResponse<MLPSolution>> response = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<RestPageResponse<MLPSolution>>() {
				});
		return response.getBody();
	}

	@Override
	public RestPageResponse<MLPSolution> searchSolutions(Map<String, Object> queryParameters, boolean isOr,
			RestPageRequest pageRequest) {
		Map<String, Object> copy = new HashMap<>(queryParameters);
		copy.put(CCDSConstants.JUNCTION_QUERY_PARAM, isOr ? "o" : "a");
		URI uri = buildUri(new String[] { CCDSConstants.SOLUTION_PATH, CCDSConstants.SEARCH_PATH }, copy, pageRequest);
		logger.debug("searchSolutions: uri {}", uri);
		ResponseEntity<RestPageResponse<MLPSolution>> response = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<RestPageResponse<MLPSolution>>() {
				});
		return response.getBody();
	}

	@Override
	public MLPSolution getSolution(String solutionId) {
		URI uri = buildUri(new String[] { CCDSConstants.SOLUTION_PATH, solutionId }, null, null);
		logger.debug("getSolution: uri {}", uri);
		ResponseEntity<MLPSolution> response = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<MLPSolution>() {
				});
		return response.getBody();
	}

	@Override
	public MLPSolution createSolution(MLPSolution solution) {
		URI uri = buildUri(new String[] { CCDSConstants.SOLUTION_PATH }, null, null);
		logger.debug("createSolution: uri {}", uri);
		return restTemplate.postForObject(uri, solution, MLPSolution.class);
	}

	@Override
	public void updateSolution(MLPSolution solution) {
		URI uri = buildUri(new String[] { CCDSConstants.SOLUTION_PATH, solution.getSolutionId() }, null, null);
		logger.debug("updateSolution: url {}", uri);
		restTemplate.put(uri, solution);
	}

	@Override
	public void incrementSolutionViewCount(String solutionId) {
		URI uri = buildUri(new String[] { CCDSConstants.SOLUTION_PATH, solutionId, CCDSConstants.VIEW_PATH }, null,
				null);
		logger.debug("incrementSolutionViewCount: url {}", uri);
		restTemplate.put(uri, null);
	}

	@Override
	public void deleteSolution(String solutionId) {
		URI uri = buildUri(new String[] { CCDSConstants.SOLUTION_PATH, solutionId }, null, null);
		logger.debug("deleteSolution: uri {}", uri);
		restTemplate.delete(uri);
	}

	@Override
	public List<MLPSolutionRevision> getSolutionRevisions(String solutionId) {
		return getSolutionRevisions(new String[] { solutionId });
	}

	@Override
	public List<MLPSolutionRevision> getSolutionRevisions(String[] solutionIds) {
		// Send solution IDs as a CSV list
		String csvSolIds = String.join(",", solutionIds);
		URI uri = buildUri(new String[] { CCDSConstants.SOLUTION_PATH, csvSolIds, CCDSConstants.REVISION_PATH }, null,
				null);
		logger.debug("getSolutionRevisions: uri {}", uri);
		ResponseEntity<List<MLPSolutionRevision>> response = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<List<MLPSolutionRevision>>() {
				});
		return response.getBody();
	}

	@Override
	public List<MLPSolutionRevision> getSolutionRevisionsForArtifact(String artifactId) {
		URI uri = buildUri(new String[] { CCDSConstants.ARTIFACT_PATH, artifactId, CCDSConstants.REVISION_PATH }, null,
				null);
		logger.debug("getSolutionRevisionsForArtifact: uri {}", uri);
		ResponseEntity<List<MLPSolutionRevision>> response = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<List<MLPSolutionRevision>>() {
				});
		return response.getBody();
	}

	@Override
	public MLPSolutionRevision getSolutionRevision(String solutionId, String revisionId) {
		URI uri = buildUri(
				new String[] { CCDSConstants.SOLUTION_PATH, solutionId, CCDSConstants.REVISION_PATH, revisionId }, null,
				null);
		logger.debug("getSolutionRevision: uri {}", uri);
		ResponseEntity<MLPSolutionRevision> response = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<MLPSolutionRevision>() {
				});
		return response.getBody();
	}

	@Override
	public MLPSolutionRevision createSolutionRevision(MLPSolutionRevision revision) {
		URI uri = buildUri(
				new String[] { CCDSConstants.SOLUTION_PATH, revision.getSolutionId(), CCDSConstants.REVISION_PATH },
				null, null);
		logger.debug("createSolutionRevision: uri {}", uri);
		return restTemplate.postForObject(uri, revision, MLPSolutionRevision.class);
	}

	@Override
	public void updateSolutionRevision(MLPSolutionRevision revision) {
		URI uri = buildUri(new String[] { CCDSConstants.SOLUTION_PATH, revision.getSolutionId(),
				CCDSConstants.REVISION_PATH, revision.getRevisionId() }, null, null);
		logger.debug("updateSolutionRevision: uri {}", uri);
		restTemplate.put(uri, revision);
	}

	@Override
	public void deleteSolutionRevision(String solutionId, String revisionId) {
		URI uri = buildUri(
				new String[] { CCDSConstants.SOLUTION_PATH, solutionId, CCDSConstants.REVISION_PATH, revisionId }, null,
				null);
		logger.debug("deleteSolutionRevision: uri {}", uri);
		restTemplate.delete(uri);
	}

	@Override
	public List<MLPArtifact> getSolutionRevisionArtifacts(String solutionId, String revisionId) {
		URI uri = buildUri(new String[] { CCDSConstants.SOLUTION_PATH, solutionId, CCDSConstants.REVISION_PATH,
				revisionId, CCDSConstants.ARTIFACT_PATH }, null, null);
		logger.debug("getSolutionRevisionArtifacts: uri {}", uri);
		ResponseEntity<List<MLPArtifact>> response = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<List<MLPArtifact>>() {
				});
		return response.getBody();
	}

	@Override
	public void addSolutionRevisionArtifact(String solutionId, String revisionId, String artifactId) {
		URI uri = buildUri(new String[] { CCDSConstants.SOLUTION_PATH, solutionId, CCDSConstants.REVISION_PATH,
				revisionId, CCDSConstants.ARTIFACT_PATH, artifactId }, null, null);
		logger.debug("addSolutionRevisionArtifact: url {}", uri);
		restTemplate.postForLocation(uri, null);
	}

	@Override
	public void dropSolutionRevisionArtifact(String solutionId, String revisionId, String artifactId) {
		URI uri = buildUri(new String[] { CCDSConstants.SOLUTION_PATH, solutionId, CCDSConstants.REVISION_PATH,
				revisionId, CCDSConstants.ARTIFACT_PATH, artifactId }, null, null);
		logger.debug("dropSolutionRevisionArtifact: url {}", uri);
		restTemplate.delete(uri);
	}

	@Override
	public RestPageResponse<MLPTag> getTags(RestPageRequest pageRequest) {
		URI uri = buildUri(new String[] { CCDSConstants.TAG_PATH }, null, pageRequest);
		logger.debug("getTags: uri {}", uri);
		ResponseEntity<RestPageResponse<MLPTag>> response = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<RestPageResponse<MLPTag>>() {
				});
		return response.getBody();
	}

	@Override
	public MLPTag createTag(MLPTag tag) {
		URI uri = buildUri(new String[] { CCDSConstants.TAG_PATH }, null, null);
		logger.debug("createTag: uri {}", uri);
		return restTemplate.postForObject(uri, tag, MLPTag.class);
	}

	@Override
	public void deleteTag(MLPTag tag) {
		URI uri = buildUri(new String[] { CCDSConstants.TAG_PATH, tag.getTag() }, null, null);
		logger.debug("deleteTag: uri {}", uri);
		restTemplate.delete(uri);
	}

	@Override
	public List<MLPTag> getSolutionTags(String solutionId) {
		URI uri = buildUri(new String[] { CCDSConstants.SOLUTION_PATH, solutionId, CCDSConstants.TAG_PATH }, null,
				null);
		logger.debug("getSolutionTags: uri {}", uri);
		ResponseEntity<List<MLPTag>> response = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<List<MLPTag>>() {
				});
		return response.getBody();
	}

	@Override
	public void addSolutionTag(String solutionId, String tag) {
		URI uri = buildUri(new String[] { CCDSConstants.SOLUTION_PATH, solutionId, CCDSConstants.TAG_PATH, tag }, null,
				null);
		logger.debug("addSolutionTag: uri {}", uri);
		restTemplate.postForLocation(uri, null);
	}

	@Override
	public void dropSolutionTag(String solutionId, String tag) {
		URI uri = buildUri(new String[] { CCDSConstants.SOLUTION_PATH, solutionId, CCDSConstants.TAG_PATH, tag }, null,
				null);
		logger.debug("dropSolutionTag: uri {}", uri);
		restTemplate.delete(uri);
	}

	@Override
	public long getArtifactCount() {
		URI uri = buildUri(new String[] { CCDSConstants.ARTIFACT_PATH, CCDSConstants.COUNT_PATH }, null, null);
		logger.debug("getArtifactCount: uri {}", uri);
		ResponseEntity<CountTransport> response = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<CountTransport>() {
				});
		return response.getBody().getCount();
	}

	@Override
	public RestPageResponse<MLPArtifact> getArtifacts(RestPageRequest pageRequest) {
		URI uri = buildUri(new String[] { CCDSConstants.ARTIFACT_PATH }, null, pageRequest);
		logger.debug("getArtifacts: uri {}", uri);
		ResponseEntity<RestPageResponse<MLPArtifact>> response = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<RestPageResponse<MLPArtifact>>() {
				});
		return response.getBody();
	}

	@Override
	public RestPageResponse<MLPArtifact> findArtifactsBySearchTerm(String searchTerm, RestPageRequest pageRequest) {
		HashMap<String, Object> parms = new HashMap<>();
		parms.put(CCDSConstants.TERM_PATH, searchTerm);
		URI uri = buildUri(new String[] { CCDSConstants.ARTIFACT_PATH, CCDSConstants.LIKE_PATH }, parms, pageRequest);
		logger.debug("findArtifactsBySearchTerm: uri {}", uri);
		ResponseEntity<RestPageResponse<MLPArtifact>> response = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<RestPageResponse<MLPArtifact>>() {
				});
		return response.getBody();
	}

	@Override
	public RestPageResponse<MLPArtifact> searchArtifacts(Map<String, Object> queryParameters, boolean isOr,
			RestPageRequest pageRequest) {
		Map<String, Object> copy = new HashMap<>(queryParameters);
		copy.put(CCDSConstants.JUNCTION_QUERY_PARAM, isOr ? "o" : "a");
		URI uri = buildUri(new String[] { CCDSConstants.ARTIFACT_PATH, CCDSConstants.SEARCH_PATH }, copy, pageRequest);
		logger.debug("searchArtifacts: uri {}", uri);
		ResponseEntity<RestPageResponse<MLPArtifact>> response = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<RestPageResponse<MLPArtifact>>() {
				});
		return response.getBody();
	}

	@Override
	public MLPArtifact getArtifact(String artifactId) {
		URI uri = buildUri(new String[] { CCDSConstants.ARTIFACT_PATH, artifactId }, null, null);
		logger.debug("getArtifact: uri {}", uri);
		ResponseEntity<MLPArtifact> response = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<MLPArtifact>() {
				});
		return response.getBody();
	}

	@Override
	public MLPArtifact createArtifact(MLPArtifact artifact) {
		URI uri = buildUri(new String[] { CCDSConstants.ARTIFACT_PATH }, null, null);
		logger.debug("createArtifact: url {}", uri);
		return restTemplate.postForObject(uri, artifact, MLPArtifact.class);
	}

	@Override
	public void updateArtifact(MLPArtifact art) {
		URI uri = buildUri(new String[] { CCDSConstants.ARTIFACT_PATH, art.getArtifactId() }, null, null);
		logger.debug("updateArtifact: uri {}", uri);
		restTemplate.put(uri, art);
	}

	@Override
	public void deleteArtifact(String artifactId) {
		URI uri = buildUri(new String[] { CCDSConstants.ARTIFACT_PATH, artifactId }, null, null);
		logger.debug("deleteArtifact: url {}", uri);
		restTemplate.delete(uri);
	}

	@Override
	public long getUserCount() {
		URI uri = buildUri(new String[] { CCDSConstants.USER_PATH, CCDSConstants.COUNT_PATH }, null, null);
		logger.debug("getUserCount: uri {}", uri);
		ResponseEntity<CountTransport> response = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<CountTransport>() {
				});
		return response.getBody().getCount();
	}

	@Override
	public RestPageResponse<MLPUser> getUsers(RestPageRequest pageRequest) {
		URI uri = buildUri(new String[] { CCDSConstants.USER_PATH }, null, pageRequest);
		logger.debug("getUsers: uri {}", uri);
		ResponseEntity<RestPageResponse<MLPUser>> response = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<RestPageResponse<MLPUser>>() {
				});
		return response.getBody();
	}

	@Override
	public RestPageResponse<MLPUser> findUsersBySearchTerm(String searchTerm, RestPageRequest pageRequest) {
		HashMap<String, Object> parms = new HashMap<>();
		parms.put(CCDSConstants.TERM_PATH, searchTerm);
		URI uri = buildUri(new String[] { CCDSConstants.USER_PATH, CCDSConstants.LIKE_PATH }, parms, pageRequest);
		logger.debug("findUsersBySearchTerm: uri {}", uri);
		ResponseEntity<RestPageResponse<MLPUser>> response = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<RestPageResponse<MLPUser>>() {
				});
		return response.getBody();
	}

	@Override
	public RestPageResponse<MLPUser> searchUsers(Map<String, Object> queryParameters, boolean isOr,
			RestPageRequest pageRequest) {
		Map<String, Object> copy = new HashMap<>(queryParameters);
		copy.put(CCDSConstants.JUNCTION_QUERY_PARAM, isOr ? "o" : "a");
		URI uri = buildUri(new String[] { CCDSConstants.USER_PATH, CCDSConstants.SEARCH_PATH }, copy, pageRequest);
		logger.debug("searchUsers: uri {}", uri);
		ResponseEntity<RestPageResponse<MLPUser>> response = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<RestPageResponse<MLPUser>>() {
				});
		return response.getBody();
	}

	@Override
	public MLPUser loginUser(String name, String pass) {
		URI uri = buildUri(new String[] { CCDSConstants.USER_PATH, CCDSConstants.LOGIN_PATH }, null, null);
		logger.debug("loginUser: uri {}", uri);
		LoginTransport credentials = new LoginTransport(name, pass);
		return restTemplate.postForObject(uri, credentials, MLPUser.class);
	}

	@Override
	public MLPUser getUser(String userId) {
		URI uri = buildUri(new String[] { CCDSConstants.USER_PATH, userId }, null, null);
		logger.debug("getUser: uri {}", uri);
		ResponseEntity<MLPUser> response = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<MLPUser>() {
				});
		return response.getBody();
	}

	@Override
	public MLPUser createUser(MLPUser solution) {
		URI uri = buildUri(new String[] { CCDSConstants.USER_PATH }, null, null);
		logger.debug("createUser: uri {}", uri);
		return restTemplate.postForObject(uri, solution, MLPUser.class);
	}

	@Override
	public void updateUser(MLPUser user) {
		URI uri = buildUri(new String[] { CCDSConstants.USER_PATH, user.getUserId() }, null, null);
		logger.debug("updateUser: url {}", uri);
		restTemplate.put(uri, user);
	}

	@Override
	public void updatePassword(MLPUser user, MLPPasswordChangeRequest changeRequest) {
		URI uri = buildUri(new String[] { CCDSConstants.USER_PATH, user.getUserId(), CCDSConstants.CHPASS_PATH }, null,
				null);
		logger.debug("updatePassword: url {}", uri);
		restTemplate.put(uri, changeRequest);
	}

	@Override
	public void deleteUser(String userId) {
		URI uri = buildUri(new String[] { CCDSConstants.USER_PATH, userId }, null, null);
		logger.debug("deleteUser: url {}", uri);
		restTemplate.delete(uri);
	}

	@Override
	public MLPUserLoginProvider getUserLoginProvider(String userId, String providerCode, String providerLogin) {
		URI uri = buildUri(new String[] { CCDSConstants.USER_PATH, userId, CCDSConstants.LOGIN_PROVIDER_PATH,
				providerCode, CCDSConstants.LOGIN_PATH, providerLogin }, null, null);
		logger.debug("getUserLoginProvider: url {}", uri);
		ResponseEntity<MLPUserLoginProvider> response = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<MLPUserLoginProvider>() {
				});
		return response.getBody();
	}

	@Override
	public List<MLPUserLoginProvider> getUserLoginProviders(String userId) {
		URI uri = buildUri(new String[] { CCDSConstants.USER_PATH, userId, CCDSConstants.LOGIN_PROVIDER_PATH }, null,
				null);
		logger.debug("getUserLoginProviders: url {}", uri);
		ResponseEntity<List<MLPUserLoginProvider>> response = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<List<MLPUserLoginProvider>>() {
				});
		return response.getBody();
	}

	@Override
	public MLPUserLoginProvider createUserLoginProvider(MLPUserLoginProvider provider) {
		URI uri = buildUri(
				new String[] { CCDSConstants.USER_PATH, provider.getUserId(), CCDSConstants.LOGIN_PROVIDER_PATH,
						provider.getProviderCode(), CCDSConstants.LOGIN_PATH, provider.getProviderUserId() },
				null, null);
		logger.debug("createUserLoginProvider: url {}", uri);
		return restTemplate.postForObject(uri, provider, MLPUserLoginProvider.class);
	}

	@Override
	public void updateUserLoginProvider(MLPUserLoginProvider provider) {
		URI uri = buildUri(
				new String[] { CCDSConstants.USER_PATH, provider.getUserId(), CCDSConstants.LOGIN_PROVIDER_PATH,
						provider.getProviderCode(), CCDSConstants.LOGIN_PATH, provider.getProviderUserId() },
				null, null);
		logger.debug("updateUserLoginProvider: url {}", uri);
		restTemplate.put(uri, provider);
	}

	@Override
	public void deleteUserLoginProvider(MLPUserLoginProvider provider) {
		URI uri = buildUri(
				new String[] { CCDSConstants.USER_PATH, provider.getUserId(), CCDSConstants.LOGIN_PROVIDER_PATH,
						provider.getProviderCode(), CCDSConstants.LOGIN_PATH, provider.getProviderUserId() },
				null, null);
		logger.debug("deleteUserLoginProvider: url {}", uri);
		restTemplate.delete(uri);
	}

	@Override
	public RestPageResponse<MLPRole> getRoles(RestPageRequest pageRequest) {
		URI uri = buildUri(new String[] { CCDSConstants.ROLE_PATH }, null, pageRequest);
		logger.debug("getRoles: uri {}", uri);
		ResponseEntity<RestPageResponse<MLPRole>> response = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<RestPageResponse<MLPRole>>() {
				});
		return response.getBody();
	}

	@Override
	public long getRoleCount() {
		URI uri = buildUri(new String[] { CCDSConstants.ROLE_PATH, CCDSConstants.COUNT_PATH }, null, null);
		logger.debug("getRoleCount: uri {}", uri);
		ResponseEntity<CountTransport> response = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<CountTransport>() {
				});
		return response.getBody().getCount();
	}

	@Override
	public RestPageResponse<MLPRole> searchRoles(Map<String, Object> queryParameters, boolean isOr,
			RestPageRequest pageRequest) {
		Map<String, Object> copy = new HashMap<>(queryParameters);
		copy.put(CCDSConstants.JUNCTION_QUERY_PARAM, isOr ? "o" : "a");
		URI uri = buildUri(new String[] { CCDSConstants.ROLE_PATH, CCDSConstants.SEARCH_PATH }, copy, pageRequest);
		logger.debug("searchRoles: uri {}", uri);
		ResponseEntity<RestPageResponse<MLPRole>> response = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<RestPageResponse<MLPRole>>() {
				});
		return response.getBody();
	}

	@Override
	public List<MLPRole> getUserRoles(String userId) {
		URI uri = buildUri(new String[] { CCDSConstants.USER_PATH, userId, CCDSConstants.ROLE_PATH }, null, null);
		logger.debug("getUserRoles: uri {}", uri);
		ResponseEntity<List<MLPRole>> response = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<List<MLPRole>>() {
				});
		return response.getBody();
	}

	@Override
	public void addUserRole(String userId, String roleId) {
		URI uri = buildUri(new String[] { CCDSConstants.USER_PATH, userId, CCDSConstants.ROLE_PATH, roleId }, null,
				null);
		logger.debug("addUserRole: uri {}", uri);
		MLPUserRoleMap map = new MLPUserRoleMap(userId, roleId);
		restTemplate.postForObject(uri, map, SuccessTransport.class);
	}

	@Override
	public void updateUserRoles(String userId, List<String> roleIds) {
		URI uri = buildUri(new String[] { CCDSConstants.USER_PATH, userId, CCDSConstants.ROLE_PATH }, null, null);
		logger.debug("updateUserRoles: uri {}", uri);
		restTemplate.put(uri, roleIds);
	}

	@Override
	public void dropUserRole(String userId, String roleId) {
		URI uri = buildUri(new String[] { CCDSConstants.USER_PATH, userId, CCDSConstants.ROLE_PATH, roleId }, null,
				null);
		logger.debug("dropUserRole: uri {}", uri);
		restTemplate.delete(uri);
	}

	@Override
	public void addUsersInRole(List<String> userIds, String roleId) {
		URI uri = buildUri(new String[] { CCDSConstants.USER_PATH, CCDSConstants.ROLE_PATH, roleId }, null, null);
		logger.debug("addUsersInRole: uri {}", uri);
		UsersRoleRequest request = new UsersRoleRequest(true, userIds, roleId);
		restTemplate.put(uri, request);
	}

	@Override
	public void dropUsersInRole(List<String> userIds, String roleId) {
		URI uri = buildUri(new String[] { CCDSConstants.USER_PATH, CCDSConstants.ROLE_PATH, roleId }, null, null);
		logger.debug("dropUsersInRole: uri {}", uri);
		UsersRoleRequest request = new UsersRoleRequest(false, userIds, roleId);
		restTemplate.put(uri, request);
	}

	@Override
	public long getRoleUsersCount(String roleId) {
		URI uri = buildUri(
				new String[] { CCDSConstants.USER_PATH, CCDSConstants.ROLE_PATH, roleId, CCDSConstants.COUNT_PATH },
				null, null);
		logger.debug("getRoleUsersCount: uri {}", uri);
		ResponseEntity<CountTransport> response = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<CountTransport>() {
				});
		return response.getBody().getCount();
	}

	@Override
	public MLPRole getRole(String roleId) {
		URI uri = buildUri(new String[] { CCDSConstants.ROLE_PATH, roleId }, null, null);
		logger.debug("getRole: uri {}", uri);
		ResponseEntity<MLPRole> response = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<MLPRole>() {
				});
		return response.getBody();
	}

	@Override
	public MLPRole createRole(MLPRole role) {
		URI uri = buildUri(new String[] { CCDSConstants.ROLE_PATH }, null, null);
		logger.debug("createRole: uri {}", uri);
		return restTemplate.postForObject(uri, role, MLPRole.class);
	}

	@Override
	public void updateRole(MLPRole role) {
		URI uri = buildUri(new String[] { CCDSConstants.ROLE_PATH, role.getRoleId() }, null, null);
		logger.debug("updateRole: url {}", uri);
		restTemplate.put(uri, role);
	}

	@Override
	public void deleteRole(String roleId) {
		URI uri = buildUri(new String[] { CCDSConstants.ROLE_PATH, roleId }, null, null);
		logger.debug("deleteRole: url {}", uri);
		restTemplate.delete(uri);
	}

	@Override
	public List<MLPRoleFunction> getRoleFunctions(String roleId) {
		URI uri = buildUri(new String[] { CCDSConstants.ROLE_PATH, roleId, CCDSConstants.FUNCTION_PATH }, null, null);
		logger.debug("getRoleFunctions: uri {}", uri);
		ResponseEntity<List<MLPRoleFunction>> response = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<List<MLPRoleFunction>>() {
				});
		return response.getBody();
	}

	@Override
	public MLPRoleFunction getRoleFunction(String roleId, String roleFunctionId) {
		URI uri = buildUri(
				new String[] { CCDSConstants.ROLE_PATH, roleId, CCDSConstants.FUNCTION_PATH, roleFunctionId }, null,
				null);
		logger.debug("getRoleFunction: uri {}", uri);
		ResponseEntity<MLPRoleFunction> response = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<MLPRoleFunction>() {
				});
		return response.getBody();
	}

	@Override
	public MLPRoleFunction createRoleFunction(MLPRoleFunction roleFunction) {
		URI uri = buildUri(
				new String[] { CCDSConstants.ROLE_PATH, roleFunction.getRoleId(), CCDSConstants.FUNCTION_PATH }, null,
				null);
		logger.debug("createRoleFunction: uri {}", uri);
		return restTemplate.postForObject(uri, roleFunction, MLPRoleFunction.class);
	}

	@Override
	public void updateRoleFunction(MLPRoleFunction roleFunction) {
		URI uri = buildUri(new String[] { CCDSConstants.ROLE_PATH, roleFunction.getRoleId(),
				CCDSConstants.FUNCTION_PATH, roleFunction.getRoleFunctionId() }, null, null);
		logger.debug("updateRoleFunction: uri {}", uri);
		restTemplate.put(uri, roleFunction);
	}

	@Override
	public void deleteRoleFunction(String roleId, String roleFunctionId) {
		URI uri = buildUri(
				new String[] { CCDSConstants.ROLE_PATH, roleId, CCDSConstants.FUNCTION_PATH, roleFunctionId }, null,
				null);
		logger.debug("deleteRoleFunction: uri {}", uri);
		restTemplate.delete(uri);
	}

	@Override
	public RestPageResponse<MLPPeer> getPeers(RestPageRequest pageRequest) {
		URI uri = buildUri(new String[] { CCDSConstants.PEER_PATH }, null, pageRequest);
		logger.debug("getPeers: uri {}", uri);
		ResponseEntity<RestPageResponse<MLPPeer>> response = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<RestPageResponse<MLPPeer>>() {
				});
		return response.getBody();
	}

	@Override
	public RestPageResponse<MLPPeer> searchPeers(Map<String, Object> queryParameters, boolean isOr,
			RestPageRequest pageRequest) {
		Map<String, Object> copy = new HashMap<>(queryParameters);
		copy.put(CCDSConstants.JUNCTION_QUERY_PARAM, isOr ? "o" : "a");
		URI uri = buildUri(new String[] { CCDSConstants.PEER_PATH, CCDSConstants.SEARCH_PATH }, copy, pageRequest);
		logger.debug("searchPeers: uri {}", uri);
		ResponseEntity<RestPageResponse<MLPPeer>> response = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<RestPageResponse<MLPPeer>>() {
				});
		return response.getBody();
	}

	@Override
	public MLPPeer getPeer(String peerId) {
		URI uri = buildUri(new String[] { CCDSConstants.PEER_PATH, peerId }, null, null);
		logger.debug("getPeer: uri {}", uri);
		ResponseEntity<MLPPeer> response = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<MLPPeer>() {
				});
		return response.getBody();
	}

	@Override
	public MLPPeer createPeer(MLPPeer solution) {
		URI uri = buildUri(new String[] { CCDSConstants.PEER_PATH }, null, null);
		logger.debug("createPeer: uri {}", uri);
		return restTemplate.postForObject(uri, solution, MLPPeer.class);
	}

	@Override
	public void updatePeer(MLPPeer peer) {
		URI uri = buildUri(new String[] { CCDSConstants.PEER_PATH, peer.getPeerId() }, null, null);
		logger.debug("updatePeer: url {}", uri);
		restTemplate.put(uri, peer);
	}

	@Override
	public void deletePeer(String peerId) {
		URI uri = buildUri(new String[] { CCDSConstants.PEER_PATH, peerId }, null, null);
		logger.debug("deletePeer: url {}", uri);
		restTemplate.delete(uri);
	}

	@Override
	public RestPageResponse<MLPSolutionDownload> getSolutionDownloads(String solutionId, RestPageRequest pageRequest) {
		URI uri = buildUri(new String[] { CCDSConstants.SOLUTION_PATH, solutionId, CCDSConstants.DOWNLOAD_PATH }, null,
				pageRequest);
		logger.debug("getSolutionDownloads: uri {}", uri);
		ResponseEntity<RestPageResponse<MLPSolutionDownload>> response = restTemplate.exchange(uri, HttpMethod.GET,
				null, new ParameterizedTypeReference<RestPageResponse<MLPSolutionDownload>>() {
				});
		return response.getBody();
	}

	@Override
	public MLPSolutionDownload createSolutionDownload(MLPSolutionDownload download) {
		URI uri = buildUri(new String[] { CCDSConstants.SOLUTION_PATH, download.getSolutionId(),
				CCDSConstants.DOWNLOAD_PATH, CCDSConstants.ARTIFACT_PATH, download.getArtifactId(),
				CCDSConstants.USER_PATH, download.getUserId(), }, null, null);
		logger.debug("createSolutionDownload: uri {}", uri);
		return restTemplate.postForObject(uri, download, MLPSolutionDownload.class);
	}

	@Override
	public void deleteSolutionDownload(MLPSolutionDownload sd) {
		URI uri = buildUri(new String[] { CCDSConstants.SOLUTION_PATH, sd.getSolutionId(), CCDSConstants.DOWNLOAD_PATH,
				Long.toString(sd.getDownloadId()) }, null, null);
		logger.debug("deleteSolutionDownload: url {}", uri);
		restTemplate.delete(uri);
	}

	@Override
	public RestPageResponse<MLPSolution> getFavoriteSolutions(String userId, RestPageRequest pageRequest) {
		URI uri = buildUri(new String[] { CCDSConstants.USER_PATH, userId, CCDSConstants.FAVORITE_PATH,
				CCDSConstants.SOLUTION_PATH }, null, pageRequest);
		logger.debug("getFavoriteSolutions: uri {}", uri);
		ResponseEntity<RestPageResponse<MLPSolution>> response = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<RestPageResponse<MLPSolution>>() {
				});
		return response.getBody();
	}

	@Override
	public MLPSolutionFavorite createSolutionFavorite(MLPSolutionFavorite solfav) {
		URI uri = buildUri(new String[] { CCDSConstants.USER_PATH, solfav.getUserId(), CCDSConstants.FAVORITE_PATH,
				CCDSConstants.SOLUTION_PATH, solfav.getSolutionId() }, null, null);
		logger.debug("createSolutionFavorite: uri {}", uri);
		return restTemplate.postForObject(uri, solfav, MLPSolutionFavorite.class);
	}

	@Override
	public void deleteSolutionFavorite(MLPSolutionFavorite solfav) {
		URI uri = buildUri(new String[] { CCDSConstants.USER_PATH, solfav.getUserId(), CCDSConstants.FAVORITE_PATH,
				CCDSConstants.SOLUTION_PATH, solfav.getSolutionId() }, null, null);
		logger.debug("deleteSolutionFavorite: url {}", uri);
		restTemplate.delete(uri);
	}

	@Override
	public RestPageResponse<MLPSolutionRating> getSolutionRatings(String solutionId, RestPageRequest pageRequest) {
		URI uri = buildUri(new String[] { CCDSConstants.SOLUTION_PATH, solutionId, CCDSConstants.RATING_PATH }, null,
				pageRequest);
		logger.debug("getSolutionRatings: uri {}", uri);
		ResponseEntity<RestPageResponse<MLPSolutionRating>> response = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<RestPageResponse<MLPSolutionRating>>() {
				});
		return response.getBody();
	}

	@Override
	public MLPSolutionRating getSolutionRating(String solutionId, String userId) {
		URI uri = buildUri(new String[] { CCDSConstants.SOLUTION_PATH, solutionId, CCDSConstants.RATING_PATH,
				CCDSConstants.USER_PATH, userId }, null, null);
		logger.debug("getSolutionRating: uri {}", uri);
		ResponseEntity<MLPSolutionRating> response = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<MLPSolutionRating>() {
				});
		return response.getBody();
	}

	@Override
	public MLPSolutionRating createSolutionRating(MLPSolutionRating rating) {
		URI uri = buildUri(new String[] { CCDSConstants.SOLUTION_PATH, rating.getSolutionId(),
				CCDSConstants.RATING_PATH, CCDSConstants.USER_PATH, rating.getUserId() }, null, null);
		logger.debug("createSolutionRating: uri {}", uri);
		return restTemplate.postForObject(uri, rating, MLPSolutionRating.class);
	}

	@Override
	public void updateSolutionRating(MLPSolutionRating rating) {
		URI uri = buildUri(new String[] { CCDSConstants.SOLUTION_PATH, rating.getSolutionId(),
				CCDSConstants.RATING_PATH, CCDSConstants.USER_PATH, rating.getUserId() }, null, null);
		logger.debug("updateSolutionRating: url {}", uri);
		restTemplate.put(uri, rating);
	}

	@Override
	public void deleteSolutionRating(MLPSolutionRating rating) {
		URI uri = buildUri(new String[] { CCDSConstants.SOLUTION_PATH, rating.getSolutionId(),
				CCDSConstants.RATING_PATH, CCDSConstants.USER_PATH, rating.getUserId() }, null, null);
		logger.debug("deleteSolutionRating: url {}", uri);
		restTemplate.delete(uri);
	}

	@Override
	public List<MLPPeerSubscription> getPeerSubscriptions(String peerId) {
		URI uri = buildUri(new String[] { CCDSConstants.PEER_PATH, peerId, CCDSConstants.SUBSCRIPTION_PATH }, null,
				null);
		logger.debug("getPeerSubscriptions: uri {}", uri);
		ResponseEntity<List<MLPPeerSubscription>> response = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<List<MLPPeerSubscription>>() {
				});
		return response.getBody();
	}

	@Override
	public MLPPeerSubscription getPeerSubscription(Long subscriptionId) {
		URI uri = buildUri(new String[] { CCDSConstants.PEER_PATH, CCDSConstants.SUBSCRIPTION_PATH,
				Long.toString(subscriptionId) }, null, null);
		logger.debug("getPeerSubscription: uri {}", uri);
		ResponseEntity<MLPPeerSubscription> response = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<MLPPeerSubscription>() {
				});
		return response.getBody();
	}

	@Override
	public MLPPeerSubscription createPeerSubscription(MLPPeerSubscription peerSub) {
		URI uri = buildUri(new String[] { CCDSConstants.PEER_PATH, CCDSConstants.SUBSCRIPTION_PATH }, null, null);
		logger.debug("createPeerSubscription: uri {}", uri);
		return restTemplate.postForObject(uri, peerSub, MLPPeerSubscription.class);
	}

	@Override
	public void updatePeerSubscription(MLPPeerSubscription peerSub) {
		URI uri = buildUri(new String[] { CCDSConstants.PEER_PATH, CCDSConstants.SUBSCRIPTION_PATH,
				Long.toString(peerSub.getSubId()) }, null, null);
		logger.debug("updatePeerSubscription: url {}", uri);
		restTemplate.put(uri, peerSub);
	}

	@Override
	public void deletePeerSubscription(Long subscriptionId) {
		URI uri = buildUri(new String[] { CCDSConstants.PEER_PATH, CCDSConstants.SUBSCRIPTION_PATH,
				Long.toString(subscriptionId) }, null, null);
		logger.debug("deletePeerSubscription: url {}", uri);
		restTemplate.delete(uri);
	}

	@Override
	public long getNotificationCount() {
		URI uri = buildUri(new String[] { CCDSConstants.NOTIFICATION_PATH, CCDSConstants.COUNT_PATH }, null, null);
		logger.debug("getNotificationCount: uri {}", uri);
		ResponseEntity<CountTransport> response = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<CountTransport>() {
				});
		return response.getBody().getCount();
	}

	@Override
	public RestPageResponse<MLPNotification> getNotifications(RestPageRequest pageRequest) {
		URI uri = buildUri(new String[] { CCDSConstants.NOTIFICATION_PATH }, null, pageRequest);
		logger.debug("getNotifications: uri {}", uri);
		ResponseEntity<RestPageResponse<MLPNotification>> response = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<RestPageResponse<MLPNotification>>() {
				});
		return response.getBody();
	}

	@Override
	public MLPNotification createNotification(MLPNotification notification) {
		URI uri = buildUri(new String[] { CCDSConstants.NOTIFICATION_PATH }, null, null);
		logger.debug("createNotification: uri {}", uri);
		return restTemplate.postForObject(uri, notification, MLPNotification.class);
	}

	@Override
	public void updateNotification(MLPNotification notification) {
		URI uri = buildUri(new String[] { CCDSConstants.NOTIFICATION_PATH, notification.getNotificationId() }, null,
				null);
		logger.debug("updateNotification: url {}", uri);
		restTemplate.put(uri, notification);
	}

	@Override
	public void deleteNotification(String notificationId) {
		URI uri = buildUri(new String[] { CCDSConstants.NOTIFICATION_PATH, notificationId }, null, null);
		logger.debug("deleteNotification: url {}", uri);
		restTemplate.delete(uri);
	}

	@Override
	public RestPageResponse<MLPUserNotification> getUserNotifications(String userId, RestPageRequest pageRequest) {
		URI uri = buildUri(new String[] { CCDSConstants.USER_PATH, userId, CCDSConstants.NOTIFICATION_PATH }, null,
				pageRequest);
		logger.debug("getUserNotifications: url {}", uri);
		ResponseEntity<RestPageResponse<MLPUserNotification>> response = restTemplate.exchange(uri, HttpMethod.GET,
				null, new ParameterizedTypeReference<RestPageResponse<MLPUserNotification>>() {
				});
		return response.getBody();
	}

	@Override
	public void addUserToNotification(String notificationId, String userId) {
		URI uri = buildUri(
				new String[] { CCDSConstants.USER_PATH, userId, CCDSConstants.NOTIFICATION_PATH, notificationId }, null,
				null);
		logger.debug("addNotificationUser: url {}", uri);
		MLPNotifUserMap map = new MLPNotifUserMap(notificationId, userId);
		restTemplate.postForObject(uri, map, SuccessTransport.class);
	}

	@Override
	public void dropUserFromNotification(String notificationId, String userId) {
		URI uri = buildUri(
				new String[] { CCDSConstants.USER_PATH, userId, CCDSConstants.NOTIFICATION_PATH, notificationId }, null,
				null);
		logger.debug("dropNotificationUser: url {}", uri);
		restTemplate.delete(uri);
	}

	@Override
	public void setUserViewedNotification(String notificationId, String userId) {
		URI uri = buildUri(
				new String[] { CCDSConstants.USER_PATH, userId, CCDSConstants.NOTIFICATION_PATH, notificationId }, null,
				null);
		logger.debug("addNotificationUser: url {}", uri);
		MLPNotifUserMap map = new MLPNotifUserMap(notificationId, userId);
		map.setViewed(new Date());
		restTemplate.put(uri, map);
	}

	@Override
	public MLPSolutionWeb getSolutionWebMetadata(String solutionId) {
		URI uri = buildUri(new String[] { CCDSConstants.SOLUTION_PATH, solutionId, CCDSConstants.WEB_PATH }, null,
				null);
		logger.debug("getSolutionStats: url {}", uri);
		ResponseEntity<MLPSolutionWeb> response = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<MLPSolutionWeb>() {
				});
		return response.getBody();
	}

	@Override
	public List<MLPUser> getSolutionAccessUsers(String solutionId) {
		URI uri = buildUri(new String[] { CCDSConstants.SOLUTION_PATH, solutionId, CCDSConstants.USER_PATH,
				CCDSConstants.ACCESS_PATH }, null, null);
		logger.debug("getSolutionAccessUsers: url {}", uri);
		ResponseEntity<List<MLPUser>> response = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<List<MLPUser>>() {
				});
		return response.getBody();
	}

	@Override
	public RestPageResponse<MLPSolution> getUserAccessSolutions(String userId, RestPageRequest pageRequest) {
		URI uri = buildUri(new String[] { CCDSConstants.SOLUTION_PATH, CCDSConstants.USER_PATH, userId,
				CCDSConstants.ACCESS_PATH }, null, pageRequest);
		logger.debug("getUserAccessSolutions: url {}", uri);
		ResponseEntity<RestPageResponse<MLPSolution>> response = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<RestPageResponse<MLPSolution>>() {
				});
		return response.getBody();
	}

	@Override
	public void addSolutionUserAccess(String solutionId, String userId) {
		URI uri = buildUri(new String[] { CCDSConstants.SOLUTION_PATH, solutionId, CCDSConstants.USER_PATH, userId,
				CCDSConstants.ACCESS_PATH }, null, null);
		logger.debug("addSolutionUserAccess: url {}", uri);
		MLPSolUserAccMap map = new MLPSolUserAccMap(solutionId, userId);
		restTemplate.postForObject(uri, map, SuccessTransport.class);
	}

	@Override
	public void dropSolutionUserAccess(String solutionId, String userId) {
		URI uri = buildUri(new String[] { CCDSConstants.SOLUTION_PATH, solutionId, CCDSConstants.USER_PATH, userId,
				CCDSConstants.ACCESS_PATH }, null, null);
		logger.debug("dropSolutionUserAccess: url {}", uri);
		restTemplate.delete(uri);
	}

	@Override
	public List<MLPSolutionValidation> getSolutionValidations(String solutionId, String revisionId) {
		URI uri = buildUri(new String[] { CCDSConstants.SOLUTION_PATH, solutionId, CCDSConstants.REVISION_PATH,
				revisionId, CCDSConstants.VALIDATION_PATH }, null, null);
		logger.debug("getSolutionValidations: uri {}", uri);
		ResponseEntity<List<MLPSolutionValidation>> response = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<List<MLPSolutionValidation>>() {
				});
		return response.getBody();
	}

	@Override
	public MLPSolutionValidation createSolutionValidation(MLPSolutionValidation validation) {
		URI uri = buildUri(
				new String[] { CCDSConstants.SOLUTION_PATH, validation.getSolutionId(), CCDSConstants.REVISION_PATH,
						validation.getRevisionId(), CCDSConstants.VALIDATION_PATH, validation.getTaskId() },
				null, null);
		logger.debug("createSolutionValidation: uri {}", uri);
		return restTemplate.postForObject(uri, validation, MLPSolutionValidation.class);
	}

	@Override
	public void updateSolutionValidation(MLPSolutionValidation validation) {
		URI uri = buildUri(
				new String[] { CCDSConstants.SOLUTION_PATH, validation.getSolutionId(), CCDSConstants.REVISION_PATH,
						validation.getRevisionId(), CCDSConstants.VALIDATION_PATH, validation.getTaskId() },
				null, null);
		logger.debug("updateSolutionRating: url {}", uri);
		restTemplate.put(uri, validation);
	}

	@Override
	public void deleteSolutionValidation(MLPSolutionValidation validation) {
		URI uri = buildUri(
				new String[] { CCDSConstants.SOLUTION_PATH, validation.getSolutionId(), CCDSConstants.REVISION_PATH,
						validation.getRevisionId(), CCDSConstants.VALIDATION_PATH, validation.getTaskId() },
				null, null);
		logger.debug("deleteSolutionValidation: url {}", uri);
		restTemplate.delete(uri);
	}

	@Override
	public List<MLPValidationSequence> getValidationSequences() {
		URI uri = buildUri(new String[] { CCDSConstants.VAL_SEQ_PATH }, null, null);
		logger.debug("getValidationSequences: uri {}", uri);
		ResponseEntity<List<MLPValidationSequence>> response = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<List<MLPValidationSequence>>() {
				});
		return response.getBody();
	}

	@Override
	public MLPValidationSequence createValidationSequence(MLPValidationSequence sequence) {
		URI uri = buildUri(new String[] { CCDSConstants.VAL_SEQ_PATH, Integer.toString(sequence.getSequence()),
				CCDSConstants.VAL_TYPE_PATH, sequence.getValTypeCode() }, null, null);
		logger.debug("createValidationSequence: uri {}", uri);
		return restTemplate.postForObject(uri, sequence, MLPValidationSequence.class);
	}

	@Override
	public void deleteValidationSequence(MLPValidationSequence sequence) {
		URI uri = buildUri(new String[] { CCDSConstants.VAL_SEQ_PATH, Integer.toString(sequence.getSequence()),
				CCDSConstants.VAL_TYPE_PATH, sequence.getValTypeCode() }, null, null);
		logger.debug("deleteValidationSequence: url {}", uri);
		restTemplate.delete(uri);
	}

	@Override
	public RestPageResponse<MLPSolutionDeployment> getUserDeployments(String userId, RestPageRequest pageRequest) {
		URI uri = buildUri(new String[] { CCDSConstants.USER_PATH, userId, CCDSConstants.DEPLOYMENT_PATH }, null,
				pageRequest);
		logger.debug("getUserDeployments: uri {}", uri);
		ResponseEntity<RestPageResponse<MLPSolutionDeployment>> response = restTemplate.exchange(uri, HttpMethod.GET,
				null, new ParameterizedTypeReference<RestPageResponse<MLPSolutionDeployment>>() {
				});
		return response.getBody();
	}

	@Override
	public RestPageResponse<MLPSolutionDeployment> getSolutionDeployments(String solutionId, String revisionId,
			RestPageRequest pageRequest) {
		URI uri = buildUri(new String[] { CCDSConstants.SOLUTION_PATH, solutionId, CCDSConstants.REVISION_PATH,
				revisionId, CCDSConstants.DEPLOYMENT_PATH }, null, pageRequest);
		logger.debug("getSolutionDeployments: uri {}", uri);
		ResponseEntity<RestPageResponse<MLPSolutionDeployment>> response = restTemplate.exchange(uri, HttpMethod.GET,
				null, new ParameterizedTypeReference<RestPageResponse<MLPSolutionDeployment>>() {
				});
		return response.getBody();
	}

	@Override
	public RestPageResponse<MLPSolutionDeployment> getUserSolutionDeployments(String solutionId, String revisionId,
			String userId, RestPageRequest pageRequest) {
		URI uri = buildUri(new String[] { CCDSConstants.SOLUTION_PATH, solutionId, CCDSConstants.REVISION_PATH,
				revisionId, CCDSConstants.USER_PATH, userId, CCDSConstants.DEPLOYMENT_PATH }, null, pageRequest);
		logger.debug("getUserSolutionDeployments: uri {}", uri);
		ResponseEntity<RestPageResponse<MLPSolutionDeployment>> response = restTemplate.exchange(uri, HttpMethod.GET,
				null, new ParameterizedTypeReference<RestPageResponse<MLPSolutionDeployment>>() {
				});
		return response.getBody();
	}

	@Override
	public MLPSolutionDeployment createSolutionDeployment(MLPSolutionDeployment deployment) {
		URI uri = buildUri(new String[] { CCDSConstants.SOLUTION_PATH, deployment.getSolutionId(),
				CCDSConstants.REVISION_PATH, deployment.getRevisionId(), CCDSConstants.DEPLOYMENT_PATH }, null, null);
		logger.debug("createSolutionDeployment: uri {}", uri);
		return restTemplate.postForObject(uri, deployment, MLPSolutionDeployment.class);
	}

	@Override
	public void updateSolutionDeployment(MLPSolutionDeployment deployment) {
		URI uri = buildUri(
				new String[] { CCDSConstants.SOLUTION_PATH, deployment.getSolutionId(), CCDSConstants.REVISION_PATH,
						deployment.getRevisionId(), CCDSConstants.DEPLOYMENT_PATH, deployment.getDeploymentId() },
				null, null);
		logger.debug("updateSolutionDeployment: url {}", uri);
		restTemplate.put(uri, deployment);
	}

	@Override
	public void deleteSolutionDeployment(MLPSolutionDeployment deployment) {
		URI uri = buildUri(
				new String[] { CCDSConstants.SOLUTION_PATH, deployment.getSolutionId(), CCDSConstants.REVISION_PATH,
						deployment.getRevisionId(), CCDSConstants.DEPLOYMENT_PATH, deployment.getDeploymentId() },
				null, null);
		logger.debug("deleteSolutionDeployment: url {}", uri);
		restTemplate.delete(uri);
	}

	@Override
	public MLPSiteConfig getSiteConfig(String configKey) {
		URI uri = buildUri(new String[] { CCDSConstants.CONFIG_PATH, configKey }, null, null);
		logger.debug("getSiteConfig: uri {}", uri);
		ResponseEntity<MLPSiteConfig> response = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<MLPSiteConfig>() {
				});
		return response.getBody();
	}

	@Override
	public MLPSiteConfig createSiteConfig(MLPSiteConfig config) {
		URI uri = buildUri(new String[] { CCDSConstants.CONFIG_PATH }, null, null);
		logger.debug("createSiteConfig: uri {}", uri);
		return restTemplate.postForObject(uri, config, MLPSiteConfig.class);
	}

	@Override
	public void updateSiteConfig(MLPSiteConfig config) {
		URI uri = buildUri(new String[] { CCDSConstants.CONFIG_PATH, config.getConfigKey() }, null, null);
		logger.debug("updateSiteConfig: url {}", uri);
		restTemplate.put(uri, config);
	}

	@Override
	public void deleteSiteConfig(String configKey) {
		URI uri = buildUri(new String[] { CCDSConstants.CONFIG_PATH, configKey }, null, null);
		logger.debug("deleteSiteConfig: url {}", uri);
		restTemplate.delete(uri);
	}

	@Override
	public long getThreadCount() {
		URI uri = buildUri(new String[] { CCDSConstants.THREAD_PATH, CCDSConstants.COUNT_PATH }, null, null);
		logger.debug("getThreadCount: uri {}", uri);
		ResponseEntity<CountTransport> response = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<CountTransport>() {
				});
		return response.getBody().getCount();
	}

	@Override
	public RestPageResponse<MLPThread> getThreads(RestPageRequest pageRequest) {
		URI uri = buildUri(new String[] { CCDSConstants.THREAD_PATH }, null, pageRequest);
		logger.debug("getThreads: uri {}", uri);
		ResponseEntity<RestPageResponse<MLPThread>> response = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<RestPageResponse<MLPThread>>() {
				});
		return response.getBody();
	}

	@Override
	public RestPageResponse<MLPThread> getSolutionRevisionThreads(String solutionId, String revisionId,
			RestPageRequest pageRequest) {
		URI uri = buildUri(new String[] { CCDSConstants.THREAD_PATH, CCDSConstants.SOLUTION_PATH, solutionId,
				CCDSConstants.REVISION_PATH, revisionId }, null, pageRequest);
		logger.debug("getThreads: uri {}", uri);
		ResponseEntity<RestPageResponse<MLPThread>> response = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<RestPageResponse<MLPThread>>() {
				});
		return response.getBody();
	}

	@Override
	public MLPThread getThread(String threadId) {
		URI uri = buildUri(new String[] { CCDSConstants.THREAD_PATH, threadId }, null, null);
		logger.debug("getThread: uri {}", uri);
		ResponseEntity<MLPThread> response = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<MLPThread>() {
				});
		return response.getBody();
	}

	@Override
	public MLPThread createThread(MLPThread thread) {
		URI uri = buildUri(new String[] { CCDSConstants.THREAD_PATH }, null, null);
		logger.debug("createThread: uri {}", uri);
		return restTemplate.postForObject(uri, thread, MLPThread.class);
	}

	@Override
	public void updateThread(MLPThread thread) {
		URI uri = buildUri(new String[] { CCDSConstants.THREAD_PATH, thread.getThreadId() }, null, null);
		logger.debug("updateThread: url {}", uri);
		restTemplate.put(uri, thread);
	}

	@Override
	public void deleteThread(String threadId) {
		URI uri = buildUri(new String[] { CCDSConstants.THREAD_PATH, threadId }, null, null);
		logger.debug("deleteThread: url {}", uri);
		restTemplate.delete(uri);
	}

	@Override
	public long getThreadCommentCount(String threadId) {
		URI uri = buildUri(new String[] { CCDSConstants.THREAD_PATH, threadId, CCDSConstants.COMMENT_PATH,
				CCDSConstants.COUNT_PATH }, null, null);
		logger.debug("getCommentCount: uri {}", uri);
		ResponseEntity<CountTransport> response = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<CountTransport>() {
				});
		return response.getBody().getCount();
	}

	@Override
	public RestPageResponse<MLPComment> getThreadComments(String threadId, RestPageRequest pageRequest) {
		URI uri = buildUri(new String[] { CCDSConstants.THREAD_PATH, threadId, CCDSConstants.COMMENT_PATH }, null,
				pageRequest);
		logger.debug("getThreadComments: uri {}", uri);
		ResponseEntity<RestPageResponse<MLPComment>> response = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<RestPageResponse<MLPComment>>() {
				});
		return response.getBody();
	}

	@Override
	public RestPageResponse<MLPComment> getSolutionRevisionComments(String solutionId, String revisionId,
			RestPageRequest pageRequest) {
		URI uri = buildUri(new String[] { CCDSConstants.THREAD_PATH, CCDSConstants.SOLUTION_PATH, solutionId,
				CCDSConstants.REVISION_PATH, revisionId, CCDSConstants.COMMENT_PATH }, null, pageRequest);
		logger.debug("getSolutionRevisionComments: uri {}", uri);
		ResponseEntity<RestPageResponse<MLPComment>> response = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<RestPageResponse<MLPComment>>() {
				});
		return response.getBody();
	}

	@Override
	public MLPComment getComment(String threadId, String commentId) {
		URI uri = buildUri(new String[] { CCDSConstants.THREAD_PATH, threadId, CCDSConstants.COMMENT_PATH, commentId },
				null, null);
		logger.debug("getComment: uri {}", uri);
		ResponseEntity<MLPComment> response = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<MLPComment>() {
				});
		return response.getBody();
	}

	@Override
	public MLPComment createComment(MLPComment comment) {
		URI uri = buildUri(
				new String[] { CCDSConstants.THREAD_PATH, comment.getThreadId(), CCDSConstants.COMMENT_PATH }, null,
				null);
		logger.debug("createComment: uri {}", uri);
		return restTemplate.postForObject(uri, comment, MLPComment.class);
	}

	@Override
	public void updateComment(MLPComment comment) {
		URI uri = buildUri(new String[] { CCDSConstants.THREAD_PATH, comment.getThreadId(), CCDSConstants.COMMENT_PATH,
				comment.getCommentId() }, null, null);
		logger.debug("updateComment: url {}", uri);
		restTemplate.put(uri, comment);
	}

	@Override
	public void deleteComment(String threadId, String commentId) {
		URI uri = buildUri(new String[] { CCDSConstants.THREAD_PATH, threadId, CCDSConstants.COMMENT_PATH, commentId },
				null, null);
		logger.debug("deleteComment: url {}", uri);
		restTemplate.delete(uri);
	}

	@Override
	public RestPageResponse<MLPStepResult> getStepResults(RestPageRequest pageRequest) {
		URI uri = buildUri(new String[] { CCDSConstants.STEP_RESULT_PATH }, null, pageRequest);
		logger.debug("getStepResults: uri {}", uri);
		ResponseEntity<RestPageResponse<MLPStepResult>> response = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<RestPageResponse<MLPStepResult>>() {
				});
		return response.getBody();
	}

	@Override
	public RestPageResponse<MLPStepResult> searchStepResults(Map<String, Object> queryParameters, boolean isOr,
			RestPageRequest pageRequest) {
		Map<String, Object> copy = new HashMap<>(queryParameters);
		copy.put(CCDSConstants.JUNCTION_QUERY_PARAM, isOr ? "o" : "a");
		URI uri = buildUri(new String[] { CCDSConstants.STEP_RESULT_PATH, CCDSConstants.SEARCH_PATH }, copy,
				pageRequest);
		logger.debug("searchStepResults: uri {}", uri);
		ResponseEntity<RestPageResponse<MLPStepResult>> response = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<RestPageResponse<MLPStepResult>>() {
				});
		return response.getBody();
	}

	@Override
	public MLPStepResult createStepResult(MLPStepResult stepResult) {
		URI uri = buildUri(new String[] { CCDSConstants.STEP_RESULT_PATH }, null, null);
		logger.debug("createStepResult: uri {}", uri);
		return restTemplate.postForObject(uri, stepResult, MLPStepResult.class);
	}

	@Override
	public void updateStepResult(MLPStepResult stepResult) {
		URI uri = buildUri(new String[] { CCDSConstants.STEP_RESULT_PATH, Long.toString(stepResult.getStepResultId()) },
				null, null);
		logger.debug("updateStepResult: url {}", uri);
		restTemplate.put(uri, stepResult);
	}

	@Override
	public void deleteStepResult(Long stepResultId) {
		URI uri = buildUri(new String[] { CCDSConstants.STEP_RESULT_PATH, Long.toString(stepResultId) }, null, null);
		logger.debug("deleteStepResult: url {}", uri);
		restTemplate.delete(uri);
	}

	@Override
	public MLPUserNotifPref getUserNotificationPreference(Long usrNotifPrefId) {
		URI uri = buildUri(new String[] { CCDSConstants.USER_PATH, CCDSConstants.NOTIFICATION_PREF_PATH,
				String.valueOf(usrNotifPrefId) }, null, null);
		logger.debug("getUserNotificationPreference: url {}", uri);
		ResponseEntity<MLPUserNotifPref> response = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<MLPUserNotifPref>() {
				});
		return response.getBody();
	}

	@Override
	public MLPUserNotifPref createUserNotificationPreference(MLPUserNotifPref usrNotifPref) {
		URI uri = buildUri(new String[] { CCDSConstants.USER_PATH, CCDSConstants.NOTIFICATION_PREF_PATH }, null, null);
		logger.debug("createUserNotificationPreference: uri {}", uri);
		return restTemplate.postForObject(uri, usrNotifPref, MLPUserNotifPref.class);
	}

	@Override
	public void updateUserNotificationPreference(MLPUserNotifPref usrNotifPref) {
		URI uri = buildUri(new String[] { CCDSConstants.USER_PATH, CCDSConstants.NOTIFICATION_PREF_PATH,
				Long.toString(usrNotifPref.getUserNotifPrefId()) }, null, null);
		logger.debug("updateUserNotificationPreference: url {}", uri);
		restTemplate.put(uri, usrNotifPref);
	}

	@Override
	public void deleteUserNotificationPreference(Long userNotifPrefId) {
		URI uri = buildUri(new String[] { CCDSConstants.USER_PATH, CCDSConstants.NOTIFICATION_PREF_PATH,
				Long.toString(userNotifPrefId) }, null, null);
		logger.debug("deleteUserNotificationPreference: url {}", uri);
		restTemplate.delete(uri);
	}

	@Override
	public List<MLPUserNotifPref> getUserNotificationPreferences(String userId) {
		URI uri = buildUri(new String[] { CCDSConstants.USER_PATH, userId, CCDSConstants.NOTIFICATION_PREF_PATH }, null,
				null);
		logger.debug("getUserNotificationPreferences: url {}", uri);
		ResponseEntity<List<MLPUserNotifPref>> response = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<List<MLPUserNotifPref>>() {
				});
		return response.getBody();
	}

	@Override
	public RestPageResponse<MLPPeerGroup> getPeerGroups(RestPageRequest pageRequest) {
		URI uri = buildUri(new String[] { CCDSConstants.GROUP_PATH, CCDSConstants.PEER_PATH }, null, pageRequest);
		logger.debug("getPeerGroups: uri {}", uri);
		ResponseEntity<RestPageResponse<MLPPeerGroup>> response = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<RestPageResponse<MLPPeerGroup>>() {
				});
		return response.getBody();
	}

	@Override
	public MLPPeerGroup createPeerGroup(MLPPeerGroup peerGroup) {
		URI uri = buildUri(new String[] { CCDSConstants.GROUP_PATH, CCDSConstants.PEER_PATH }, null, null);
		logger.debug("createPeerGroup: uri {}", uri);
		return restTemplate.postForObject(uri, peerGroup, MLPPeerGroup.class);
	}

	@Override
	public void updatePeerGroup(MLPPeerGroup peerGroup) {
		URI uri = buildUri(new String[] { CCDSConstants.GROUP_PATH, Long.toString(peerGroup.getGroupId()),
				CCDSConstants.PEER_PATH }, null, null);
		logger.debug("updatePeerGroup: url {}", uri);
		restTemplate.put(uri, peerGroup);
	}

	@Override
	public void deletePeerGroup(Long peerGroupId) {
		URI uri = buildUri(
				new String[] { CCDSConstants.GROUP_PATH, Long.toString(peerGroupId), CCDSConstants.PEER_PATH }, null,
				null);
		logger.debug("deletePeerGroup: url {}", uri);
		restTemplate.delete(uri);
	}

	@Override
	public RestPageResponse<MLPSolutionGroup> getSolutionGroups(RestPageRequest pageRequest) {
		URI uri = buildUri(new String[] { CCDSConstants.GROUP_PATH, CCDSConstants.SOLUTION_PATH }, null, pageRequest);
		logger.debug("getSolutionGroups: uri {}", uri);
		ResponseEntity<RestPageResponse<MLPSolutionGroup>> response = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<RestPageResponse<MLPSolutionGroup>>() {
				});
		return response.getBody();
	}

	@Override
	public MLPSolutionGroup createSolutionGroup(MLPSolutionGroup solutionGroup) {
		URI uri = buildUri(new String[] { CCDSConstants.GROUP_PATH, CCDSConstants.SOLUTION_PATH }, null, null);
		logger.debug("createSolutionGroup: uri {}", uri);
		return restTemplate.postForObject(uri, solutionGroup, MLPSolutionGroup.class);
	}

	@Override
	public void updateSolutionGroup(MLPSolutionGroup solutionGroup) {
		URI uri = buildUri(new String[] { CCDSConstants.GROUP_PATH, Long.toString(solutionGroup.getGroupId()),
				CCDSConstants.SOLUTION_PATH }, null, null);
		logger.debug("updateSolutionGroup: url {}", uri);
		restTemplate.put(uri, solutionGroup);
	}

	@Override
	public void deleteSolutionGroup(Long solutionGroupId) {
		URI uri = buildUri(
				new String[] { CCDSConstants.GROUP_PATH, Long.toString(solutionGroupId), CCDSConstants.SOLUTION_PATH },
				null, null);
		logger.debug("deleteSolutionGroup: url {}", uri);
		restTemplate.delete(uri);
	}

	@Override
	public RestPageResponse<MLPPeer> getPeersInGroup(Long peerGroupId, RestPageRequest pageRequest) {
		URI uri = buildUri(
				new String[] { CCDSConstants.GROUP_PATH, Long.toString(peerGroupId), CCDSConstants.PEER_PATH }, null,
				pageRequest);
		logger.debug("getPeersInGroup: uri {}", uri);
		ResponseEntity<RestPageResponse<MLPPeer>> response = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<RestPageResponse<MLPPeer>>() {
				});
		return response.getBody();
	}

	@Override
	public void addPeerToGroup(String peerId, Long peerGroupId) {
		URI uri = buildUri(
				new String[] { CCDSConstants.GROUP_PATH, Long.toString(peerGroupId), CCDSConstants.PEER_PATH, peerId },
				null, null);
		logger.debug("addPeerToGroup: url {}", uri);
		MLPPeerGrpMemMap map = new MLPPeerGrpMemMap(peerGroupId, peerId);
		restTemplate.postForObject(uri, map, SuccessTransport.class);
	}

	@Override
	public void dropPeerFromGroup(String peerId, Long peerGroupId) {
		URI uri = buildUri(
				new String[] { CCDSConstants.GROUP_PATH, Long.toString(peerGroupId), CCDSConstants.PEER_PATH, peerId },
				null, null);
		logger.debug("dropPeerFromGroup: url {}", uri);
		restTemplate.delete(uri);
	}

	@Override
	public RestPageResponse<MLPSolution> getSolutionsInGroup(Long solutionGroupId, RestPageRequest pageRequest) {
		URI uri = buildUri(
				new String[] { CCDSConstants.GROUP_PATH, Long.toString(solutionGroupId), CCDSConstants.SOLUTION_PATH },
				null, pageRequest);
		logger.debug("getSolutionsInGroup: uri {}", uri);
		ResponseEntity<RestPageResponse<MLPSolution>> response = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<RestPageResponse<MLPSolution>>() {
				});
		return response.getBody();
	}

	@Override
	public void addSolutionToGroup(String solutionId, Long solutionGroupId) {
		URI uri = buildUri(new String[] { CCDSConstants.GROUP_PATH, Long.toString(solutionGroupId),
				CCDSConstants.SOLUTION_PATH, solutionId }, null, null);
		logger.debug("addSolutionToGroup: url {}", uri);
		MLPPeerGrpMemMap map = new MLPPeerGrpMemMap(solutionGroupId, solutionId);
		restTemplate.postForObject(uri, map, SuccessTransport.class);
	}

	@Override
	public void dropSolutionFromGroup(String solutionId, Long solutionGroupId) {
		URI uri = buildUri(new String[] { CCDSConstants.GROUP_PATH, Long.toString(solutionGroupId),
				CCDSConstants.SOLUTION_PATH, solutionId }, null, null);
		logger.debug("dropSolutionFromGroup: url {}", uri);
		restTemplate.delete(uri);
	}

	@Override
	public RestPageResponse<MLPPeerSolAccMap> getPeerSolutionGroupMaps(RestPageRequest pageRequest) {
		URI uri = buildUri(
				new String[] { CCDSConstants.GROUP_PATH, CCDSConstants.PEER_PATH, CCDSConstants.SOLUTION_PATH }, null,
				pageRequest);
		logger.debug("getPeerSolutionGroupMaps: uri {}", uri);
		ResponseEntity<RestPageResponse<MLPPeerSolAccMap>> response = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<RestPageResponse<MLPPeerSolAccMap>>() {
				});
		return response.getBody();
	}

	public void mapPeerSolutionGroups(Long peerGroupId, Long solutionGroupId) {
		URI uri = buildUri(new String[] { CCDSConstants.GROUP_PATH, CCDSConstants.PEER_PATH, Long.toString(peerGroupId),
				CCDSConstants.SOLUTION_PATH, Long.toString(solutionGroupId) }, null, null);
		logger.debug("mapPeerSolutionGroups: url {}", uri);
		MLPPeerSolAccMap map = new MLPPeerSolAccMap(peerGroupId, solutionGroupId, true);
		restTemplate.postForObject(uri, map, SuccessTransport.class);
	}

	@Override
	public void unmapPeerSolutionGroups(Long peerGroupId, Long solutionGroupId) {
		URI uri = buildUri(new String[] { CCDSConstants.GROUP_PATH, CCDSConstants.PEER_PATH, Long.toString(peerGroupId),
				CCDSConstants.SOLUTION_PATH, Long.toString(solutionGroupId) }, null, null);
		logger.debug("unmapPeerSolutionGroups: url {}", uri);
		restTemplate.delete(uri);
	}

	@Override
	public void mapPeerPeerGroups(Long principalGroupId, Long resourceGroupId) {
		URI uri = buildUri(new String[] { CCDSConstants.GROUP_PATH, CCDSConstants.PEER_PATH,
				Long.toString(principalGroupId), CCDSConstants.PEER_PATH, Long.toString(resourceGroupId) }, null, null);
		logger.debug("mapPeerPeerGroups: url {}", uri);
		MLPPeerPeerAccMap map = new MLPPeerPeerAccMap(principalGroupId, resourceGroupId);
		restTemplate.postForObject(uri, map, SuccessTransport.class);
	}

	@Override
	public void unmapPeerPeerGroups(Long principalGroupId, Long resourceGroupId) {
		URI uri = buildUri(new String[] { CCDSConstants.GROUP_PATH, CCDSConstants.PEER_PATH,
				Long.toString(principalGroupId), CCDSConstants.PEER_PATH, Long.toString(resourceGroupId) }, null, null);
		logger.debug("unmapPeerPeerGroups: url {}", uri);
		restTemplate.delete(uri);
	}

	@Override
	public long checkPeerSolutionAccess(String peerId, String solutionId) {
		URI uri = buildUri(new String[] { CCDSConstants.GROUP_PATH, CCDSConstants.PEER_PATH, peerId,
				CCDSConstants.SOLUTION_PATH, solutionId, CCDSConstants.ACCESS_PATH }, null, null);
		logger.debug("checkPeerSolutionAccess: uri {}", uri);
		ResponseEntity<CountTransport> response = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<CountTransport>() {
				});
		return response.getBody().getCount();
	}

	@Override
	public List<MLPPeer> getPeerAccess(String peerId) {
		URI uri = buildUri(
				new String[] { CCDSConstants.GROUP_PATH, CCDSConstants.PEER_PATH, peerId, CCDSConstants.ACCESS_PATH },
				null, null);
		logger.debug("getPeerAccess: uri {}", uri);
		ResponseEntity<List<MLPPeer>> response = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<List<MLPPeer>>() {
				});
		return response.getBody();
	}

}

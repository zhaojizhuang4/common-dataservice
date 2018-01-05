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

package org.acumos.cds.test;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.acumos.cds.AccessTypeCode;
import org.acumos.cds.ArtifactTypeCode;
import org.acumos.cds.DeploymentStatusCode;
import org.acumos.cds.LoginProviderCode;
import org.acumos.cds.ModelTypeCode;
import org.acumos.cds.ToolkitTypeCode;
import org.acumos.cds.ValidationStatusCode;
import org.acumos.cds.ValidationTypeCode;
import org.acumos.cds.client.CommonDataServiceRestClientImpl;
import org.acumos.cds.client.ICommonDataServiceRestClient;
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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;

/**
 * Tests server controller classes by sending in requests with the client. The
 * server is launched with a Derby in-memory database.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class CdsControllerTest {

	private static Logger logger = LoggerFactory.getLogger(CdsControllerTest.class);

	// Defined in the default application.properties file
	private final String hostname = "localhost";

	// For tripping length constraints
	private final String s64 = "12345678901234567890123456789012345678901234567890123456789012345";

	// From properties
	@Value("${server.contextPath}")
	private String contextPath;
	@Value("${security.user.name}")
	private String userName;
	@Value("${security.user.password}")
	private String password;
	// Created by Spring black magic
	// https://spring.io/guides/gs/testing-web/
	@LocalServerPort
	private int port;

	private ICommonDataServiceRestClient client;

	@Before
	public void createClient() throws Exception {
		// e.g., "http://localhost:8081/ccds"
		logger.info("createClient: port is {}", port);
		URL url = new URL("http", hostname, port, contextPath);
		logger.info("createClient: URL is {}", url);
		client = CommonDataServiceRestClientImpl.getInstance(url.toString(), userName, password);
	}

	@Test
	public void basicSequenceDemo() throws Exception {
		try {
			MLPUser cu = new MLPUser();
			cu.setLoginName("user_login1");
			cu.setLoginHash("user_pass");
			cu.setFirstName("First Name");
			cu.setLastName("Last Name");
			cu = client.createUser(cu);
			logger.info("Created user {}", cu);

			cu.setMiddleName("middle");
			client.updateUser(cu);

			// query the user to be sure
			MLPUser cu2 = client.getUser(cu.getUserId());
			Assert.assertTrue(cu.getUserId().equals(cu2.getUserId()));

			MLPSolution cs = new MLPSolution();
			cs.setName("solution name");
			cs.setOwnerId(cu.getUserId());
			cs.setValidationStatusCode(ValidationStatusCode.IP.name());
			cs.setProvider("Big Data Org");
			cs.setAccessTypeCode(AccessTypeCode.PB.name());
			cs.setModelTypeCode(ModelTypeCode.CL.name());
			cs.setToolkitTypeCode(ToolkitTypeCode.CP.name());
			cs.setActive(true);
			cs = client.createSolution(cs);
			logger.info("Created solution {}", cs);

			cs.setDescription("some description");
			client.updateSolution(cs);
			
			MLPSolution fetched = client.getSolution(cs.getSolutionId());
			Assert.assertTrue(fetched != null && fetched.getTags() != null && fetched.getWebStats() != null);

			MLPSolutionRevision cr = new MLPSolutionRevision();
			cr.setSolutionId(cs.getSolutionId());
			cr.setVersion("1.0R");
			cr.setDescription("Some description");
			cr.setOwnerId(cu.getUserId());
			cr = client.createSolutionRevision(cr);
			logger.info("Created solution revision {}", cr);

			// Query for the revision
			MLPSolutionRevision crq = client.getSolutionRevision(cs.getSolutionId(), cr.getRevisionId());
			Assert.assertNotNull(crq);

			final String version = "1.0A";
			MLPArtifact ca = new MLPArtifact(version, ArtifactTypeCode.DI.toString(), "artifact name",
					"http://nexus/artifact", cu.getUserId(), 1);
			ca = client.createArtifact(ca);
			Assert.assertNotNull(ca.getArtifactId());
			Assert.assertNotNull(ca.getCreated());
			logger.info("Created artifact {}", ca);

			logger.info("Adding artifact to revision");
			client.addSolutionRevisionArtifact(cs.getSolutionId(), cr.getRevisionId(), ca.getArtifactId());

			logger.info("Deleting objects");
			client.dropSolutionRevisionArtifact(cs.getSolutionId(), cr.getRevisionId(), ca.getArtifactId());
			client.deleteArtifact(ca.getArtifactId());
			client.deleteSolutionRevision(cs.getSolutionId(), cr.getRevisionId());
			client.deleteSolution(cs.getSolutionId());
			client.deleteUser(cu.getUserId());
		} catch (HttpStatusCodeException ex) {
			logger.error("basicSequenceDemo failed: " + ex.getResponseBodyAsString(), ex);
			throw ex;
		}
	}

	@Test
	public void getServerInfo() throws Exception {
		SuccessTransport health = client.getHealth();
		Assert.assertNotNull(health.getData());
		logger.info("Server health {}", health.getData());

		SuccessTransport version = client.getVersion();
		Assert.assertNotNull(version.getData());
		logger.info("Server version: {}", version.getData());
	}

	@Test
	public void getCodeValueConstants() throws Exception {
		List<MLPAccessType> act = client.getAccessTypes();
		Assert.assertTrue(act.size() > 0);
		for (MLPAccessType c : act)
			logger.info("Access type {}", c);

		List<MLPArtifactType> art = client.getArtifactTypes();
		Assert.assertTrue(art.size() > 0);
		for (MLPArtifactType r : art)
			logger.info("Artifact type {}", r);

		List<MLPDeploymentStatus> ds = client.getDeploymentStatuses();
		Assert.assertTrue(ds.size() > 0);
		for (MLPDeploymentStatus d : ds)
			logger.info("Deployment status {}", d);

		List<MLPLoginProvider> lp = client.getLoginProviders();
		Assert.assertTrue(lp.size() > 0);
		for (MLPLoginProvider l : lp)
			logger.info("Login provider {}", l);

		List<MLPModelType> mt = client.getModelTypes();
		Assert.assertTrue(mt.size() > 0);
		for (MLPModelType m : mt)
			logger.info("Model type {}", m);

		List<MLPToolkitType> tt = client.getToolkitTypes();
		Assert.assertTrue(tt.size() > 0);
		for (MLPToolkitType m : tt)
			logger.info("Toolkit type {}", m);

		List<MLPValidationStatus> vs = client.getValidationStatuses();
		Assert.assertTrue(vs.size() > 0);
		for (MLPValidationStatus v : vs)
			logger.info("Validation status {}", v);

		List<MLPValidationType> vt = client.getValidationTypes();
		Assert.assertTrue(tt.size() > 0);
		for (MLPValidationType m : vt)
			logger.info("Validation type {}", m);
	}

	@Test
	public void testUserLoginProvider() throws Exception {
		try {
			MLPUser cu = new MLPUser();
			cu.setLoginName("user_login");
			cu.setLoginHash("user_pass");
			cu.setFirstName("First Name");
			cu.setLastName("Last Name");
			cu = client.createUser(cu);

			MLPUserLoginProvider prov = new MLPUserLoginProvider();
			prov.setUserId(cu.getUserId());
			final String providerCode = LoginProviderCode.FB.name();
			prov.setProviderCode(providerCode);
			final String userLogin = "foobar";
			prov.setProviderUserId(userLogin);
			prov.setRank(1);
			prov.setAccessToken("access token");
			prov = client.createUserLoginProvider(prov);

			prov.setDisplayName("display");
			client.updateUserLoginProvider(prov);

			MLPUserLoginProvider provq = client.getUserLoginProvider(cu.getUserId(), providerCode, userLogin);
			Assert.assertNotNull(provq);

			client.deleteUserLoginProvider(prov);
			client.deleteUser(cu.getUserId());
		} catch (HttpStatusCodeException ex) {
			logger.error("testUserLoginProvider failed", ex);
			throw ex;
		}
	}

	@Test
	public void createSolutionWithArtifacts() throws Exception {
		/** Delete data added in test? */
		final boolean cleanup = true;
		try {
			// Use this repeatedly :)
			RestPageRequest rp = new RestPageRequest(0, 1);

			MLPUser cu = new MLPUser();
			String unique = Long.toString(new Date().getTime());
			final String loginName = "user-" + unique;
			final String loginPass = "test_client_pass";
			cu.setLoginName(loginName);
			cu.setLoginHash(loginPass);
			final String firstName = "test_" + unique;
			cu.setFirstName(firstName);
			final String lastName = "test-last-name";
			cu.setLastName(lastName);
			cu.setActive(true);
			cu.setLoginPassExpire(new Date());
			final Byte[] fakePicture = new Byte[] { 1, 2, 3, 4, 5 };
			cu.setPicture(fakePicture);
			cu = client.createUser(cu);
			Assert.assertNotNull(cu.getUserId());
			Assert.assertNotNull(cu.getCreated());
			Assert.assertNotNull(cu.getModified());
			// Password must not come back as JSON
			Assert.assertNull(cu.getLoginHash());
			logger.info("Created user {}", cu);

			RestPageResponse<MLPUser> users = client.getUsers(rp);
			Assert.assertTrue(users.getNumberOfElements() > 0);
			for (MLPUser u : users.getContent()) 
				logger.info("Fetched user: " + u);

			// Login
			MLPUser loggedIn = client.loginUser(loginName, loginPass);
			Assert.assertNotNull(loggedIn);
			logger.info("Logged in successfully, password expires {}", loggedIn.getLoginPassExpire());
			Assert.assertArrayEquals(fakePicture, loggedIn.getPicture());

			// Fetch it back
			HashMap<String, Object> userRestr = new HashMap<>();
			userRestr.put("active", "true");
			userRestr.put("firstName", firstName);
			userRestr.put("lastName", lastName);
			List<MLPUser> userList = client.searchUsers(userRestr, false);
			Assert.assertTrue(userList.size() == 1);
			MLPUser testUser = userList.get(0);
			// Password must not come back as JSON
			Assert.assertNull(testUser.getLoginHash());

			// Search for the user
			Map<String, String> fieldMap = new HashMap<>();
			fieldMap.put("firstName", "DESC");
			RestPageResponse<MLPUser> lu = client.findUsersBySearchTerm("name", new RestPageRequest(0, 1, fieldMap));
			Assert.assertTrue(lu != null && lu.getContent() != null && lu.getContent().size() > 0);
			MLPUser searchUser = lu.getContent().get(0);
			Assert.assertNull(searchUser.getLoginHash());

			// Check count
			long userCountTrans = client.getUserCount();
			Assert.assertTrue(userCountTrans > 0);

			List<MLPLoginProvider> provs = client.getLoginProviders();
			Assert.assertTrue(provs != null && provs.size() > 0);
			logger.info("Count of login provider {}", provs.size());

			MLPUserLoginProvider clp = new MLPUserLoginProvider(cu.getUserId(), provs.get(0).getProviderCode(),
					"something", "access token", 0);
			clp = client.createUserLoginProvider(clp);
			logger.info("Created user login provider {}", clp);

			// Fetch all login providers for user
			List<MLPUserLoginProvider> userProvs = client.getUserLoginProviders(cu.getUserId());
			Assert.assertTrue(userProvs != null && userProvs.size() > 0);

			// Create Peer
			MLPPeer pr = new MLPPeer();
			final String peerName = "Peer-" + Long.toString(new Date().getTime());
			pr.setName(peerName);
			pr.setSubjectName("subject name");
			pr.setApiUrl("http://peer-api");
			pr.setWebUrl("https://web-url");
			pr.setContact1("Katherine Globe");
			pr.setContact2("Aemon Targaryen");
			pr.setTrustLevel(1);
			pr = client.createPeer(pr);
			logger.info("Created peer with ID {}", pr.getPeerId());

			pr.setSelf(false);
			client.updatePeer(pr);

			RestPageResponse<MLPPeer> peerPage = client.getPeers(rp);
			Assert.assertTrue(peerPage.getNumberOfElements() > 0);

			HashMap<String, Object> peerRestr = new HashMap<>();
			peerRestr.put("name", peerName);
			List<MLPPeer> peerSearchResult = client.searchPeers(peerRestr, false);
			Assert.assertTrue(peerSearchResult.size() > 0);

			MLPPeer pr2 = client.getPeer(pr.getPeerId());
			Assert.assertEquals(pr.getPeerId(), pr2.getPeerId());

			MLPPeerSubscription ps = new MLPPeerSubscription();
			ps.setPeerId(pr.getPeerId());
			ps = client.createPeerSubscription(ps);
			logger.info("Created peer subscription {}", ps);

			ps.setMaxArtifactSize(9999L);
			client.updatePeerSubscription(ps);

			MLPPeerSubscription ps2 = client.getPeerSubscription(ps.getSubId());
			Assert.assertNotNull(ps2);

			List<MLPPeerSubscription> peerSubs = client.getPeerSubscriptions(pr.getPeerId());
			Assert.assertTrue(peerSubs != null && peerSubs.size() > 0);
			logger.info("Fetched list of peer subscriptions of size {}", peerSubs.size());

			MLPPeerSubscription fetchedPeerSub = client.getPeerSubscription(ps.getSubId());
			Assert.assertTrue(fetchedPeerSub != null);
			logger.info("Fetched peer subscriptions {}", fetchedPeerSub);

			MLPArtifact ca = new MLPArtifact();
			final String version = "1.0A";
			ca.setVersion(version);
			ca.setName("artifact name");
			ca.setUri("http://nexus/artifact");
			ca.setOwnerId(cu.getUserId());
			ca.setArtifactTypeCode(ArtifactTypeCode.DI.toString());
			ca.setSize(1);
			ca = client.createArtifact(ca);
			logger.info("Created artifact with ID {}", ca.getArtifactId());

			ca.setSize(2);
			client.updateArtifact(ca);

			MLPArtifact art2 = client.getArtifact(ca.getArtifactId());
			Assert.assertEquals(ca.getArtifactId(), art2.getArtifactId());

			// Check count
			long artCountTrans = client.getArtifactCount();
			Assert.assertTrue(artCountTrans > 0);

			final String artId = "e007ce63-086f-4f33-84c6-cac270874d81";
			logger.info("Creating artifact with ID {}", artId);
			MLPArtifact ca2 = new MLPArtifact();
			ca2.setArtifactId(artId);
			ca2.setVersion("2.0A");
			ca2.setName("replicated artifact ");
			ca2.setUri("http://other.foo");
			ca2.setArtifactTypeCode(ArtifactTypeCode.CD.toString());
			ca2.setOwnerId(cu.getUserId());
			ca2.setSize(456);
			ca2 = client.createArtifact(ca2);
			Assert.assertEquals(artId, ca2.getArtifactId());
			logger.info("Created artifact with preset ID {}", artId);
			client.deleteArtifact(ca2.getArtifactId());

			// Get list
			RestPageResponse<MLPArtifact> arts = client.getArtifacts(new RestPageRequest(0, 100, "artifactId"));
			Assert.assertTrue(arts.getNumberOfElements() > 0);
			// Search like
			RestPageResponse<MLPArtifact> likes = client.findArtifactsBySearchTerm("artifact",
					new RestPageRequest(0, 10, "name"));
			Assert.assertTrue(likes.getNumberOfElements() > 0);
			// Search exactly
			HashMap<String, Object> restr = new HashMap<>();
			restr.put("version", version);
			List<MLPArtifact> filtered = client.searchArtifacts(restr, true);
			Assert.assertTrue(filtered.size() > 0);

			// This will get no results but will cover some clauses
			restr.clear();
			restr.put("created", new Date());
			restr.put("size", 0);
			filtered = client.searchArtifacts(restr, true);
			Assert.assertTrue(filtered.isEmpty());

			final String tagName1 = "tag1-" + Long.toString(new Date().getTime());
			final String tagName2 = "tag-2" + Long.toString(new Date().getTime());
			MLPTag tag1 = new MLPTag(tagName1);
			MLPTag tag2 = new MLPTag(tagName2);
			tag1 = client.createTag(tag1);
			logger.info("Created tag {}", tag1);
			tag2 = client.createTag(tag2);
			// Get list
			RestPageResponse<MLPTag> tags = client.getTags(new RestPageRequest(0, 100));
			Assert.assertTrue(tags.getNumberOfElements() > 0);

			MLPSolution cs = new MLPSolution();
			cs.setName("solution name");
			cs.setOwnerId(cu.getUserId());
			cs.setValidationStatusCode(ValidationStatusCode.IP.name());
			cs.setProvider("Big Data Org");
			cs.setAccessTypeCode(AccessTypeCode.PB.name());
			cs.setModelTypeCode(ModelTypeCode.CL.name());
			cs.setToolkitTypeCode(ToolkitTypeCode.CP.name());
			cs.setActive(true);
			cs.getTags().add(tag1);
			cs = client.createSolution(cs);
			Assert.assertNotNull(cs.getSolutionId());
			Assert.assertFalse(cs.getTags().isEmpty());
			logger.info("Created solution {}", cs);

			MLPSolution inactive = new MLPSolution();
			inactive.setName("inactive solution name");
			inactive.setOwnerId(cu.getUserId());
			inactive.setValidationStatusCode(ValidationStatusCode.FA.name());
			inactive.setProvider("Inactive Data Org");
			inactive.setAccessTypeCode(AccessTypeCode.OR.name());
			inactive.setModelTypeCode(ModelTypeCode.DS.name());
			inactive.setToolkitTypeCode(ToolkitTypeCode.SK.name());
			inactive.setActive(false);
			inactive = client.createSolution(inactive);
			Assert.assertNotNull(inactive.getSolutionId());
			logger.info("Created inactive solution {}", inactive);

			// Check count
			long solCountTrans = client.getSolutionCount();
			Assert.assertTrue(solCountTrans > 0);

			// Increment view count
			logger.info("Incrementing solution view count");
			client.incrementSolutionViewCount(cs.getSolutionId());
			MLPSolutionWeb stats = client.getSolutionWebMetadata(cs.getSolutionId());
			Assert.assertNotNull(stats);
			logger.info("Solution stats {}", stats);

			// add and drop tags
			logger.info("Tagging solutions");
			client.addSolutionTag(cs.getSolutionId(), tagName1);
			client.addSolutionTag(cs.getSolutionId(), tagName2);
			client.dropSolutionTag(cs.getSolutionId(), tagName2);

			logger.info("Fetching back newly created solution");
			MLPSolution s = client.getSolution(cs.getSolutionId());
			Assert.assertTrue(s != null && !s.getTags().isEmpty() && s.getWebStats() != null);
			logger.info("Solution {}", s);

			// Query for tags
			List<MLPTag> solTags = client.getSolutionTags(cs.getSolutionId());
			Assert.assertTrue(solTags.size() > 0);
			logger.info("Found tag on solution {}", solTags.get(0));

			logger.info("Getting all solutions");
			RestPageResponse<MLPSolution> page = client.getSolutions(new RestPageRequest(0, 2));
			Assert.assertTrue(page != null && page.getNumberOfElements() > 0);

			cs.setDescription("some description");
			client.updateSolution(cs);
			logger.info("Fetching back updated solution");
			MLPSolution updated = client.getSolution(cs.getSolutionId());
			Assert.assertTrue(updated != null && !updated.getTags().isEmpty() && updated.getWebStats() != null && updated.getWebStats().getViewCount() > 0);

			logger.info("Querying for active PB solutions");
			Map<String, Object> activePb = new HashMap<>();
			activePb.put("accessTypeCode", AccessTypeCode.PB.name());
			activePb.put("active", Boolean.TRUE);
			List<MLPSolution> activePbList = client.searchSolutions(activePb, false);
			Assert.assertTrue(activePbList != null && !activePbList.isEmpty());
			logger.info("Active PB solution count {}", activePbList.size());

			logger.info("Querying for inactive solutions");
			Map<String, Object> inactiveSols = new HashMap<>();
			inactiveSols.put("active", Boolean.TRUE);
			List<MLPSolution> inactiveSolList = client.searchSolutions(inactiveSols, false);
			Assert.assertTrue(inactiveSolList != null && !inactiveSolList.isEmpty());
			logger.info("Inactive PB solution count {}", inactiveSolList.size());

			logger.info("Querying for solutions with similar names");
			RestPageResponse<MLPSolution> sl1 = client.findSolutionsBySearchTerm("solution", new RestPageRequest(0, 1));
			Assert.assertTrue(sl1 != null && sl1.getNumberOfElements() > 0);
			RestPageResponse<MLPSolution> sl2 = client.findSolutionsByTag(tagName1, new RestPageRequest(0, 1));
			Assert.assertTrue(sl2 != null && sl2.getNumberOfElements() > 0);

			// Portal dynamic search
			String [] searchTags = new String [] { tagName1 };
			RestPageResponse<MLPSolution> portalTagMatches = client.findPortalSolutions(null, null, true, null, null, null, null, searchTags, 
					new RestPageRequest(0, 1));
			Assert.assertTrue(portalTagMatches != null && portalTagMatches.getNumberOfElements() > 0);
	
			String [] bogusTags = new String [] { "bogus" };
			RestPageResponse<MLPSolution> portalTagNoMatches = client.findPortalSolutions(null, null, true, null, null, null, null, bogusTags, 
					new RestPageRequest(0, 1));
			Assert.assertTrue(portalTagNoMatches != null && portalTagNoMatches.getNumberOfElements() == 0);

			RestPageResponse<MLPSolution> portalInactiveMatches = client.findPortalSolutions(null, null, false, null, null, null, null, null, 
					new RestPageRequest(0, 1));
			Assert.assertTrue(portalInactiveMatches != null && portalInactiveMatches.getNumberOfElements() > 0);

			String [] nameKw = null;
			String [] descKw = null;
			String [] owners = { cu.getUserId() };
			String [] accessTypeCodes = { AccessTypeCode.PB.name() };
			String [] modelTypeCodes = null;
			String [] valStatusCodes = { ValidationStatusCode.IP.name(), "null" };
			searchTags = null;
			RestPageResponse<MLPSolution> portalActiveMatches = client.findPortalSolutions(nameKw, descKw, true, owners, accessTypeCodes, modelTypeCodes, valStatusCodes, searchTags, 
					new RestPageRequest(0, 1));
			Assert.assertTrue(portalActiveMatches != null && portalActiveMatches.getNumberOfElements() > 0);

			// Add user access
			client.addSolutionUserAccess(cs.getSolutionId(), cu.getUserId());

			// Query two ways
			List<MLPUser> solUserAccList = client.getSolutionAccessUsers(cs.getSolutionId());
			Assert.assertTrue(solUserAccList != null && solUserAccList.size() > 0);
			logger.info("Got users with access to solution {}", cs.getSolutionId());
			RestPageResponse<MLPSolution> userSolAccList = client.getUserAccessSolutions(cu.getUserId(),
					new RestPageRequest(0, 1));
			Assert.assertTrue(userSolAccList != null && userSolAccList.getNumberOfElements() > 0);
			logger.info("Got solutions accessible by user {}", cu.getUserId());
			client.dropSolutionUserAccess(cs.getSolutionId(), cu.getUserId());

			MLPSolutionRevision cr = new MLPSolutionRevision();
			cr.setSolutionId(cs.getSolutionId());
			cr.setVersion("1.0R");
			cr.setDescription("Some description");
			cr.setOwnerId(cu.getUserId());
			cr = client.createSolutionRevision(cr);
			client.updateSolutionRevision(cr);
			Assert.assertNotNull(cr.getRevisionId());
			logger.info("Created solution revision {}", cr.getRevisionId());

			logger.info("Adding artifact to revision");
			client.addSolutionRevisionArtifact(cs.getSolutionId(), cr.getRevisionId(), ca.getArtifactId());

			logger.info("Querying for revisions by solution");
			List<MLPSolutionRevision> revs = client.getSolutionRevisions(new String[] { s.getSolutionId() });
			Assert.assertTrue(revs != null && revs.size() > 0);
			for (MLPSolutionRevision r : revs) {
				logger.info("\tRevision: {}", r);
				List<MLPArtifact> al = client.getSolutionRevisionArtifacts(cs.getSolutionId(), cr.getRevisionId());
				for (MLPArtifact a : al) {
					logger.info("\t\tArtifact: {}", a);
				}
			}

			// Create Solution Rating
			logger.info("Creating solution rating");
			MLPSolutionRating ur = new MLPSolutionRating();
			ur.setSolutionId(cs.getSolutionId());
			ur.setUserId(cu.getUserId());
			ur.setRating(4);
			ur.setTextReview("Awesome");
			ur.setCreated(new Date());
			ur = client.createSolutionRating(ur);
			logger.info("Created solution rating {}", ur);
			MLPSolutionRating rating = client.getSolutionRating(cs.getSolutionId(), cu.getUserId());
			Assert.assertNotNull(rating);
			logger.info("Fetched solution rating {}", rating);
			ur.setTextReview("Yet awesomer");
			ur.setRating(5);
			client.updateSolutionRating(ur);
			logger.info("Updated solution rating {}", ur);
			RestPageResponse<MLPSolutionRating> ratings = client.getSolutionRatings(cs.getSolutionId(),
					new RestPageRequest(0, 1));
			Assert.assertTrue(ratings != null && ratings.getNumberOfElements() > 0);
			logger.info("Solution rating count {}", ratings.getNumberOfElements());

			// Compute the average rating
			stats = client.getSolutionWebMetadata(cs.getSolutionId());
			Assert.assertNotNull(stats);
			Assert.assertTrue(stats.getRatingAverageTenths() > 0);
			logger.info("Computed solution rating average: {}", stats.getRatingAverageTenths());

			// Test validation
			MLPSolutionValidation sv = new MLPSolutionValidation();
			sv.setSolutionId(cs.getSolutionId());
			sv.setRevisionId(cr.getRevisionId());
			sv.setTaskId("fake-task-id");
			sv.setValidationTypeCode(ValidationTypeCode.LC.name());
			sv = client.createSolutionValidation(sv);
			Assert.assertNotNull(sv);
			logger.info("Created solution validation {}", sv);
			List<MLPSolutionValidation> vals = client.getSolutionValidations(cs.getSolutionId(), cr.getRevisionId());
			Assert.assertTrue(vals != null && vals.size() > 0);
			logger.info("Fetched solution validation {}", vals.get(0));
			sv.setValidationStatusCode(ValidationStatusCode.FA.toString());
			client.updateSolutionValidation(sv);

			MLPValidationSequence seq = new MLPValidationSequence(1, ValidationTypeCode.LC.toString());
			seq = client.createValidationSequence(seq);
			logger.info("Created validation sequence {}", seq.toString());
			List<MLPValidationSequence> seqList = client.getValidationSequences();
			Assert.assertTrue(seqList != null && seqList.size() > 0);
			client.deleteValidationSequence(seq);

			// Create Solution download
			MLPSolutionDownload sd = new MLPSolutionDownload(cs.getSolutionId(), ca.getArtifactId(), cu.getUserId());
			sd = client.createSolutionDownload(sd);
			logger.info("Created solution download {}", sd);

			// Query for downloads
			RestPageResponse<MLPSolutionDownload> dnls = client.getSolutionDownloads(cs.getSolutionId(), rp);
			Assert.assertTrue(dnls.getNumberOfElements() > 0);

			// Count the downloads
			MLPSolutionWeb readStats = client.getSolutionWebMetadata(cs.getSolutionId());
			Assert.assertNotNull(readStats);
			Assert.assertTrue(readStats.getDownloadCount() > 0);
			logger.info("Solution download count is {}", readStats.getDownloadCount());

			// Create Solution favorite for a user
			MLPSolutionFavorite sf1 = new MLPSolutionFavorite();
			sf1.setSolutionId(cs.getSolutionId());
			sf1.setUserId(cu.getUserId());
			sf1 = client.createSolutionFavorite(sf1);
			logger.info("Created a favorite solution {}", sf1);

			// Get favorite solutions
			RestPageResponse<MLPSolution> favePage = client.getFavoriteSolutions(cu.getUserId(), rp);
			Assert.assertNotNull(favePage);
			Assert.assertTrue(favePage.getNumberOfElements() > 0);
			for (MLPSolution mlpsol : favePage)
				logger.info("Favorite Solution for user {} is {}, name {}", cu.getUserId(), mlpsol.getSolutionId(),
						mlpsol.getName());
			// Create and update solution deployment
			MLPSolutionDeployment dep = new MLPSolutionDeployment();
			dep.setDeploymentId(UUID.randomUUID().toString());
			dep.setSolutionId(cs.getSolutionId());
			dep.setRevisionId(cr.getRevisionId());
			dep.setUserId(cu.getUserId());
			dep.setDeploymentStatusCode(DeploymentStatusCode.ST.name());
			dep = client.createSolutionDeployment(dep);
			Assert.assertNotNull(dep.getDeploymentId());
			logger.info("Created solution deployent {}", dep);

			dep.setDetail("{ 'tag' : 'value' }");
			client.updateSolutionDeployment(dep);

			// Query for solution deployments
			RestPageResponse<MLPSolutionDeployment> userDeps = client.getUserDeployments(cu.getUserId(), rp);
			Assert.assertTrue(userDeps != null & userDeps.getNumberOfElements() > 0);
			RestPageResponse<MLPSolutionDeployment> deps = client.getSolutionDeployments(cs.getSolutionId(),
					cr.getRevisionId(), rp);
			Assert.assertTrue(deps != null && deps.getNumberOfElements() > 0);
			RestPageResponse<MLPSolutionDeployment> userSolDeps = client.getUserSolutionDeployments(cs.getSolutionId(),
					cr.getRevisionId(), cu.getUserId(), rp);
			Assert.assertTrue(userSolDeps != null && userSolDeps.getNumberOfElements() > 0);

			// delete the deployment
			client.deleteSolutionDeployment(dep);

			logger.info("Querying for revisions by artifact");
			List<MLPSolutionRevision> revsByArt = client.getSolutionRevisionsForArtifact(ca.getArtifactId());
			Assert.assertTrue(revsByArt != null && revsByArt.size() > 0);
			for (MLPSolutionRevision r : revs)
				logger.info("\tRevision: {}", r);

			if (cleanup) {
				logger.info("Deleting newly created instances");
				client.dropSolutionTag(cs.getSolutionId(), tagName1);
				client.deleteTag(tag1);
				client.deleteTag(tag2);
				// Server SHOULD cascade deletes.
				client.deleteSolutionRating(ur);
				client.deleteSolutionValidation(sv);
				client.deleteSolutionDownload(sd);
				client.deleteSolutionFavorite(sf1);
				// delete-solution should cascade to sol-rev-art
				client.deleteSolution(cs.getSolutionId());
				client.deleteArtifact(ca.getArtifactId());
				client.deletePeerSubscription(ps.getSubId());
				client.deletePeer(pr.getPeerId());
				client.deleteUserLoginProvider(clp);
				client.deleteUser(cu.getUserId());

				try {
					MLPSolution deleted = client.getSolution(cs.getSolutionId());
					throw new Exception("Found a deleted solution: " + deleted);
				} catch (HttpClientErrorException ex) {
					// this is expected, the item should not exist
					logger.info("Caught expected exception: " + ex.getResponseBodyAsString());
				}

			} // cleanup

		} catch (HttpStatusCodeException ex) {
			logger.error("createSolutionWithArtifacts failed: " + ex.getResponseBodyAsString(), ex);
			throw ex;
		}

	}

	@Test
	public void testRoleAndFunctions() throws Exception {
		try {
			MLPUser cu = new MLPUser();
			String unique = Long.toString(new Date().getTime());
			final String loginName = "user-" + unique;
			final String loginPass = "test_client_pass";
			cu.setLoginName(loginName);
			cu.setLoginHash(loginPass);
			final String firstName = "test_" + unique;
			cu.setFirstName(firstName);
			final String lastName = "test-last-name";
			cu.setLastName(lastName);
			cu.setActive(true);
			cu.setLoginPassExpire(new Date());
			cu = client.createUser(cu);
			Assert.assertNotNull(cu.getUserId());
			logger.info("Created user with ID {}", cu.getUserId());

			logger.info("Creating a password change request");
			MLPPasswordChangeRequest req = new MLPPasswordChangeRequest(loginPass, "HardToRemember");
			client.updatePassword(cu, req);
			logger.info("Password changed successfully");

			MLPRole cr = new MLPRole();
			cr.setName("something or the other");
			cr = client.createRole(cr);
			Assert.assertNotNull(cr.getRoleId());
			logger.info("Created role: {}", cr);

			final String roleName = "My test role";
			cr.setName(roleName);
			client.updateRole(cr);

			MLPRole cr2 = new MLPRole();
			cr2.setName("Second role");
			cr2 = client.createRole(cr2);

			long roleCount = client.getRoleCount();
			Assert.assertTrue(roleCount > 0);

			RestPageResponse<MLPRole> roles = client.getRoles(new RestPageRequest(0, 1));
			Assert.assertTrue(roles.getNumberOfElements() > 0);

			HashMap<String, Object> roleRestr = new HashMap<>();
			roleRestr.put("name", roleName);
			List<MLPRole> roleResult = client.searchRoles(roleRestr, false);
			Assert.assertTrue(roleResult.size() > 0);

			MLPRoleFunction crf = new MLPRoleFunction();
			final String roleFuncName = "My test role function";
			crf.setName(roleFuncName);
			crf.setRoleId(cr.getRoleId());
			crf = client.createRoleFunction(crf);
			logger.info("Created role function {}", crf);
			Assert.assertNotNull(crf.getRoleFunctionId());

			crf.setName("My test role function updated");
			client.updateRoleFunction(crf);

			MLPRole res = client.getRole(cr.getRoleId());
			Assert.assertNotNull(res.getRoleId());
			logger.info("Retrieved role {}", res);

			List<MLPRoleFunction> fetchedRoleFns = client.getRoleFunctions(cr.getRoleId());
			Assert.assertTrue(fetchedRoleFns != null && fetchedRoleFns.size() > 0);
			MLPRoleFunction roleFnFromList = fetchedRoleFns.get(0);
			logger.info("First role function in list {}", roleFnFromList);

			MLPRoleFunction roleFn = client.getRoleFunction(cr.getRoleId(), roleFnFromList.getRoleFunctionId());
			logger.info("Single role function {}", roleFn);
			Assert.assertNotNull(roleFn);

			logger.info("Adding role 1 for user");
			client.addUserRole(cu.getUserId(), cr.getRoleId());
			List<MLPRole> addedRoles = client.getUserRoles(cu.getUserId());
			Assert.assertTrue(addedRoles != null && addedRoles.size() == 1);

			logger.info("Adding role 2 for user");
			List<String> userIds = new ArrayList<>();
			userIds.add(cu.getUserId());
			client.addUsersInRole(userIds, cr2.getRoleId());

			long role2count = client.getRoleUsersCount(cr2.getRoleId());
			Assert.assertTrue(role2count == 1);
			logger.info("Count of users in role 2: {}", role2count);

			addedRoles = client.getUserRoles(cu.getUserId());
			Assert.assertTrue(addedRoles != null && addedRoles.size() == 2);
			logger.info("Count of roles for user: {}", addedRoles.size());

			logger.info("Dropping role 1 for user");
			client.dropUserRole(cu.getUserId(), cr.getRoleId());

			logger.info("Dropping role 2 for user");
			client.dropUsersInRole(userIds, cr2.getRoleId());

			List<MLPRole> revisedUserRoles = client.getUserRoles(cu.getUserId());
			Assert.assertTrue(revisedUserRoles != null && revisedUserRoles.isEmpty());
			logger.info("User role count is zero");

			List<String> roleIds = new ArrayList<>();
			roleIds.add(cr.getRoleId());
			roleIds.add(cr2.getRoleId());
			client.updateUserRoles(cu.getUserId(), roleIds);
			revisedUserRoles = client.getUserRoles(cu.getUserId());
			Assert.assertTrue(revisedUserRoles != null && revisedUserRoles.size() == 2);
			logger.info("User role count is back at 2");

			logger.info("Deleting role function");
			client.deleteRoleFunction(cr.getRoleId(), crf.getRoleFunctionId());

			logger.info("Deleting role 1");
			client.deleteRole(cr.getRoleId());
		} catch (HttpClientErrorException ex) {
			logger.error("Client reported error; body is {}", ex.getResponseBodyAsString());
			throw ex;
		} catch (HttpServerErrorException ex) {
			logger.error("Server reported error; body is {}", ex.getResponseBodyAsString());
			throw ex;
		} catch (Exception ex) {
			logger.error("testRoleAndFunctions failed", ex);
			throw ex;
		}

	}

	@Test
	public void testNotifications() throws Exception {
		try {
			MLPUser cu = new MLPUser();
			final String loginName = "notif_" + Long.toString(new Date().getTime());
			cu.setLoginName(loginName);
			cu = client.createUser(cu);
			Assert.assertNotNull(cu.getUserId());

			MLPNotification no = new MLPNotification();
			no.setTitle("notif title");
			no.setMessage("notif msg");
			no.setUrl("http://notify.me");
			Date now = new Date();
			no.setStart(new Date(now.getTime() - 60 * 1000));
			no.setEnd(new Date(now.getTime() + 60 * 1000));
			no = client.createNotification(no);
			Assert.assertNotNull(no.getNotificationId());

			no.setMessage("New enhanced message");
			client.updateNotification(no);

			MLPNotification no2 = new MLPNotification();
			no2.setTitle("notif2 title");
			no2.setMessage("notif2 msg");
			no2.setUrl("http://notify2.me");
			no2.setStart(new Date(now.getTime() - 60 * 1000));
			no2.setEnd(new Date(now.getTime() + 60 * 1000));
			no2 = client.createNotification(no2);

			long notCountTrans = client.getNotificationCount();
			Assert.assertTrue(notCountTrans > 0);

			RestPageResponse<MLPNotification> notifics = client.getNotifications(new RestPageRequest(0, 100));
			Assert.assertTrue(notifics.getNumberOfElements() == notCountTrans);

			// Assign user to this notification
			client.addUserToNotification(no.getNotificationId(), cu.getUserId());
			RestPageResponse<MLPUserNotification> userNotifs = client.getUserNotifications(cu.getUserId(), null);
			Assert.assertTrue(userNotifs.iterator().hasNext());
			logger.info("First user notification {}", userNotifs.iterator().next());

			// This next step mimics what a controller will do
			client.setUserViewedNotification(no.getNotificationId(), cu.getUserId());

			userNotifs = client.getUserNotifications(cu.getUserId(), null);
			// Assert.assertFalse(userNotifs.iterator().next().());

			client.dropUserFromNotification(no.getNotificationId(), cu.getUserId());
			client.deleteNotification(no.getNotificationId());
			client.deleteUser(cu.getUserId());
		} catch (HttpStatusCodeException ex) {
			logger.error("testNotifications got response {}", ex.getResponseBodyAsString());
			logger.error("testNotifications failed", ex);
			throw ex;
		}

	}

	@Test
	public void testSiteConfig() throws Exception {
		final String s64 = "12345678901234567890123456789012345678901234567890123456789012345";
		MLPUser cu = new MLPUser("loginId", true);
		cu = client.createUser(cu);
		Assert.assertNotNull(cu.getUserId());
		try {
			client.getSiteConfig("bogus");
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Failed to get missing config as expected {}", ex.getResponseBodyAsString());
		}
		final String key = "myKey";
		try {
			MLPSiteConfig sc = new MLPSiteConfig(key, "{ 'some' : 'json' }");
			sc.setUserId("bogus");
			client.createSiteConfig(sc);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Failed to create config with bad user as expected {}", ex.getResponseBodyAsString());
		}
		try {
			client.createSiteConfig(new MLPSiteConfig(s64, "{ 'some' : 'json' }"));
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Failed to create config with long key as expected {}", ex.getResponseBodyAsString());
		}
		try {
			client.updateSiteConfig(new MLPSiteConfig(key, "{ 'some' : 'json' }"));
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Failed to update config as expected {}", ex.getResponseBodyAsString());
		}
		// this should work
		MLPSiteConfig config = new MLPSiteConfig(key, "{ 'some' : 'json' }");
		config = client.createSiteConfig(config);
		Assert.assertNotNull(config.getCreated());
		logger.info("Created site config {}", config);
		config = client.getSiteConfig(key);
		Assert.assertNotNull(config);

		try {
			client.createSiteConfig(config);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Failed to create dupe config as expected {}", ex.getResponseBodyAsString());
		}
		config.setConfigValue("{ 'other' : 'stuff' }");
		client.updateSiteConfig(config);
		try {
			config.setConfigValue(null);
			client.updateSiteConfig(config);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Failed to update bad config as expected {}", ex.getResponseBodyAsString());
		}
		client.deleteSiteConfig(config.getConfigKey());
		try {
			client.deleteSiteConfig(config.getConfigKey());
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Failed to delete fake config as expected {}", ex.getResponseBodyAsString());
		}
		client.deleteUser(cu.getUserId());
	}

	@Test
	public void testThreadsComments() throws Exception {
		MLPUser cu = new MLPUser("commentUser", true);
		cu = client.createUser(cu);
		Assert.assertNotNull(cu.getUserId());

		MLPSolution cs = new MLPSolution("solution name", cu.getUserId(), true);
		cs = client.createSolution(cs);
		Assert.assertNotNull(cs.getSolutionId());

		MLPSolutionRevision cr = new MLPSolutionRevision(cs.getSolutionId(), "1.0", cu.getUserId());
		cr = client.createSolutionRevision(cr);
		Assert.assertNotNull(cr.getRevisionId());
			
		MLPThread thread = client.createThread(new MLPThread(cs.getSolutionId(), cr.getRevisionId()));
		Assert.assertTrue(thread != null && thread.getThreadId() != null);
		RestPageResponse<MLPThread> threads = client.getThreads(new RestPageRequest(0, 1));
		Assert.assertTrue(threads != null && threads.getNumberOfElements() > 0);

		MLPThread retrieved = client.getThread(thread.getThreadId());
		Assert.assertNotNull(retrieved);
		
		RestPageResponse<MLPThread> threadsById = client.getSolutionRevisionThreads(cs.getSolutionId(), cr.getRevisionId(), new RestPageRequest(0, 1));
		Assert.assertTrue(threadsById != null && threadsById.getNumberOfElements() > 0);

		long threadCount = client.getThreadCount();
		Assert.assertTrue(threadCount > 0);

		thread.setTitle("thread title");
		client.updateThread(thread);

		// Violate contraints
		try {
			MLPThread t = new MLPThread();
			t.setTitle(s64 + s64);
			client.createThread(t);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Failed constraints on create as expected {}", ex.getResponseBodyAsString());
		}
		try {
			thread.setTitle(s64 + s64);
			client.updateThread(thread);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Failed constraints on update as expected {}", ex.getResponseBodyAsString());
		}
		// set back old title
		thread.setTitle("thread title");

		// Recreate should fail
		try {
			client.createThread(thread);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Failed to create dupe as expected {}", ex.getResponseBodyAsString());
		}
		MLPThread bogus = new MLPThread();
		bogus.setThreadId("bogus");
		try {
			client.getThread("bogus");
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Failed to get missing as expected {}", ex.getResponseBodyAsString());
		}
		// Update of missing should fail
		try {
			client.updateThread(bogus);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Failed to update missing as expected {}", ex.getResponseBodyAsString());
		}
		// Delete of missing should fail
		try {
			client.deleteThread("bogus");
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Failed to delete missing as expected {}", ex.getResponseBodyAsString());
		}

		MLPComment parent = client.createComment(new MLPComment(thread.getThreadId(), cu.getUserId(), "parent text"));
		Assert.assertTrue(parent != null && parent.getCommentId() != null);
		logger.info("Created parent comment: " + parent.toString());

		parent = client.getComment(thread.getThreadId(), parent.getCommentId());
		Assert.assertNotNull(parent);

		MLPComment reply = new MLPComment(thread.getThreadId(), cu.getUserId(), "child text");
		reply.setParentId(parent.getCommentId());
		reply = client.createComment(reply);
		Assert.assertTrue(reply != null && reply.getCommentId() != null);
		logger.info("Created reply comment: " + reply.toString());

		reply.setText(s64);
		client.updateComment(reply);

		long commentCount = client.getThreadCommentCount(thread.getThreadId());
		Assert.assertTrue(commentCount > 0);

		RestPageResponse<MLPComment> threadComments = client.getThreadComments(thread.getThreadId(),
				new RestPageRequest(0, 1));
		Assert.assertTrue(threadComments != null && threadComments.hasContent());

		RestPageResponse<MLPComment> commentsById = client.getSolutionRevisionComments(cs.getSolutionId(), cr.getRevisionId(), new RestPageRequest(0, 1));
		Assert.assertTrue(commentsById != null && commentsById.getNumberOfElements() > 0);

		try {
			client.getComment("bogus", "bogus");
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Failed to get missing comment as expected {}", ex.getResponseBodyAsString());
		}
		try {
			MLPComment c = new MLPComment("thread", "user", "text");
			c.setParentId("bogus");
			client.createComment(c);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Failed to create comment bad parent as expected {}", ex.getResponseBodyAsString());
		}
		try {
			client.createComment(new MLPComment("bogus", "bogus", "text"));
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Failed to create comment bad thread as expected {}", ex.getResponseBodyAsString());
		}
		try {
			client.createComment(new MLPComment(thread.getThreadId(), "bogus", "text"));
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Failed to create comment bad user as expected {}", ex.getResponseBodyAsString());
		}
		char [] longCommentChars = new char[8193];
		for (int i = 0; i < longCommentChars.length; ++i)
			longCommentChars[i] = 'x';
		String longCommentString = new String(longCommentChars);
		try {
			MLPComment large = new MLPComment(thread.getThreadId(), cu.getUserId(), longCommentString);
			client.createComment(large);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Failed to create comment with large text as expected {}", ex.getResponseBodyAsString());
		}
		try {
			client.createComment(reply);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Failed to create dupe as expected {}", ex.getResponseBodyAsString());
		}
		try {
			MLPComment c = new MLPComment("bogus", "bogus", "text");
			c.setCommentId("bogus");
			client.updateComment(c);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Failed to update missing comment as expected {}", ex.getResponseBodyAsString());
			reply.setThreadId(thread.getThreadId());
		}
		try {
			reply.setThreadId("bogus");
			client.updateComment(reply);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Failed to update with bad thread as expected {}", ex.getResponseBodyAsString());
			reply.setThreadId(thread.getThreadId());
		}
		try {
			reply.setParentId("bogus");
			client.updateComment(reply);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Failed to update with bad parent as expected {}", ex.getResponseBodyAsString());
			reply.setParentId(parent.getCommentId());
		}
		try {
			reply.setUserId("bogus");
			client.updateComment(reply);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Failed to update with bad user as expected {}", ex.getResponseBodyAsString());
			reply.setUserId(cu.getUserId());
		}
		try {
			reply.setText(longCommentString);
			client.updateComment(reply);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Failed to update existing comment with large text as expected {}",
					ex.getResponseBodyAsString());
			reply.setText("short");
		}
		try {
			client.updateComment(new MLPComment(thread.getThreadId(), "bogus", "text"));
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Failed to update missing comment as expected {}", ex.getResponseBodyAsString());
		}
		try {
			client.deleteComment("bogus", "bogus");
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Failed to delete missing as expected {}", ex.getResponseBodyAsString());
		}

		client.deleteComment(thread.getThreadId(), parent.getCommentId());
		client.deleteComment(thread.getThreadId(), reply.getCommentId());
		client.deleteThread(thread.getThreadId());
		client.deleteSolutionRevision(cs.getSolutionId(), cr.getRevisionId());
		client.deleteSolution(cs.getSolutionId());
		client.deleteUser(cu.getUserId());
	}

	@Test
	public void testErrorConditions() throws Exception {

		MLPUser cu = new MLPUser();
		cu.setUserId(UUID.randomUUID().toString());
		try {
			client.getUser(cu.getUserId());
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Get user failed as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			client.deleteUser(cu.getUserId());
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Delete user failed as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			client.createUser(cu);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Create user failed on empty as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			client.updateUser(cu);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Update user failed on missing as expected: {}", ex.getResponseBodyAsString());
		}
		// Value too long
		try {
			client.createUser(new MLPUser(s64, true));
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Create user failed on constraint as expected: {}", ex.getResponseBodyAsString());
		}
		// This one is supposed to work
		final String loginName = "user-" + Long.toString(new Date().getTime());
		final String sillyGoose = "sillygoose";
		cu = new MLPUser(loginName, true);
		cu.setLoginHash(sillyGoose);
		cu = client.createUser(cu);
		try {
			client.createUser(cu);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Create user failed on dupe as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			cu.setLoginHash("bogus");
			cu.setEmail(s64);
			client.updateUser(cu);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Update user failed on constraint as expected: {}", ex.getResponseBodyAsString());
		}

		try {
			client.loginUser(loginName, "");
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Loging rejected on empty pass, as expected {}", ex.getResponseBodyAsString());
		}
		try {
			client.loginUser(loginName, sillyGoose + "bogus");
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Loging rejected on bad pass, as expected {}", ex.getResponseBodyAsString());
		}

		try {
			MLPUser bogusUser = new MLPUser();
			bogusUser.setUserId("bogus");
			client.updatePassword(bogusUser, new MLPPasswordChangeRequest(sillyGoose + "bogus", "happyfeet"));
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Password update rejected on bad user, as expected {}", ex.getResponseBodyAsString());
		}
		try {
			client.updatePassword(cu, new MLPPasswordChangeRequest(sillyGoose + "bogus", "happyfeet"));
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Password update rejected invalid old password, as expected {}", ex.getResponseBodyAsString());
		}
		try {
			client.updatePassword(cu, new MLPPasswordChangeRequest(sillyGoose, ""));
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Password update rejected empty new password as expected {}", ex.getResponseBodyAsString());
		}
		// No way to trigger constraint violation for password change

		try {
			client.addUserRole("bogusUser", "bogusRole");
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Add role failed on bad user as expected {}", ex.getResponseBodyAsString());
		}
		try {
			client.addUserRole(cu.getUserId(), "bogusRole");
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Add role failed on bad role as expected {}", ex.getResponseBodyAsString());
		}
		List<String> roles = new ArrayList<>();
		roles.add("bogusRole");
		try {
			client.updateUserRoles("boguUser", roles);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Update user roles failed on bad user as expected {}", ex.getResponseBodyAsString());
		}
		try {
			client.updateUserRoles(cu.getUserId(), roles);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Update user roles failed on bad role as expected {}", ex.getResponseBodyAsString());
		}
		try {
			client.dropUserRole("bogusUser", "bogusRole");
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Drop role failed on bad user as expected {}", ex.getResponseBodyAsString());
		}
		try {
			client.dropUserRole(cu.getUserId(), "bogusRole");
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Drop role failed on bad role as expected {}", ex.getResponseBodyAsString());
		}
		List<String> users = new ArrayList<>();
		try {
			client.addUsersInRole(users, "bogusRole");
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("addUsersInRole failed on bad role as expected {}", ex.getResponseBodyAsString());
		}
		// Supposed to succeed
		MLPRole cr = client.createRole(new MLPRole("some name", true));
		try {
			client.addUsersInRole(users, cr.getRoleId());
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("addUsersInRole failed on empty list as expected {}", ex.getResponseBodyAsString());
		}
		users.add("bogusUser");
		try {
			client.addUsersInRole(users, cr.getRoleId());
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("addUsersInRole failed on bad user as expected {}", ex.getResponseBodyAsString());
		}
		users.clear();
		users.add(cu.getUserId());
		try {
			client.dropUsersInRole(users, cr.getRoleId());
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("dropUsersInRole failed on not-in-role as expected {}", ex.getResponseBodyAsString());
		}
		// supposed to work
		client.addUsersInRole(users, cr.getRoleId());
		try {
			client.addUsersInRole(users, cr.getRoleId());
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("addUsersInRole failed on already-in-role as expected {}", ex.getResponseBodyAsString());
		}

		try {
			client.getUserLoginProvider("bogusUser", "bogusCode", "bogusLogin");
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("getUserLoginProvider failed on bad user as expected {}", ex.getResponseBodyAsString());
		}
		try {
			client.getUserLoginProvider(cu.getUserId(), "bogusCode", "bogusLogin");
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("getUserLoginProvider failed on bad code as expected {}", ex.getResponseBodyAsString());
		}
		try {
			client.getUserLoginProvider(cu.getUserId(), LoginProviderCode.FB.name(), "bogusLogin");
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("getUserLoginProvider failed on bad login as expected {}", ex.getResponseBodyAsString());
		}
		try {
			client.getUserLoginProviders("bogusUser");
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("getUserLoginProviders failed on bad user as expected {}", ex.getResponseBodyAsString());
		}
		try {
			client.createUserLoginProvider(new MLPUserLoginProvider("bogus", "bogus", "something", "access token", 1));
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("createUserLoginProvider failed on bad user as expected {}", ex.getResponseBodyAsString());
		}
		try {
			client.createUserLoginProvider(
					new MLPUserLoginProvider(cu.getUserId(), "bogus", "something", "access token", 1));
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("createUserLoginProvider failed on bad code as expected {}", ex.getResponseBodyAsString());
		}
		MLPUserLoginProvider clp = new MLPUserLoginProvider(cu.getUserId(), LoginProviderCode.GH.name(), "something",
				"access token", 1);
		clp = client.createUserLoginProvider(clp);
		Assert.assertNotNull(clp.getCreated());
		try {
			clp.setDisplayName(s64 + s64 + s64 + s64 + s64);
			client.createUserLoginProvider(clp);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("createUserLoginProvider failed on constraint as expected {}", ex.getResponseBodyAsString());
		}
		try {
			// display name should violate constraint
			clp.setDisplayName(s64 + s64 + s64 + s64 + s64);
			client.updateUserLoginProvider(clp);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("updateUserLoginProvider failed on constraint as expected {}", ex.getResponseBodyAsString());
		}
		try {
			clp.setUserId("bogus");
			client.updateUserLoginProvider(clp);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("updateUserLoginProvider failed on bad user as expected {}", ex.getResponseBodyAsString());
		}
		try {
			clp.setUserId(cu.getUserId());
			clp.setProviderCode("bogus");
			client.updateUserLoginProvider(clp);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("updateUserLoginProvider failed on bad code as expected {}", ex.getResponseBodyAsString());
		}
		try {
			client.deleteUserLoginProvider(clp);
		} catch (HttpStatusCodeException ex) {
			logger.info("deleteUserLoginProvider failed as expected {}", ex.getResponseBodyAsString());
		}

		try {
			Map<String, Object> queryParameters = new HashMap<>();
			queryParameters.put("bogusFieldName", "bogusFieldFValue");
			client.searchUsers(queryParameters, false);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Search users failed on bad field as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			Map<String, Object> queryParameters = new HashMap<>();
			queryParameters.put("picture", new MLPUser());
			client.searchUsers(queryParameters, false);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Search users failed on bad type as expected: {}", ex.getResponseBodyAsString());
		}

		MLPSolution cs = new MLPSolution();
		cs.setSolutionId(UUID.randomUUID().toString());
		try {
			client.getSolution(cs.getSolutionId());
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Get solution failed as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			client.deleteSolution(cs.getSolutionId());
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Delete solution failed as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			client.createSolution(cs);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Create solution failed on empty as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			client.updateSolution(cs);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Update solution failed on empty as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			client.createSolution(new MLPSolution("name", s64, true));
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Create solution failed on constraints as expected: {}", ex.getResponseBodyAsString());
		}
		// This one is supposed to work
		cs = new MLPSolution("sol name", cu.getUserId(), true);
		cs = client.createSolution(cs);
		try {
			client.createSolution(cs);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Create solution failed on dupe as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			cs.setOwnerId(s64);
			client.updateSolution(cs);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Update solution failed on constraints as expected: {}", ex.getResponseBodyAsString());
		}
		// restore valid value
		cs.setOwnerId(cu.getUserId());

		try {
			Map<String, Object> queryParameters = new HashMap<>();
			queryParameters.put("bogusFieldName", "bogusFieldFValue");
			client.searchSolutions(queryParameters, false);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Search solution failed as expected: {}", ex.getResponseBodyAsString());
		}

		MLPSolutionRevision csr = new MLPSolutionRevision();
		csr.setRevisionId(UUID.randomUUID().toString());
		csr.setSolutionId("bogus");
		try {
			client.getSolutionRevision(cs.getSolutionId(), csr.getRevisionId());
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Get solution revision failed as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			client.deleteSolutionRevision("bogus", "bogus");
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Delete solution revision failed as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			client.deleteSolutionRevision(cs.getSolutionId(), csr.getRevisionId());
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Delete solution revision failed as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			client.createSolutionRevision(csr);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Create solution revision failed on empty as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			client.updateSolutionRevision(csr);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Update solution revision failed on empty as expected: {}", ex.getResponseBodyAsString());
		}
		csr = new MLPSolutionRevision(cs.getSolutionId(), s64, cu.getUserId());
		try {
			client.createSolutionRevision(csr);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Create revision failed on constraints as expected: {}", ex.getResponseBodyAsString());
		}
		// This one is supposed to work
		final String solRevVersion = "1.0R";
		csr.setVersion(solRevVersion);
		csr = client.createSolutionRevision(csr);
		try {
			client.createSolutionRevision(csr);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Create revision failed on dupe as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			csr.setVersion(s64);
			client.updateSolutionRevision(csr);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Update solution revision failed on constraints as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			client.updateSolutionRevision(csr);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Update solution revision failed on empty as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			MLPSolutionRevision r = new MLPSolutionRevision(cs.getSolutionId(), "version", "ownerId");
			r.setRevisionId("bogus");
			client.updateSolutionRevision(r);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Update solution revision failed on bad rev as expected: {}", ex.getResponseBodyAsString());
		}
		// Restore valid value
		csr.setVersion(solRevVersion);

		try {
			client.deleteSolutionValidation(
					new MLPSolutionValidation("solId", "revId", "taskId", ValidationTypeCode.LC.name()));
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Delete solution validation failed as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			client.createSolutionValidation(
					new MLPSolutionValidation("solId", "revId", "taskId", ValidationTypeCode.LC.name()));
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Create solution validation failed as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			client.createSolutionValidation(
					new MLPSolutionValidation(cs.getSolutionId(), "revId", "taskId", ValidationTypeCode.LC.name()));
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Create solution validation failed on bad rev as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			client.createSolutionValidation(new MLPSolutionValidation(cs.getSolutionId(), csr.getRevisionId(), s64,
					ValidationTypeCode.LC.name()));
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Create solution validation failed on bad task id as expected: {}",
					ex.getResponseBodyAsString());
		}
		try {
			client.updateSolutionValidation(
					new MLPSolutionValidation(cs.getSolutionId(), "revId", "taskId", ValidationTypeCode.LC.name()));
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Update solution validation failed as expected: {}", ex.getResponseBodyAsString());
		}
		// should succeed
		MLPSolutionValidation sv = client.createSolutionValidation(new MLPSolutionValidation(cs.getSolutionId(),
				csr.getRevisionId(), "task", ValidationTypeCode.LC.name()));
		try {
			sv.setValidationTypeCode(s64);
			client.updateSolutionValidation(sv);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Update solution validation failed on constraint as expected: {}",
					ex.getResponseBodyAsString());
		}

		MLPArtifact ca = new MLPArtifact();
		ca.setArtifactId(UUID.randomUUID().toString());
		try {
			client.getArtifact(ca.getArtifactId());
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Get artifact failed as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			client.deleteArtifact(ca.getArtifactId());
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Delete artifact failed as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			client.createArtifact(ca);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Create artifact failed as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			client.updateArtifact(ca);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Update artifact failed as expected: {}", ex.getResponseBodyAsString());
		}
		// These should succeed
		ca = client.createArtifact(
				new MLPArtifact("version", ArtifactTypeCode.BP.name(), "name", "URI", cu.getUserId(), 1));
		try {
			client.createArtifact(ca);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Create artifact failed on dupe as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			HashMap<String, Object> restr = new HashMap<>();
			restr.put("bogus", "value");
			client.searchArtifacts(restr, false);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Search artifacts failed as expected: {}", ex.getResponseBodyAsString());
		}

		client.addSolutionRevisionArtifact(cs.getSolutionId(), csr.getRevisionId(), ca.getArtifactId());
		try {
			ca.setOwnerId(s64);
			client.updateArtifact(ca);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Update artifact failed on constraints as expected: {}", ex.getResponseBodyAsString());
		}
		// Restore valid value
		ca.setOwnerId(cu.getUserId());

		try {
			client.incrementSolutionViewCount("bogus");
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Increment soln view count failed as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			client.getSolutionDownloads("bogus", new RestPageRequest(0, 1));
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Get solution access users failed as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			client.getSolutionRatings("bogus", new RestPageRequest(0, 1));
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Get solution ratings users failed as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			client.getSolutionRevisionsForArtifact("bogus");
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Get solution revisions failed as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			client.getSolutionValidations("bogus", "bogus");
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Get solution validations failed as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			client.getSolutionWebMetadata("bogus");
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Get solution web meta failed as expected: {}", ex.getResponseBodyAsString());
		}

		try {
			MLPValidationSequence seq = new MLPValidationSequence(0, s64);
			client.createValidationSequence(seq);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("createValidationSequence failed as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			client.deleteValidationSequence(new MLPValidationSequence(0, "bogus"));
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("deleteValidationSequence failed as expected: {}", ex.getResponseBodyAsString());
		}

		try {
			client.deleteTag(new MLPTag("bogus"));
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Delete tag failed as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			client.createTag(new MLPTag(s64));
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Create tag failed on constraints as expected: {}", ex.getResponseBodyAsString());
		}
		// This should succeed
		MLPTag ct = client.createTag(new MLPTag("tag-" + new Date().toString()));
		try {
			client.createTag(ct);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Create tag failed on dupe as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			client.addSolutionTag("bogus", ct.getTag());
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Add tag failed as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			client.findSolutionsByTag("bogus-bogus-bogus", new RestPageRequest(0, 1));
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Find sols by tag failed as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			String [] searchTags = new String [] { "%" };
			client.findPortalSolutions(null, null, true, null, null, null, null, searchTags, new RestPageRequest(0,1)); 
			// I have not been able to make findPortalSolutions fail.
			// all arguments are optional; there is no illegal value; etc.
			// TODO: throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Find portal solutions failed as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			client.addSolutionTag("bogus", "bogus");
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Add tag failed as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			client.addSolutionTag(cs.getSolutionId(), "bogus");
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Add tag failed as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			client.dropSolutionTag("bogus", "bogus");
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Drop tag failed as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			client.dropSolutionTag("bogus", ct.getTag());
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Drop tag failed as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			client.dropSolutionTag(cs.getSolutionId(), "bogus");
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Drop tag failed as expected: {}", ex.getResponseBodyAsString());
		}
		client.deleteTag(ct);

		try {
			MLPSolutionDownload sd = new MLPSolutionDownload(s64, "artId", s64);
			sd.setDownloadId(999L);
			client.deleteSolutionDownload(sd);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Delete sol download failed as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			client.createSolutionDownload(new MLPSolutionDownload(s64, "artId", s64));
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Create sol download failed on constraint as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			client.createSolutionDownload(new MLPSolutionDownload(cs.getSolutionId(), "artId", "userId"));
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Create sol download failed on bad artifact as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			client.createSolutionDownload(new MLPSolutionDownload(cs.getSolutionId(), ca.getArtifactId(), "userId"));
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Create sol download failed on bad user as expected: {}", ex.getResponseBodyAsString());
		}

		try {
			client.deleteSolutionRating(new MLPSolutionRating("solutionId", "userId", 1));
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Delete rating failed as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			client.createSolutionRating(new MLPSolutionRating("solId", "userId", 1));
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Create rating failed on bad solution as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			client.createSolutionRating(new MLPSolutionRating(cs.getSolutionId(), "userId", 1));
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Create rating failed on bad user as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			MLPSolutionRating rating = new MLPSolutionRating(cs.getSolutionId(), cu.getUserId(), 0);
			rating.setRating(null);
			client.createSolutionRating(rating);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Create rating failed on constraint as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			client.updateSolutionRating(new MLPSolutionRating(cs.getSolutionId(), cu.getUserId(), 1));
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Update rating failed on missing as expected: {}", ex.getResponseBodyAsString());
		}
		MLPSolutionRating sr = new MLPSolutionRating(cs.getSolutionId(), cu.getUserId(), 1);
		sr = client.createSolutionRating(sr);
		try {
			sr.setRating(null);
			client.updateSolutionRating(sr);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Update rating failed on constraint as expected: {}", ex.getResponseBodyAsString());
		}

		MLPSolutionDeployment solDep = new MLPSolutionDeployment();
		solDep.setDeploymentId(UUID.randomUUID().toString());
		try {
			client.getSolutionDeployments("bogus", "bogus", new RestPageRequest());
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Get solution deployments failed as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			solDep.setSolutionId("bogus");
			solDep.setRevisionId("bogus");
			client.createSolutionDeployment(solDep);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Create solution deployment failed on bad solution ID as expected: {}",
					ex.getResponseBodyAsString());
		}
		try {
			solDep.setSolutionId(cs.getSolutionId());
			solDep.setRevisionId("bogus");
			client.createSolutionDeployment(solDep);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Create solution deployment failed on bad revision ID as expected: {}",
					ex.getResponseBodyAsString());
		}
		solDep.setRevisionId(csr.getRevisionId());
		try {
			solDep.setSolutionId(cs.getSolutionId());
			solDep.setRevisionId(csr.getRevisionId());
			solDep.setUserId("bogus");
			client.createSolutionDeployment(solDep);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Create solution deployment failed on bad user ID as expected: {}",
					ex.getResponseBodyAsString());
		}
		try {
			solDep.setUserId(cu.getUserId());
			// Field too large
			solDep.setTarget(s64 + s64);
			client.createSolutionDeployment(solDep);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Create solution deployment failed on constraints as expected: {}",
					ex.getResponseBodyAsString());
		}

		try {
			client.updateSolutionDeployment(solDep);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Update solution deployment failed as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			client.deleteSolutionDeployment(solDep);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Delete solution deployment failed as expected: {}", ex.getResponseBodyAsString());
		}
		// Should succeed
		solDep = client.createSolutionDeployment(new MLPSolutionDeployment(cs.getSolutionId(), csr.getRevisionId(),
				cu.getUserId(), DeploymentStatusCode.DP.name()));
		try {
			client.createSolutionDeployment(solDep);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Create dupe solution deployment failed as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			solDep.setTarget(s64 + s64);
			client.updateSolutionDeployment(solDep);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Update solution deployment failed on constraint as expected: {}",
					ex.getResponseBodyAsString());
		}

		try {
			client.addSolutionUserAccess(cs.getSolutionId(), "bogus");
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Add user solution ACL bad user failed as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			client.addSolutionUserAccess("solId", cu.getUserId());
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Add user solution ACL bad sol failed as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			client.dropSolutionUserAccess("solId", cu.getUserId());
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Drop user solution ACL failed bad sol as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			client.dropSolutionUserAccess(cs.getSolutionId(), "bogus");
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Drop user solution ACL failed bad user as expected: {}", ex.getResponseBodyAsString());
		}

		MLPNotification cn = new MLPNotification();
		cn.setNotificationId(UUID.randomUUID().toString());
		try {
			client.deleteNotification(cn.getNotificationId());
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Delete notification failed as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			client.createNotification(cn);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Create notification failed on missing as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			client.updateNotification(cn);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Update notification failed on missing as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			cn.setTitle(s64 + s64);
			cn.setStart(new Date());
			cn.setEnd(new Date());
			client.createNotification(cn);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Update notification failed on constraint as expected: {}", ex.getResponseBodyAsString());
		}
		// This one should work
		cn.setTitle("notif title");
		cn = client.createNotification(cn);
		try {
			client.createNotification(cn);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Create dupe notification failed as expected: {}", ex.getResponseBodyAsString());
		}
		cn.setTitle(s64 + s64);
		try {
			client.updateNotification(cn);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Create notification failed on constreaint as expected: {}", ex.getResponseBodyAsString());
		}

		MLPPeer cp = new MLPPeer();
		cp.setPeerId(UUID.randomUUID().toString());
		try {
			client.getPeer(cp.getPeerId());
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Get peer failed as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			client.deletePeer(cp.getPeerId());
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Delete peer failed as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			client.createPeer(cp);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Create peer failed as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			client.updatePeer(cp);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Update peer failed as expected: {}", ex.getResponseBodyAsString());
		}
		cp.setActive(true);
		cp.setSelf(false);
		cp.setContact1("me");
		cp.setContact2("you");
		try {
			cp.setName(s64);
			cp = client.createPeer(cp);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Create peer failed on constraint as expected: {}", ex.getResponseBodyAsString());
		}
		// This one is supposed to work
		cp = client.createPeer(
				new MLPPeer("peer name", "subj name", "api url", "web url", true, false, "contact 1", "contact 2", 1));

		try {
			cp = client.createPeer(cp);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Create peer failed on dupe as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			cp.setName(s64);
			client.updatePeer(cp);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Update peer failed on constraint as expected: {}", ex.getResponseBodyAsString());
		}

		try {
			client.getPeerSubscriptions("bogus");
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Get peer subs failed as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			client.getPeerSubscription(0L);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Get peer sub failed as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			client.createPeerSubscription(new MLPPeerSubscription("peerId"));
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Create peer sub failed as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			MLPPeerSubscription ps = new MLPPeerSubscription(cp.getPeerId());
			ps.setSelector(
					s64 + s64 + s64 + s64 + s64 + s64 + s64 + s64 + s64 + s64 + s64 + s64 + s64 + s64 + s64 + s64);
			client.createPeerSubscription(ps);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Create peer sub failed as expected: {}", ex.getResponseBodyAsString());
		}
		// Supposed to work
		MLPPeerSubscription ps = new MLPPeerSubscription(cp.getPeerId());
		ps = client.createPeerSubscription(ps);
		try {
			ps.setSelector(
					s64 + s64 + s64 + s64 + s64 + s64 + s64 + s64 + s64 + s64 + s64 + s64 + s64 + s64 + s64 + s64);
			client.updatePeerSubscription(ps);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Update peer sub failed on constraint as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			ps.setSubId(999L);
			client.updatePeerSubscription(ps);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Update peer sub failed on bad sub ID as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			client.deletePeerSubscription(999L);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Delete peer sub failed on bad sub ID as expected: {}", ex.getResponseBodyAsString());
		}

		try {
			HashMap<String, Object> peerRestr = new HashMap<>();
			peerRestr.put("bogus", "name");
			client.searchPeers(peerRestr, false);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Search peers failed on bad field as expected: {}", ex.getResponseBodyAsString());
		}

		MLPRole crCustomId = new MLPRole();
		crCustomId.setRoleId(UUID.randomUUID().toString());
		try {
			client.getRole(crCustomId.getRoleId());
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Get missing role failed as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			client.deleteRole(crCustomId.getRoleId());
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Delete  missing role failed as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			client.createRole(crCustomId);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Create role failed on missing name as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			client.updateRole(crCustomId);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Update missing role failed as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			HashMap<String, Object> roleRestr = new HashMap<>();
			roleRestr.put("bogus", "value");
			client.searchRoles(roleRestr, false);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Search role failed as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			client.getRoleFunctions("bogus");
		} catch (HttpStatusCodeException ex) {
			logger.info("Get role functions failed on invalid role as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			client.getRoleFunction("bogusRole", "bogusFn");
		} catch (HttpStatusCodeException ex) {
			logger.info("Get role function failed on invalid role as expected: {}", ex.getResponseBodyAsString());
		}

		try {
			client.createRole(cr);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Create role failed on dupe as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			cr.setName(s64 + s64);
			client.updateRole(cr);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Update role failed on constraint as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			client.createRoleFunction(new MLPRoleFunction("roleId", "name"));
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Create role fn failed on bad role as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			client.createRoleFunction(new MLPRoleFunction(cr.getRoleId(), s64 + s64));
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Create role fn failed on constraint as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			client.deleteRoleFunction("roleId", "name");
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Delete role fn failed as expected: {}", ex.getResponseBodyAsString());
		}
		// Supposed to work
		MLPRoleFunction rf = client.createRoleFunction(new MLPRoleFunction(cr.getRoleId(), "otherRoleFuncName"));
		try {
			rf.setName(s64 + s64);
			client.updateRoleFunction(rf);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Update role fn failed on constraint as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			rf.setRoleFunctionId("bogus");
			client.updateRoleFunction(rf);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Create role fn failed on bad role fn id as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			rf.setRoleId("bogus");
			client.updateRoleFunction(rf);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Create role fn failed on bad role as expected: {}", ex.getResponseBodyAsString());
		}

		try {
			client.getFavoriteSolutions("bogus", new RestPageRequest(0, 1));
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Get fave sols failed as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			MLPSolutionFavorite sf = new MLPSolutionFavorite("solId", "userId");
			client.createSolutionFavorite(sf);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Create fave sol failed as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			MLPSolutionFavorite sf = new MLPSolutionFavorite(cs.getSolutionId(), "userId");
			client.createSolutionFavorite(sf);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Create fave sol failed as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			MLPSolutionFavorite sf = new MLPSolutionFavorite(cs.getSolutionId(), "userId");
			client.deleteSolutionFavorite(sf);
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Delete fave sol failed as expected: {}", ex.getResponseBodyAsString());
		}

		try {
			client.addUserToNotification("notifId", "userId");
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Add user to notif failed as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			client.addUserToNotification("notifId", cu.getUserId());
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Add user to notif failed as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			client.setUserViewedNotification("notifId", "userId");
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Set user viewed notif failed as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			client.setUserViewedNotification("notifId", cu.getUserId());
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Add user to notif failed as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			client.dropUserFromNotification("notifId", "userId");
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Drop user notif failed as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			client.dropUserFromNotification("notifId", cu.getUserId());
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Add user to notif failed as expected: {}", ex.getResponseBodyAsString());
		}

		try {
			client.getUserDeployments("userId", new RestPageRequest(0, 1));
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Get user deps failed as expected: {}", ex.getResponseBodyAsString());
		}
		try {
			client.getUserSolutionDeployments("solId", "revId", "userId", new RestPageRequest(0, 1));
			throw new Exception("Unexpected success");
		} catch (HttpStatusCodeException ex) {
			logger.info("Get user soln deps failed as expected: {}", ex.getResponseBodyAsString());
		}

		try {
			client.deleteSolutionValidation(sv);
			client.deleteSolutionDeployment(solDep);
			client.dropSolutionRevisionArtifact(cs.getSolutionId(), csr.getRevisionId(), ca.getArtifactId());
			client.deleteSolutionRevision(cs.getSolutionId(), csr.getRevisionId());
			client.deleteArtifact(ca.getArtifactId());
			client.deletePeer(cp.getPeerId());
			client.deleteSolution(cs.getSolutionId());
			client.deleteUser(cu.getUserId());
		} catch (HttpStatusCodeException ex) {
			logger.info("Failed: {}", ex.getResponseBodyAsString());
			throw ex;
		}
	}

}

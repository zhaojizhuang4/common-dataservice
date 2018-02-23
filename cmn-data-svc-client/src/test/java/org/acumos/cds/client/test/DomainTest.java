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

package org.acumos.cds.client.test;

import org.acumos.cds.domain.MLPAccessType;
import org.acumos.cds.domain.MLPArtifact;
import org.acumos.cds.domain.MLPArtifactType;
import org.acumos.cds.domain.MLPCodeNamePair;
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
import org.acumos.cds.domain.MLPPeerStatus;
import org.acumos.cds.domain.MLPPeerSubscription;
import org.acumos.cds.domain.MLPRole;
import org.acumos.cds.domain.MLPRoleFunction;
import org.acumos.cds.domain.MLPSiteConfig;
import org.acumos.cds.domain.MLPSolGrpMemMap;
import org.acumos.cds.domain.MLPSolRevArtMap;
import org.acumos.cds.domain.MLPSolTagMap;
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
import org.acumos.cds.domain.MLPUserNotifPref;
import org.acumos.cds.domain.MLPUserNotification;
import org.acumos.cds.domain.MLPUserRoleMap;
import org.acumos.cds.domain.MLPValidationSequence;
import org.acumos.cds.domain.MLPValidationStatus;
import org.acumos.cds.domain.MLPValidationType;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests getters and setters of client-side domain (model) classes.
 */
public class DomainTest extends AbstractModelTest {

	private static Logger logger = LoggerFactory.getLogger(DomainTest.class);

	@Test
	public void testMLPAccessType() {
		MLPAccessType m = new MLPAccessType();
		m.setCode(s1);
		m.setName(s2);
		Assert.assertEquals(s1, m.getCode());
		Assert.assertEquals(s2, m.getName());
		Assert.assertFalse(m.equals(null));
		Assert.assertFalse(m.equals(new Object()));
		Assert.assertTrue(m.equals(m));
		Assert.assertNotNull(m.hashCode());
		logger.info(m.toString());
	}

	@Test
	public void testMLPArtifact() {
		MLPArtifact m = new MLPArtifact(s1, s1, s1, s1, s1, i1);
		m = new MLPArtifact();
		m.setArtifactId(s1);
		m.setArtifactTypeCode(s2);
		m.setCreated(d1);
		m.setDescription(s3);
		m.setMetadata(s4);
		m.setModified(d2);
		m.setName(s5);
		m.setOwnerId(s6);
		m.setSize(i1);
		m.setUri(s7);
		m.setVersion(s8);
		Assert.assertEquals(s1, m.getArtifactId());
		Assert.assertEquals(s2, m.getArtifactTypeCode());
		Assert.assertEquals(d1, m.getCreated());
		Assert.assertEquals(s3, m.getDescription());
		Assert.assertEquals(s4, m.getMetadata());
		Assert.assertEquals(d2, m.getModified());
		Assert.assertEquals(s5, m.getName());
		Assert.assertEquals(s6, m.getOwnerId());
		Assert.assertEquals(i1, m.getSize());
		Assert.assertEquals(s7, m.getUri());
		Assert.assertEquals(s8, m.getVersion());
		Assert.assertFalse(m.equals(null));
		Assert.assertFalse(m.equals(new Object()));
		Assert.assertFalse(m.equals(new Object()));
		Assert.assertTrue(m.equals(m));
		Assert.assertNotNull(m.hashCode());
		logger.info(m.toString());
		try {
			new MLPArtifact(null, null, null, null, null, 0);
			Assert.assertTrue("Unexpected success", false);
		} catch (IllegalArgumentException iae) {
			// null arg is rejected
		}
	}

	@Test
	public void testMLPArtifactType() {
		MLPArtifactType m = new MLPArtifactType();
		m.setCode(s1);
		m.setName(s2);
		Assert.assertEquals(s1, m.getCode());
		Assert.assertEquals(s2, m.getName());
		Assert.assertFalse(m.equals(null));
		Assert.assertFalse(m.equals(new Object()));
		Assert.assertTrue(m.equals(m));
		Assert.assertNotNull(m.hashCode());
		logger.info(m.toString());
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testMLPCodeNamePair() {
		MLPCodeNamePair m = new MLPCodeNamePair(s1, s1);
		m = new MLPCodeNamePair();
		m.setCode(s1);
		m.setName(s2);
		Assert.assertEquals(s1, m.getCode());
		Assert.assertEquals(s2, m.getName());
		Assert.assertFalse(m.equals(null));
		Assert.assertFalse(m.equals(new Object()));
		Assert.assertTrue(m.equals(m));
		Assert.assertNotNull(m.hashCode());
		Assert.assertNotNull(m.getStatusCode());
		Assert.assertNotNull(m.getTypeCode());
		Assert.assertNotNull(m.getTypeName());
		logger.info(m.toString());
		try {
			new MLPPeerGroup(null);
			Assert.assertTrue("Unexpected success", false);
		} catch (IllegalArgumentException iae) {
			// null arg is rejected
		}
	}

	@Test
	public void testMLPComment() {
		MLPComment m = new MLPComment();
		m = new MLPComment(s1, s2, s3);
		m.setCommentId(s1);
		m.setParentId(s2);
		m.setText(s3);
		m.setThreadId(s4);
		m.setUserId(s6);
		Assert.assertEquals(s1, m.getCommentId());
		Assert.assertEquals(s2, m.getParentId());
		Assert.assertEquals(s3, m.getText());
		Assert.assertEquals(s4, m.getThreadId());
		Assert.assertEquals(s6, m.getUserId());
		Assert.assertFalse(m.equals(null));
		Assert.assertFalse(m.equals(new Object()));
		Assert.assertTrue(m.equals(m));
		Assert.assertNotNull(m.hashCode());
		logger.info(m.toString());
		try {
			new MLPComment(null, null, null);
			Assert.assertTrue("Unexpected success", false);
		} catch (IllegalArgumentException iae) {
			// null arg is rejected
		}
	}

	@Test
	public void testMLPDeploymentStatus() {
		MLPDeploymentStatus m = new MLPDeploymentStatus();
		m.setCode(s1);
		m.setName(s2);
		Assert.assertEquals(s1, m.getCode());
		Assert.assertEquals(s2, m.getName());
		Assert.assertFalse(m.equals(null));
		Assert.assertFalse(m.equals(new Object()));
		Assert.assertTrue(m.equals(m));
		Assert.assertNotNull(m.hashCode());
		logger.info(m.toString());
	}

	@Test
	public void testMLPLoginProvider() {
		MLPLoginProvider m = new MLPLoginProvider();
		m.setCode(s1);
		m.setName(s2);
		Assert.assertEquals(s1, m.getCode());
		Assert.assertEquals(s2, m.getName());
		Assert.assertFalse(m.equals(null));
		Assert.assertFalse(m.equals(new Object()));
		Assert.assertTrue(m.equals(m));
		Assert.assertNotNull(m.hashCode());
		logger.info(m.toString());
	}

	@Test
	public void testMLPModelType() {
		MLPModelType m = new MLPModelType();
		m.setCode(s1);
		m.setName(s2);
		Assert.assertEquals(s1, m.getCode());
		Assert.assertEquals(s2, m.getName());
		Assert.assertFalse(m.equals(null));
		Assert.assertFalse(m.equals(new Object()));
		Assert.assertTrue(m.equals(m));
		Assert.assertNotNull(m.hashCode());
		logger.info(m.toString());
	}

	@Test
	public void testMLPNotification() {
		MLPNotification m = new MLPNotification(s1, s6, d1, d1);
		m = new MLPNotification();
		m.setCreated(d1);
		m.setEnd(d2);
		m.setMessage(s1);
		m.setModified(d3);
		m.setNotificationId(s2);
		m.setStart(d4);
		m.setTitle(s3);
		m.setUrl(s4);
		m.setMsgSeverityCode(s6);
		Assert.assertEquals(d1, m.getCreated());
		Assert.assertEquals(d2, m.getEnd());
		Assert.assertEquals(s1, m.getMessage());
		Assert.assertEquals(d3, m.getModified());
		Assert.assertEquals(s2, m.getNotificationId());
		Assert.assertEquals(d4, m.getStart());
		Assert.assertEquals(s3, m.getTitle());
		Assert.assertEquals(s4, m.getUrl());
		Assert.assertEquals(s6, m.getMsgSeverityCode());
		Assert.assertFalse(m.equals(null));
		Assert.assertFalse(m.equals(new Object()));
		Assert.assertTrue(m.equals(m));
		Assert.assertNotNull(m.hashCode());
		logger.info(m.toString());
		try {
			new MLPNotification(null, null, null, null);
			Assert.assertTrue("Unexpected success", false);
		} catch (IllegalArgumentException iae) {
			// null arg is rejected
		}
	}

	@Test
	public void testMLPNotifUserMap() {
		MLPNotifUserMap m = new MLPNotifUserMap();
		m = new MLPNotifUserMap(s1, s2);
		m.setNotificationId(s1);
		m.setUserId(s2);
		m.setViewed(d1);
		Assert.assertEquals(s1, m.getNotificationId());
		Assert.assertEquals(s2, m.getUserId());
		Assert.assertEquals(d1, m.getViewed());
		Assert.assertFalse(m.equals(null));
		Assert.assertFalse(m.equals(new Object()));
		Assert.assertTrue(m.equals(m));
		Assert.assertNotNull(m.hashCode());
		logger.info(m.toString());
		MLPNotifUserMap.NotifUserMapPK pk = new MLPNotifUserMap.NotifUserMapPK();
		pk = new MLPNotifUserMap.NotifUserMapPK(s1, s2);
		Assert.assertFalse(pk.equals(null));
		Assert.assertFalse(pk.equals(new Object()));
		Assert.assertTrue(pk.equals(pk));
		Assert.assertFalse(pk.hashCode() == 0);
		logger.info(pk.toString());
		try {
			new MLPNotifUserMap(null, null);
			Assert.assertTrue("Unexpected success", false);
		} catch (IllegalArgumentException iae) {
			// null arg is rejected
		}
	}

	@Test
	public void testMLPUserNotifPreference() {
		MLPUserNotifPref m = new MLPUserNotifPref(s1, s1, s1);
		m = new MLPUserNotifPref();
		m.setUserNotifPrefId(l1);
		m.setMsgSeverityCode(s1);
		m.setNotfDelvMechCode(s2);
		m.setUserId(s3);
		Assert.assertEquals(l1, m.getUserNotifPrefId());
		Assert.assertEquals(s1, m.getMsgSeverityCode());
		Assert.assertEquals(s2, m.getNotfDelvMechCode());
		Assert.assertEquals(s3, m.getUserId());
		Assert.assertFalse(m.equals(null));
		Assert.assertFalse(m.equals(new Object()));
		Assert.assertTrue(m.equals(m));
		Assert.assertNotNull(m.hashCode());
		logger.info(m.toString());
		try {
			new MLPUserNotifPref(null, null, null);
			Assert.assertTrue("Unexpected success", false);
		} catch (IllegalArgumentException iae) {
			// null arg is rejected
		}
	}

	@Test
	public void testMLPPasswordChangeRequest() {
		MLPPasswordChangeRequest m = new MLPPasswordChangeRequest(s1, s1);
		m = new MLPPasswordChangeRequest();
		m.setNewLoginPass(s1);
		m.setOldLoginPass(s2);
		Assert.assertEquals(s1, m.getNewLoginPass());
		Assert.assertEquals(s2, m.getOldLoginPass());
		Assert.assertFalse(m.equals(null));
		Assert.assertFalse(m.equals(new Object()));
		Assert.assertTrue(m.equals(m));
		Assert.assertNotNull(m.hashCode());
		logger.info(m.toString());
		try {
			new MLPPasswordChangeRequest(null, null);
			Assert.assertTrue("Unexpected success", false);
		} catch (IllegalArgumentException iae) {
			// null arg is rejected
		}
	}

	@Test
	public void testMLPPeer() {
		MLPPeer m = new MLPPeer(s1, s1, s1, b1, b1, s1, s1, s1);
		m = new MLPPeer();
		m.setApiUrl(s1);
		m.setContact1(s2);
		m.setCreated(d1);
		m.setDescription(s4);
		m.setLocal(b1);
		m.setModified(d2);
		m.setName(s5);
		m.setPeerId(s6);
		m.setSelf(b2);
		m.setStatusCode(s7);
		m.setSubjectName(s8);
		m.setValidationStatusCode(s9);
		m.setWebUrl(s10);
		Assert.assertEquals(s1, m.getApiUrl());
		Assert.assertEquals(s2, m.getContact1());
		Assert.assertEquals(d1, m.getCreated());
		Assert.assertEquals(s4, m.getDescription());
		Assert.assertEquals(b1, m.isLocal());
		Assert.assertEquals(d2, m.getModified());
		Assert.assertEquals(s5, m.getName());
		Assert.assertEquals(s6, m.getPeerId());
		Assert.assertEquals(b2, m.isSelf());
		Assert.assertEquals(s7, m.getStatusCode());
		Assert.assertEquals(s8, m.getSubjectName());
		Assert.assertEquals(s9, m.getValidationStatusCode());
		Assert.assertEquals(s10, m.getWebUrl());
		Assert.assertFalse(m.equals(null));
		Assert.assertFalse(m.equals(new Object()));
		Assert.assertTrue(m.equals(m));
		Assert.assertNotNull(m.hashCode());
		logger.info(m.toString());
		try {
			new MLPPeer(null, null, null, b1, b1, null, null, null);
			Assert.assertTrue("Unexpected success", false);
		} catch (IllegalArgumentException iae) {
			// null arg is rejected
		}
	}

	@Test
	public void testMLPPeerGroup() {
		MLPPeerGroup m = new MLPPeerGroup(s1);
		m = new MLPPeerGroup();
		m.setCreated(d1);
		m.setDescription(s1);
		m.setGroupId(l1);
		m.setModified(d2);
		m.setName(s2);
		Assert.assertEquals(d1, m.getCreated());
		Assert.assertEquals(s1, m.getDescription());
		Assert.assertEquals(l1, m.getGroupId());
		Assert.assertEquals(d2, m.getModified());
		Assert.assertEquals(s2, m.getName());
		Assert.assertNotNull(m.hashCode());
		logger.info(m.toString());
		try {
			new MLPPeerGroup(null);
			Assert.assertTrue("Unexpected success", false);
		} catch (IllegalArgumentException iae) {
			// null arg is rejected
		}
	}

	@Test
	public void testMLPPeerStatus() {
		MLPPeerStatus m = new MLPPeerStatus();
		m.setCode(s1);
		m.setName(s2);
		Assert.assertEquals(s1, m.getCode());
		Assert.assertEquals(s2, m.getName());
		Assert.assertFalse(m.equals(null));
		Assert.assertFalse(m.equals(new Object()));
		Assert.assertTrue(m.equals(m));
		Assert.assertNotNull(m.hashCode());
		logger.info(m.toString());
	}

	@Test
	public void testMLPPeerSubscription() {
		MLPPeerSubscription m = new MLPPeerSubscription(s1, s2, s3, s4);
		m = new MLPPeerSubscription();
		m.setAccessType(s1);
		m.setCreated(d1);
		m.setMaxArtifactSize(l1);
		m.setModified(d2);
		m.setOptions(s2);
		m.setOwnerId(s3);
		m.setPeerId(s4);
		m.setProcessed(d3);
		m.setRefreshInterval(l2);
		m.setScopeType(s5);
		m.setSelector(s6);
		m.setSubId(l3);
		Assert.assertEquals(s1, m.getAccessType());
		Assert.assertEquals(d1, m.getCreated());
		Assert.assertEquals(l1, m.getMaxArtifactSize());
		Assert.assertEquals(d2, m.getModified());
		Assert.assertEquals(s2, m.getOptions());
		Assert.assertEquals(s3, m.getOwnerId());
		Assert.assertEquals(s4, m.getPeerId());
		Assert.assertEquals(d3, m.getProcessed());
		Assert.assertEquals(l2, m.getRefreshInterval());
		Assert.assertEquals(s5, m.getScopeType());
		Assert.assertEquals(s6, m.getSelector());
		Assert.assertEquals(l3, m.getSubId());
		Assert.assertFalse(m.equals(null));
		Assert.assertFalse(m.equals(new Object()));
		Assert.assertTrue(m.equals(m));
		Assert.assertNotNull(m.hashCode());
		logger.info(m.toString());
		try {
			new MLPPeerSubscription(null, null, null, null);
			Assert.assertTrue("Unexpected success", false);
		} catch (IllegalArgumentException iae) {
			// null arg is rejected
		}
	}

	@Test
	public void testMLPPeerGrpMemMap() {
		MLPPeerGrpMemMap m = new MLPPeerGrpMemMap(l1, s1);
		m = new MLPPeerGrpMemMap();
		m.setGroupId(l1);
		m.setPeerId(s1);
		Assert.assertEquals(l1, m.getGroupId());
		Assert.assertEquals(s1, m.getPeerId());
		Assert.assertFalse(m.equals(null));
		Assert.assertFalse(m.equals(new Object()));
		Assert.assertTrue(m.equals(m));
		Assert.assertNotNull(m.hashCode());
		try {
			new MLPPeerGrpMemMap(null, null);
			Assert.assertTrue("Unexpected success", false);
		} catch (IllegalArgumentException iae) {
			// null arg is rejected
		}
		logger.info(m.toString());
		MLPPeerGrpMemMap.PeerGrpMemMapPK pk = new MLPPeerGrpMemMap.PeerGrpMemMapPK();
		pk = new MLPPeerGrpMemMap.PeerGrpMemMapPK(l1, s1);
		Assert.assertFalse(pk.equals(null));
		Assert.assertFalse(pk.equals(new Object()));
		Assert.assertTrue(pk.equals(pk));
		Assert.assertFalse(pk.hashCode() == 0);
		logger.info(pk.toString());
	}

	@Test
	public void testMLPPeerPeerAccMap() {
		MLPPeerPeerAccMap m = new MLPPeerPeerAccMap(l1, l2);
		m = new MLPPeerPeerAccMap();
		m.setPrincipalPeerGroupId(l1);
		m.setResourcePeerGroupId(l2);
		Assert.assertEquals(l1, m.getPrincipalPeerGroupId());
		Assert.assertEquals(l2, m.getResourcePeerGroupId());
		Assert.assertFalse(m.equals(null));
		Assert.assertFalse(m.equals(new Object()));
		Assert.assertTrue(m.equals(m));
		Assert.assertNotNull(m.hashCode());
		try {
			new MLPPeerPeerAccMap(null, null);
			Assert.assertTrue("Unexpected success", false);
		} catch (IllegalArgumentException iae) {
			// null arg is rejected
		}
		logger.info(m.toString());
		MLPPeerPeerAccMap.PeerPeerAccMapPK pk = new MLPPeerPeerAccMap.PeerPeerAccMapPK();
		pk = new MLPPeerPeerAccMap.PeerPeerAccMapPK(l1, l2);
		Assert.assertFalse(pk.equals(null));
		Assert.assertFalse(pk.equals(new Object()));
		Assert.assertTrue(pk.equals(pk));
		Assert.assertFalse(pk.hashCode() == 0);
		logger.info(pk.toString());
	}

	@Test
	public void testMLPPeerSolAccMap() {
		MLPPeerSolAccMap m = new MLPPeerSolAccMap(l1, l2, true);
		m = new MLPPeerSolAccMap();
		m.setPeerGroupId(l1);
		m.setSolutionGroupId(l2);
		Assert.assertEquals(l1, m.getPeerGroupId());
		Assert.assertEquals(l2, m.getSolutionGroupId());
		Assert.assertFalse(m.equals(null));
		Assert.assertFalse(m.equals(new Object()));
		Assert.assertTrue(m.equals(m));
		Assert.assertNotNull(m.hashCode());
		try {
			new MLPPeerSolAccMap(null, null, false);
			Assert.assertTrue("Unexpected success", false);
		} catch (IllegalArgumentException iae) {
			// null arg is rejected
		}
		logger.info(m.toString());
		MLPPeerSolAccMap.PeerSolAccMapPK pk = new MLPPeerSolAccMap.PeerSolAccMapPK();
		pk = new MLPPeerSolAccMap.PeerSolAccMapPK(l1, l2);
		Assert.assertFalse(pk.equals(null));
		Assert.assertFalse(pk.equals(new Object()));
		Assert.assertTrue(pk.equals(pk));
		Assert.assertFalse(pk.hashCode() == 0);
		logger.info(pk.toString());
	}

	@Test
	public void testMLPRole() {
		MLPRole m = new MLPRole(s1, b1);
		m = new MLPRole();
		m.setCreated(d1);
		m.setModified(d2);
		m.setName(s1);
		m.setRoleId(s2);
		Assert.assertEquals(d1, m.getCreated());
		Assert.assertEquals(d2, m.getModified());
		Assert.assertEquals(s1, m.getName());
		Assert.assertEquals(s2, m.getRoleId());
		Assert.assertFalse(m.equals(null));
		Assert.assertFalse(m.equals(new Object()));
		Assert.assertTrue(m.equals(m));
		Assert.assertNotNull(m.hashCode());
		logger.info(m.toString());
		try {
			new MLPRole(null, true);
			Assert.assertTrue("Unexpected success", false);
		} catch (IllegalArgumentException iae) {
			// null arg is rejected
		}
	}

	@Test
	public void testMLPRoleFunction() {
		MLPRoleFunction m = new MLPRoleFunction(s1, s1);
		m = new MLPRoleFunction();
		m.setCreated(d1);
		m.setModified(d2);
		m.setName(s1);
		m.setRoleFunctionId(s2);
		m.setRoleId(s3);
		Assert.assertEquals(d1, m.getCreated());
		Assert.assertEquals(d2, m.getModified());
		Assert.assertEquals(s1, m.getName());
		Assert.assertEquals(s2, m.getRoleFunctionId());
		Assert.assertEquals(s3, m.getRoleId());
		Assert.assertFalse(m.equals(null));
		Assert.assertFalse(m.equals(new Object()));
		Assert.assertTrue(m.equals(m));
		Assert.assertNotNull(m.hashCode());
		logger.info(m.toString());
		try {
			new MLPRoleFunction(null, null);
			Assert.assertTrue("Unexpected success", false);
		} catch (IllegalArgumentException iae) {
			// null arg is rejected
		}
	}

	@Test
	public void testMLPSiteConfig() {
		MLPSiteConfig m = new MLPSiteConfig(s1, s1);
		m = new MLPSiteConfig();
		m.setConfigKey(s1);
		m.setConfigValue(s2);
		m.setCreated(d1);
		m.setModified(d2);
		m.setUserId(s3);
		Assert.assertEquals(s1, m.getConfigKey());
		Assert.assertEquals(s2, m.getConfigValue());
		Assert.assertEquals(d1, m.getCreated());
		Assert.assertEquals(d2, m.getModified());
		Assert.assertEquals(s3, m.getUserId());
		Assert.assertFalse(m.equals(null));
		Assert.assertFalse(m.equals(new Object()));
		Assert.assertTrue(m.equals(m));
		Assert.assertNotNull(m.hashCode());
		logger.info(m.toString());
		try {
			new MLPSiteConfig(null, null);
			Assert.assertTrue("Unexpected success", false);
		} catch (IllegalArgumentException iae) {
			// null arg is rejected
		}
	}

	@Test
	public void testMLPSolRevArtMap() {
		MLPSolRevArtMap m = new MLPSolRevArtMap(s1, s1);
		m = new MLPSolRevArtMap();
		m.setArtifactId(s1);
		m.setRevisionId(s2);
		Assert.assertEquals(s1, m.getArtifactId());
		Assert.assertEquals(s2, m.getRevisionId());
		Assert.assertFalse(m.equals(null));
		Assert.assertFalse(m.equals(new Object()));
		Assert.assertTrue(m.equals(m));
		Assert.assertNotNull(m.hashCode());
		logger.info(m.toString());
		try {
			new MLPSolRevArtMap(null, null);
			Assert.assertTrue("Unexpected success", false);
		} catch (IllegalArgumentException iae) {
			// null arg is rejected
		}
		MLPSolRevArtMap.SolRevArtMapPK pk = new MLPSolRevArtMap.SolRevArtMapPK();
		pk = new MLPSolRevArtMap.SolRevArtMapPK(s1, s2);
		Assert.assertFalse(pk.equals(null));
		Assert.assertFalse(pk.equals(new Object()));
		Assert.assertTrue(pk.equals(pk));
		Assert.assertFalse(pk.hashCode() == 0);
		logger.info(pk.toString());
	}

	@Test
	public void testMLPSolTagMap() {
		MLPSolTagMap m = new MLPSolTagMap(s1, s1);
		m = new MLPSolTagMap();
		m.setSolutionId(s1);
		m.setTag(s2);
		Assert.assertEquals(s1, m.getSolutionId());
		Assert.assertEquals(s2, m.getTag());
		Assert.assertFalse(m.equals(null));
		Assert.assertFalse(m.equals(new Object()));
		Assert.assertTrue(m.equals(m));
		Assert.assertNotNull(m.hashCode());
		try {
			new MLPSolTagMap(null, null);
			Assert.assertTrue("Unexpected success", false);
		} catch (IllegalArgumentException iae) {
			// null arg is rejected
		}
		logger.info(m.toString());
		MLPSolTagMap.SolTagMapPK pk = new MLPSolTagMap.SolTagMapPK();
		pk = new MLPSolTagMap.SolTagMapPK(s1, s2);
		Assert.assertFalse(pk.equals(null));
		Assert.assertFalse(pk.equals(new Object()));
		Assert.assertTrue(pk.equals(pk));
		Assert.assertFalse(pk.hashCode() == 0);
		logger.info(pk.toString());
	}

	@Test
	public void testMLPSolGrpMemMap() {
		MLPSolGrpMemMap m = new MLPSolGrpMemMap(l1, s1);
		m = new MLPSolGrpMemMap();
		m.setGroupId(l1);
		m.setSolutionId(s1);
		Assert.assertEquals(l1, m.getGroupId());
		Assert.assertEquals(s1, m.getSolutionId());
		Assert.assertFalse(m.equals(null));
		Assert.assertFalse(m.equals(new Object()));
		Assert.assertTrue(m.equals(m));
		Assert.assertNotNull(m.hashCode());
		try {
			new MLPSolGrpMemMap(null, null);
			Assert.assertTrue("Unexpected success", false);
		} catch (IllegalArgumentException iae) {
			// null arg is rejected
		}
		logger.info(m.toString());
		MLPSolGrpMemMap.SolGrpMemMapPK pk = new MLPSolGrpMemMap.SolGrpMemMapPK();
		pk = new MLPSolGrpMemMap.SolGrpMemMapPK(l1, s1);
		Assert.assertFalse(pk.equals(null));
		Assert.assertFalse(pk.equals(new Object()));
		Assert.assertTrue(pk.equals(pk));
		Assert.assertFalse(pk.hashCode() == 0);
		logger.info(pk.toString());
	}

	@Test
	public void testMLPSolUserAccMap() {
		MLPSolUserAccMap m = new MLPSolUserAccMap(s1, s1);
		m = new MLPSolUserAccMap();
		m.setSolutionId(s1);
		m.setUserId(s2);
		Assert.assertEquals(s1, m.getSolutionId());
		Assert.assertEquals(s2, m.getUserId());
		Assert.assertFalse(m.equals(null));
		Assert.assertFalse(m.equals(new Object()));
		Assert.assertTrue(m.equals(m));
		Assert.assertNotNull(m.hashCode());
		logger.info(m.toString());
		try {
			new MLPSolUserAccMap(null, null);
			Assert.assertTrue("Unexpected success", false);
		} catch (IllegalArgumentException iae) {
			// null arg is rejected
		}
		MLPSolUserAccMap.SolUserAccessMapPK pk = new MLPSolUserAccMap.SolUserAccessMapPK();
		pk = new MLPSolUserAccMap.SolUserAccessMapPK(s1, s2);
		Assert.assertFalse(pk.equals(null));
		Assert.assertFalse(pk.equals(new Object()));
		Assert.assertTrue(pk.equals(pk));
		Assert.assertFalse(pk.hashCode() == 0);
		logger.info(pk.toString());
	}

	@Test
	public void testMLPSolution() {
		MLPSolution m = new MLPSolution(s1, s1, b1);
		m = new MLPSolution();
		m.setAccessTypeCode(s1);
		m.setActive(b1);
		m.setCreated(d1);
		m.setDescription(s2);
		m.setMetadata(s3);
		m.setModelTypeCode(s4);
		m.setModified(d2);
		m.setName(s5);
		m.setOrigin(s6);
		m.setOwnerId(s7);
		m.setSourceId(s8);
		m.setProvider(s9);
		m.setSolutionId(s10);
		m.setToolkitTypeCode(s11);
		m.setValidationStatusCode(s12);
		Assert.assertEquals(s1, m.getAccessTypeCode());
		Assert.assertEquals(b1, m.isActive());
		Assert.assertEquals(d1, m.getCreated());
		Assert.assertEquals(s2, m.getDescription());
		Assert.assertEquals(s3, m.getMetadata());
		Assert.assertEquals(s4, m.getModelTypeCode());
		Assert.assertEquals(d2, m.getModified());
		Assert.assertEquals(s5, m.getName());
		Assert.assertEquals(s6, m.getOrigin());
		Assert.assertEquals(s7, m.getOwnerId());
		Assert.assertEquals(s8, m.getSourceId());
		Assert.assertEquals(s9, m.getProvider());
		Assert.assertEquals(s10, m.getSolutionId());
		Assert.assertEquals(s11, m.getToolkitTypeCode());
		Assert.assertEquals(s12, m.getValidationStatusCode());
		Assert.assertFalse(m.equals(null));
		Assert.assertFalse(m.equals(new Object()));
		Assert.assertTrue(m.equals(m));
		Assert.assertNotNull(m.hashCode());
		logger.info(m.toString());
		try {
			new MLPSolution(null, null, true);
			Assert.assertTrue("Unexpected success", false);
		} catch (IllegalArgumentException iae) {
			// null arg is rejected
		}
	}

	@Test
	public void testMLPSolutionDeployment() {
		MLPSolutionDeployment m = new MLPSolutionDeployment(s1, s1, s1, s1);
		m = new MLPSolutionDeployment();
		m.setCreated(d1);
		m.setDeploymentId(s1);
		m.setDeploymentStatusCode(s2);
		m.setDetail(s3);
		m.setModified(d2);
		m.setRevisionId(s4);
		m.setSolutionId(s5);
		m.setTarget(s6);
		m.setUserId(s7);
		Assert.assertEquals(d1, m.getCreated());
		Assert.assertEquals(s1, m.getDeploymentId());
		Assert.assertEquals(s2, m.getDeploymentStatusCode());
		Assert.assertEquals(s3, m.getDetail());
		Assert.assertEquals(s4, m.getRevisionId());
		Assert.assertEquals(s5, m.getSolutionId());
		Assert.assertEquals(s6, m.getTarget());
		Assert.assertEquals(s7, m.getUserId());
		Assert.assertFalse(m.equals(null));
		Assert.assertFalse(m.equals(new Object()));
		Assert.assertTrue(m.equals(m));
		Assert.assertNotNull(m.hashCode());
		logger.info(m.toString());
		try {
			new MLPSolutionDeployment(null, null, null, null);
			Assert.assertTrue("Unexpected success", false);
		} catch (IllegalArgumentException iae) {
			// null arg is rejected
		}
	}

	@Test
	public void testMLPSolutionDownload() {
		MLPSolutionDownload m = new MLPSolutionDownload(s1, s2, s3);
		m = new MLPSolutionDownload();
		m.setArtifactId(s1);
		m.setDownloadId(l1);
		m.setSolutionId(s2);
		m.setUserId(s3);
		m.setDownloadDate(d1);
		Assert.assertEquals(s1, m.getArtifactId());
		Assert.assertEquals(l1, m.getDownloadId());
		Assert.assertEquals(s2, m.getSolutionId());
		Assert.assertEquals(s3, m.getUserId());
		Assert.assertEquals(d1, m.getDownloadDate());
		Assert.assertFalse(m.equals(null));
		Assert.assertFalse(m.equals(new Object()));
		Assert.assertTrue(m.equals(m));
		Assert.assertNotNull(m.hashCode());
		logger.info(m.toString());
		try {
			new MLPSolutionDownload(null, null, null);
			Assert.assertTrue("Unexpected success", false);
		} catch (IllegalArgumentException iae) {
			// null arg is rejected
		}
	}

	@Test
	public void testMLPSolutionFavorite() {
		MLPSolutionFavorite m = new MLPSolutionFavorite(s1, s1);
		m = new MLPSolutionFavorite();
		m.setSolutionId(s1);
		m.setUserId(s2);
		Assert.assertEquals(s1, m.getSolutionId());
		Assert.assertEquals(s2, m.getUserId());
		Assert.assertFalse(m.equals(null));
		Assert.assertFalse(m.equals(new Object()));
		Assert.assertTrue(m.equals(m));
		Assert.assertNotNull(m.hashCode());
		logger.info(m.toString());
		try {
			new MLPSolutionFavorite(null, null);
			Assert.assertTrue("Unexpected success", false);
		} catch (IllegalArgumentException iae) {
			// null arg is rejected
		}
		MLPSolutionFavorite.SolutionFavoritePK pk = new MLPSolutionFavorite.SolutionFavoritePK();
		pk = new MLPSolutionFavorite.SolutionFavoritePK(s1, s2);
		Assert.assertFalse(pk.equals(null));
		Assert.assertFalse(pk.equals(new Object()));
		Assert.assertTrue(pk.equals(pk));
		Assert.assertFalse(pk.hashCode() == 0);
		logger.info(pk.toString());
	}

	@Test
	public void testMLPSolutionGroup() {
		MLPSolutionGroup m = new MLPSolutionGroup(s1);
		m = new MLPSolutionGroup();
		m.setCreated(d1);
		m.setDescription(s1);
		m.setGroupId(l1);
		m.setModified(d2);
		m.setName(s2);
		Assert.assertEquals(d1, m.getCreated());
		Assert.assertEquals(s1, m.getDescription());
		Assert.assertEquals(l1, m.getGroupId());
		Assert.assertEquals(d2, m.getModified());
		Assert.assertEquals(s2, m.getName());
		Assert.assertFalse(m.equals(null));
		Assert.assertFalse(m.equals(new Object()));
		Assert.assertTrue(m.equals(m));
		Assert.assertNotNull(m.hashCode());
		logger.info(m.toString());
		try {
			new MLPSolutionGroup(null);
			Assert.assertTrue("Unexpected success", false);
		} catch (IllegalArgumentException iae) {
			// null arg is rejected
		}
	}

	@Test
	public void testMLPSolutionRating() {
		MLPSolutionRating m = new MLPSolutionRating(s1, s1, i1);
		m = new MLPSolutionRating();
		m.setCreated(d1);
		m.setRating(i1);
		m.setSolutionId(s1);
		m.setTextReview(s2);
		m.setUserId(s3);
		Assert.assertEquals(d1, m.getCreated());
		Assert.assertEquals(i1, m.getRating());
		Assert.assertEquals(s1, m.getSolutionId());
		Assert.assertEquals(s2, m.getTextReview());
		Assert.assertEquals(s3, m.getUserId());
		Assert.assertFalse(m.equals(null));
		Assert.assertFalse(m.equals(new Object()));
		Assert.assertTrue(m.equals(m));
		Assert.assertNotNull(m.hashCode());
		logger.info(m.toString());
		try {
			new MLPSolutionRating(null, null, 0);
			Assert.assertTrue("Unexpected success", false);
		} catch (IllegalArgumentException iae) {
			// null arg is rejected
		}
		MLPSolutionRating.SolutionRatingPK pk = new MLPSolutionRating.SolutionRatingPK();
		pk = new MLPSolutionRating.SolutionRatingPK(s1, s2);
		Assert.assertFalse(pk.equals(null));
		Assert.assertFalse(pk.equals(new Object()));
		Assert.assertTrue(pk.equals(pk));
		Assert.assertFalse(pk.hashCode() == 0);
		logger.info(pk.toString());
	}

	@Test
	public void testMLPSolutionRevision() {
		MLPSolutionRevision m = new MLPSolutionRevision(s1, s1, s1);
		m = new MLPSolutionRevision();
		m.setCreated(d1);
		m.setDescription(s1);
		m.setMetadata(s2);
		m.setModified(d2);
		m.setOrigin(s3);
		m.setOwnerId(s4);
		m.setRevisionId(s5);
		m.setSolutionId(s6);
		m.setSourceId(s7);
		m.setVersion(s8);
		Assert.assertEquals(d1, m.getCreated());
		Assert.assertEquals(s1, m.getDescription());
		Assert.assertEquals(s2, m.getMetadata());
		Assert.assertEquals(d2, m.getModified());
		Assert.assertEquals(s3, m.getOrigin());
		Assert.assertEquals(s4, m.getOwnerId());
		Assert.assertEquals(s5, m.getRevisionId());
		Assert.assertEquals(s6, m.getSolutionId());
		Assert.assertEquals(s7, m.getSourceId());
		Assert.assertEquals(s8, m.getVersion());
		Assert.assertFalse(m.equals(null));
		Assert.assertFalse(m.equals(new Object()));
		Assert.assertTrue(m.equals(m));
		Assert.assertNotNull(m.hashCode());
		logger.info(m.toString());
		try {
			new MLPSolutionRevision(null, null, null);
			Assert.assertTrue("Unexpected success", false);
		} catch (IllegalArgumentException iae) {
			// null arg is rejected
		}
	}

	@Test
	public void testMLPSolutionValidation() {
		MLPSolutionValidation m = new MLPSolutionValidation(s1, s1, s1, s1);
		m = new MLPSolutionValidation();
		m.setCreated(d1);
		m.setDetail(s1);
		m.setModified(d2);
		m.setRevisionId(s2);
		m.setSolutionId(s3);
		m.setTaskId(s4);
		m.setValidationStatusCode(s5);
		m.setValidationTypeCode(s6);
		Assert.assertEquals(d1, m.getCreated());
		Assert.assertEquals(s1, m.getDetail());
		Assert.assertEquals(d2, m.getModified());
		Assert.assertEquals(s2, m.getRevisionId());
		Assert.assertEquals(s3, m.getSolutionId());
		Assert.assertEquals(s4, m.getTaskId());
		Assert.assertEquals(s5, m.getValidationStatusCode());
		Assert.assertEquals(s6, m.getValidationTypeCode());
		Assert.assertFalse(m.equals(null));
		Assert.assertFalse(m.equals(new Object()));
		Assert.assertTrue(m.equals(m));
		Assert.assertNotNull(m.hashCode());
		logger.info(m.toString());
		try {
			new MLPSolutionValidation(null, null, null, null);
			Assert.assertTrue("Unexpected success", false);
		} catch (IllegalArgumentException iae) {
			// null arg is rejected
		}
		MLPSolutionValidation.SolutionValidationPK pk = new MLPSolutionValidation.SolutionValidationPK();
		pk = new MLPSolutionValidation.SolutionValidationPK(s1, s2, s3);
		Assert.assertFalse(pk.equals(null));
		Assert.assertFalse(pk.equals(new Object()));
		Assert.assertTrue(pk.equals(pk));
		Assert.assertFalse(pk.hashCode() == 0);
		logger.info(pk.toString());
	}

	@Test
	public void testMLPSolutionWeb() {
		MLPSolutionWeb m = new MLPSolutionWeb(s1);
		m = new MLPSolutionWeb();
		m.setDownloadCount(l1);
		m.setFeatured(b1);
		m.setLastDownload(d1);
		m.setRatingAverageTenths(l2);
		m.setRatingCount(l3);
		m.setSolutionId(s1);
		m.setViewCount(l4);
		Assert.assertEquals(l1, m.getDownloadCount());
		Assert.assertEquals(b1, m.isFeatured());
		Assert.assertEquals(d1, m.getLastDownload());
		Assert.assertEquals(l2, m.getRatingAverageTenths());
		Assert.assertEquals(l3, m.getRatingCount());
		Assert.assertEquals(s1, m.getSolutionId());
		Assert.assertEquals(l4, m.getViewCount());
		Assert.assertFalse(m.equals(null));
		Assert.assertFalse(m.equals(new Object()));
		Assert.assertTrue(m.equals(m));
		Assert.assertNotNull(m.hashCode());
		logger.info(m.toString());
		try {
			new MLPSolutionWeb(null);
			Assert.assertTrue("Unexpected success", false);
		} catch (IllegalArgumentException iae) {
			// null arg is rejected
		}
	}

	@Test
	public void testMLPStepResult() {
		MLPStepResult m = new MLPStepResult(s1, s2, s3, d1);
		m = new MLPStepResult();
		m.setArtifactId(s1);
		m.setEndDate(d1);
		m.setName(s2);
		m.setResult(s3);
		m.setRevisionId(s4);
		m.setSolutionId(s5);
		m.setStartDate(d2);
		m.setStatusCode(s6);
		m.setStepCode(s7);
		Assert.assertEquals(s1, m.getArtifactId());
		Assert.assertEquals(d1, m.getEndDate());
		Assert.assertEquals(s2, m.getName());
		Assert.assertEquals(s3, m.getResult());
		Assert.assertEquals(s4, m.getRevisionId());
		Assert.assertEquals(s5, m.getSolutionId());
		Assert.assertEquals(d2, m.getStartDate());
		Assert.assertEquals(s6, m.getStatusCode());
		Assert.assertEquals(s7, m.getStepCode());
		Assert.assertFalse(m.equals(null));
		Assert.assertFalse(m.equals(new Object()));
		Assert.assertTrue(m.equals(m));
		Assert.assertNotNull(m.hashCode());
		logger.info(m.toString());
		try {
			new MLPStepResult(null, null, null, null);
			Assert.assertTrue("Unexpected success", false);
		} catch (IllegalArgumentException iae) {
			// null arg is rejected
		}
	}

	@Test
	public void testMLPStepStatus() {
		MLPStepStatus m = new MLPStepStatus();
		m.setCode(s1);
		m.setName(s2);
		Assert.assertEquals(s1, m.getCode());
		Assert.assertEquals(s2, m.getName());
		Assert.assertFalse(m.equals(null));
		Assert.assertFalse(m.equals(new Object()));
		Assert.assertTrue(m.equals(m));
		Assert.assertNotNull(m.hashCode());
		logger.info(m.toString());
	}

	@Test
	public void testMLPStepType() {
		MLPStepType m = new MLPStepType();
		m.setCode(s1);
		m.setName(s2);
		Assert.assertEquals(s1, m.getCode());
		Assert.assertEquals(s2, m.getName());
		Assert.assertFalse(m.equals(null));
		Assert.assertFalse(m.equals(new Object()));
		Assert.assertTrue(m.equals(m));
		Assert.assertNotNull(m.hashCode());
		logger.info(m.toString());
	}

	@Test
	public void testMLPTag() {
		MLPTag m = new MLPTag(s1);
		m = new MLPTag();
		m.setTag(s1);
		Assert.assertEquals(s1, m.getTag());
		Assert.assertFalse(m.equals(null));
		Assert.assertFalse(m.equals(new Object()));
		Assert.assertTrue(m.equals(m));
		Assert.assertNotNull(m.hashCode());
		logger.info(m.toString());
		try {
			new MLPTag(null);
			Assert.assertTrue("Unexpected success", false);
		} catch (IllegalArgumentException iae) {
			// null arg is rejected
		}
	}

	@Test
	public void testMLPThread() {
		MLPThread m = new MLPThread(s1, s1);
		m = new MLPThread();
		m.setRevisionId(s1);
		m.setSolutionId(s2);
		m.setThreadId(s3);
		m.setTitle(s4);
		Assert.assertEquals(s1, m.getRevisionId());
		Assert.assertEquals(s2, m.getSolutionId());
		Assert.assertEquals(s3, m.getThreadId());
		Assert.assertEquals(s4, m.getTitle());
		Assert.assertFalse(m.equals(null));
		Assert.assertFalse(m.equals(new Object()));
		Assert.assertTrue(m.equals(m));
		Assert.assertNotNull(m.hashCode());
		logger.info(m.toString());
		try {
			new MLPThread(null, null);
			Assert.assertTrue("Unexpected success", false);
		} catch (IllegalArgumentException iae) {
			// null arg is rejected
		}
	}

	@Test
	public void testMLPToolkitType() {
		MLPToolkitType m = new MLPToolkitType();
		m.setCode(s1);
		m.setName(s2);
		Assert.assertEquals(s1, m.getCode());
		Assert.assertEquals(s2, m.getName());
		Assert.assertFalse(m.equals(null));
		Assert.assertFalse(m.equals(new Object()));
		Assert.assertTrue(m.equals(m));
		Assert.assertNotNull(m.hashCode());
		logger.info(m.toString());
	}

	@Test
	public void testMLPUser() {
		MLPUser m = new MLPUser(s1, b1);
		m = new MLPUser();
		m.setActive(b1);
		m.setAuthToken(s1);
		m.setCreated(d1);
		m.setEmail(s2);
		m.setFirstName(s3);
		m.setLastLogin(d2);
		m.setLastName(s4);
		m.setLoginHash(s5);
		m.setLoginName(s6);
		m.setLoginPassExpire(d3);
		m.setMiddleName(s7);
		m.setModified(d4);
		m.setOrgName(s8);
		m.setPicture(by1);
		m.setUserId(s9);
		Assert.assertEquals(b1, m.isActive());
		Assert.assertEquals(s1, m.getAuthToken());
		Assert.assertEquals(d1, m.getCreated());
		Assert.assertEquals(s2, m.getEmail());
		Assert.assertEquals(s3, m.getFirstName());
		Assert.assertEquals(d2, m.getLastLogin());
		Assert.assertEquals(s4, m.getLastName());
		Assert.assertEquals(s5, m.getLoginHash());
		Assert.assertEquals(s6, m.getLoginName());
		Assert.assertEquals(d3, m.getLoginPassExpire());
		Assert.assertEquals(s7, m.getMiddleName());
		Assert.assertEquals(s8, m.getOrgName());
		Assert.assertArrayEquals(by1, m.getPicture());
		Assert.assertEquals(s9, m.getUserId());
		Assert.assertFalse(m.equals(null));
		Assert.assertFalse(m.equals(new Object()));
		Assert.assertTrue(m.equals(m));
		Assert.assertNotNull(m.hashCode());
		logger.info(m.toString());
		try {
			new MLPUser(null, true);
			Assert.assertTrue("Unexpected success", false);
		} catch (IllegalArgumentException iae) {
			// null arg is rejected
		}
	}

	@Test
	public void testMLPUserLoginProvider() {
		MLPUserLoginProvider m = new MLPUserLoginProvider(s1, s1, s1, s1, i1);
		m = new MLPUserLoginProvider();
		m.setAccessToken(s1);
		m.setCreated(d1);
		m.setDisplayName(s2);
		m.setImageUrl(s3);
		m.setModified(d2);
		m.setProfileUrl(s4);
		m.setProviderCode(s5);
		m.setProviderUserId(s6);
		m.setRank(i1);
		m.setRefreshToken(s7);
		m.setSecret(s8);
		m.setUserId(s9);
		Assert.assertEquals(s1, m.getAccessToken());
		Assert.assertEquals(d1, m.getCreated());
		Assert.assertEquals(s2, m.getDisplayName());
		Assert.assertEquals(s3, m.getImageUrl());
		Assert.assertEquals(s4, m.getProfileUrl());
		Assert.assertEquals(s5, m.getProviderCode());
		Assert.assertEquals(s6, m.getProviderUserId());
		Assert.assertEquals(i1, m.getRank());
		Assert.assertEquals(s7, m.getRefreshToken());
		Assert.assertEquals(s8, m.getSecret());
		Assert.assertEquals(s9, m.getUserId());
		Assert.assertFalse(m.equals(null));
		Assert.assertFalse(m.equals(new Object()));
		Assert.assertTrue(m.equals(m));
		Assert.assertNotNull(m.hashCode());
		logger.info(m.toString());
		try {
			new MLPUserLoginProvider(null, null, null, null, 0);
			Assert.assertTrue("Unexpected success", false);
		} catch (IllegalArgumentException iae) {
			// null arg is rejected
		}
		MLPUserLoginProvider.UserLoginProviderPK pk = new MLPUserLoginProvider.UserLoginProviderPK();
		pk = new MLPUserLoginProvider.UserLoginProviderPK(s1, s2, s3);
		MLPUserLoginProvider.UserLoginProviderPK pk2 = new MLPUserLoginProvider.UserLoginProviderPK(s1, s2, s3);
		Assert.assertFalse(pk.equals(null));
		Assert.assertFalse(pk.equals(new Object()));
		Assert.assertTrue(pk.equals(pk2));
		Assert.assertFalse(pk.hashCode() == 0);
		logger.info(pk.toString());
	}

	@Test
	public void testMLPUserNotification() {
		MLPUserNotification m = new MLPUserNotification(s1, s1, s1, s1, d1, d1, d1);
		m = new MLPUserNotification();
		m.setCreated(d1);
		m.setEnd(d2);
		m.setMessage(s1);
		m.setModified(d3);
		m.setNotificationId(s2);
		m.setStart(d4);
		m.setTitle(s3);
		m.setUrl(s4);
		m.setViewed(d5);
		Assert.assertEquals(d1, m.getCreated());
		Assert.assertEquals(d2, m.getEnd());
		Assert.assertEquals(s1, m.getMessage());
		Assert.assertEquals(d3, m.getModified());
		Assert.assertEquals(s2, m.getNotificationId());
		Assert.assertEquals(d4, m.getStart());
		Assert.assertEquals(s3, m.getTitle());
		Assert.assertEquals(s4, m.getUrl());
		Assert.assertEquals(d5, m.getViewed());
		Assert.assertFalse(m.equals(null));
		Assert.assertFalse(m.equals(new Object()));
		Assert.assertTrue(m.equals(m));
		Assert.assertNotNull(m.hashCode());
		logger.info(m.toString());
	}

	@Test
	public void testMLPUserRoleMap() {
		MLPUserRoleMap m = new MLPUserRoleMap(s1, s1);
		m = new MLPUserRoleMap();
		m.setRoleId(s1);
		m.setUserId(s2);
		Assert.assertEquals(s1, m.getRoleId());
		Assert.assertEquals(s2, m.getUserId());
		Assert.assertFalse(m.equals(null));
		Assert.assertFalse(m.equals(new Object()));
		Assert.assertTrue(m.equals(m));
		Assert.assertNotNull(m.hashCode());
		logger.info(m.toString());
		try {
			new MLPUserRoleMap(null, null);
			Assert.assertTrue("Unexpected success", false);
		} catch (IllegalArgumentException iae) {
			// null arg is rejected
		}
		MLPUserRoleMap.UserRoleMapPK pk = new MLPUserRoleMap.UserRoleMapPK();
		pk = new MLPUserRoleMap.UserRoleMapPK(s1, s2);
		Assert.assertFalse(pk.equals(null));
		Assert.assertFalse(pk.equals(new Object()));
		Assert.assertTrue(pk.equals(pk));
		Assert.assertFalse(pk.hashCode() == 0);
		logger.info(pk.toString());
	}

	@Test
	public void testMLPValidationSequence() {
		MLPValidationSequence m = new MLPValidationSequence(i1, s1);
		m = new MLPValidationSequence();
		m.setCreated(d1);
		m.setSequence(i1);
		m.setValTypeCode(s1);
		Assert.assertEquals(d1, m.getCreated());
		Assert.assertEquals(i1, m.getSequence());
		Assert.assertEquals(s1, m.getValTypeCode());
		Assert.assertFalse(m.equals(null));
		Assert.assertFalse(m.equals(new Object()));
		Assert.assertTrue(m.equals(m));
		Assert.assertNotNull(m.hashCode());
		logger.info(m.toString());
		try {
			new MLPValidationSequence(-1, null);
			Assert.assertTrue("Unexpected success", false);
		} catch (IllegalArgumentException iae) {
			// null arg is rejected
		}
		MLPValidationSequence.ValidationSequencePK pk = new MLPValidationSequence.ValidationSequencePK();
		pk = new MLPValidationSequence.ValidationSequencePK(i1, s1);
		Assert.assertFalse(pk.equals(null));
		Assert.assertFalse(pk.equals(new Object()));
		Assert.assertTrue(pk.equals(pk));
		Assert.assertFalse(pk.hashCode() == 0);
		logger.info(pk.toString());
	}

	@Test
	public void testMLPValidationStatus() {
		MLPValidationStatus m = new MLPValidationStatus();
		m.setCode(s1);
		m.setName(s2);
		Assert.assertEquals(s1, m.getCode());
		Assert.assertEquals(s2, m.getName());
		Assert.assertFalse(m.equals(null));
		Assert.assertFalse(m.equals(new Object()));
		Assert.assertTrue(m.equals(m));
		Assert.assertNotNull(m.hashCode());
		logger.info(m.toString());
	}

	@Test
	public void testMLPValidationType() {
		MLPValidationType m = new MLPValidationType();
		m.setCode(s1);
		m.setName(s2);
		Assert.assertEquals(s1, m.getCode());
		Assert.assertEquals(s2, m.getName());
		Assert.assertFalse(m.equals(null));
		Assert.assertFalse(m.equals(new Object()));
		Assert.assertTrue(m.equals(m));
		Assert.assertNotNull(m.hashCode());
		logger.info(m.toString());
	}

}

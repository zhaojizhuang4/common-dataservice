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

import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.acumos.cds.CCDSConstants;
import org.acumos.cds.CodeNameType;
import org.acumos.cds.domain.MLPPeer;
import org.acumos.cds.domain.MLPPeerSubscription;
import org.acumos.cds.repository.PeerRepository;
import org.acumos.cds.repository.PeerSubscriptionRepository;
import org.acumos.cds.service.PeerSearchService;
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
 * Answers REST requests to get, add, update and delete peers.
 * 
 * https://stackoverflow.com/questions/942951/rest-api-error-return-good-practices
 */
@Controller
@RequestMapping("/" + CCDSConstants.PEER_PATH)
public class PeerController extends AbstractController {

	private static final EELFLoggerDelegate logger = EELFLoggerDelegate.getLogger(PeerController.class);

	@Autowired
	private PeerRepository peerRepository;
	@Autowired
	private PeerSubscriptionRepository peerSubRepository;
	@Autowired
	private PeerSearchService peerSearchService;

	/**
	 * 
	 * @param pageable
	 *            Sort and page criteria
	 * @return List of artifacts, for serialization as JSON
	 */
	@ApiOperation(value = "Gets a page of peers, optionally sorted on fields.", response = MLPPeer.class, responseContainer = "Page")
	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	public Page<MLPPeer> getPageOfPeers(Pageable pageable) {
		return peerRepository.findAll(pageable);
	}

	/**
	 * @param queryParameters
	 *            Map of String (field name) to String (value) for restricting the
	 *            query
	 * @param pageable
	 *            Sort and page criteria
	 * @param response
	 *            HttpServletResponse
	 * @return Page of peers, for serialization as JSON.
	 */
	@ApiOperation(value = "Searches for peers using the field name - field value pairs specified as query parameters. Defaults to and (conjunction); send junction query parameter = o for or (disjunction).", response = MLPPeer.class, responseContainer = "Page")
	@RequestMapping(value = "/" + CCDSConstants.SEARCH_PATH, method = RequestMethod.GET)
	@ResponseBody
	public Object searchPeers(@RequestParam MultiValueMap<String, String> queryParameters, Pageable pageable,
			HttpServletResponse response) {
		cleanPageableParameters(queryParameters);
		List<String> junction = queryParameters.remove(CCDSConstants.JUNCTION_QUERY_PARAM);
		boolean isOr = junction != null && junction.size() == 1 && "o".equals(junction.get(0));
		if (queryParameters.size() == 0) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "Missing query", null);
		}
		try {
			Map<String, Object> convertedQryParm = convertQueryParameters(MLPPeer.class, queryParameters);
			return peerSearchService.findPeers(convertedQryParm, isOr, pageable);
		} catch (Exception ex) {
			logger.warn(EELFLoggerDelegate.errorLogger, "searchPeers failed", ex.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST,
					ex.getCause() != null ? ex.getCause().getMessage() : "searchPeers failed", ex);
		}
	}

	/**
	 * @param peerId
	 *            Path parameter with row ID
	 * @param response
	 *            HttpServletResponse
	 * @return A peer if found, an error otherwise.
	 */
	@ApiOperation(value = "Gets the peer for the specified ID.", response = MLPPeer.class)
	@RequestMapping(value = "/{peerId}", method = RequestMethod.GET)
	@ResponseBody
	public Object getPeer(@PathVariable("peerId") String peerId, HttpServletResponse response) {
		MLPPeer peer = peerRepository.findOne(peerId);
		if (peer == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + peerId, null);
		}
		return peer;
	}

	/**
	 * @param peer
	 *            peer to save
	 * @param response
	 *            HttpServletResponse
	 * @return peer model to be serialized as JSON
	 */
	@ApiOperation(value = "Creates a new peer.", response = MLPPeer.class)
	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public Object createPeer(@RequestBody MLPPeer peer, HttpServletResponse response) {
		logger.debug(EELFLoggerDelegate.debugLogger, "postPeer: received object: {} ", peer);
		Object result;
		try {
			String id = peer.getPeerId();
			if (id != null) {
				UUID.fromString(id);
				if (peerRepository.findOne(id) != null) {
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					result = new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "ID exists: " + id);
					return result;
				}
			}
			// Validate enum codes
			super.validateCode(peer.getStatusCode(), CodeNameType.PEER_STATUS);
			super.validateCode(peer.getValidationStatusCode(), CodeNameType.VALIDATION_STATUS);
			// Create a new row
			result = peerRepository.save(peer);
			response.setStatus(HttpServletResponse.SC_CREATED);
			// This is a hack to create the location path.
			response.setHeader(HttpHeaders.LOCATION, CCDSConstants.PEER_PATH + "/" + peer.getPeerId());
		} catch (Exception ex) {
			Exception cve = findConstraintViolationException(ex);
			logger.warn(EELFLoggerDelegate.errorLogger, "createPeer", cve.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			result = new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "createPeer failed", cve);
		}
		return result;
	}

	/**
	 * @param peerId
	 *            Path parameter with the row ID
	 * @param peer
	 *            Peer data to be updated
	 * @param response
	 *            HttpServletResponse
	 * @return Transport model with success or failure
	 */
	@ApiOperation(value = "Updates a peer.", response = SuccessTransport.class)
	@RequestMapping(value = "/{peerId}", method = RequestMethod.PUT)
	@ResponseBody
	public Object updatePeer(@PathVariable("peerId") String peerId, @RequestBody MLPPeer peer,
			HttpServletResponse response) {
		logger.debug(EELFLoggerDelegate.debugLogger, "updatePeer: received {} ", peer);
		// Get the existing one
		if (peerRepository.findOne(peerId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + peerId, null);
		}
		MLPTransportModel result = null;
		try {
			// Validate enum codes
			super.validateCode(peer.getStatusCode(), CodeNameType.PEER_STATUS);
			super.validateCode(peer.getValidationStatusCode(), CodeNameType.VALIDATION_STATUS);
			// Use the path-parameter id; don't trust the one in the object
			peer.setPeerId(peerId);
			// Update the existing row
			peerRepository.save(peer);
			result = new SuccessTransport(HttpServletResponse.SC_OK, null);
		} catch (Exception ex) {
			Exception cve = findConstraintViolationException(ex);
			logger.warn(EELFLoggerDelegate.errorLogger, "updatePeer", cve.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			result = new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "updatePeer failed", cve);
		}
		return result;
	}

	/**
	 * Originally this was declared void and accordingly returned nothing. But when
	 * used in SpringBoot, after invoking the method it would look for a ThymeLeaf
	 * template, fail to find it, then throw internal server error.
	 * 
	 * @param peerId
	 *            Path parameter that identifies the instance
	 * @param response
	 *            HttpServletResponse
	 * @return Transport model with success or failure
	 */
	@ApiOperation(value = "Deletes a peer.", response = SuccessTransport.class)
	@RequestMapping(value = "/{peerId}", method = RequestMethod.DELETE)
	@ResponseBody
	public MLPTransportModel deletePeer(@PathVariable("peerId") String peerId, HttpServletResponse response) {
		try {
			Iterable<MLPPeerSubscription> subs = peerSubRepository.findByPeerId(peerId);
			if (subs != null)
				peerSubRepository.delete(subs);
			peerRepository.delete(peerId);
			return new SuccessTransport(HttpServletResponse.SC_OK, null);
		} catch (Exception ex) {
			// e.g., EmptyResultDataAccessException is NOT an internal server error
			logger.warn(EELFLoggerDelegate.errorLogger, "deletePeer", ex.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "deletePeer failed", ex);
		}
	}

	/* Peer Subscriptions */

	/**
	 * @param peerId
	 *            Path parameter that identifies the instance
	 * @param pageable
	 *            Sort and page criteria
	 * @param response
	 *            HttpServletResponse
	 * @return List of artifacts, for serialization as JSON
	 */
	@ApiOperation(value = "Gets all subscriptions for the specified peer.", response = MLPPeerSubscription.class, responseContainer = "List")
	@RequestMapping(value = "/{peerId}/" + CCDSConstants.SUBSCRIPTION_PATH, method = RequestMethod.GET)
	@ResponseBody
	public Object getPeerSubs(@PathVariable("peerId") String peerId, Pageable pageable, HttpServletResponse response) {
		// Get the existing one
		if (peerRepository.findOne(peerId) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + peerId, null);
		}
		return peerSubRepository.findByPeerId(peerId);
	}

	/**
	 * @param subId
	 *            Path parameter with subscription ID
	 * @param response
	 *            HttpServletResponse
	 * @return A peer if found, an error otherwise.
	 */
	@ApiOperation(value = "Gets the peer subscription for the specified ID.", response = MLPPeerSubscription.class)
	@RequestMapping(value = "/" + CCDSConstants.SUBSCRIPTION_PATH + "/{subId}", method = RequestMethod.GET)
	@ResponseBody
	public Object getPeerSub(@PathVariable("subId") Long subId, HttpServletResponse response) {
		MLPPeerSubscription peerSub = peerSubRepository.findOne(subId);
		if (peerSub == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + subId, null);
		}
		return peerSub;
	}

	/**
	 * @param peerSub
	 *            peerSubscription to save
	 * @param response
	 *            HttpServletResponse
	 * @return peer model to be serialized as JSON
	 */
	@ApiOperation(value = "Creates a new peer subscription.", response = MLPPeerSubscription.class)
	@RequestMapping(value = "/" + CCDSConstants.SUBSCRIPTION_PATH, method = RequestMethod.POST)
	@ResponseBody
	public Object createPeerSub(@RequestBody MLPPeerSubscription peerSub, HttpServletResponse response) {
		logger.debug(EELFLoggerDelegate.debugLogger, "createPeerSub: received object: {} ", peerSub);
		if (peerRepository.findOne(peerSub.getPeerId()) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + peerSub.getPeerId(), null);
		}
		Object result;
		try {
			// Validate enum codes
			super.validateCode(peerSub.getAccessType(), CodeNameType.ACCESS_TYPE);
			super.validateCode(peerSub.getScopeType(), CodeNameType.SUBSCRIPTION_SCOPE);
			// Null out any existing ID to get an auto-generated ID
			peerSub.setSubId(null);
			// Create a new row
			result = peerSubRepository.save(peerSub);
			response.setStatus(HttpServletResponse.SC_CREATED);
			// This is a hack to create the location path.
			response.setHeader(HttpHeaders.LOCATION,
					CCDSConstants.PEER_PATH + "/" + CCDSConstants.SUBSCRIPTION_PATH + "/" + peerSub.getSubId());
		} catch (Exception ex) {
			Exception cve = findConstraintViolationException(ex);
			logger.warn(EELFLoggerDelegate.errorLogger, "createPeerSub", cve.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			result = new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "createPeerSub failed", cve);
		}
		return result;
	}

	/**
	 * @param subId
	 *            Path parameter with the row ID
	 * @param peerSub
	 *            Peer data to be updated
	 * @param response
	 *            HttpServletResponse
	 * @return Transport model with success or failure
	 */
	@ApiOperation(value = "Updates a peer subscription.", response = SuccessTransport.class)
	@RequestMapping(value = "/" + CCDSConstants.SUBSCRIPTION_PATH + "/{subId}", method = RequestMethod.PUT)
	@ResponseBody
	public Object updatePeerSub(@PathVariable("subId") Long subId, @RequestBody MLPPeerSubscription peerSub,
			HttpServletResponse response) {
		logger.debug(EELFLoggerDelegate.debugLogger, "updatePeerSub: received {} ", peerSub);
		// Get the existing one
		MLPPeerSubscription existingPeerSub = peerSubRepository.findOne(subId);
		if (existingPeerSub == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + subId, null);
		}
		MLPTransportModel result = null;
		try {
			// Validate enum codes
			super.validateCode(peerSub.getAccessType(), CodeNameType.ACCESS_TYPE);
			super.validateCode(peerSub.getScopeType(), CodeNameType.SUBSCRIPTION_SCOPE);
			// Use the path-parameter id; don't trust the one in the object
			peerSub.setSubId(subId);
			// Update the existing row
			peerSubRepository.save(peerSub);
			result = new SuccessTransport(HttpServletResponse.SC_OK, null);
		} catch (Exception ex) {
			Exception cve = findConstraintViolationException(ex);
			logger.warn(EELFLoggerDelegate.errorLogger, "updatePeerSub", cve.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			result = new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "updatePeerSub failed", cve);
		}
		return result;
	}

	/**
	 * @param subId
	 *            Path parameter that identifies the peer subscription
	 * @param response
	 *            HttpServletResponse
	 * @return Transport model with success or failure
	 */
	@ApiOperation(value = "Deletes a peer subscription.", response = SuccessTransport.class)
	@RequestMapping(value = "/" + CCDSConstants.SUBSCRIPTION_PATH + "/{subId}", method = RequestMethod.DELETE)
	@ResponseBody
	public MLPTransportModel deletePeerSub(@PathVariable("subId") Long subId, HttpServletResponse response) {
		try {
			peerSubRepository.delete(subId);
			return new SuccessTransport(HttpServletResponse.SC_OK, null);
		} catch (Exception ex) {
			// e.g., EmptyResultDataAccessException is NOT an internal server error
			logger.warn(EELFLoggerDelegate.errorLogger, "deletePeerSub", ex.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "deletePeerSub failed", ex);
		}
	}

}

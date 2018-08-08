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
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.acumos.cds.CCDSConstants;
import org.acumos.cds.CodeNameType;
import org.acumos.cds.domain.MLPCodeNamePair;
import org.acumos.cds.service.CodeNameService;
import org.acumos.cds.transport.ErrorTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.ApiOperation;

/**
 * Provides getters for all code-name value sets, which are obtained from
 * property sources.
 */
@Controller
@RequestMapping(value = "/" + CCDSConstants.CODE_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
public class CodeTableController extends AbstractController {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Autowired
	private CodeNameService codeNameService;

	/**
	 * @return List of value set names that can be supplied to
	 *         {@link #getCodeNamePairs(String, HttpServletResponse)}
	 */
	@ApiOperation(value = "Gets the list of value set names.", response = String.class, responseContainer = "List")
	@RequestMapping(value = "/" + CCDSConstants.PAIR_PATH, method = RequestMethod.GET)
	@ResponseBody
	public List<String> getValueSetNames() {
		logger.info("getValueSetNames");
		List<String> list = new ArrayList<>();
		for (CodeNameType cn : CodeNameType.values()) {
			list.add(cn.name());
		}
		return list;
	}

	/**
	 * @param valueSetName
	 *            Name of a field in enum {@link org.acumos.cds.CodeNameType}
	 * @param response
	 *            HttpServletResponse
	 * @return List of MLPCodeNamePair objects for the specified value set.
	 */
	@ApiOperation(value = "Gets the list of code-name pairs for the specified value set.", response = MLPCodeNamePair.class, responseContainer = "List")
	@RequestMapping(value = "/" + CCDSConstants.PAIR_PATH + "/{name}", method = RequestMethod.GET)
	@ResponseBody
	public Object getCodeNamePairs(@PathVariable(CCDSConstants.NAME_PATH) String valueSetName,
			HttpServletResponse response) {
		logger.info("getCodeNamePairs {}", valueSetName);
		CodeNameType type;
		try {
			type = CodeNameType.valueOf(valueSetName);
			Object result = codeNameService.getCodeNamePairs(type);
			return result;
		} catch (Exception ex) {
			// e.g., EmptyResultDataAccessException is NOT an internal server error
			logger.warn("getCodeNamePairs failed: {}", ex.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "Unexpected value set name " + valueSetName);
		}
	}

	/**
	 * @deprecated Use {@link #getCodeNamePairs(String, HttpServletResponse)}
	 * @param response
	 *            HttpServletResponse
	 * @return List of MLPCodeNamePair objects with access type code-name pairs
	 */
	@ApiOperation(value = "Gets the list of access type codes.", response = MLPCodeNamePair.class, responseContainer = "List")
	@RequestMapping(value = "/" + CCDSConstants.ACCESS_PATH + "/" + CCDSConstants.TYPE_PATH, method = RequestMethod.GET)
	@ResponseBody
	@Deprecated
	public Object getAccessTypeList(HttpServletResponse response) {
		return getCodeNamePairs(CodeNameType.ACCESS_TYPE.name(), response);
	}

	/**
	 * @deprecated Use {@link #getCodeNamePairs(String, HttpServletResponse)}
	 * @param response
	 *            HttpServletResponse
	 * @return List of MLPCodeNamePair with artifact type code-name pairs
	 */
	@ApiOperation(value = "Gets the list of artifact type codes.", response = MLPCodeNamePair.class, responseContainer = "List")
	@RequestMapping(value = "/" + CCDSConstants.ARTIFACT_PATH + "/"
			+ CCDSConstants.TYPE_PATH, method = RequestMethod.GET)
	@ResponseBody
	@Deprecated
	public Object getArtifactTypeList(HttpServletResponse response) {
		return getCodeNamePairs(CodeNameType.ARTIFACT_TYPE.name(), response);
	}

	/**
	 * @deprecated Use {@link #getCodeNamePairs(String, HttpServletResponse)}
	 * @param response
	 *            HttpServletResponse
	 * @return List of MLPCodeNamePair with deployment status code-name pairs
	 */
	@ApiOperation(value = "Gets the list of deployment status codes.", response = MLPCodeNamePair.class, responseContainer = "List")
	@RequestMapping(value = "/" + CCDSConstants.DEPLOY_PATH + "/"
			+ CCDSConstants.STATUS_PATH, method = RequestMethod.GET)
	@ResponseBody
	@Deprecated
	public Object getDeploymentStatusList(HttpServletResponse response) {
		return getCodeNamePairs(CodeNameType.DEPLOYMENT_STATUS.name(), response);
	}

	/**
	 * @deprecated Use {@link #getCodeNamePairs(String, HttpServletResponse)}
	 * @param response
	 *            HttpServletResponse
	 * @return List of MLPCodeNamePair with login provider code-name pairs
	 */
	@ApiOperation(value = "Gets the list of login provider codes.", response = MLPCodeNamePair.class, responseContainer = "List")
	@RequestMapping(value = "/" + CCDSConstants.LOGIN_PROVIDER_PATH, method = RequestMethod.GET)
	@ResponseBody
	@Deprecated
	public Object getLoginProviderList(HttpServletResponse response) {
		return getCodeNamePairs(CodeNameType.LOGIN_PROVIDER.name(), response);
	}

	/**
	 * @deprecated Use {@link #getCodeNamePairs(String, HttpServletResponse)}
	 * @param response
	 *            HttpServletResponse
	 * @return List of MLPCodeNamePair with message severity code-name pairs
	 */
	@ApiOperation(value = "Gets the list of message severity codes.", response = MLPCodeNamePair.class, responseContainer = "List")
	@RequestMapping(value = "/" + CCDSConstants.MSG_SEV_PATH, method = RequestMethod.GET)
	@ResponseBody
	@Deprecated
	public Object getMessageSeverityList(HttpServletResponse response) {
		return getCodeNamePairs(CodeNameType.MESSAGE_SEVERITY.name(), response);
	}

	/**
	 * @deprecated Use {@link #getCodeNamePairs(String, HttpServletResponse)}
	 * @param response
	 *            HttpServletResponse
	 * @return List of MLPCodeNamePair with model type code-name pairs
	 */
	@ApiOperation(value = "Gets the list of model type codes.", response = MLPCodeNamePair.class, responseContainer = "List")
	@RequestMapping(value = "/" + CCDSConstants.MODEL_PATH + "/" + CCDSConstants.TYPE_PATH, method = RequestMethod.GET)
	@ResponseBody
	@Deprecated
	public Object getModelTypeList(HttpServletResponse response) {
		return getCodeNamePairs(CodeNameType.MODEL_TYPE.name(), response);
	}

	/**
	 * @deprecated Use {@link #getCodeNamePairs(String, HttpServletResponse)}
	 * @param response
	 *            HttpServletResponse
	 * @return List of MLPCodeNamePair with notification delivery mechanism
	 *         code-name pairs
	 */
	@ApiOperation(value = "Gets the list of notification delivery mechanism codes.", response = MLPCodeNamePair.class, responseContainer = "List")
	@RequestMapping(value = "/" + CCDSConstants.NOTIFICATION_MECH_PATH, method = RequestMethod.GET)
	@ResponseBody
	@Deprecated
	public Object getNotificationDeliveryMechanismList(HttpServletResponse response) {
		return getCodeNamePairs(CodeNameType.NOTIFICATION_DELIVERY_MECHANISM.name(), response);
	}

	/**
	 * @deprecated Use {@link #getCodeNamePairs(String, HttpServletResponse)}
	 * @param response
	 *            HttpServletResponse
	 * @return List of MLPCodeNamePair with peer status code-name pairs
	 */
	@ApiOperation(value = "Gets the list of peer status codes.", response = MLPCodeNamePair.class, responseContainer = "List")
	@RequestMapping(value = "/" + CCDSConstants.PEER_PATH + "/" + CCDSConstants.STATUS_PATH, method = RequestMethod.GET)
	@ResponseBody
	@Deprecated
	public Object getPeerStatusList(HttpServletResponse response) {
		return getCodeNamePairs(CodeNameType.PEER_STATUS.name(), response);
	}

	/**
	 * @deprecated Use {@link #getCodeNamePairs(String, HttpServletResponse)}
	 * @param response
	 *            HttpServletResponse
	 * @return List of MLPCodeNamePair with step status code-name pairs
	 */
	@ApiOperation(value = "Gets the list of step status codes.", response = MLPCodeNamePair.class, responseContainer = "List")
	@RequestMapping(value = "/" + CCDSConstants.STEP_PATH + "/" + CCDSConstants.STATUS_PATH, method = RequestMethod.GET)
	@ResponseBody
	@Deprecated
	public Object getStepStatusList(HttpServletResponse response) {
		return getCodeNamePairs(CodeNameType.STEP_STATUS.name(), response);
	}

	/**
	 * @deprecated Use {@link #getCodeNamePairs(String, HttpServletResponse)}
	 * @param response
	 *            HttpServletResponse
	 * @return List of MLPCodeNamePair with step type code-name pairs
	 */
	@ApiOperation(value = "Gets the list of step type codes.", response = MLPCodeNamePair.class, responseContainer = "List")
	@RequestMapping(value = "/" + CCDSConstants.STEP_PATH + "/" + CCDSConstants.TYPE_PATH, method = RequestMethod.GET)
	@ResponseBody
	@Deprecated
	public Object getStepTypeList(HttpServletResponse response) {
		return getCodeNamePairs(CodeNameType.STEP_TYPE.name(), response);
	}

	/**
	 * @deprecated Use {@link #getCodeNamePairs(String, HttpServletResponse)}
	 * @param response
	 *            HttpServletResponse
	 * @return List of MLPCodeNamePair with subscription scope code-name pairs
	 */
	@ApiOperation(value = "Gets the list of subscription scope codes.", response = MLPCodeNamePair.class, responseContainer = "List")
	@RequestMapping(value = "/" + CCDSConstants.SUBSCRIPTION_PATH + "/"
			+ CCDSConstants.TYPE_PATH, method = RequestMethod.GET)
	@ResponseBody
	@Deprecated
	public Object getSubscriptionScopes(HttpServletResponse response) {
		return getCodeNamePairs(CodeNameType.SUBSCRIPTION_SCOPE.name(), response);
	}

	/**
	 * @deprecated Use {@link #getCodeNamePairs(String, HttpServletResponse)}
	 * @param response
	 *            HttpServletResponse
	 * @return List of MLPCodeNamePair with toolkit type code-name pairs
	 */
	@ApiOperation(value = "Gets the list of toolkit type codes.", response = MLPCodeNamePair.class, responseContainer = "List")
	@RequestMapping(value = "/" + CCDSConstants.TOOLKIT_PATH + "/"
			+ CCDSConstants.TYPE_PATH, method = RequestMethod.GET)
	@ResponseBody
	@Deprecated
	public Object getToolkitTypeList(HttpServletResponse response) {
		return getCodeNamePairs(CodeNameType.TOOLKIT_TYPE.name(), response);
	}

	/**
	 * @deprecated Use {@link #getCodeNamePairs(String, HttpServletResponse)}
	 * @param response
	 *            HttpServletResponse
	 * @return List of MLPCodeNamePair with validation status code-name pairs
	 */
	@ApiOperation(value = "Gets the list of validation status codes.", response = MLPCodeNamePair.class, responseContainer = "List")
	@RequestMapping(value = "/" + CCDSConstants.VAL_PATH + "/" + CCDSConstants.STATUS_PATH, method = RequestMethod.GET)
	@ResponseBody
	@Deprecated
	public Object getValidationStatusList(HttpServletResponse response) {
		return getCodeNamePairs(CodeNameType.VALIDATION_STATUS.name(), response);
	}

	/**
	 * @deprecated Use {@link #getCodeNamePairs(String, HttpServletResponse)}
	 * @param response
	 *            HttpServletResponse
	 * @return List of MLPCodeNamePair with validation type code-name pairs
	 */
	@ApiOperation(value = "Gets the list of validation type codes.", response = MLPCodeNamePair.class, responseContainer = "List")
	@RequestMapping(value = "/" + CCDSConstants.VAL_PATH + "/" + CCDSConstants.TYPE_PATH, method = RequestMethod.GET)
	@ResponseBody
	@Deprecated
	public Object getValidationTypeList(HttpServletResponse response) {
		return getCodeNamePairs(CodeNameType.VALIDATION_TYPE.name(), response);
	}

}

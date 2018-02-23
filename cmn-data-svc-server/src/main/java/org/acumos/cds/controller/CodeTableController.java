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

import java.util.ArrayList;
import java.util.List;

import org.acumos.cds.AccessTypeCode;
import org.acumos.cds.ArtifactTypeCode;
import org.acumos.cds.CCDSConstants;
import org.acumos.cds.DeploymentStatusCode;
import org.acumos.cds.LoginProviderCode;
import org.acumos.cds.MessageSeverityCode;
import org.acumos.cds.ModelTypeCode;
import org.acumos.cds.NotificationDeliveryMechanismCode;
import org.acumos.cds.PeerStatusCode;
import org.acumos.cds.StepStatusCode;
import org.acumos.cds.StepTypeCode;
import org.acumos.cds.SubscriptionScopeCode;
import org.acumos.cds.ToolkitTypeCode;
import org.acumos.cds.ValidationStatusCode;
import org.acumos.cds.ValidationTypeCode;
import org.acumos.cds.domain.MLPCodeNamePair;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.ApiOperation;

/**
 * Provides getters for all code-name value sets. Fixed value sets are
 * implemented as Java enums, so these controllers are only of interest to
 * systems that don't use this project's Java client.
 */
@Controller
@RequestMapping("/" + CCDSConstants.CODE_PATH)
public class CodeTableController extends AbstractController {

	/**
	 * @return List of MLPCodeNamePair objects with access type code-name pairs
	 */
	@ApiOperation(value = "Gets the list of access type codes.", response = MLPCodeNamePair.class, responseContainer = "List")
	@RequestMapping(value = "/" + CCDSConstants.ACCESS_PATH + "/" + CCDSConstants.TYPE_PATH, method = RequestMethod.GET)
	@ResponseBody
	public List<MLPCodeNamePair> getAccessTypeList() {
		List<MLPCodeNamePair> list = new ArrayList<>();
		for (AccessTypeCode cn : AccessTypeCode.values())
			list.add(new MLPCodeNamePair(cn.name(), cn.getTypeName()));
		return list;
	}

	/**
	 * @return List of MLPCodeNamePair with artifact type code-name pairs
	 */
	@ApiOperation(value = "Gets the list of artifact type codes.", response = MLPCodeNamePair.class, responseContainer = "List")
	@RequestMapping(value = "/" + CCDSConstants.ARTIFACT_PATH + "/"
			+ CCDSConstants.TYPE_PATH, method = RequestMethod.GET)
	@ResponseBody
	public List<MLPCodeNamePair> getArtifactTypeList() {
		List<MLPCodeNamePair> list = new ArrayList<>();
		for (ArtifactTypeCode cn : ArtifactTypeCode.values())
			list.add(new MLPCodeNamePair(cn.name(), cn.getTypeName()));
		return list;
	}

	/**
	 * @return List of MLPCodeNamePair with deployment status code-name pairs
	 */
	@ApiOperation(value = "Gets the list of deployment status codes.", response = MLPCodeNamePair.class, responseContainer = "List")
	@RequestMapping(value = "/" + CCDSConstants.DEPLOY_PATH + "/"
			+ CCDSConstants.STATUS_PATH, method = RequestMethod.GET)
	@ResponseBody
	public List<MLPCodeNamePair> getDeploymentStatusList() {
		List<MLPCodeNamePair> list = new ArrayList<>();
		for (DeploymentStatusCode cn : DeploymentStatusCode.values())
			list.add(new MLPCodeNamePair(cn.name(), cn.getStatusName()));
		return list;
	}

	/**
	 * @return List of MLPCodeNamePair with login provider code-name pairs
	 */
	@ApiOperation(value = "Gets the list of login provider codes.", response = MLPCodeNamePair.class, responseContainer = "List")
	@RequestMapping(value = "/" + CCDSConstants.LOGIN_PROVIDER_PATH, method = RequestMethod.GET)
	@ResponseBody
	public List<MLPCodeNamePair> getLoginProviderList() {
		List<MLPCodeNamePair> list = new ArrayList<>();
		for (LoginProviderCode cn : LoginProviderCode.values())
			list.add(new MLPCodeNamePair(cn.name(), cn.getProviderName()));
		return list;
	}

	/**
	 * @return List of MLPCodeNamePair with message severity code-name pairs
	 */
	@ApiOperation(value = "Gets the list of message severity codes.", response = MLPCodeNamePair.class, responseContainer = "List")
	@RequestMapping(value = "/" + CCDSConstants.MSG_SEV_PATH, method = RequestMethod.GET)
	@ResponseBody
	public Iterable<MLPCodeNamePair> getMessageSeverityList() {
		List<MLPCodeNamePair> list = new ArrayList<>();
		for (MessageSeverityCode cn : MessageSeverityCode.values())
			list.add(new MLPCodeNamePair(cn.name(), cn.getTypeName()));
		return list;
	}

	/**
	 * @return List of MLPCodeNamePair with model type code-name pairs
	 */
	@ApiOperation(value = "Gets the list of model type codes.", response = MLPCodeNamePair.class, responseContainer = "List")
	@RequestMapping(value = "/" + CCDSConstants.MODEL_PATH + "/" + CCDSConstants.TYPE_PATH, method = RequestMethod.GET)
	@ResponseBody
	public List<MLPCodeNamePair> getModelTypeList() {
		List<MLPCodeNamePair> list = new ArrayList<>();
		for (ModelTypeCode cn : ModelTypeCode.values())
			list.add(new MLPCodeNamePair(cn.name(), cn.getTypeName()));
		return list;
	}

	/**
	 * @return List of MLPCodeNamePair with notification delivery mechanism
	 *         code-name pairs
	 */
	@ApiOperation(value = "Gets the list of notification delivery mechanism codes.", response = MLPCodeNamePair.class, responseContainer = "List")
	@RequestMapping(value = "/" + CCDSConstants.NOTIFICATION_MECH_PATH, method = RequestMethod.GET)
	@ResponseBody
	public List<MLPCodeNamePair> getNotificationDeliveryMechanismList() {
		List<MLPCodeNamePair> list = new ArrayList<>();
		for (NotificationDeliveryMechanismCode cn : NotificationDeliveryMechanismCode.values())
			list.add(new MLPCodeNamePair(cn.name(), cn.getTypeName()));
		return list;
	}

	/**
	 * @return List of MLPCodeNamePair with peer status code-name pairs
	 */
	@ApiOperation(value = "Gets the list of peer status codes.", response = MLPCodeNamePair.class, responseContainer = "List")
	@RequestMapping(value = "/" + CCDSConstants.PEER_PATH + "/" + CCDSConstants.STATUS_PATH, method = RequestMethod.GET)
	@ResponseBody
	public List<MLPCodeNamePair> getPeerStatusList() {
		List<MLPCodeNamePair> list = new ArrayList<>();
		for (PeerStatusCode cn : PeerStatusCode.values())
			list.add(new MLPCodeNamePair(cn.name(), cn.getStatusName()));
		return list;
	}

	/**
	 * @return List of MLPCodeNamePair with step status code-name pairs
	 */
	@ApiOperation(value = "Gets the list of step status codes.", response = MLPCodeNamePair.class, responseContainer = "List")
	@RequestMapping(value = "/" + CCDSConstants.STEP_PATH + "/" + CCDSConstants.STATUS_PATH, method = RequestMethod.GET)
	@ResponseBody
	public List<MLPCodeNamePair> getStepStatusList() {
		List<MLPCodeNamePair> list = new ArrayList<>();
		for (StepStatusCode cn : StepStatusCode.values())
			list.add(new MLPCodeNamePair(cn.name(), cn.getStatusName()));
		return list;
	}

	/**
	 * @return List of MLPCodeNamePair with step type code-name pairs
	 */
	@ApiOperation(value = "Gets the list of step type codes.", response = MLPCodeNamePair.class, responseContainer = "List")
	@RequestMapping(value = "/" + CCDSConstants.STEP_PATH + "/" + CCDSConstants.TYPE_PATH, method = RequestMethod.GET)
	@ResponseBody
	public List<MLPCodeNamePair> getStepTypeList() {
		List<MLPCodeNamePair> list = new ArrayList<>();
		for (StepTypeCode cn : StepTypeCode.values())
			list.add(new MLPCodeNamePair(cn.name(), cn.getStepName()));
		return list;
	}

	/**
	 * @return List of MLPCodeNamePair with subscription scope code-name pairs
	 */
	@ApiOperation(value = "Gets the list of subscription scope codes.", response = MLPCodeNamePair.class, responseContainer = "List")
	@RequestMapping(value = "/" + CCDSConstants.SUBSCRIPTION_PATH + "/"
			+ CCDSConstants.TYPE_PATH, method = RequestMethod.GET)
	@ResponseBody
	public List<MLPCodeNamePair> getSubscriptionScopes() {
		List<MLPCodeNamePair> list = new ArrayList<>();
		for (SubscriptionScopeCode cn : SubscriptionScopeCode.values())
			list.add(new MLPCodeNamePair(cn.name(), cn.getScopeName()));
		return list;
	}

	/**
	 * @return List of MLPCodeNamePair with toolkit type code-name pairs
	 */
	@ApiOperation(value = "Gets the list of toolkit type codes.", response = MLPCodeNamePair.class, responseContainer = "List")
	@RequestMapping(value = "/" + CCDSConstants.TOOLKIT_PATH + "/"
			+ CCDSConstants.TYPE_PATH, method = RequestMethod.GET)
	@ResponseBody
	public Iterable<MLPCodeNamePair> getToolkitTypeList() {
		List<MLPCodeNamePair> list = new ArrayList<>();
		for (ToolkitTypeCode cn : ToolkitTypeCode.values())
			list.add(new MLPCodeNamePair(cn.name(), cn.getTypeName()));
		return list;
	}

	/**
	 * @return List of MLPCodeNamePair with validation status code-name pairs
	 */
	@ApiOperation(value = "Gets the list of validation status codes.", response = MLPCodeNamePair.class, responseContainer = "List")
	@RequestMapping(value = "/" + CCDSConstants.VAL_PATH + "/" + CCDSConstants.STATUS_PATH, method = RequestMethod.GET)
	@ResponseBody
	public List<MLPCodeNamePair> getValidationStatusList() {
		List<MLPCodeNamePair> list = new ArrayList<>();
		for (ValidationStatusCode cn : ValidationStatusCode.values())
			list.add(new MLPCodeNamePair(cn.name(), cn.getStatusName()));
		return list;
	}

	/**
	 * @return List of MLPCodeNamePair with validation type code-name pairs
	 */
	@ApiOperation(value = "Gets the list of validation type codes.", response = MLPCodeNamePair.class, responseContainer = "List")
	@RequestMapping(value = "/" + CCDSConstants.VAL_PATH + "/" + CCDSConstants.TYPE_PATH, method = RequestMethod.GET)
	@ResponseBody
	public List<MLPCodeNamePair> getValidationTypeList() {
		List<MLPCodeNamePair> list = new ArrayList<>();
		for (ValidationTypeCode cn : ValidationTypeCode.values())
			list.add(new MLPCodeNamePair(cn.name(), cn.getTypeName()));
		return list;
	}

}

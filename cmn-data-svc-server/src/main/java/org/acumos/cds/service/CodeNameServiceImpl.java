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

package org.acumos.cds.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.acumos.cds.CodeNameType;
import org.acumos.cds.config.CodeNameConfiguration;
import org.acumos.cds.domain.MLPCodeNamePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("codeNameService")
public class CodeNameServiceImpl implements CodeNameService {

	@Autowired
	private CodeNameConfiguration codeNameConfig;

	/**
	 * Translates a value set name into a set of pairs.
	 * 
	 * Unfortunately the structure of the CodeNameConfiguration class required the
	 * big switch-case to answer appropriately.
	 */
	private Map<String, String> getPairs(CodeNameType type) {
		switch (type) {
		case ACCESS_TYPE:
			return codeNameConfig.getAccessType();
		case ARTIFACT_TYPE:
			return codeNameConfig.getArtifactType();
		case DEPLOYMENT_STATUS:
			return codeNameConfig.getDeploymentStatus();
		case LOGIN_PROVIDER:
			return codeNameConfig.getLoginProvider();
		case MESSAGE_SEVERITY:
			return codeNameConfig.getMessageSeverity();
		case MODEL_TYPE:
			return codeNameConfig.getModelType();
		case NOTIFICATION_DELIVERY_MECHANISM:
			return codeNameConfig.getNotificationDeliveryMechanism();
		case PEER_STATUS:
			return codeNameConfig.getPeerStatus();
		case STEP_STATUS:
			return codeNameConfig.getStepStatus();
		case STEP_TYPE:
			return codeNameConfig.getStepType();
		case SUBSCRIPTION_SCOPE:
			return codeNameConfig.getSubscriptionScope();
		case TOOLKIT_TYPE:
			return codeNameConfig.getToolkitType();
		case VALIDATION_STATUS:
			return codeNameConfig.getValidationStatus();
		case VALIDATION_TYPE:
			return codeNameConfig.getValidationType();
		}
		throw new IllegalArgumentException("unimplemented case for type " + type.name());
	}

	@Override
	public List<MLPCodeNamePair> getCodeNamePairs(CodeNameType type) {
		Map<String, String> pairs = getPairs(type);
		List<MLPCodeNamePair> list = new ArrayList<>();
		for (Map.Entry<String, String> entry : pairs.entrySet())
			list.add(new MLPCodeNamePair(entry.getKey(), entry.getValue()));
		return list;
	}

	@Override
	public boolean validateCode(String code, CodeNameType type) {
		Map<String, String> pairs = getPairs(type);
		return pairs.containsKey(code);
	}

}

/*-
 * ===============LICENSE_START=======================================================
 * Acumos
 * ===================================================================================
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
 * ===================================================================================
 * This Acumos software file is distributed by AT&T
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
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.acumos.cds.CCDSConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.ApiOperation;

/**
 * Answers REST requests to get, create, update and delete datasets.
 */
@Controller
@RequestMapping(value = "/" + CCDSConstants.DATASET_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
public class DatasetController extends AbstractController {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	
	/**
	 * @param datasetId
	 *            Path parameter with row ID
	 * @param response
	 *            HttpServletResponse
	 * @return A dataset if found, an error otherwise.
	 */
	@ApiOperation(value = "Gets the dataset for the specified ID.", response = Map.class)
	@RequestMapping(value = "/{datasetId}", method = RequestMethod.GET)
	@ResponseBody
	public Object getDataset(@PathVariable("datasetId") String datasetId, HttpServletResponse response) {
		logger.info("getDataset ID {}", datasetId);
		Map<String, Object> dummy = new HashMap<>();
		return dummy;
	}

}

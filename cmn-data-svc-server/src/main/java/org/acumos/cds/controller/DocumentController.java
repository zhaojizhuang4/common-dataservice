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
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.acumos.cds.CCDSConstants;
import org.acumos.cds.domain.MLPDocument;
import org.acumos.cds.repository.DocumentRepository;
import org.acumos.cds.transport.ErrorTransport;
import org.acumos.cds.transport.MLPTransportModel;
import org.acumos.cds.transport.SuccessTransport;
import org.acumos.cds.util.EELFLoggerDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.ApiOperation;

/**
 * Answers REST requests to get, create, update and delete documents.
 */
@Controller
@RequestMapping(value = "/" + CCDSConstants.DOCUMENT_PATH, produces = CCDSConstants.APPLICATION_JSON)
public class DocumentController extends AbstractController {

	private static final EELFLoggerDelegate logger = EELFLoggerDelegate.getLogger(MethodHandles.lookup().lookupClass());

	@Autowired
	private DocumentRepository documentRepository;

	/**
	 * @param documentId
	 *            Path parameter with row ID
	 * @param response
	 *            HttpServletResponse
	 * @return A document if found, an error otherwise.
	 */
	@ApiOperation(value = "Gets the document for the specified ID.", response = MLPDocument.class)
	@RequestMapping(value = "/{documentId}", method = RequestMethod.GET)
	@ResponseBody
	public Object getDocument(@PathVariable("documentId") String documentId, HttpServletResponse response) {
		Date beginDate = new Date();
		MLPDocument da = documentRepository.findOne(documentId);
		logger.audit(beginDate, "getDocument ID {}", documentId);
		if (da == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + documentId, null);
		}
		return da;
	}

	/**
	 * @param document
	 *            document to save. If no ID is set a new one will be generated; if
	 *            an ID value is set, it will be used if valid and not in table.
	 * @param response
	 *            HttpServletResponse
	 * @return Entity on success; error on failure.
	 */
	@ApiOperation(value = "Creates a new document.", response = MLPDocument.class)
	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public Object createDocument(@RequestBody MLPDocument document, HttpServletResponse response) {
		Date beginDate = new Date();
		try {
			String id = document.getDocumentId();
			if (id != null) {
				UUID.fromString(id);
				if (documentRepository.findOne(id) != null) {
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "ID exists: " + id);
				}
			}
			// Create a new row
			Object result = documentRepository.save(document);
			response.setStatus(HttpServletResponse.SC_CREATED);
			// This is a hack to create the location path.
			response.setHeader(HttpHeaders.LOCATION, CCDSConstants.DOCUMENT_PATH + "/" + document.getDocumentId());
			logger.audit(beginDate, "createDocument ID {}", document.getDocumentId());
			return result;
		} catch (Exception ex) {
			// e.g., EmptyResultDataAccessException is NOT an internal server error
			Exception cve = findConstraintViolationException(ex);
			logger.warn(EELFLoggerDelegate.errorLogger, "createDocument failed: {}", cve.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "createDocument failed", cve);
		}
	}

	/**
	 * @param documentId
	 *            Path parameter with the row ID
	 * @param document
	 *            document data to be updated
	 * @param response
	 *            HttpServletResponse
	 * @return Document that maps String to Object, for serialization as JSON
	 */
	@ApiOperation(value = "Updates a document.", response = SuccessTransport.class)
	@RequestMapping(value = "/{documentId}", method = RequestMethod.PUT)
	@ResponseBody
	public Object updateDocument(@PathVariable("documentId") String documentId, @RequestBody MLPDocument document,
			HttpServletResponse response) {
		Date beginDate = new Date();
		// Check for existing because the Hibernate save() method doesn't distinguish
		MLPDocument existing = documentRepository.findOne(documentId);
		if (existing == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + documentId, null);
		}
		try {
			// Use the path-parameter id; don't trust the one in the object
			document.setDocumentId(documentId);
			// Update the existing row
			documentRepository.save(document);
			Object result = new SuccessTransport(HttpServletResponse.SC_OK, null);
			logger.audit(beginDate, "updateDocument ID {}", documentId);
			return result;
		} catch (Exception ex) {
			// e.g., EmptyResultDataAccessException is NOT an internal server error
			Exception cve = findConstraintViolationException(ex);
			logger.warn(EELFLoggerDelegate.errorLogger, "updateDocument failed: {}", cve.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "updateDocument failed", cve);
		}
	}

	/**
	 * 
	 * @param documentId
	 *            Path parameter that identifies the instance
	 * @param response
	 *            HttpServletResponse
	 * @return Document that maps String to Object, for serialization as JSON
	 */
	@ApiOperation(value = "Deletes the document with the specified ID.", response = SuccessTransport.class)
	@RequestMapping(value = "/{documentId}", method = RequestMethod.DELETE)
	@ResponseBody
	public MLPTransportModel deleteDocument(@PathVariable("documentId") String documentId,
			HttpServletResponse response) {
		Date beginDate = new Date();
		try {
			documentRepository.delete(documentId);
			logger.audit(beginDate, "deleteDocument ID {}", documentId);
			return new SuccessTransport(HttpServletResponse.SC_OK, null);
		} catch (Exception ex) {
			// e.g., EmptyResultDataAccessException is NOT an internal server error
			logger.warn(EELFLoggerDelegate.errorLogger, "deleteDocument failed: {}", ex.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "deleteDocument failed", ex);
		}
	}

}

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
import org.acumos.cds.domain.MLPComment;
import org.acumos.cds.domain.MLPThread;
import org.acumos.cds.repository.CommentRepository;
import org.acumos.cds.repository.ThreadRepository;
import org.acumos.cds.repository.UserRepository;
import org.acumos.cds.transport.CountTransport;
import org.acumos.cds.transport.ErrorTransport;
import org.acumos.cds.transport.MLPTransportModel;
import org.acumos.cds.transport.SuccessTransport;
import org.acumos.cds.util.EELFLoggerDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.ApiOperation;

/**
 * Provides methods to create and delete threads.
 */
@Controller
@RequestMapping("/" + CCDSConstants.THREAD_PATH)
public class ThreadController extends AbstractController {

	private static final EELFLoggerDelegate logger = EELFLoggerDelegate.getLogger(MethodHandles.lookup().lookupClass());

	@Autowired
	private CommentRepository commentRepository;
	@Autowired
	private ThreadRepository threadRepository;
	@Autowired
	private UserRepository userRepository;

	/**
	 * @return Model that maps String to Object, for serialization as JSON
	 */
	@ApiOperation(value = "Gets the count of threads.", response = CountTransport.class)
	@RequestMapping(value = CCDSConstants.COUNT_PATH, method = RequestMethod.GET)
	@ResponseBody
	public CountTransport getThreadCount() {
		Date beginDate = new Date();
		Long count = threadRepository.count();
		logger.audit(beginDate, "getThreadCount");
		return new CountTransport(count);
	}

	/**
	 * @param pageable
	 *            Sort and page criteria
	 * @return Page of threads
	 */
	@ApiOperation(value = "Gets a page of threads, optionally sorted.", response = MLPThread.class, responseContainer = "Page")
	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	public Page<MLPThread> getThreads(Pageable pageable) {
		Date beginDate = new Date();
		Page<MLPThread> result = threadRepository.findAll(pageable);
		logger.audit(beginDate, "getThreads {}", pageable);
		return result;
	}

	/**
	 * @param solutionId
	 *            Solution ID
	 * @param revisionId
	 *            Revision ID
	 * @param pageable
	 *            Sort and page criteria
	 * @return Page of threads for specified solution and revision
	 */
	@ApiOperation(value = "Gets a page of threads for the solution and revision IDs, optionally sorted.", response = MLPThread.class, responseContainer = "Page")
	@RequestMapping(value = CCDSConstants.SOLUTION_PATH + "/{solutionId}/" + CCDSConstants.REVISION_PATH
			+ "/{revisionId}", method = RequestMethod.GET)
	@ResponseBody
	public Page<MLPThread> getSolutionRevisionThreads(@PathVariable("solutionId") String solutionId,
			@PathVariable("revisionId") String revisionId, Pageable pageable) {
		Date beginDate = new Date();
		Page<MLPThread> result = threadRepository.findBySolutionIdAndRevisionId(solutionId, revisionId, pageable);
		logger.audit(beginDate, "getSolutionRevisionThreads: solutionId {} revisionId {}", solutionId, revisionId);
		return result;
	}

	/**
	 * @param threadId
	 *            Path parameter with row ID
	 * @param response
	 *            HttpServletResponse
	 * @return A user if found, an error otherwise.
	 */
	@ApiOperation(value = "Gets the thread for the specified ID.", response = MLPThread.class)
	@RequestMapping(value = "{threadId}", method = RequestMethod.GET)
	@ResponseBody
	public Object getThread(@PathVariable("threadId") String threadId, HttpServletResponse response) {
		Date beginDate = new Date();
		MLPThread thread = threadRepository.findOne(threadId);
		if (thread == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + threadId, null);
		}
		logger.audit(beginDate, "getThread: threadId {}", threadId);
		return thread;
	}

	/**
	 * @param thread
	 *            Thread details
	 * @param response
	 *            HttpServletResponse
	 * @return MLPThread
	 */
	@ApiOperation(value = "Creates a thread.", response = MLPThread.class)
	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public Object createThread(@RequestBody MLPThread thread, HttpServletResponse response) {
		Date beginDate = new Date();
		try {
			String id = thread.getThreadId();
			if (id != null) {
				UUID.fromString(id);
				if (threadRepository.findOne(id) != null) {
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "ID exists: " + id);
				}
			}
			// Create a new row
			MLPThread newThread = threadRepository.save(thread);
			response.setStatus(HttpServletResponse.SC_CREATED);
			// This is a hack to create the location path.
			response.setHeader(HttpHeaders.LOCATION, CCDSConstants.THREAD_PATH + "/" + newThread.getThreadId());
			logger.audit(beginDate, "createThread: threadId {}", newThread.getThreadId());
			return newThread;
		} catch (Exception ex) {
			Exception cve = findConstraintViolationException(ex);
			logger.warn(EELFLoggerDelegate.errorLogger, "createThread", cve.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "createThread failed", cve);
		}
	}

	/**
	 * @param threadId
	 *            Path parameter with the row ID
	 * @param thread
	 *            data to be updated
	 * @param response
	 *            HttpServletResponse
	 * @return Status message
	 */
	@ApiOperation(value = "Updates a thread.", response = SuccessTransport.class)
	@RequestMapping(value = "{threadId}", method = RequestMethod.PUT)
	@ResponseBody
	public Object updateThread(@PathVariable("threadId") String threadId, @RequestBody MLPThread thread,
			HttpServletResponse response) {
		Date beginDate = new Date();
		// Get the existing one
		MLPThread existing = threadRepository.findOne(threadId);
		if (existing == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + threadId, null);
		}
		try {
			// Use the path-parameter id; don't trust the one in the object
			thread.setThreadId(threadId);
			threadRepository.save(thread);
			logger.audit(beginDate, "updateThread: threadId {}", threadId);
			return new SuccessTransport(HttpServletResponse.SC_OK, null);
		} catch (Exception ex) {
			Exception cve = findConstraintViolationException(ex);
			logger.warn(EELFLoggerDelegate.errorLogger, "updateThread", cve.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "updateThread failed", cve);
		}
	}

	/**
	 * @param threadId
	 *            Path parameter that identifies the instance
	 * @param response
	 *            HttpServletResponse
	 * @return Solution that maps String to Object, for serialization as JSON
	 */
	@ApiOperation(value = "Deletes a thread.", response = SuccessTransport.class)
	@RequestMapping(value = "{threadId}", method = RequestMethod.DELETE)
	@ResponseBody
	public MLPTransportModel deleteThread(@PathVariable("threadId") String threadId, HttpServletResponse response) {
		Date beginDate = new Date();
		try {
			// cascade the delete
			commentRepository.deleteByThreadId(threadId);
			threadRepository.delete(threadId);
			logger.audit(beginDate, "deleteThread: threadId {}", threadId);
			return new SuccessTransport(HttpServletResponse.SC_OK, null);
		} catch (Exception ex) {
			// e.g., EmptyResultDataAccessException is NOT an internal server error
			logger.warn(EELFLoggerDelegate.errorLogger, "deleteThread failed", ex.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "deleteThread failed", ex);
		}
	}

	/**
	 * @param threadId
	 *            Path parameter that identifies the instance
	 * @return Model that maps String to Object, for serialization as JSON
	 */
	@ApiOperation(value = "Gets the number of comments in the thread.", response = CountTransport.class)
	@RequestMapping(value = "{threadId}/" + CCDSConstants.COMMENT_PATH + "/"
			+ CCDSConstants.COUNT_PATH, method = RequestMethod.GET)
	@ResponseBody
	public CountTransport getCommentCount(@PathVariable("threadId") String threadId) {
		Date beginDate = new Date();
		Long count = commentRepository.countThreadComments(threadId);
		logger.audit(beginDate, "getCommentCount: threadId {}", threadId);
		return new CountTransport(count);
	}

	/**
	 * 
	 * @param threadId
	 *            Thread ID
	 * @param pageable
	 *            Sort and page criteria
	 * @param response
	 *            HttpServletResponse
	 * @return Page of comments
	 */
	@ApiOperation(value = "Gets a page of comments in the thread.", response = MLPComment.class, responseContainer = "Page")
	@RequestMapping(value = "{threadId}/" + CCDSConstants.COMMENT_PATH, method = RequestMethod.GET)
	@ResponseBody
	public Object getThreadComments(@PathVariable("threadId") String threadId, Pageable pageable,
			HttpServletResponse response) {
		Date beginDate = new Date();
		MLPThread thread = threadRepository.findOne(threadId);
		if (thread == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + threadId, null);
		}
		Object result = commentRepository.findByThreadId(threadId, pageable);
		logger.audit(beginDate, "getThreadComments: threadId {}", threadId);
		return result;
	}

	/**
	 * @param solutionId
	 *            Solution ID
	 * @param revisionId
	 *            Revision ID
	 * @param pageable
	 *            Sort and page criteria
	 * @return Page of comments for specified solution and revision, which may
	 *         include multiple threads
	 */
	@ApiOperation(value = "Gets a page of comments for the solution and revision IDs, optionally sorted.", response = MLPThread.class, responseContainer = "Page")
	@RequestMapping(value = CCDSConstants.SOLUTION_PATH + "/{solutionId}/" + CCDSConstants.REVISION_PATH
			+ "/{revisionId}/" + CCDSConstants.COMMENT_PATH, method = RequestMethod.GET)
	@ResponseBody
	public Page<MLPComment> getSolutionRevisionComments(@PathVariable("solutionId") String solutionId,
			@PathVariable("revisionId") String revisionId, Pageable pageable) {
		Date beginDate = new Date();
		Page<MLPComment> result = commentRepository.findBySolutionIdAndRevisionId(solutionId, revisionId, pageable);
		logger.audit(beginDate, "getSolutionRevisionComments: solutionId {} revisionId {}", solutionId, revisionId);
		return result;
	}

	/**
	 * @param threadId
	 *            Thread ID
	 * @param commentId
	 *            Path parameter with row ID
	 * @param response
	 *            HttpServletResponse
	 * @return A user if found, an error otherwise.
	 */
	@ApiOperation(value = "Gets the comment for the specified ID.", response = MLPComment.class)
	@RequestMapping(value = "{threadId}/" + CCDSConstants.COMMENT_PATH + "/{commentId}", method = RequestMethod.GET)
	@ResponseBody
	public Object getComment(@PathVariable("threadId") String threadId, @PathVariable("commentId") String commentId,
			HttpServletResponse response) {
		Date beginDate = new Date();
		MLPComment comment = commentRepository.findOne(commentId);
		if (comment == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + commentId, null);
		}
		logger.audit(beginDate, "getComment: threadId {} commentId {}", threadId, commentId);
		return comment;
	}

	/**
	 * @param threadId
	 *            Thread ID
	 * @param comment
	 *            Comment details
	 * @param response
	 *            HttpServletResponse
	 * @return MLPComment
	 */
	@ApiOperation(value = "Creates a comment.", response = MLPComment.class)
	@RequestMapping(value = "{threadId}/" + CCDSConstants.COMMENT_PATH, method = RequestMethod.POST)
	@ResponseBody
	public Object createComment(@PathVariable("threadId") String threadId, @RequestBody MLPComment comment,
			HttpServletResponse response) {
		Date beginDate = new Date();
		try {
			String id = comment.getCommentId();
			if (id != null) {
				UUID.fromString(id);
				if (commentRepository.findOne(id) != null) {
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "ID exists: " + id);
				}
			}
			if (comment.getParentId() != null && commentRepository.findOne(comment.getParentId()) == null) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + comment.getParentId());
			}
			if (threadRepository.findOne(comment.getThreadId()) == null) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + comment.getThreadId());
			}
			if (userRepository.findOne(comment.getUserId()) == null) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + comment.getUserId());
			}
			// Create a new row
			MLPComment newComment = commentRepository.save(comment);
			response.setStatus(HttpServletResponse.SC_CREATED);
			// This is a hack to create the location path.
			response.setHeader(HttpHeaders.LOCATION, CCDSConstants.COMMENT_PATH + "/" + newComment.getCommentId());
			logger.audit(beginDate, "createComment: threadId {} commentId {}", threadId, newComment.getCommentId());
			return newComment;
		} catch (Exception ex) {
			Exception cve = findConstraintViolationException(ex);
			logger.warn(EELFLoggerDelegate.errorLogger, "createComment", cve.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "createComment failed", cve);
		}
	}

	/**
	 * @param threadId
	 *            Thread ID. Not used, comment ID is sufficient.
	 * @param commentId
	 *            Path parameter with the comment ID
	 * @param comment
	 *            data to be updated
	 * @param response
	 *            HttpServletResponse
	 * @return Status message
	 */
	@ApiOperation(value = "Updates a comment.", response = SuccessTransport.class)
	@RequestMapping(value = "{threadId}/" + CCDSConstants.COMMENT_PATH + "/{commentId}", method = RequestMethod.PUT)
	@ResponseBody
	public Object updateComment(@PathVariable("threadId") String threadId, @PathVariable("commentId") String commentId,
			@RequestBody MLPComment comment, HttpServletResponse response) {
		Date beginDate = new Date();
		// Get the existing one
		MLPComment existing = commentRepository.findOne(commentId);
		if (existing == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + commentId, null);
		}
		if (comment.getParentId() != null && commentRepository.findOne(comment.getParentId()) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + comment.getParentId());
		}
		if (threadRepository.findOne(comment.getThreadId()) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + comment.getThreadId());
		}
		if (userRepository.findOne(comment.getUserId()) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, NO_ENTRY_WITH_ID + comment.getUserId());
		}
		try {
			// Use the path-parameter id; don't trust the one in the object
			comment.setCommentId(commentId);
			commentRepository.save(comment);
			logger.audit(beginDate, "updateComment: threadId {} commentId {}", threadId, commentId);
			return new SuccessTransport(HttpServletResponse.SC_OK, null);
		} catch (Exception ex) {
			Exception cve = findConstraintViolationException(ex);
			logger.warn(EELFLoggerDelegate.errorLogger, "updateComment", cve.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "updateComment failed", cve);
		}
	}

	/**
	 * @param threadId
	 *            Thread ID. Not used, comment ID is sufficient.
	 * @param commentId
	 *            Path parameter with the comment ID
	 * @param response
	 *            HttpServletResponse
	 * @return Solution that maps String to Object, for serialization as JSON
	 */
	@ApiOperation(value = "Deletes a comment.", response = SuccessTransport.class)
	@RequestMapping(value = "{threadId}/" + CCDSConstants.COMMENT_PATH + "/{commentId}", method = RequestMethod.DELETE)
	@ResponseBody
	public MLPTransportModel deleteComment(@PathVariable("threadId") String threadId,
			@PathVariable("commentId") String commentId, HttpServletResponse response) {
		Date beginDate = new Date();
		try {
			commentRepository.delete(commentId);
			logger.audit(beginDate, "deleteComment: threadId {} commentId {}", threadId, commentId);
			return new SuccessTransport(HttpServletResponse.SC_OK, null);
		} catch (Exception ex) {
			// e.g., EmptyResultDataAccessException is NOT an internal server error
			logger.warn(EELFLoggerDelegate.errorLogger, "deleteComment failed", ex.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ErrorTransport(HttpServletResponse.SC_BAD_REQUEST, "deleteComment failed", ex);
		}
	}
}

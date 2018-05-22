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

package org.acumos.cds.util;

import java.net.InetAddress;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.MDC;

import com.att.eelf.configuration.Configuration;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.att.eelf.configuration.SLF4jWrapper;

/**
 * Extends the EELF logger (which extends the SLF4j logger) so the output
 * includes the CLASS NAME. Example usage:
 * 
 * <pre>
 * private final EELFLoggerDelegate logger = EELFLoggerDelegate.getLogger(MyClass.class);
 * ..
 * void methodName() {
 *   try {
 *     String result = doWork();
 *     logger.debug(EELFLoggerDelegate.debugLogger, "methodName: result is {} ", result);
 *   }
 *   catch (Exception ex) {
 *     logger.error(EELFLoggerDelegate.errorLogger, "methodName failed", ex);
 *   }
 * }
 * </pre>
 *
 */
public class EELFLoggerDelegate extends SLF4jWrapper {

	public static final EELFLogger applicationLogger = EELFManager.getInstance().getApplicationLogger();
	public static final EELFLogger auditLogger = EELFManager.getInstance().getAuditLogger();
	public static final EELFLogger debugLogger = EELFManager.getInstance().getDebugLogger();
	public static final EELFLogger errorLogger = EELFManager.getInstance().getErrorLogger();
	public static final EELFLogger metricsLogger = EELFManager.getInstance().getMetricsLogger();

	/* This parameter must appear in the pattern defined in logback.xml */
	private static final String MDC_CLASS_NAME = "ClassName";
	private static final ConcurrentMap<String, EELFLoggerDelegate> classMap = new ConcurrentHashMap<>();
	private final String className;

	/**
	 * Gets a logger for the specified class.
	 * 
	 * @param className
	 *            Class name
	 */
	public EELFLoggerDelegate(final String className) {
		super(className);
		this.className = className;
	}

	/**
	 * Convenience method that gets a logger for the specified class.
	 * 
	 * @see #getLogger(String)
	 * 
	 * @param clazz
	 *            class
	 * @return Instance of EELFLoggerDelegate
	 */
	public static EELFLoggerDelegate getLogger(Class<?> clazz) {
		return getLogger(clazz.getName());
	}

	/**
	 * Gets a logger for the specified class name. If the logger does not already
	 * exist in the map, this creates a new logger.
	 * 
	 * @param className
	 *            If null or empty, uses EELFLoggerDelegate as the class name.
	 * @return Instance of EELFLoggerDelegate
	 */
	public static EELFLoggerDelegate getLogger(final String className) {
		// Silence a Sonar complaint about reusing parameter.
		String displayClassName = className;
		if (displayClassName == null || displayClassName.length() == 0)
			displayClassName = EELFLoggerDelegate.class.getName();
		EELFLoggerDelegate delegate = classMap.get(displayClassName);
		if (delegate == null) {
			delegate = new EELFLoggerDelegate(displayClassName);
			classMap.put(displayClassName, delegate);
		}
		return delegate;
	}

	/**
	 * Logs a message at the lowest level: trace.
	 * 
	 * @param logger
	 *            Logger to use
	 * @param msg
	 *            message to log
	 */
	public void trace(EELFLogger logger, String msg) {
		MDC.put(MDC_CLASS_NAME, className);
		logger.trace(msg);
		MDC.remove(MDC_CLASS_NAME);
	}

	/**
	 * Logs a message with parameters at the lowest level: trace.
	 * 
	 * @param logger
	 *            Logger to use
	 * @param msg
	 *            message to log
	 * @param arguments
	 *            arguments to interpolate into message
	 */
	public void trace(EELFLogger logger, String msg, Object... arguments) {
		MDC.put(MDC_CLASS_NAME, className);
		logger.trace(msg, arguments);
		MDC.remove(MDC_CLASS_NAME);
	}

	/**
	 * Logs a message and throwable at the lowest level: trace.
	 * 
	 * @param logger
	 *            Logger to use
	 * @param msg
	 *            message to log
	 * @param th
	 *            throwable to show as a stack trace
	 */
	public void trace(EELFLogger logger, String msg, Throwable th) {
		MDC.put(MDC_CLASS_NAME, className);
		logger.trace(msg, th);
		MDC.remove(MDC_CLASS_NAME);
	}

	/**
	 * Logs a message at the second-lowest level: debug.
	 * 
	 * @param logger
	 *            Logger to use
	 * @param msg
	 *            message to log
	 */
	public void debug(EELFLogger logger, String msg) {
		MDC.put(MDC_CLASS_NAME, className);
		logger.debug(msg);
		MDC.remove(MDC_CLASS_NAME);
	}

	/**
	 * Logs a message with parameters at the second-lowest level: debug.
	 * 
	 * @param logger
	 *            Logger to use
	 * @param msg
	 *            message to log
	 * @param arguments
	 *            arguments to interpolate into message
	 */
	public void debug(EELFLogger logger, String msg, Object... arguments) {
		MDC.put(MDC_CLASS_NAME, className);
		logger.debug(msg, arguments);
		MDC.remove(MDC_CLASS_NAME);
	}

	/**
	 * Logs a message and throwable at the second-lowest level: debug.
	 * 
	 * @param logger
	 *            Logger to use
	 * @param msg
	 *            message to log
	 * @param th
	 *            throwable to show as a stack trace
	 */
	public void debug(EELFLogger logger, String msg, Throwable th) {
		MDC.put(MDC_CLASS_NAME, className);
		logger.debug(msg, th);
		MDC.remove(MDC_CLASS_NAME);
	}

	/**
	 * Logs a message at info level.
	 * 
	 * @param logger
	 *            Logger to use
	 * @param msg
	 *            message to log
	 */
	public void info(EELFLogger logger, String msg) {
		MDC.put(MDC_CLASS_NAME, className);
		logger.info(msg);
		MDC.remove(MDC_CLASS_NAME);
	}

	/**
	 * Logs a message with parameters at info level.
	 *
	 * @param logger
	 *            Logger to use
	 * @param msg
	 *            message to log
	 * @param arguments
	 *            arguments to interpolate into message
	 */
	public void info(EELFLogger logger, String msg, Object... arguments) {
		MDC.put(MDC_CLASS_NAME, className);
		logger.info(msg, arguments);
		MDC.remove(MDC_CLASS_NAME);
	}

	/**
	 * Logs a message and throwable at info level.
	 * 
	 * @param logger
	 *            Logger to use
	 * @param msg
	 *            message to log
	 * @param th
	 *            throwable to show as a stack trace
	 */
	public void info(EELFLogger logger, String msg, Throwable th) {
		MDC.put(MDC_CLASS_NAME, className);
		logger.info(msg, th);
		MDC.remove(MDC_CLASS_NAME);
	}

	/**
	 * Logs a message at warn level.
	 * 
	 * @param logger
	 *            Logger to use
	 * @param msg
	 *            message to log
	 */
	public void warn(EELFLogger logger, String msg) {
		MDC.put(MDC_CLASS_NAME, className);
		logger.warn(msg);
		MDC.remove(MDC_CLASS_NAME);
	}

	/**
	 * Logs a message with parameters at warn level.
	 * 
	 * @param logger
	 *            Logger to use
	 * @param msg
	 *            message to log
	 * @param arguments
	 *            arguments to interpolate into message
	 */
	public void warn(EELFLogger logger, String msg, Object... arguments) {
		MDC.put(MDC_CLASS_NAME, className);
		logger.warn(msg, arguments);
		MDC.remove(MDC_CLASS_NAME);
	}

	/**
	 * Logs a message and throwable at warn level.
	 * 
	 * @param logger
	 *            Logger to use
	 * @param msg
	 *            message to log
	 * @param th
	 *            throwable to show as a stack trace
	 */
	public void warn(EELFLogger logger, String msg, Throwable th) {
		MDC.put(MDC_CLASS_NAME, className);
		logger.warn(msg, th);
		MDC.remove(MDC_CLASS_NAME);
	}

	/**
	 * Logs a message at error level.
	 * 
	 * @param logger
	 *            Logger to use
	 * @param msg
	 *            message to log
	 */
	public void error(EELFLogger logger, String msg) {
		MDC.put(MDC_CLASS_NAME, className);
		logger.error(msg);
		MDC.remove(MDC_CLASS_NAME);
	}

	/**
	 * Logs a message with parameters at error level.
	 * 
	 * @param logger
	 *            Logger to use
	 * @param msg
	 *            message to log
	 * @param arguments
	 *            arguments to interpolate into message
	 */
	public void error(EELFLogger logger, String msg, Object... arguments) {
		MDC.put(MDC_CLASS_NAME, className);
		logger.error(msg, arguments);
		MDC.remove(MDC_CLASS_NAME);
	}

	/**
	 * Logs a message and throwable at error level.
	 * 
	 * @param logger
	 *            Logger to use
	 * @param msg
	 *            message to log
	 * @param th
	 *            throwable to show as a stack trace
	 */
	public void error(EELFLogger logger, String msg, Throwable th) {
		MDC.put(MDC_CLASS_NAME, className);
		logger.error(msg, th);
		MDC.remove(MDC_CLASS_NAME);
	}

	/**
	 * Initializes the logger context and logs a message to all logs.
	 */
	public void init() {
		setGlobalLoggingContext();
		final String msg = "EELFLoggerDelegate initialized";
		info(applicationLogger, msg);
		info(auditLogger, msg);
		error(errorLogger, msg);
		debug(debugLogger, msg);
	}

	/**
	 * Puts server host name and numeric address into the MDC context for the CURRENT THREAD ONLY.
	 */
	public void setGlobalLoggingContext() {
		try {
			MDC.put(Configuration.MDC_SERVER_FQDN, InetAddress.getLocalHost().getHostName());
			MDC.put(Configuration.MDC_SERVER_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress());
		} catch (Exception e) {
			error(errorLogger, "setGlobalLoggingContext failed", e);
		}
	}

	/**
	 * Logs a message at level INFO to the audit logger. Sets the class name and
	 * date values using MDC parameters, then removes them after calling the logger.
	 * 
	 * Must convert the timestamp here because MDC requires String values.
	 * 
	 * @param beginTs
	 *            Instant when the operation began, logged as UTC in ISO8601 format;
	 *            required
	 * @param endTs
	 *            Instant when the operation ended, logged as UTC in ISO8601 format;
	 *            required
	 * @param message
	 *            Message to log; required
	 * @param arguments
	 *            Arguments to interpolate into message; optional
	 */
	public void audit(Instant beginTs, Instant endTs, String message,
			Object... arguments) {
		MDC.put(MDC_CLASS_NAME, className);
		MDC.put(Configuration.MDC_BEGIN_TIMESTAMP, beginTs.atZone(ZoneOffset.UTC).toString());
		MDC.put(Configuration.MDC_END_TIMESTAMP, endTs.atZone(ZoneOffset.UTC).toString());
		auditLogger.info(message, arguments);
		MDC.remove(MDC_CLASS_NAME);
		MDC.remove(Configuration.MDC_BEGIN_TIMESTAMP);
		MDC.remove(Configuration.MDC_END_TIMESTAMP);
	}

	/**
	 * Convenience method that calls
	 * {@link #audit(Instant, Instant, String, Object...)} with the
	 * current time for the endTs parameter, and nulls for the remote client info.
	 * 
	 * @param beginTs
	 *            Point in time when the operation began
	 * @param message
	 *            Message to log
	 * @param arguments
	 *            Arguments to interpolate into message (optional)
	 */
	public void audit(Instant beginTs, String message, Object... arguments) {
		audit(beginTs, Instant.now(), message, arguments);
	}

	/**
	 * Convenience method that converts Date to Instant then calls
	 * {@link #audit(Instant, Instant, String, Object...)} with the current time for the
	 * endTs parameter.
	 * 
	 * @param beginDate
	 *            Point in time when the operation began
	 * @param message
	 *            Message to log
	 * @param arguments
	 *            Arguments to interpolate into message (optional)
	 */
	public void audit(Date beginDate, String message, Object... arguments) {
		audit(Instant.ofEpochMilli(beginDate.getTime()), Instant.now(), message, arguments);
	}

}

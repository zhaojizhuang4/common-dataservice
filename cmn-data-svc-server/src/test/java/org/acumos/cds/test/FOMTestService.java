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

import java.util.List;

import javax.transaction.Transactional;

import org.acumos.cds.domain.MLPArtifact;
import org.acumos.cds.domain.MLPArtifactFOM;
import org.acumos.cds.domain.MLPSolution;
import org.acumos.cds.domain.MLPSolutionFOM;
import org.acumos.cds.domain.MLPSolutionRevision;
import org.acumos.cds.domain.MLPSolutionRevisionFOM;
import org.acumos.cds.util.EELFLoggerDelegate;
import org.hibernate.SessionFactory;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Provides a method to exercise the full object model classes.
 */
@Service("fomTestService")
@Transactional
public class FOMTestService {

	private final EELFLoggerDelegate logger = EELFLoggerDelegate.getLogger(FOMTestService.class);

	@Autowired
	private SessionFactory sessionFactory;

	/**
	 * Queries for solutions, revisions and artifacts using the plain and full
	 * object mapping classes. Results must match. If not, there is an annotation
	 * error.
	 */
	@SuppressWarnings("rawtypes")
	public void testFomAnnotations() {
		List sol = sessionFactory.getCurrentSession().createCriteria(MLPSolutionFOM.class).list();
		logger.info("Found sol fom count: {}", sol.size());
		List solPlain = sessionFactory.getCurrentSession().createCriteria(MLPSolution.class).list();
		logger.info("Found sol plain count: {}", solPlain.size());
		Assert.assertEquals("solutions", sol.size(), solPlain.size());

		List rev = sessionFactory.getCurrentSession().createCriteria(MLPSolutionRevisionFOM.class).list();
		logger.info("Found rev fom count: {}", rev.size());
		List revPlain = sessionFactory.getCurrentSession().createCriteria(MLPSolutionRevision.class).list();
		logger.info("Found rev plain count: {}", revPlain.size());
		Assert.assertEquals("revisions", rev.size(), revPlain.size());

		List art = sessionFactory.getCurrentSession().createCriteria(MLPArtifactFOM.class).list();
		logger.info("Found art fom count: {}", art.size());
		List artPlain = sessionFactory.getCurrentSession().createCriteria(MLPArtifact.class).list();
		logger.info("Found art plain count: {}", artPlain.size());
		Assert.assertEquals("artifact", art.size(), artPlain.size());
	}
}

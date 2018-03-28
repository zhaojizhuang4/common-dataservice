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

import java.util.Date;
import java.util.List;

import org.acumos.cds.domain.MLPArtifact;
import org.acumos.cds.domain.MLPSolRevArtMap;
import org.acumos.cds.domain.MLPSolution;
import org.acumos.cds.domain.MLPSolutionFOM;
import org.acumos.cds.domain.MLPSolutionRevision;
import org.acumos.cds.domain.MLPUser;
import org.acumos.cds.repository.ArtifactRepository;
import org.acumos.cds.repository.SolRevArtMapRepository;
import org.acumos.cds.repository.SolutionFOMRepository;
import org.acumos.cds.repository.SolutionRepository;
import org.acumos.cds.repository.SolutionRevisionRepository;
import org.acumos.cds.repository.UserRepository;
import org.acumos.cds.service.SolutionSearchService;
import org.acumos.cds.util.EELFLoggerDelegate;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Tests the domain models that have complex mappings.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class FOMRepositoryTest {

	private final static EELFLoggerDelegate logger = EELFLoggerDelegate.getLogger(FOMRepositoryTest.class);

	@Autowired
	private ArtifactRepository artifactRepository;
	@Autowired
	private SolutionRevisionRepository revisionRepository;
	@Autowired
	private SolutionRepository solutionRepository;
	@Autowired
	private SolutionFOMRepository solutionFOMRepository;
	@Autowired
	private SolRevArtMapRepository solRevArtMapRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private SolutionSearchService solutionSearchService;

	@Test
	public void testEntities() throws Exception {

		// Can query existing items in db
		boolean setupTeardown = true;

		MLPUser cu = null;
		MLPSolution cs = null;
		MLPSolutionRevision cr = null;
		MLPArtifact ca = null;
		MLPSolRevArtMap map = null;

		if (setupTeardown) {
			// Create entities for query

			final String loginName = "user_" + Long.toString(new Date().getTime());
			cu = new MLPUser(loginName, "entitytestuser@abc.com", true);
			cu = userRepository.save(cu);
			Assert.assertNotNull(cu.getUserId());
			logger.info("Created user {}", cu);

			cs = new MLPSolution("sol name", cu.getUserId(), true);
			cs = solutionRepository.save(cs);
			Assert.assertNotNull("Solution ID", cs.getSolutionId());

			cr = new MLPSolutionRevision(cs.getSolutionId(), "version", cu.getUserId(), "PR", "NV");
			cr = revisionRepository.save(cr);
			Assert.assertNotNull("Revision ID", cr.getRevisionId());
			logger.info("Created solution revision {}", cr.getRevisionId());

			ca = new MLPArtifact("version", "BP", "name", "uri", cu.getUserId(), 1);
			ca = artifactRepository.save(ca);
			Assert.assertNotNull(ca.getArtifactId());
			logger.info("Created artifact {}", ca);

			map = new MLPSolRevArtMap(cr.getRevisionId(), ca.getArtifactId());
			solRevArtMapRepository.save(map);
		}

		// Find all via Spring repository
		logger.info("Querying for FOM via repo findAll method");
		List<MLPSolutionFOM> foms = solutionFOMRepository.findAll();
		Assert.assertTrue(foms != null && foms.size() > 0);
		logger.info("Found FOM row count {}", foms.size());

		// Find by modified date

		String[] accTypes = new String[] { "PR" };
		String[] valCodes = new String[] { "NV" };
		Date modifiedDate = new Date();
		modifiedDate.setTime(modifiedDate.getTime() - 60 * 1000);

		// Via Hibernate constraint
		logger.info("Querying for FOM via search service");
		Page<MLPSolution> solsByDate = solutionSearchService.findSolutionsByModifiedDate(true, accTypes, valCodes,
				modifiedDate, new PageRequest(0, 6, null));
		Assert.assertTrue(solsByDate != null && solsByDate.getNumberOfElements() > 0);
		logger.info("Found sols by date via criteria: size {}", solsByDate.getContent().size());

		if (setupTeardown) {
			// Clean up
			solRevArtMapRepository.delete(map);
			artifactRepository.delete(ca);
			revisionRepository.delete(cr);
			solutionRepository.delete(cs);
			userRepository.delete(cu);
		}
	}

}

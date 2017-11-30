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

package org.acumos.cds.client.test;

import org.acumos.cds.query.SearchCriteria;
import org.acumos.cds.query.SearchCriterion;
import org.acumos.cds.query.SearchOperation;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SearchCriteriaTest extends AbstractModelTest {

	private static Logger logger = LoggerFactory.getLogger(SearchCriteriaTest.class);

	@Test
	public void testSearchCriterion() {
		SearchCriterion sc = new SearchCriterion("a", SearchOperation.NOT_EQUALS, "b");
		sc.setOrPredicate(true);
		sc.setKey(s1);
		sc.setOperation(SearchOperation.EQUALS);
		sc.setValue(s2);
		Assert.assertTrue(sc.isOrPredicate());
		Assert.assertEquals(s1, sc.getKey());
		Assert.assertEquals(SearchOperation.EQUALS, sc.getOperation());
		Assert.assertEquals(s2, sc.getValue());

		final String cond1 = "a:b";
		final String cond2 = "|c>d";
		SearchCriterion sc1 = new SearchCriterion(cond1);
		logger.info("Sc1: " + sc1.toString());
		SearchCriterion sc2 = new SearchCriterion(cond2);
		logger.info("Sc2: " + sc2.toString());
		final String cond3 = "e_f,g";
		SearchCriterion sc3 = new SearchCriterion(cond3);
		logger.info("Sc3: " + sc3.toString());
		try {
			new SearchCriterion("");
		} catch (IllegalArgumentException ex) {
			logger.info("Caught expected exception on empty");
		}
		try {
			new SearchCriterion("x", SearchOperation.IN, "y");
		} catch (IllegalArgumentException ex) {
			logger.info("Caught expected exception on IN without array");
		}

		SearchCriteria sOr = new SearchCriteria(sc).or(sc2);
		logger.info(sOr.toString());
		SearchCriteria sa = new SearchCriteria("a:b;e_f,g");
		String asString = sa.toString();
		logger.info("As string: " + asString);
		SearchCriteria parsed = new SearchCriteria(asString);
		logger.info("Parsed: " + parsed);
		Assert.assertEquals(asString, parsed.toString());
	}

}

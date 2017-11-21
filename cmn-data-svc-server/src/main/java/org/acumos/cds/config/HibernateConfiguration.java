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

package org.acumos.cds.config;

import javax.sql.DataSource;

import org.acumos.cds.domain.MLPSolution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Provides a Hibernate session factory.
 */
@org.springframework.context.annotation.Configuration
public class HibernateConfiguration {

	@Autowired
	private DataSource dataSource;

	/**
	 * Sets up the data source and package to scan for annotated classes.
	 * 
	 * @return LocalSessionFactoryBean
	 */
	@Bean
	public LocalSessionFactoryBean sessionFactory() {
		LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
		sessionFactory.setDataSource(dataSource);
		// The package is maintained in the client library.
		// This avoids hard-coding a package name as string
		sessionFactory.setPackagesToScan(new String[] { MLPSolution.class.getPackage().getName() });
		return sessionFactory;
	}

	/**
	 * @return new JpaTransactionManager
	 */
	@Bean
	public PlatformTransactionManager transactionManager() {
		return new JpaTransactionManager();
	}

}

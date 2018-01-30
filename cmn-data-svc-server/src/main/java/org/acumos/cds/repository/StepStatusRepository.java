package org.acumos.cds.repository;

import org.acumos.cds.domain.MLPStepStatus;
import org.springframework.data.repository.Repository;

public interface StepStatusRepository extends Repository<MLPStepStatus, String> {
	/**
	 * Returns all instances of the type.
	 * 
	 * @return all entities
	 */
	Iterable<MLPStepStatus> findAll();
}

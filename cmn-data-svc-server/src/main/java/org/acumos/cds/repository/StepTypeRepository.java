package org.acumos.cds.repository;

import org.acumos.cds.domain.MLPStepType;
import org.springframework.data.repository.Repository;

public interface StepTypeRepository extends Repository<MLPStepType, String> {
	/**
	 * Returns all instances of the type.
	 * 
	 * @return all entities
	 */
	Iterable<MLPStepType> findAll();

}

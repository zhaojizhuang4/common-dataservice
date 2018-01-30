package org.acumos.cds.domain;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "C_STEP_STATUS")
public class MLPStepStatus extends MLPStatusCodeEntity implements Serializable {

	private static final long serialVersionUID = -8342728048884890038L;

}

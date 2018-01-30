package org.acumos.cds.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.CreationTimestamp;

/**
 * Model for a step result
 */
@Entity
@Table(name = "C_STEP_RESULT")
public class MLPStepResult implements MLPEntity, Serializable {

	private static final long serialVersionUID = -595148641870461125L;

	// Hibernate is weak on the ID column generator, the method is specific to
	// the backing database. For portability, specify AUTO and define the column
	// appropriately in the database, which in MySQL requires "AUTO_INCREMENT".
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "ID", updatable = false, nullable = false, columnDefinition = "INT")
	private Long stepResultId;

	@Column(name = "TRACKING_ID", updatable = false, columnDefinition = "CHAR(36)")
	@Size(max = 36)
	private String trackingId;

	@Column(name = "STEP_CD", nullable = false, columnDefinition = "CHAR(2)")
	@NotNull(message = "StepCode cannot be null")
	@Size(max = 2)
	private String stepCode;

	@Column(name = "SOLUTION_ID", columnDefinition = "CHAR(36)")
	@Size(max = 36)
	private String solutionId;

	@Column(name = "REVISION_ID", columnDefinition = "CHAR(36)")
	@Size(max = 36)
	private String revisionId;

	@Column(name = "ARTIFACT_ID", columnDefinition = "CHAR(36)")
	@Size(max = 36)
	private String artifactId;

	@Column(name = "USER_ID", columnDefinition = "CHAR(36)")
	@Size(max = 36)
	private String userId;

	@Column(name = "NAME", nullable = false, columnDefinition = "VARCHAR(100)")
	@NotNull(message = "Step name cannot be null")
	@Size(max = 100)
	private String name;

	@Column(name = "STATUS_CD", nullable = false, columnDefinition = "CHAR(2)")
	@NotNull(message = "StatusCode cannot be null")
	@Size(max = 2)
	private String statusCode;

	@Column(name = "RESULT", columnDefinition = "VARCHAR(512)")
	@Size(max = 512)
	private String result;

	@CreationTimestamp
	@Column(name = "START_DATE", nullable = false, updatable = false)
	private Date startDate;

	@CreationTimestamp
	@Column(name = "END_DATE", updatable = false)
	private Date endDate;

	/**
	 * No-arg constructor
	 */
	public MLPStepResult() {
		// no-arg constructor
		startDate = new Date();
	}

	/**
	 * This constructor accepts the required fields; i.e., the minimum that the user
	 * must supply to create a valid instance.
	 * 
	 * @param stepCode
	 *            Step Code
	 * @param name
	 *            Step type
	 * @param statusCode
	 *            Status Code
	 * @param startDate
	 *            Start Date
	 */
	public MLPStepResult(String stepCode, String name, String statusCode, Date startDate) {
		if (stepCode == null || name == null || statusCode == null || startDate == null)
			throw new IllegalArgumentException("Null not permitted");
		this.stepCode = stepCode;
		this.name = name;
		this.statusCode = statusCode;
		this.startDate = startDate;
	}

	public Long getStepResultId() {
		return stepResultId;
	}

	public void setStepResultId(Long stepResultId) {
		this.stepResultId = stepResultId;
	}

	public String getTrackingId() {
		return trackingId;
	}

	public void setTrackingId(String trackingId) {
		this.trackingId = trackingId;
	}

	public String getStepCode() {
		return stepCode;
	}

	public void setStepCode(String stepCode) {
		this.stepCode = stepCode;
	}

	public String getSolutionId() {
		return solutionId;
	}

	public void setSolutionId(String solutionId) {
		this.solutionId = solutionId;
	}

	public String getRevisionId() {
		return revisionId;
	}

	public void setRevisionId(String revisionId) {
		this.revisionId = revisionId;
	}

	public String getArtifactId() {
		return artifactId;
	}

	public void setArtifactId(String artifactId) {
		this.artifactId = artifactId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	@Override
	public boolean equals(Object that) {
		if (that == null)
			return false;
		if (!(that instanceof MLPStepResult))
			return false;
		MLPStepResult thatObj = (MLPStepResult) that;
		return Objects.equals(stepResultId, thatObj.stepResultId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(stepResultId);
	}

	@Override
	public String toString() {
		return this.getClass().getName() + "[stepResultId=" + stepResultId + ", trackingId=" + trackingId + ", "
				+ "stepCode=" + stepCode + "solutionId=" + solutionId + "artifactId=" + artifactId + ", userId="
				+ userId + "name=" + name + "statusCode=" + statusCode + "result=" + result + ", startDate=" + startDate
				+ ", endDate=" + endDate + "]";
	}

}

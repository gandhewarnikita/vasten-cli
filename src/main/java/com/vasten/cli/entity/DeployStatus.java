package com.vasten.cli.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "deploystatus")
public class DeployStatus {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;

	@ManyToOne
	@JoinColumn(name = "deployment_id")
	private Deployments deploymentId;

	@Column(name = "deployment_type")
	@Enumerated(EnumType.STRING)
	private DeploymentType type;

	@Column(name = "deployment_status")
	@Enumerated(EnumType.STRING)
	private DeploymentStatus status;

	@Column(name = "deployment_type_name")
	private String deploymentTypeName;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public DeploymentStatus getStatus() {
		return status;
	}

	public void setStatus(DeploymentStatus status) {
		this.status = status;
	}

	public Deployments getDeploymentId() {
		return deploymentId;
	}

	public void setDeploymentId(Deployments deploymentId) {
		this.deploymentId = deploymentId;
	}

	public DeploymentType getType() {
		return type;
	}

	public void setType(DeploymentType type) {
		this.type = type;
	}

	public String getDeploymentTypeName() {
		return deploymentTypeName;
	}

	public void setDeploymentTypeName(String deploymentTypeName) {
		this.deploymentTypeName = deploymentTypeName;
	}

	@Override
	public String toString() {
		return "DeployStatus [id=" + id + ", deploymentId=" + deploymentId + ", type=" + type + ", status=" + status
				+ ", deploymentTypeName=" + deploymentTypeName + "]";
	}

}

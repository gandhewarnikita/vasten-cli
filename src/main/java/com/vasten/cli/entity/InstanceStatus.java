package com.vasten.cli.entity;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

public class InstanceStatus {

	private String instanceName;
	
	@Enumerated(EnumType.STRING)
	private DeploymentStatus instanceStatus;
	
	public String getInstanceName() {
		return instanceName;
	}

	public void setInstanceName(String instanceName) {
		this.instanceName = instanceName;
	}

	public DeploymentStatus getInstanceStatus() {
		return instanceStatus;
	}

	public void setInstanceStatus(DeploymentStatus instanceStatus) {
		this.instanceStatus = instanceStatus;
	}
}

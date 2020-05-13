package com.vasten.cli.entity;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

public class ClusterStatus {

	private String name;
	
	@Enumerated(EnumType.STRING)
	private DeploymentStatus status;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public DeploymentStatus getStatus() {
		return status;
	}

	public void setStatus(DeploymentStatus status) {
		this.status = status;
	}
}

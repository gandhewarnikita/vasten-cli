package com.vasten.cli.entity;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

public class NfsStatus {

	private String nfsName;
	
	@Enumerated(EnumType.STRING)
	private DeploymentStatus nfsStatus;
	
	public String getNfsName() {
		return nfsName;
	}

	public void setNfsName(String nfsName) {
		this.nfsName = nfsName;
	}

	public DeploymentStatus getNfsStatus() {
		return nfsStatus;
	}

	public void setNfsStatus(DeploymentStatus nfsStatus) {
		this.nfsStatus = nfsStatus;
	}
}

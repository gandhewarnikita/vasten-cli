package com.vasten.cli.entity;

/**
 * Enum for Deployment type
 * 
 * @author scriptuit
 *
 */
public enum DeploymentType {

	CLUSTER("CLUSTER"),INSTANCE("INSTANCE"), NFS("NFS");
	
	private String status;

	DeploymentType(String status) {
		this.setStatus(status);
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}

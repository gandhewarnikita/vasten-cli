package com.vasten.cli.entity;

/**
 * Enum for Deployment status
 * 
 * @author scriptuit
 *
 */
public enum DeploymentStatus {

	PENDING("PENDING"), SUCCESS("SUCCESS"), ERROR("ERROR"), PROVISIONING("PROVISIONING");
	
	private String status;

	DeploymentStatus(String status) {
		this.setStatus(status);
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}

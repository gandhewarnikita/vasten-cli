package com.vasten.cli.entity;

public enum DeploymentStatus {

	PENDING("PENDING"), SUCCESS("SUCCESS"), ERROR("ERROR");
	
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

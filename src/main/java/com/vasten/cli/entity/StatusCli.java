package com.vasten.cli.entity;

/**
 * Class for storing Deployment status data
 * 
 * @author scriptuit
 *
 */
public class StatusCli {

	private String type;
	private String status;
	private String deploymentTypeName;
	private String externalIp;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDeploymentTypeName() {
		return deploymentTypeName;
	}

	public void setDeploymentTypeName(String deploymentTypeName) {
		this.deploymentTypeName = deploymentTypeName;
	}

	public String getExternalIp() {
		return externalIp;
	}

	public void setExternalIp(String externalIp) {
		this.externalIp = externalIp;
	}

	@Override
	public String toString() {
		return "StatusCli [type=" + type + ", status=" + status + ", deploymentTypeName=" + deploymentTypeName
				+ ", externalIp=" + externalIp + "]";
	}

}

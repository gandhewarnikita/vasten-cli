package com.vasten.cli.entity;

public class MountFileStore {

	private String deploymentName;
	private String nfsHost;
	private String nfsPath;

	public MountFileStore() {
	}

	public MountFileStore(String deploymentName, String nfsHost, String nfsPath) {
		this.deploymentName = deploymentName;
		this.nfsHost = nfsHost;
		this.nfsPath = nfsPath;
	}

	public String getDeploymentName() {
		return deploymentName;
	}

	public void setDeploymentName(String deploymentName) {
		this.deploymentName = deploymentName;
	}

	public String getNfsHost() {
		return nfsHost;
	}

	public void setNfsHost(String nfsHost) {
		this.nfsHost = nfsHost;
	}

	public String getNfsPath() {
		return nfsPath;
	}

	public void setNfsPath(String nfsPath) {
		this.nfsPath = nfsPath;
	}

	@Override
	public String toString() {
		return "MountFileStore [deploymentName=" + deploymentName + ", nfsHost=" + nfsHost + ", nfsPath=" + nfsPath
				+ "]";
	}

}

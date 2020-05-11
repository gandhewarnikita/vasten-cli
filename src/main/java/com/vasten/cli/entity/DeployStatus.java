package com.vasten.cli.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "deploystatus")
public class DeployStatus {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;

	@Column(name = "deployment_name")
	private String name;

	@Column(name = "deployment_status")
	@Enumerated(EnumType.STRING)
	private DeploymentStatus status;

	@Column(name = "instance_name")
	private String instanceName;

	@Column(name = "instance_status")
	@Enumerated(EnumType.STRING)
	private DeploymentStatus instanceStatus;

	@Column(name = "nfs_name")
	private String nfsName;

	@Column(name = "nfs_status")
	@Enumerated(EnumType.STRING)
	private DeploymentStatus nfsStatus;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

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

	@Override
	public String toString() {
		return "DeployStatus [id=" + id + ", name=" + name + ", status=" + status + ", instanceName=" + instanceName
				+ ", instanceStatus=" + instanceStatus + ", nfsName=" + nfsName + ", nfsStatus=" + nfsStatus + "]";
	}

}

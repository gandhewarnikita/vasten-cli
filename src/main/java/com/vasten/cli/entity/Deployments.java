package com.vasten.cli.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "deployments")
public class Deployments {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;

	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;

	@Column(name = "name")
	private String name;

	@Column(name = "status")
	private DeploymentStatus status;

	@Column(name = "prefix")
	private String prefix;

	@Column(name = "is_deleted")
	private boolean isDeleted;

	@Column(name = "property_file")
	private String fileName;

	@Transient
	private Integer clusterNodes;

	@Transient
	private String clusterMachineType;

	@Transient
	private Integer clusterMachineCores;

	// Validate minimum 30 capacity and maximum 1024 capacity
	@Transient
	private Integer clusterLocalStoreCapacity;

	@Transient
	private String toolVersion;

	// validate minimum 1024 capacity and maximum 3072 capacity
	@Transient
	private Integer nfsCapacity;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
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

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public boolean isDeleted() {
		return isDeleted;
	}

	public void setDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public Integer getClusterNodes() {
		return clusterNodes;
	}

	public void setClusterNodes(Integer clusterNodes) {
		this.clusterNodes = clusterNodes;
	}

	public String getClusterMachineType() {
		return clusterMachineType;
	}

	public void setClusterMachineType(String clusterMachineType) {
		this.clusterMachineType = clusterMachineType;
	}

	public Integer getClusterMachineCores() {
		return clusterMachineCores;
	}

	public void setClusterMachineCores(Integer clusterMachineCores) {
		this.clusterMachineCores = clusterMachineCores;
	}

	public Integer getClusterLocalStoreCapacity() {
		return clusterLocalStoreCapacity;
	}

	public void setClusterLocalStoreCapacity(Integer clusterLocalStoreCapacity) {
		this.clusterLocalStoreCapacity = clusterLocalStoreCapacity;
	}

	public String getToolVersion() {
		return toolVersion;
	}

	public void setToolVersion(String toolVersion) {
		this.toolVersion = toolVersion;
	}

	public Integer getNfsCapacity() {
		return nfsCapacity;
	}

	public void setNfsCapacity(Integer nfsCapacity) {
		this.nfsCapacity = nfsCapacity;
	}

	@Override
	public String toString() {
		return "Deployments [id=" + id + ", user=" + user + ", name=" + name + ", status=" + status + ", prefix="
				+ prefix + ", isDeleted=" + isDeleted + ", fileName=" + fileName + ", clusterNodes=" + clusterNodes
				+ ", clusterMachineType=" + clusterMachineType + ", clusterMachineCores=" + clusterMachineCores
				+ ", clusterLocalStoreCapacity=" + clusterLocalStoreCapacity + ", toolVersion=" + toolVersion
				+ ", nfsCapacity=" + nfsCapacity + "]";
	}

}

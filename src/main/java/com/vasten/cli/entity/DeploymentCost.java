package com.vasten.cli.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Class for Deployment cost data
 * 
 * @author scriptuit
 *
 */
@Entity
@Table(name = "deployment_cost")
public class DeploymentCost {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;

	@Column(name = "start_date")
	private Date startDate;

	@Column(name = "end_date")
	private Date endDate;

	@Column(name = "deployment_name")
	private String deploymentName;

	@Column(name = "nfs_name")
	private String nfsName;

	@Column(name = "cluster_cost")
	private Double clusterCost;

	@Column(name = "nfs_cost")
	private Double nfsCost;

	@Column(name = "total_cost")
	private Double totalCost;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public String getDeploymentName() {
		return deploymentName;
	}

	public void setDeploymentName(String deploymentName) {
		this.deploymentName = deploymentName;
	}

	public String getNfsName() {
		return nfsName;
	}

	public void setNfsName(String nfsName) {
		this.nfsName = nfsName;
	}

	public Double getClusterCost() {
		return clusterCost;
	}

	public void setClusterCost(Double clusterCost) {
		this.clusterCost = clusterCost;
	}

	public Double getNfsCost() {
		return nfsCost;
	}

	public void setNfsCost(Double nfsCost) {
		this.nfsCost = nfsCost;
	}

	public Double getTotalCost() {
		return totalCost;
	}

	public void setTotalCost(Double totalCost) {
		this.totalCost = totalCost;
	}

	@Override
	public String toString() {
		return "DeploymentCost [id=" + id + ", startDate=" + startDate + ", endDate=" + endDate + ", deploymentName="
				+ deploymentName + ", nfsName=" + nfsName + ", clusterCost=" + clusterCost + ", nfsCost=" + nfsCost
				+ ", totalCost=" + totalCost + "]";
	}

}

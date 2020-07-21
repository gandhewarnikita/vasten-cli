package com.vasten.cli.entity;

import java.time.LocalDate;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
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

	@OneToOne
	@JoinColumn(name = "deployment_id")
	private Deployments deploymentId;

	@Column(name = "deployment_type")
	@Enumerated(EnumType.STRING)
	private DeploymentType type;

	@Column(name = "compute_cost")
	private Double computeCost;

	@Column(name = "network_cost")
	private Double networkCost;

	@Column(name = "storage_cost")
	private Double storageCost;

	@Column(name = "cost_last_updated")
	private Date costLastUpdated;

	@Column(name = "deployment_type_name")
	private String deploymentTypeName;

	@Column(name = "usage_data_cost")
	private LocalDate usageDataCost;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Deployments getDeploymentId() {
		return deploymentId;
	}

	public void setDeploymentId(Deployments deploymentId) {
		this.deploymentId = deploymentId;
	}

	public DeploymentType getType() {
		return type;
	}

	public void setType(DeploymentType type) {
		this.type = type;
	}

	public Double getComputeCost() {
		return computeCost;
	}

	public void setComputeCost(Double computeCost) {
		this.computeCost = computeCost;
	}

	public Double getNetworkCost() {
		return networkCost;
	}

	public void setNetworkCost(Double networkCost) {
		this.networkCost = networkCost;
	}

	public Double getStorageCost() {
		return storageCost;
	}

	public void setStorageCost(Double storageCost) {
		this.storageCost = storageCost;
	}

	public Date getCostLastUpdated() {
		return costLastUpdated;
	}

	public void setCostLastUpdated(Date costLastUpdated) {
		this.costLastUpdated = costLastUpdated;
	}

	public String getDeploymentTypeName() {
		return deploymentTypeName;
	}

	public void setDeploymentTypeName(String deploymentTypeName) {
		this.deploymentTypeName = deploymentTypeName;
	}

	public LocalDate getUsageDataCost() {
		return usageDataCost;
	}

	public void setUsageDataCost(LocalDate usageDataCost) {
		this.usageDataCost = usageDataCost;
	}

	@Override
	public String toString() {
		return "DeploymentCost [id=" + id + ", deploymentId=" + deploymentId + ", type=" + type + ", computeCost="
				+ computeCost + ", networkCost=" + networkCost + ", storageCost=" + storageCost + ", costLastUpdated="
				+ costLastUpdated + ", deploymentTypeName=" + deploymentTypeName + ", usageDataCost=" + usageDataCost
				+ "]";
	}

}

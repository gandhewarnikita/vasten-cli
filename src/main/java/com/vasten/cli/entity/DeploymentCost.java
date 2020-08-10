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

	@ManyToOne
	@JoinColumn(name = "deployment_id")
	private Deployments deploymentId;

	@Column(name = "total_cost")
	private Double totalCost;

	@Column(name = "cost_last_updated")
	private Date costLastUpdated;

	@Column(name = "deployment_type_name")
	private String deploymentTypeName;

	@Column(name = "usage_date")
	private LocalDate usageDate;

	@ManyToOne
	@JoinColumn(name = "client_name")
	private Clients clientName;

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

	public Double getTotalCost() {
		return totalCost;
	}

	public void setTotalCost(Double totalCost) {
		this.totalCost = totalCost;
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

	public LocalDate getUsageDate() {
		return usageDate;
	}

	public void setUsageDate(LocalDate usageDate) {
		this.usageDate = usageDate;
	}

	public Clients getClientName() {
		return clientName;
	}

	public void setClientName(Clients clientName) {
		this.clientName = clientName;
	}

	@Override
	public String toString() {
		return "DeploymentCost [id=" + id + ", deploymentId=" + deploymentId + ", totalCost=" + totalCost
				+ ", costLastUpdated=" + costLastUpdated + ", deploymentTypeName=" + deploymentTypeName + ", usageDate="
				+ usageDate + ", clientName=" + clientName + "]";
	}

}

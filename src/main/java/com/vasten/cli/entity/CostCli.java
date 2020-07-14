package com.vasten.cli.entity;

import java.util.Date;

public class CostCli {

	private String type;
	private Double computeCost;
//	private Double networkCost;
//	private Double storageCost;
	private Date costLastUpdated;
	private Double totalCost;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Double getComputeCost() {
		return computeCost;
	}

	public void setComputeCost(Double computeCost) {
		this.computeCost = computeCost;
	}

//	public Double getNetworkCost() {
//		return networkCost;
//	}
//
//	public void setNetworkCost(Double networkCost) {
//		this.networkCost = networkCost;
//	}
//
//	public Double getStorageCost() {
//		return storageCost;
//	}
//
//	public void setStorageCost(Double storageCost) {
//		this.storageCost = storageCost;
//	}

	public Date getCostLastUpdated() {
		return costLastUpdated;
	}

	public void setCostLastUpdated(Date costLastUpdated) {
		this.costLastUpdated = costLastUpdated;
	}

	public Double getTotalCost() {
		return totalCost;
	}

	public void setTotalCost(Double totalCost) {
		this.totalCost = totalCost;
	}

//	@Override
//	public String toString() {
//		return "CostCli [type=" + type + ", computeCost=" + computeCost + ", networkCost=" + networkCost
//				+ ", storageCost=" + storageCost + ", costLastUpdated=" + costLastUpdated + ", totalCost=" + totalCost
//				+ "]";
//	}

	@Override
	public String toString() {
		return "CostCli [type=" + type + ", computeCost=" + computeCost + ", totalCost=" + totalCost
				+ ", costLastUpdated=" + costLastUpdated + ", totalCost=" + totalCost + "]";
	}

}

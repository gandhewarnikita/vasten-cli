package com.vasten.cli.entity;

import java.util.Date;

public class ClientCostDetails {

	private Double totalCost;
	private Date costLastUpdated;

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

	@Override
	public String toString() {
		return "ClientCostDetails [totalCost=" + totalCost + ", costLastUpdated=" + costLastUpdated + "]";
	}

}

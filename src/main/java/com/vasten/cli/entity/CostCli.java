package com.vasten.cli.entity;

import java.util.Date;

public class CostCli {
	private Date costLastUpdated;
	private Double totalCost;

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

	@Override
	public String toString() {
		return "CostCli [costLastUpdated=" + costLastUpdated + ", totalCost=" + totalCost + "]";
	}

}

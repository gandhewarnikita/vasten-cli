package com.vasten.cli.service;

import java.util.List;

import com.vasten.cli.entity.DeployStatus;
import com.vasten.cli.entity.Deployments;

public interface DeploymentsService {

	public Deployments createDeployment(int id, Deployments provisionData);

	public List<Deployments> getAll(int id, String name);

	public DeployStatus getStatus(Integer id, String name);

	public float getCost(String name);

	public void deProvision(Integer id, String name);

}

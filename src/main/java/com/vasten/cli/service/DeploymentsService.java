package com.vasten.cli.service;

import java.util.List;

import com.vasten.cli.entity.Deployments;

public interface DeploymentsService {

	public Deployments createDeployment(int id, Deployments provisionData);

	public List<Deployments> getAll(int clientId, String name);

	public Deployments getStatus(String name);

	public float getCost(String name);

}

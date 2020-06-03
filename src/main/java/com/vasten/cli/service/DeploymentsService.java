package com.vasten.cli.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import com.vasten.cli.entity.DeployStatus;
import com.vasten.cli.entity.Deployments;

public interface DeploymentsService {

	public Deployments createDeployment(int id, Deployments provisionData);

	public List<Deployments> getAll(int id, String name);

	public List<DeployStatus> getStatus(int deploymentId);

	public float getCost(int deploymentId) throws FileNotFoundException, IOException;

	public void deProvision(Integer id, Integer deploymentId);

	public void mountNfs(String deploymentName);

}

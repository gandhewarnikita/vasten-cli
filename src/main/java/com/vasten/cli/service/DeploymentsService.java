package com.vasten.cli.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import com.vasten.cli.entity.DeployStatus;
import com.vasten.cli.entity.Deployments;

public interface DeploymentsService {

	public Deployments createDeployment(int id, Deployments provisionData);

	public List<Deployments> getAll(int id, String name);

	public DeployStatus getStatus(String name);

	public float getCost(String name, Long startDate, Long endDate) throws FileNotFoundException, IOException;

	public void deProvision(Integer id, String name);

}

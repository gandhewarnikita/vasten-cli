package com.vasten.cli.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.vasten.cli.entity.CostCli;
import com.vasten.cli.entity.DeploymentCost;
import com.vasten.cli.entity.Deployments;
import com.vasten.cli.entity.StatusCli;

/**
 * Service interface for Deployment related activities
 * 
 * @author scriptuit
 *
 */
public interface DeploymentsService {

	/**
	 * Create deployment for a user
	 * 
	 * @param id
	 * @param provisionData
	 * @return
	 */
	public Deployments createDeployment(int id, Deployments provisionData);

	/**
	 * Get all deployments of a user
	 * 
	 * @param id
	 * @param name
	 * @return
	 */
	public List<Deployments> getAll(int id, String name);

	/**
	 * Get status of cluster, instances and file store of a deployment
	 * 
	 * @param deploymentName
	 * @return
	 */
	public Map<String, List<StatusCli>> getStatus(String deploymentName);

	/**
	 * Get cost of a deployment
	 * 
	 * @param deploymentName
	 * @param startDate 
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public Map<String, CostCli> getCost(String deploymentName, Long startDate) throws FileNotFoundException, IOException;

	/**
	 * Delete a deployment of user
	 * 
	 * @param id
	 * @param deploymentName
	 */
	public void deProvision(Integer userId, String deploymentName);

	/**
	 * Mount external file store for a user
	 * 
	 * @param userId
	 * 
	 * @param deploymentName
	 */
	public void mountNfs(Integer userId, String deploymentName);

	/**
	 * Delete external file store of a user
	 * 
	 * @param userId
	 * @param deploymentName
	 */
	public void deProvisionRemote(Integer userId, String deploymentName);

	/**
	 * Run tool for a deployment
	 * 
	 * @param id
	 * @param deploymentName
	 */
	public void runTool(Integer id, String deploymentName);

}

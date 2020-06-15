package com.vasten.cli.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.vasten.cli.entity.DeployStatus;
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
	 * Get status of cluster, instances and nfs status for a deployment
	 * 
	 * @param deploymentId
	 * @param userId
	 * @return
	 */
	public Map<String, List<StatusCli>> getStatus(int deploymentId, int userId);

	/**
	 * Get cost of deployment
	 * 
	 * @param deploymentId
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public float getCost(int deploymentId) throws FileNotFoundException, IOException;

	/**
	 * Delete a deployment of user
	 * 
	 * @param id
	 * @param deploymentId
	 */
	public void deProvision(Integer userId, Integer deploymentId);

	/**
	 * Mount external file store
	 * 
	 * @param userId
	 * 
	 * @param deploymentName
	 */
	public void mountNfs(Integer userId, String deploymentName);

	public void deProvisionRemote(Integer userId, String deploymentName);

}

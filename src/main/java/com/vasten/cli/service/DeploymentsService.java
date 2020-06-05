package com.vasten.cli.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import com.vasten.cli.entity.DeployStatus;
import com.vasten.cli.entity.Deployments;

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
	public List<DeployStatus> getStatus(int deploymentId, int userId);

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
	public void deProvision(Integer id, Integer deploymentId);

	/**
	 * MOunt external file store 
	 * 
	 * @param deploymentName
	 */
	public void mountNfs(String deploymentName);

}

package com.vasten.cli.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import javax.websocket.server.PathParam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vasten.cli.entity.Clients;
import com.vasten.cli.entity.DeployStatus;
import com.vasten.cli.entity.Deployments;
import com.vasten.cli.entity.User;
import com.vasten.cli.security.config.SecurityUtil;
import com.vasten.cli.service.DeploymentsService;

@RestController
@RequestMapping(value = "/api")
public class DeploymentsController {

	private static final Logger LOGGER = LoggerFactory.getLogger(DeploymentsController.class);

	@Autowired
	private DeploymentsService deploymentsService;

	@Autowired
	private SecurityUtil securityUtil;

	/**
	 * Create deployment of client
	 * 
	 * @param provisionData
	 * @return
	 */
	@RequestMapping(value = "/provision", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public Deployments create(@RequestBody Deployments provisionData) {
		LOGGER.info("Api receives to create deployments");

		User user = securityUtil.getLoggedInUser();

		Deployments newDeployment = deploymentsService.createDeployment(user.getId(), provisionData);

		return newDeployment;
	}

	/**
	 * Get all deployments of client
	 * 
	 * @param clientId
	 * @return
	 */
	@RequestMapping(value = "/profile", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<Deployments> getAllDeployments(@RequestParam(value = "name", required = false) String name) {
		LOGGER.info("Api received to get all deployments of user");

		User user = securityUtil.getLoggedInUser();

		List<Deployments> deploymentList = deploymentsService.getAll(user.getId(), name);

		return deploymentList;
	}

	/**
	 * Get status of a deployment
	 * 
	 * @param name
	 * @return
	 */
	@RequestMapping(value = "/status/deploymentId/{deploymentId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<DeployStatus> getStatus(@PathVariable int deploymentId) {
		LOGGER.info("Api received to get status of deployment");
		List<DeployStatus> deploymentStatus = deploymentsService.getStatus(deploymentId);
		return deploymentStatus;
	}

	/**
	 * Get cost of a deployment
	 * 
	 * @param name
	 * @return
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	@RequestMapping(value = "/cost/startDate/{startDate}/enddate/{endDate}/name/{name}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public float getCost(@PathVariable String name, @PathVariable Long startDate, @PathVariable Long endDate) throws FileNotFoundException, IOException {
		LOGGER.info("Api received to get cost of deployment between start date and end date");
		float deploymentCost = deploymentsService.getCost(name, startDate, endDate);
		return deploymentCost;
	}
	
	/**
	 * Delete deployment of user
	 * 
	 * @param name
	 */
	@RequestMapping(value = "/name/{name}", method = RequestMethod.DELETE)
	public void deProvision(@PathVariable String name) {
		LOGGER.info("Api received to delete deployment");
		
		User user = securityUtil.getLoggedInUser();
		
		deploymentsService.deProvision(user.getId(), name);
	}
}

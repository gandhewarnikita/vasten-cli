package com.vasten.cli.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

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
import com.vasten.cli.entity.StatusCli;
import com.vasten.cli.entity.User;
import com.vasten.cli.security.config.SecurityUtil;
import com.vasten.cli.service.DeploymentsService;

/**
 * Controller class for Deployment
 * 
 * @author scriptuit
 *
 */
@RestController
@RequestMapping(value = "/api")
public class DeploymentsController {

	private static final Logger LOGGER = LoggerFactory.getLogger(DeploymentsController.class);

	@Autowired
	private DeploymentsService deploymentsService;

	@Autowired
	private SecurityUtil securityUtil;

	/**
	 * Create deployment of user
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
	 * Get all deployments of user
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
	public Map<String, List<StatusCli>> getStatus(@PathVariable int deploymentId) {
		LOGGER.info("Api received to get status of deployment");
		User user = securityUtil.getLoggedInUser();
		Map<String, List<StatusCli>> deploymentStatus = deploymentsService.getStatus(user.getId(), deploymentId);
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
	@RequestMapping(value = "/cost/deploymentId/{deploymentId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public float getCost(@PathVariable int deploymentId) throws FileNotFoundException, IOException {
		LOGGER.info("Api received to get cost of deployment between start date and end date");
		float deploymentCost = deploymentsService.getCost(deploymentId);
		return deploymentCost;
	}

	/**
	 * Delete deployment of user
	 * 
	 * @param name
	 */
	@RequestMapping(value = "/deploymentId/{deploymentId}", method = RequestMethod.DELETE)
	public void deProvision(@PathVariable Integer deploymentId) {
		LOGGER.info("Api received to delete deployment");

		User user = securityUtil.getLoggedInUser();

		deploymentsService.deProvision(user.getId(), deploymentId);
	}

	/**
	 * Mount external filestore
	 * 
	 * @param deploymentName
	 */
	@RequestMapping(value = "/mount/deploymentName/{deploymentName}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public void mountNfs(@PathVariable String deploymentName) {
		LOGGER.info("Api received to mount nfs");
		User user = securityUtil.getLoggedInUser();
		deploymentsService.mountNfs(user.getId(), deploymentName);
	}

	@RequestMapping(value = "/deleteMount/deploymentName/{deploymentname}", method = RequestMethod.DELETE)
	public void deProvisionRemote(@PathVariable String deploymentName) {
		LOGGER.info("Api received to delete mounted nfs");
		User user = securityUtil.getLoggedInUser();
		deploymentsService.deProvisionRemote(user.getId(), deploymentName);
	}
}

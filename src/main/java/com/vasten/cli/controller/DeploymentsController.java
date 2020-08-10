package com.vasten.cli.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.annotation.security.RolesAllowed;

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

import com.vasten.cli.entity.ClientCostDetails;
import com.vasten.cli.entity.CostCli;
import com.vasten.cli.entity.DeploymentCost;
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
	 * Create deployment for a user
	 * 
	 * @param provisionData
	 * @return
	 */
	@RolesAllowed({ "ROLE_USER", "ROLE_ADMIN" })
	@RequestMapping(value = "/provision", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public Deployments create(@RequestBody Deployments provisionData) {
		LOGGER.info("Api receives to create deployments");

		User user = securityUtil.getLoggedInUser();

		Deployments newDeployment = deploymentsService.createDeployment(user.getId(), provisionData);

		return newDeployment;
	}

	/**
	 * Get all deployments of a user
	 * 
	 * @param clientId
	 * @return
	 */
	@RolesAllowed({ "ROLE_USER", "ROLE_ADMIN" })
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
	@RolesAllowed({ "ROLE_USER", "ROLE_ADMIN" })
	@RequestMapping(value = "/status/deploymentName/{deploymentName}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<String, List<StatusCli>> getStatus(@PathVariable String deploymentName) {
		LOGGER.info("Api received to get status of deployment");
		User user = securityUtil.getLoggedInUser();
		Map<String, List<StatusCli>> deploymentStatus = deploymentsService.getStatus(user.getId(), deploymentName);
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
	@RolesAllowed({ "ROLE_USER", "ROLE_ADMIN" })
	@RequestMapping(value = "/cost/deploymentName/{deploymentName}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<String, CostCli> getCost(@PathVariable String deploymentName,
			@RequestParam(value = "startDate", required = false) String startDate)
			throws FileNotFoundException, IOException {

		LOGGER.info("Api received to get cost of deployment");
		User user = securityUtil.getLoggedInUser();
		Map<String, CostCli> deploymentCostList = deploymentsService.getCost(user.getId(), deploymentName, startDate);
		return deploymentCostList;
	}

	/**
	 * Delete deployment of a user
	 * 
	 * @param name
	 */
	@RolesAllowed({ "ROLE_USER", "ROLE_ADMIN" })
	@RequestMapping(value = "/deploymentName/{deploymentName}", method = RequestMethod.DELETE)
	public void deProvision(@PathVariable String deploymentName) {
		LOGGER.info("Api received to delete deployment");

		User user = securityUtil.getLoggedInUser();

		deploymentsService.deProvision(user.getId(), deploymentName);
	}

	/**
	 * Mount external file store for a user
	 * 
	 * @param deploymentName
	 */
	@RolesAllowed({ "ROLE_USER", "ROLE_ADMIN" })
	@RequestMapping(value = "/mount/deploymentName/{deploymentName}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public void mountNfs(@PathVariable String deploymentName) {
		LOGGER.info("Api received to mount nfs");
		User user = securityUtil.getLoggedInUser();
		deploymentsService.mountNfs(user.getId(), deploymentName);
	}

	/**
	 * Delete external file store of a user
	 * 
	 * @param deploymentName
	 */
	@RolesAllowed({ "ROLE_USER", "ROLE_ADMIN" })
	@RequestMapping(value = "/deleteMount/deploymentName/{deploymentname}", method = RequestMethod.DELETE)
	public void deProvisionRemote(@PathVariable String deploymentName) {
		LOGGER.info("Api received to delete mounted nfs");
		User user = securityUtil.getLoggedInUser();
		deploymentsService.deProvisionRemote(user.getId(), deploymentName);
	}

	/**
	 * Run tool for a deployment
	 * 
	 * @param deploymentName
	 */
	@RolesAllowed({ "ROLE_USER", "ROLE_ADMIN" })
	@RequestMapping(value = "/run/deploymentName/{deploymentName}/filename/{filename}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public void runTool(@PathVariable String deploymentName,
			@RequestParam(value = "clusternodes", required = false) Integer clusternodes,
			@RequestParam(value = "iplist", required = false) List<String> iplist, @PathVariable String filename) {

		LOGGER.info("Api received to run tool");
		User user = securityUtil.getLoggedInUser();
		deploymentsService.runTool(user.getId(), deploymentName, clusternodes, iplist, filename);
	}

//	@RolesAllowed("ROLE_USER")
	@RolesAllowed({ "ROLE_CLIENT_ADMIN", "ROLE_ADMIN" })
	@RequestMapping(value = "/getcost/clientName/{clientName}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<String, ClientCostDetails> getCostClient(@PathVariable String clientName,
			@RequestParam(value = "startDate", required = false) String startDate) {

		LOGGER.info("Api received to get total cost of all deployments of all users of a client");
		User user = securityUtil.getLoggedInUser();
		Map<String, ClientCostDetails> costClientList = deploymentsService.getClientCost(user.getId(), clientName,
				startDate);
		return costClientList;

	}
}

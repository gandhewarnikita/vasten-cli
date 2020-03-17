package com.vasten.cli.controller;

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

import com.vasten.cli.entity.Deployments;
import com.vasten.cli.service.DeploymentsService;

@RestController
@RequestMapping(value = "/api")
public class DeploymentsController {

	private static final Logger LOGGER = LoggerFactory.getLogger(DeploymentsController.class);
	
	@Autowired
	private DeploymentsService deploymentsService;
	
	/**
	 * Create deployment of client
	 * 
	 * @param provisionData
	 * @return
	 */
	@RequestMapping(value = "/provision", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public Deployments create(@RequestBody Deployments provisionData) {
		LOGGER.info("Api receives to create deployments");
		
		Deployments newDeployment = deploymentsService.createDeployment(provisionData);
		
		return newDeployment;
	}
	
	/**
	 * Get all deployments of client
	 * 
	 * @param clientId
	 * @return
	 */
	@RequestMapping(value = "/profile/clientId/{clientId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<Deployments> getAllDeployments(@PathVariable int clientId, @RequestParam(value="name", required=false) String name){
		LOGGER.info("Api received to get all deployments of client");
		
		List<Deployments> deploymentList = deploymentsService.getAll(clientId, name);
		
		return deploymentList;
	}
}

package com.vasten.cli.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.vasten.cli.entity.Clients;
import com.vasten.cli.security.config.SecurityUtil;
import com.vasten.cli.service.ClientsService;

@RestController
public class ClientsController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ClientsController.class);
	
	@Autowired
	private SecurityUtil securityUtil;
	
	@Autowired
	private ClientsService clientsService;

	@RequestMapping(value = "/loggedIn", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Clients getLoggedInUser() {
		return securityUtil.getLoggedInUser();
	}
	
	@RequestMapping(value = "/api/client", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public Clients createClient(@RequestBody Clients clientData) {
		LOGGER.info("Api received to create new client");
		Clients newClient = clientsService.createClient(clientData);
		return newClient;
	}
}

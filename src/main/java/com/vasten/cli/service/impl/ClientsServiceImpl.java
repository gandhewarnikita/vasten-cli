package com.vasten.cli.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vasten.cli.entity.Clients;
import com.vasten.cli.repository.ClientsRepository;
import com.vasten.cli.service.ClientsService;
import com.vasten.cli.utility.ValidationUtility;

@Service
public class ClientsServiceImpl implements ClientsService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ClientsServiceImpl.class);

	@Autowired
	private ClientsRepository clientsRepository;
	
	@Autowired
	private ValidationUtility validationUtility;

	@Override
	public Clients createClient(Clients clientData) {
		LOGGER.info("Creating new client");

		validationUtility.validateClientData(clientData);

		Clients newClient = new Clients();
		
		newClient.setName(clientData.getName());
		
		clientsRepository.save(newClient);

		return newClient;
	}
}

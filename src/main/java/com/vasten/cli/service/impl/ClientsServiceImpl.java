package com.vasten.cli.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vasten.cli.entity.Clients;
import com.vasten.cli.repository.ClientsRepository;
import com.vasten.cli.service.ClientsService;
import com.vasten.cli.utility.ValidationUtility;

/**
 * Service implementation class for Client related activities
 * 
 * @author scriptuit
 *
 */
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

		Integer id = 1;

		newClient.setName(clientData.getName());

		List<Clients> clientList = new ArrayList<Clients>();
		clientList = clientsRepository.findAll();

		if (clientList == null || clientList.isEmpty()) {
			newClient.setId(id);

		} else {

			Integer index = clientList.size() - 1;
			Clients dbClient = clientList.get(index);
			Integer dbClientId = dbClient.getId();
			Integer newId = dbClientId + 1;

			newClient.setId(newId);
		}

		clientsRepository.save(newClient);

		return newClient;
	}
}

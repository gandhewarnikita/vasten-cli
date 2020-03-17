package com.vasten.cli.service.impl;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.vasten.cli.entity.Clients;
import com.vasten.cli.error.ValidationError;
import com.vasten.cli.exception.CliBadRequestException;
import com.vasten.cli.repository.ClientsRepository;
import com.vasten.cli.service.ClientsService;

@Service
public class ClientsServiceImpl implements ClientsService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ClientsServiceImpl.class);

	@Autowired
	private ClientsRepository clientsRepository;

	@Override
	public Clients createClient(Clients clientData) {
		LOGGER.info("Creating new client");

		validateClientData(clientData);

		Clients newClient = new Clients();

		newClient.setCreatedDate(new Date());
		newClient.setUpdatedDate(new Date());
		newClient.setEmail(clientData.getEmail());
		newClient.setName(clientData.getName());

		BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
		String password = bCryptPasswordEncoder.encode(clientData.getPassword());

		newClient.setPassword(password);

		clientsRepository.save(newClient);

		return newClient;
	}

	private void validateClientData(Clients clientData) {

		List<ValidationError> validationErrorList = new ArrayList<ValidationError>();

		if (clientData.getEmail() == null || clientData.getEmail().isEmpty()) {
			LOGGER.error("Invalid email");
			validationErrorList.add(new ValidationError("email", "Email is mandatory"));
		}

		if (clientData.getPassword() == null || clientData.getPassword().isEmpty()) {
			LOGGER.error("Invalid password");
			validationErrorList.add(new ValidationError("password", "Password is mandatory"));
		}

		if (validationErrorList != null && !validationErrorList.isEmpty()) {
			throw new CliBadRequestException("Bad Request", validationErrorList);
		}

	}
}

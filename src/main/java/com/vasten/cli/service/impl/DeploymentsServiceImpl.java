package com.vasten.cli.service.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vasten.cli.entity.Clients;
import com.vasten.cli.entity.DeploymentStatus;
import com.vasten.cli.entity.Deployments;
import com.vasten.cli.error.ValidationError;
import com.vasten.cli.exception.CliBadRequestException;
import com.vasten.cli.repository.ClientsRepository;
import com.vasten.cli.repository.DeploymentsRepository;
import com.vasten.cli.service.DeploymentsService;
import com.vasten.cli.utility.ValidationUtility;

@Service
public class DeploymentsServiceImpl implements DeploymentsService {

	private static final Logger LOGGER = LoggerFactory.getLogger(DeploymentsServiceImpl.class);

	@Autowired
	private DeploymentsRepository deploymentsRepository;

	@Autowired
	private ValidationUtility validationUtility;

	@Autowired
	private ClientsRepository clientsRepository;

	@Override
	public Deployments createDeployment(Deployments provisionData) {
		LOGGER.info("Creating deployment");

		validationUtility.validateDeploymentData(provisionData);

		int clientId = provisionData.getClients().getId();

		Deployments newDeployment = new Deployments();

		newDeployment.setClients(provisionData.getClients());
		newDeployment.setName(provisionData.getName());
		newDeployment.setStatus(DeploymentStatus.PENDING);

		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SS");
		Calendar calobj = Calendar.getInstance();

		String dateTime = df.format(calobj.getTime());

		UUID uuid = UUID.randomUUID();

		String prefix = clientId + "_" + uuid + "_" + dateTime;
		LOGGER.info("prefix : " + prefix);

		newDeployment.setPrefix(prefix);

		return deploymentsRepository.save(newDeployment);
	}

	@Override
	public List<Deployments> getAll(int clientId, String name) {
		LOGGER.info("Getting all deployments");

		List<Deployments> deploymentList = new ArrayList<Deployments>();
		List<ValidationError> validationErrorList = new ArrayList<ValidationError>();

		validationUtility.validateClientId(clientId);

		Clients dbClient = clientsRepository.findOneById(clientId);

		if (name == null) {

			deploymentList = deploymentsRepository.findAllByClients(dbClient);
			return deploymentList;
			
		} else {

			Deployments dbDeployments = deploymentsRepository.findByName(name);

			if (dbDeployments == null) {
				LOGGER.error("Deployment with this name does not exist");
				validationErrorList.add(new ValidationError("name", "Deployment with this name does not exist"));
				
			} else {
				Deployments dbDeployment = deploymentsRepository.findByClientsAndName(dbClient, name);
				deploymentList.add(dbDeployment);
				return deploymentList;
			}
			
			if (validationErrorList != null && !validationErrorList.isEmpty()) {
				throw new CliBadRequestException("Bad Request", validationErrorList);
			}
		}
		return deploymentList;

	}

}

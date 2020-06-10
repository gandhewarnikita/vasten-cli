package com.vasten.cli.utility;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.vasten.cli.entity.Clients;
import com.vasten.cli.entity.DeployStatus;
import com.vasten.cli.entity.Deployments;
import com.vasten.cli.entity.User;
import com.vasten.cli.error.ValidationError;
import com.vasten.cli.exception.CliBadRequestException;
import com.vasten.cli.repository.ClientsRepository;
import com.vasten.cli.repository.DeployStatusRepository;
import com.vasten.cli.repository.DeploymentsRepository;
import com.vasten.cli.repository.UserRepository;

@Component
public class ValidationUtility {

	private static final Logger LOGGER = LoggerFactory.getLogger(ValidationUtility.class);

	@Autowired
	private ClientsRepository clientsRepository;

	@Autowired
	private DeploymentsRepository deploymentsRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private DeployStatusRepository deployStatusRepository;

	public void validateDeploymentData(int id, Deployments provisionData) {
		List<ValidationError> validationErrorList = new ArrayList<ValidationError>();

		User dbUser = userRepository.findOneById(id);

		if (dbUser == null) {
			LOGGER.error("User does not exist");
			validationErrorList.add(new ValidationError("user", "User does not exist"));
		}

		if (provisionData.getName() == null || provisionData.getName().isEmpty()) {
			LOGGER.info("Deployment name is mandatory");
			validationErrorList.add(new ValidationError("name", "Deployment name is mandatory"));

		} else if (provisionData.getName().length() > 8) {
			LOGGER.info("Deployment name should not be greater than 8 characters");
			validationErrorList
					.add(new ValidationError("name", "Deployment name should not be greater than 8 characters"));

		} else {

			String deploymentName = provisionData.getName().toLowerCase();

			Deployments dbDeployment = deploymentsRepository.findByName(deploymentName);

			if (dbDeployment != null) {
				LOGGER.error("Deployment with this name already exists");
				validationErrorList.add(new ValidationError("name", "Deployment with this name already exists"));
			}
		}

		if (provisionData.getClusterNodes() == null || provisionData.getClusterNodes() < 0) {
			LOGGER.error("Cluster node is mandatory");
			validationErrorList.add(new ValidationError("clusterNode", "Cluster node is mandatory"));
		}

		if (provisionData.getToolVersion() == null || provisionData.getToolVersion().isEmpty()) {
			LOGGER.error("Tool version is mandatory");
			validationErrorList.add(new ValidationError("toolVserion", "Tool version is mandatory"));

		}

		if (provisionData.getClusterMachineType() == null || provisionData.getClusterMachineType().isEmpty()) {
			LOGGER.error("Cluster machine type is mandatory");
			validationErrorList.add(new ValidationError("clusterMachineType", "Cluster machine type is mandatory"));

		} else if (!provisionData.getClusterMachineType().equals("n1-standard")) {
			LOGGER.error("Cluster machine type does not contain default value");
			validationErrorList.add(
					new ValidationError("clusterMachineType", "Cluster machine type does not contain default value"));
		}

		if (provisionData.getClusterLocalStoreCapacity() == null) {
			LOGGER.error("Cluster local store capacity is mandatory");
			validationErrorList
					.add(new ValidationError("clusterLocalStoreCapacity", "Cluster local store capacity is mandatory"));

		} else if ((provisionData.getClusterLocalStoreCapacity() < 30)
				|| (provisionData.getClusterLocalStoreCapacity() > 1024)) {

			LOGGER.error("Cluster local store capacity is not in range min 30 to max 1024");
			validationErrorList.add(new ValidationError("clusterLocalStoreCapacity",
					"Cluster local store capacity is not in range min 30 to max 1024"));
		}

		if (provisionData.getNfsCapacity() == null) {
			LOGGER.error("Nfs capacity is mandatory");
			validationErrorList.add(new ValidationError("nfscapacity", "Nfs capacity is mandatory"));

		} else if ((provisionData.getNfsCapacity() < 1024) || (provisionData.getNfsCapacity() > 3072)) {
			LOGGER.error("Nfs capacity is not in range min 1024 to max 3072");
			validationErrorList
					.add(new ValidationError("nfscapacity", "Nfs capacity is not in range min 1024 to max 3072"));
		}

//		if (provisionData.getToolName() == null || provisionData.getToolName().isEmpty()) {
//			LOGGER.info("Tool name is mandatory");
//			validationErrorList.add(new ValidationError("toolName", "Tool name is mandatory"));
//
//		} else if (!provisionData.getToolName().equalsIgnoreCase("vasten")) {
//			LOGGER.info("Tool name should be same as project name");
//			validationErrorList.add(new ValidationError("toolName", "Tool name should be same as project name"));
//		}

		if (validationErrorList != null && !validationErrorList.isEmpty()) {
			throw new CliBadRequestException("Bad Request", validationErrorList);
		}

	}

	public void validateClientId(int clientId) {
		List<ValidationError> validationErrorList = new ArrayList<ValidationError>();

		Clients dbClient = clientsRepository.findOneById(clientId);

		if (dbClient == null) {
			LOGGER.error("Client does not exist");
			validationErrorList.add(new ValidationError("client", "Client does not exist"));
		}

		if (validationErrorList != null && !validationErrorList.isEmpty()) {
			throw new CliBadRequestException("Bad Request", validationErrorList);
		}

	}

	public void validateDeployment(Integer userId, Integer deploymentId) {
		List<ValidationError> validationErrorList = new ArrayList<ValidationError>();

		User dbUser = userRepository.findOneById(userId);
		Deployments dbDeployment = deploymentsRepository.findByUserAndIdAndIsDeletedFalse(dbUser, deploymentId);

		if (dbDeployment == null) {
			LOGGER.error("Deployment does not exist with this name");
			validationErrorList.add(new ValidationError("name", "Deployment does not exist with this name"));
		}

		if (validationErrorList != null && !validationErrorList.isEmpty()) {
			throw new CliBadRequestException("Bad Request", validationErrorList);
		}

	}

	public void validateClientData(Clients clientData) {
		List<ValidationError> validationErrorList = new ArrayList<ValidationError>();

		if (clientData.getName() == null || clientData.getName().isEmpty()) {
			LOGGER.error("Client name is mandatory");
			validationErrorList.add(new ValidationError("name", "Client name is mandatory"));
		} else {
			Clients dbClient = clientsRepository.findByName(clientData.getName());

			if (dbClient != null) {
				LOGGER.error("Client already exists");
				validationErrorList.add(new ValidationError("name", "Client already exists"));
			}
		}

		if (validationErrorList != null && !validationErrorList.isEmpty()) {
			throw new CliBadRequestException("Bad Request", validationErrorList);
		}

	}

	public void validateDeploymentId(int deploymentId) {
		List<ValidationError> validationErrorList = new ArrayList<ValidationError>();

		Deployments dbDeployment = deploymentsRepository.findOneByIdAndIsDeletedFalse(deploymentId);

		if (dbDeployment == null) {
			LOGGER.error("Deployment does not exist with this name");
			validationErrorList.add(new ValidationError("name", "Deployment does not exist with this name"));
		}

		if (validationErrorList != null && !validationErrorList.isEmpty()) {
			throw new CliBadRequestException("Bad Request", validationErrorList);
		}

	}

	public void validateDeploymentName(String deploymentName) {
		List<ValidationError> validationErrorList = new ArrayList<ValidationError>();

		Deployments dbDeployment = deploymentsRepository.findByNameAndIsDeletedFalse(deploymentName);

		if (dbDeployment == null) {
			LOGGER.error("Deployment does not exist with this name");
			validationErrorList.add(new ValidationError("name", "Deployment does not exist with this name"));
		}

		if (validationErrorList != null && !validationErrorList.isEmpty()) {
			throw new CliBadRequestException("Bad Request", validationErrorList);
		}

	}

//	public void validateClusterName(String name) {
//		List<ValidationError> validationErrorList = new ArrayList<ValidationError>();
//
//		DeployStatus dbDeployment = deployStatusRepository.findByName(name);
//
//		if (dbDeployment == null) {
//			LOGGER.error("Deployment does not exist with this name");
//			validationErrorList.add(new ValidationError("name", "Deployment does not exist with this name"));
//		}
//
//		if (validationErrorList != null && !validationErrorList.isEmpty()) {
//			throw new CliBadRequestException("Bad Request", validationErrorList);
//		}
//
//	}

}

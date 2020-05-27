package com.vasten.cli.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.api.services.cloudbilling.Cloudbilling;
import com.google.api.services.cloudbilling.model.BillingAccount;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.billing.v1.CloudBillingClient;
import com.google.cloud.billing.v1.CloudBillingSettings;
import com.google.cloud.billing.v1.CloudCatalogClient;
import com.google.cloud.billing.v1.CloudCatalogClient.ListServicesPagedResponse;
import com.google.cloud.billing.v1.CloudCatalogSettings;
import com.google.common.collect.Lists;
import com.vasten.cli.entity.Clients;
import com.vasten.cli.entity.DeployStatus;
import com.vasten.cli.entity.DeploymentStatus;
import com.vasten.cli.entity.Deployments;
import com.vasten.cli.entity.User;
import com.vasten.cli.error.ValidationError;
import com.vasten.cli.exception.CliBadRequestException;
import com.vasten.cli.repository.ClientsRepository;
import com.vasten.cli.repository.DeployStatusRepository;
import com.vasten.cli.repository.DeploymentsRepository;
import com.vasten.cli.repository.UserRepository;
import com.vasten.cli.service.DeploymentsService;
import com.vasten.cli.utility.ValidationUtility;

@Service
public class DeploymentsServiceImpl implements DeploymentsService {

	private static final Logger LOGGER = LoggerFactory.getLogger(DeploymentsServiceImpl.class);

	@Value("${VARS_FILE_PATH}")
	public String varsFilePath;

	@Value("${OUTPUT_FILE_PATH}")
	public String outputFilePath;

	@Value("${APPLY_SHELL_PATH}")
	public String applyShellPath;

	@Value("${DESTROY_SHELL_PATH}")
	public String destroyShellPath;

	@Autowired
	private DeployStatusRepository deployStatusRepository;

	private static final String billingUrl = "https://cloudbilling.googleapis.com/v1/";

	@Autowired
	private DeploymentsRepository deploymentsRepository;

	@Autowired
	private ValidationUtility validationUtility;

	@Autowired
	private ClientsRepository clientsRepository;

	@Autowired
	private UserRepository userRepository;

	ExecutorService executorService = Executors.newFixedThreadPool(5);

	@Override
	public Deployments createDeployment(int id, Deployments provisionData) {

		LOGGER.info("Creating deployment : " + provisionData.getName());

		validationUtility.validateDeploymentData(id, provisionData);

		User dbUser = userRepository.findOneById(id);
		Clients dbClient = clientsRepository.findOneById(dbUser.getClients().getId());
		int clientId = dbClient.getId();

		Deployments newDeployment = new Deployments();

		newDeployment.setUser(dbUser);

		String deploymentName = provisionData.getName().toLowerCase();

		newDeployment.setName(deploymentName);
		newDeployment.setStatus(DeploymentStatus.PENDING);
		newDeployment.setClusterNodes(provisionData.getClusterNodes());

		Date date = new Date();
		long currentTimestamp = date.getTime();

		String prefix = clientId + "-" + currentTimestamp;
		LOGGER.info("prefix : " + prefix);

		newDeployment.setPrefix(prefix);

		String fileName = provisionData.getName() + "_terraform.tfvars";
		newDeployment.setFileName(fileName);

		deploymentsRepository.save(newDeployment);

		FileInputStream instream = null;
		FileOutputStream outstream = null;
		File file = null;
		File outfile = null;

		try {
			file = new File(varsFilePath);
			outfile = new File(outputFilePath + fileName);

			instream = new FileInputStream(file);
			outstream = new FileOutputStream(outfile);

			byte[] buffer = new byte[1024];

			int length;

			while ((length = instream.read(buffer)) > 0) {
				outstream.write(buffer, 0, length);
			}

			instream.close();
			outstream.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		try {
			BufferedReader reader = new BufferedReader(new FileReader(outfile));
			String line = "", oldtext = "";
			while ((line = reader.readLine()) != null) {
				oldtext += line + "\r\n";
			}
			reader.close();

			String node = String.valueOf(provisionData.getClusterNodes());
			String core = String.valueOf(provisionData.getClusterMachineCores());
			String capacity = String.valueOf(provisionData.getClusterLocalStoreCapacity());
			String nfsCapacity = String.valueOf(provisionData.getNfsCapacity());
			String machineType = provisionData.getClusterMachineType();

			String newtext = oldtext.replaceAll("ujmnhy", provisionData.getToolName())
					.replaceAll("pqlamz", provisionData.getToolVersion()).replaceAll("ioplkj", "latest")
					.replaceAll("qazxsw", deploymentName).replaceAll("mkoijn", node).replaceAll("qwecxz", machineType)
					.replaceAll("poibnm", core).replaceAll("tyunbv", capacity)
					.replaceAll("yuilkj", provisionData.getNfsName()).replaceAll("vgyuhb", "us-west1-a")
					.replaceAll("yuiklj", nfsCapacity).replaceAll("ijnbhu", provisionData.getFileStoreHost())
					.replaceAll("itungf", provisionData.getFileStorePath());

			FileWriter writer = new FileWriter(outfile);
			writer.write(newtext);

			writer.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		String[] cmd = { applyShellPath, fileName };

		executorService.execute(new Runnable() {

			@Override
			public void run() {
				ProcessBuilder pb = new ProcessBuilder(cmd);

				try {
					Process process = pb.start();
					BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
					StringBuilder builder = new StringBuilder();
					String line = null;
					while ((line = reader.readLine()) != null) {
						builder.append(line);
					}
					String result = builder.toString();
					LOGGER.info(result);
					LOGGER.info("end of script execution");
				} catch (IOException e) {
					LOGGER.error("error");
					e.printStackTrace();
				}

			}
		});

		return newDeployment;
	}

	@Override
	public List<Deployments> getAll(int id, String name) {
		LOGGER.info("Getting all deployments");

		List<Deployments> deploymentList = new ArrayList<Deployments>();
		List<ValidationError> validationErrorList = new ArrayList<ValidationError>();

		// validationUtility.validateClientId(clientId);

		// Clients dbClient = clientsRepository.findOneById(clientId);

		User dbUser = userRepository.findOneById(id);

		if (name == null) {

			deploymentList = deploymentsRepository.findAllByUser(dbUser);
			return deploymentList;

		} else {

			Deployments dbDeployments = deploymentsRepository.findByName(name);

			if (dbDeployments == null) {
				LOGGER.error("Deployment with this name does not exist");
				validationErrorList.add(new ValidationError("name", "Deployment with this name does not exist"));

			} else {
				Deployments dbDeployment = deploymentsRepository.findByUserAndName(dbUser, name);
				deploymentList.add(dbDeployment);
				return deploymentList;
			}

			if (validationErrorList != null && !validationErrorList.isEmpty()) {
				throw new CliBadRequestException("Bad Request", validationErrorList);
			}
		}
		return deploymentList;

	}

	@Override
	public List<DeployStatus> getStatus(int deploymentId) {
		LOGGER.info("Getting status of a deployment");

		validationUtility.validateDeploymentId(deploymentId);

		Deployments dbDeployment = deploymentsRepository.findOneByIdAndIsDeletedFalse(deploymentId);

		List<DeployStatus> deployStatusList = deployStatusRepository.findAllByDeploymentId(dbDeployment);

		return deployStatusList;
	}

	@Override
	public void deProvision(Integer id, String name) {
		LOGGER.info("Deleting instance by name of deployment");

		User dbUser = userRepository.findOneById(id);

		validationUtility.validateDeploymentName(dbUser.getId(), name);

		Deployments dbDeployment = deploymentsRepository.findByUserAndNameAndIsDeletedFalse(dbUser, name);
		String propertyFile = dbDeployment.getFileName();

		dbDeployment.setDeleted(true);

		deploymentsRepository.save(dbDeployment);

		String[] cmdarr = { destroyShellPath, propertyFile };

		executorService.execute(new Runnable() {

			@Override
			public void run() {
				ProcessBuilder pbs = new ProcessBuilder(cmdarr);

				try {
					Process process = pbs.start();
					BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
					StringBuilder builder = new StringBuilder();
					String line = null;
					while ((line = reader.readLine()) != null) {
						builder.append(line);
					}
					String result = builder.toString();
					LOGGER.info("result : " + result);
					LOGGER.info("end of script execution");
				} catch (IOException e) {
					LOGGER.error("error");
					e.printStackTrace();
				}

			}
		});

	}

	@Override
	public float getCost(int deploymentId) throws FileNotFoundException, IOException {
		LOGGER.info("Getting the cost of deployment");

		String jsonPath = "/home/scriptuit/Downloads/gold-braid-268003-fa0b37fc4447.json";
//
//		String requestUrl = "https://cloudbilling.googleapis.com/v1/billingAccounts/01463E-59892A-CB4390";
//

//
//		credentials.refresh();
//
//		AccessToken token = credentials.getAccessToken();
//
//		RestTemplate template = new RestTemplate();
//
//		HttpHeaders headers = new HttpHeaders();
//		headers.set("Authorization", "Bearer " + token.getTokenValue());
//		headers.set("Content-Type", "application/json");
//
//		HttpEntity<Object> entity = new HttpEntity<Object>(null, headers);
//
//		ResponseEntity<Object> resultList = template.exchange(requestUrl, HttpMethod.GET, entity, Object.class);
//		LOGGER.info("response : " + resultList);

		GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(jsonPath))
				.createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));

		CloudBillingSettings cloudBillingSettings = CloudBillingSettings.newBuilder()
				.setCredentialsProvider(FixedCredentialsProvider.create(credentials)).build();

		CloudBillingClient cloudBillingClient = CloudBillingClient.create(cloudBillingSettings);

		CloudCatalogSettings cloudCatalogSettings = CloudCatalogSettings.newBuilder()
				.setCredentialsProvider(FixedCredentialsProvider.create(credentials)).build();

		CloudCatalogClient cloudCatalogClient = CloudCatalogClient.create(cloudCatalogSettings);
		ListServicesPagedResponse response = cloudCatalogClient.listServices();

		return 0;
	}

}

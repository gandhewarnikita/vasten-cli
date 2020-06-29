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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.FieldValueList;
import com.google.cloud.bigquery.Job;
import com.google.cloud.bigquery.JobException;
import com.google.cloud.bigquery.JobId;
import com.google.cloud.bigquery.JobInfo;
import com.google.cloud.bigquery.QueryJobConfiguration;
import com.google.cloud.bigquery.TableResult;
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
import com.vasten.cli.entity.StatusCli;
import com.vasten.cli.entity.User;
import com.vasten.cli.error.ValidationError;
import com.vasten.cli.exception.CliBadRequestException;
import com.vasten.cli.repository.ClientsRepository;
import com.vasten.cli.repository.DeployStatusRepository;
import com.vasten.cli.repository.DeploymentsRepository;
import com.vasten.cli.repository.UserRepository;
import com.vasten.cli.service.DeploymentsService;
import com.vasten.cli.utility.ValidationUtility;

/**
 * Service implementation class for Deployment related activities
 * 
 * @author scriptuit
 *
 */
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

	@Value("${APPLY_REMOTE_SHELL_PATH}")
	public String applyRemoteShellPath;

	@Value("${DESTROY_REMOTE_SHELL_PATH}")
	public String destroyRemoteShellPath;
	
	@Value("${NEW_PROJECT_KEYFILE_PATH}")
	private String newProjectKeyFilePath;

	@Autowired
	private DeployStatusRepository deployStatusRepository;

	@Autowired
	private DeploymentsRepository deploymentsRepository;

	@Autowired
	private ValidationUtility validationUtility;

	@Autowired
	private ClientsRepository clientsRepository;

	@Autowired
	private UserRepository userRepository;

	ExecutorService executorService = Executors.newFixedThreadPool(5);

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vasten.cli.service.DeploymentsService#createDeployment(int,
	 * com.vasten.cli.entity.Deployments)
	 */
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
		newDeployment.setNfsExternal(provisionData.isNfsExternal());

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
			String nfsExternal = String.valueOf(provisionData.isNfsExternal());

			String newtext = "";

			// If user wants his own file store, then set the values of filestore host and
			// path in the properties file
			if (provisionData.isNfsExternal()) {

				LOGGER.info("nfs external with host and path : " + provisionData.isNfsExternal());

				newtext = oldtext.replaceAll("ujmnhy", provisionData.getToolName())
						.replaceAll("pqlamz", provisionData.getToolVersion())
						.replaceAll("ioplkj", provisionData.getImageTag()).replaceAll("qazxsw", deploymentName)
						.replaceAll("mkoijn", node).replaceAll("qwecxz", machineType).replaceAll("poibnm", core)
						.replaceAll("tyunbv", capacity).replaceAll("yuiklj", nfsCapacity)
						.replaceAll("ijnbhu", provisionData.getFileStoreHost())
						.replaceAll("itungf", provisionData.getFileStorePath()).replaceAll("\"lothxs\"", nfsExternal);

			} else {
				LOGGER.info("nfs external without host and path : " + provisionData.isNfsExternal());

				newtext = oldtext.replaceAll("ujmnhy", provisionData.getToolName())
						.replaceAll("pqlamz", provisionData.getToolVersion())
						.replaceAll("ioplkj", provisionData.getImageTag()).replaceAll("qazxsw", deploymentName)
						.replaceAll("mkoijn", node).replaceAll("qwecxz", machineType).replaceAll("poibnm", core)
						.replaceAll("tyunbv", capacity).replaceAll("yuiklj", nfsCapacity)
						.replaceAll("\"lothxs\"", nfsExternal);
			}

			FileWriter writer = new FileWriter(outfile);
			writer.write(newtext);

			writer.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		try (BufferedReader br = new BufferedReader(new FileReader(outputFilePath + fileName))) {
			String line;
			while ((line = br.readLine()) != null) {
				LOGGER.info(line);
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		String[] cmd = { applyShellPath, fileName };

		ProcessBuilder pb = new ProcessBuilder(cmd);

		executorService.execute(new Runnable() {

			@Override
			public void run() {
				try {
					Process process = pb.start();
					int exitCode = process.waitFor();
					LOGGER.info("exit code : " + exitCode);
					LOGGER.info("end of script execution");
				} catch (IOException e) {
					LOGGER.error("error");
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}
		});

		return newDeployment;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vasten.cli.service.DeploymentsService#getAll(int, java.lang.String)
	 */
	@Override
	public List<Deployments> getAll(int id, String name) {
		LOGGER.info("Getting all deployments");

		List<Deployments> deploymentList = new ArrayList<Deployments>();
		List<ValidationError> validationErrorList = new ArrayList<ValidationError>();

		User dbUser = userRepository.findOneById(id);

		if (name == null) {

			deploymentList = deploymentsRepository.findAllByUserAndIsDeletedFalse(dbUser);
			return deploymentList;

		} else {

			Deployments dbDeployments = deploymentsRepository.findByName(name);

			if (dbDeployments == null) {
				LOGGER.error("Deployment with this name does not exist");
				validationErrorList.add(new ValidationError("name", "Deployment with this name does not exist"));

			} else {
				Deployments dbDeployment = deploymentsRepository.findByUserAndNameAndIsDeletedFalse(dbUser, name);

				if (dbDeployment != null) {
					deploymentList.add(dbDeployment);
					return deploymentList;
				}
			}

			if (validationErrorList != null && !validationErrorList.isEmpty()) {
				throw new CliBadRequestException("Bad Request", validationErrorList);
			}
		}
		return deploymentList;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vasten.cli.service.DeploymentsService#getStatus(int)
	 */
	@Override
	public Map<String, List<StatusCli>> getStatus(int deploymentId) {
		LOGGER.info("Getting status of a deployment");

		validationUtility.validateDeploymentId(deploymentId);

		String deploymentName = "";
		Map<String, List<StatusCli>> statusMap = new HashMap<String, List<StatusCli>>();
		List<StatusCli> statusCliList = new ArrayList<StatusCli>();

		Deployments dbDeployment = deploymentsRepository.findOneByIdAndIsDeletedFalse(deploymentId);
		deploymentName = dbDeployment.getName();

		List<DeployStatus> deployStatusList = deployStatusRepository.findAllByDeploymentId(dbDeployment);

		for (DeployStatus deployStatusObj : deployStatusList) {
			StatusCli statusCli = new StatusCli();

			statusCli.setDeploymentTypeName(deployStatusObj.getDeploymentTypeName());
			statusCli.setExternalIp(deployStatusObj.getExternalIp());
			statusCli.setStatus(deployStatusObj.getStatus().toString());
			statusCli.setType(deployStatusObj.getType().toString());

			statusCliList.add(statusCli);
		}

		statusMap.put(deploymentName, statusCliList);

		return statusMap;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vasten.cli.service.DeploymentsService#deProvision(java.lang.Integer,
	 * java.lang.Integer)
	 */
	@Override
	public void deProvision(Integer userId, Integer deploymentId) {
		LOGGER.info("Deleting instance by id of deployment");

		User dbUser = userRepository.findOneById(userId);

		validationUtility.validateDeployment(dbUser.getId(), deploymentId);

		Deployments dbDeployment = deploymentsRepository.findByUserAndIdAndIsDeletedFalse(dbUser, deploymentId);
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
					int exitCode = process.waitFor();
					LOGGER.info("exit code : " + exitCode);
					LOGGER.info("end of script execution");
				} catch (IOException e) {
					LOGGER.error("error");
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}
		});

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vasten.cli.service.DeploymentsService#getCost(int)
	 */
	@Override
	public float getCost(int deploymentId) throws FileNotFoundException, IOException {
		LOGGER.info("Getting the cost of deployment");

//		GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(jsonPath))
//				.createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));
//
//		CloudBillingSettings cloudBillingSettings = CloudBillingSettings.newBuilder()
//				.setCredentialsProvider(FixedCredentialsProvider.create(credentials)).build();
//
//		CloudBillingClient cloudBillingClient = CloudBillingClient.create(cloudBillingSettings);
//
//		CloudCatalogSettings cloudCatalogSettings = CloudCatalogSettings.newBuilder()
//				.setCredentialsProvider(FixedCredentialsProvider.create(credentials)).build();
//
//		CloudCatalogClient cloudCatalogClient = CloudCatalogClient.create(cloudCatalogSettings);
//		ListServicesPagedResponse response = cloudCatalogClient.listServices();

		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vasten.cli.service.DeploymentsService#mountNfs(java.lang.Integer,
	 * java.lang.String)
	 */
	@Override
	public void mountNfs(Integer userId, String deploymentName) {
		LOGGER.info("Mounting nfs file store");

		validationUtility.validateDeploymentName(deploymentName);

		User dbUser = userRepository.findOneById(userId);
		Deployments dbDeployment = deploymentsRepository.findByUserAndNameAndIsDeletedFalse(dbUser, deploymentName);

		String propertyFile = dbDeployment.getFileName();

		String[] cmd = { applyRemoteShellPath, propertyFile };

		executorService.execute(new Runnable() {

			@Override
			public void run() {
				ProcessBuilder pb = new ProcessBuilder(cmd);

				try {
					Process process = pb.start();
					int exitCode = process.waitFor();
					LOGGER.info("exit code : " + exitCode);
					LOGGER.info("end of script execution");
				} catch (IOException e) {
					LOGGER.error("error");
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}
		});

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vasten.cli.service.DeploymentsService#deProvisionRemote(java.lang.
	 * Integer, java.lang.String)
	 */
	@Override
	public void deProvisionRemote(Integer userId, String deploymentName) {
		LOGGER.info("Deleting the mounted file store");

		User dbUser = userRepository.findOneById(userId);

		validationUtility.validateDeployment(dbUser.getId(), deploymentName);

		Deployments dbDeployment = deploymentsRepository.findByUserAndNameAndIsDeletedFalse(dbUser, deploymentName);
		String propertyFile = dbDeployment.getFileName();

		dbDeployment.setDeleted(true);

		deploymentsRepository.save(dbDeployment);

		String[] cmdarr = { destroyRemoteShellPath, propertyFile };

		executorService.execute(new Runnable() {

			@Override
			public void run() {
				ProcessBuilder pbs = new ProcessBuilder(cmdarr);

				try {
					Process process = pbs.start();
					int exitCode = process.waitFor();
					LOGGER.info("exit code : " + exitCode);
					LOGGER.info("end of script execution");
				} catch (IOException e) {
					LOGGER.error("error");
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}
		});

	}

}

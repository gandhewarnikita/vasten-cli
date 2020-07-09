package com.vasten.cli.service.impl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.compute.v1.AccessConfig;
import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstanceClient;
import com.google.cloud.compute.v1.InstanceGroupManagerClient;
import com.google.cloud.compute.v1.InstanceGroupManagerClient.ListInstanceGroupManagersPagedResponse;
import com.google.cloud.compute.v1.InstanceGroupManagerSettings;
import com.google.cloud.compute.v1.InstanceClient.ListInstancesPagedResponse;
import com.google.cloud.compute.v1.InstanceGroupClient.ListInstanceGroupsPagedResponse;
import com.google.cloud.compute.v1.InstanceSettings;
import com.google.cloud.compute.v1.NetworkInterface;
import com.google.cloud.compute.v1.ProjectZoneName;
import com.google.common.collect.Lists;
import com.vasten.cli.entity.DeployStatus;
import com.vasten.cli.entity.DeploymentStatus;
import com.vasten.cli.entity.DeploymentType;
import com.vasten.cli.entity.Deployments;
import com.vasten.cli.repository.DeployStatusRepository;
import com.vasten.cli.repository.DeploymentsRepository;

import net.bytebuddy.asm.Advice.This;

import com.google.cloud.compute.v1.InstanceGroupManager;

/**
 * Scheduler for fetching status of instance groups, instances and nfs from
 * google cloud
 * 
 * @author scriptuit
 *
 */
@Component
public class DeploymentStatusScheduler {

	private static final Logger LOGGER = LoggerFactory.getLogger(DeploymentStatusScheduler.class);

	@Value("${PROJECT_ID}")
	public String projectId;

	@Value("${ZONE}")
	private String zone;

	@Value("${ACCESS_TOKEN}")
	private String accessToken;

	@Value("${NEW_PROJECT_ID}")
	public String newProjectId;

	@Value("${NEW_ZONE}")
	private String newZone;

	@Value("${PROJECT_KEYFILE_PATH}")
	private String projectKeyFilePath;

	@Value("${NEW_PROJECT_KEYFILE_PATH}")
	private String newProjectKeyFilePath;

	@Autowired
	private DeploymentsRepository deploymentsRepository;

	@Autowired
	private DeployStatusRepository deployStatusRepository;

	final String LABEL_KEY_DEPLOYMENT_NAME = "deployment_name";

	@Scheduled(cron = "0 0/5 * * * *")
//	@Scheduled(cron = "10 * * * * *")
	public void statusScheduler() throws IOException, GeneralSecurityException {
		LOGGER.info("In the deployment status update scheduler");

		List<Deployments> deploymentList = new ArrayList<Deployments>();

		// Fetch all instance groups and build instance group map
		// Key is instance group name and value is instance group stability
		Map<String, Boolean> instanceGroupMap = new HashMap<String, Boolean>();
		try {
			ListInstanceGroupManagersPagedResponse instanceGroupList = this.fetchAllInstanceGroups();

			if (instanceGroupList != null) {

				if (instanceGroupList.iterateAll().iterator().hasNext()) {

					for (InstanceGroupManager element : instanceGroupList.iterateAll()) {

						String instanceGroupName = element.getName();
						LOGGER.info("instanceGroupName : " + instanceGroupName);
						Boolean instanceGroupStability = element.getStatus().getIsStable();
						LOGGER.info("instanceGroupStability : " + instanceGroupStability);
						instanceGroupMap.put(instanceGroupName, instanceGroupStability);
					}
				}
			}
		} catch (IOException ex) {
			LOGGER.error("Error in fetching instance groups list", ex);
		}

		// Fetch all instances and build instance map
		// Key is deployment name, value is list of instances
		Map<String, List<Instance>> instanceMap = new HashMap<String, List<Instance>>();
		try {
			ListInstancesPagedResponse instanceList = this.fetchAllInstances();

			if (instanceList != null) {

				if (instanceList.iterateAll().iterator().hasNext()) {

					for (Instance instance : instanceList.iterateAll()) {

						LOGGER.info("instance name in instance list : " + instance.getName());
						LOGGER.info("instance status in instance list : " + instance.getStatus());
						Map<String, String> labelsMap = instance.getLabelsMap();
						if (labelsMap != null) {
							String deploymentName = labelsMap.get(LABEL_KEY_DEPLOYMENT_NAME);
							if (deploymentName != null) {
								if (instanceMap.get(deploymentName) == null) {
									instanceMap.put(deploymentName, new ArrayList<Instance>());
								}
								instanceMap.get(deploymentName).add(instance);
							}
						}
					}
				}
			}
		} catch (IOException ex) {
			LOGGER.error("Error in fetching instance list", ex);
		}

		// Fetch all file stores and build filestore map
		// Key is deployment name and value is list of filestore objects
		Map<String, List<JSONObject>> filestoreMap = new HashMap<String, List<JSONObject>>();
		try {
			JSONArray filestoreList = this.fetchAllFilestores();
			if (filestoreList != null) {

				for (int i = 0; i < filestoreList.length(); i++) {

					JSONObject filstoreObject = filestoreList.getJSONObject(i);
					String deploymentName = filstoreObject.query("/labels/" + LABEL_KEY_DEPLOYMENT_NAME).toString();

					if (deploymentName != null) {
						if (filestoreMap.get(deploymentName) == null) {
							filestoreMap.put(deploymentName, new ArrayList<JSONObject>());
						}
						filestoreMap.get(deploymentName).add(filstoreObject);
					}
				}
			}
		} catch (IOException ex) {
			LOGGER.error("Error in fetching filestore list", ex);
		}

		// Fetch Pending deployment list
		deploymentList = deploymentsRepository.findAllByStatusAndIsDeletedFalse(DeploymentStatus.PENDING);
		if (deploymentList != null) {
			for (Deployments deployment : deploymentList) {
				String deploymentName = deployment.getName();
				Integer deploymentId = deployment.getId();
				DeploymentStatus finalDeploymentStatus = deployment.getStatus();

				// Save and Update Instance Group Status
				String deploymentTypeName = deploymentName + "-instance-group";
				DeployStatus instanceGroupDb = deployStatusRepository.findOneByDeploymentTypeNameAndTypeAndDeploymentId(
						deploymentTypeName, DeploymentType.INSTANCE_GROUP, deployment);
				if (instanceGroupDb == null) {

					instanceGroupDb = new DeployStatus();
					instanceGroupDb.setDeploymentId(deployment);
					instanceGroupDb.setDeploymentTypeName(deploymentTypeName);
					instanceGroupDb.setType(DeploymentType.INSTANCE_GROUP);
				}
				instanceGroupDb.setStatus(DeploymentStatus.PENDING);

				if (instanceGroupMap.containsKey(deploymentTypeName)
						&& instanceGroupMap.get(deploymentTypeName) == true) {

					instanceGroupDb.setStatus(DeploymentStatus.SUCCESS);
					finalDeploymentStatus = DeploymentStatus.SUCCESS;
				}
				deployStatusRepository.save(instanceGroupDb);

				// Save and Update Instances Statuses
				List<Instance> instanceList = instanceMap.get(deploymentName);
				if (instanceList != null) {

					for (Instance instance : instanceList) {

						DeployStatus instanceDb = deployStatusRepository
								.findOneByDeploymentTypeNameAndTypeAndDeploymentId(instance.getName(),
										DeploymentType.INSTANCE, deployment);
						if (instanceDb == null) {

							instanceDb = new DeployStatus();
							instanceDb.setDeploymentId(deployment);
							instanceDb.setDeploymentTypeName(instance.getName());
							instanceDb.setType(DeploymentType.INSTANCE);
						}
						if (instance.getNetworkInterfacesList() != null
								&& !instance.getNetworkInterfacesList().isEmpty()
								&& instance.getNetworkInterfacesList().get(0) != null
								&& instance.getNetworkInterfacesList().get(0).getAccessConfigsList() != null
								&& !instance.getNetworkInterfacesList().get(0).getAccessConfigsList().isEmpty()) {

							String externalIp = "";
							for (AccessConfig accessConfig : instance.getNetworkInterfacesList().get(0)
									.getAccessConfigsList()) {
								externalIp = accessConfig.getNatIP();
								LOGGER.info("external ip of instance : " + externalIp);
							}
							instanceDb.setExternalIp(externalIp);
						}
						if (instance.getStatus().equals("RUNNING")) {

							instanceDb.setStatus(DeploymentStatus.SUCCESS);
							finalDeploymentStatus = DeploymentStatus.SUCCESS;
						} else if (instance.getStatus().equals("PROVISIONING")) {
							instanceDb.setStatus(DeploymentStatus.PROVISIONING);
							finalDeploymentStatus = DeploymentStatus.PENDING;
						} else if ((instance.getStatus().equals("TERMINATED"))
								|| (instance.getStatus().equals("DELETING"))
								|| (instance.getStatus().equals("DELETED"))) {
							instanceDb.setStatus(DeploymentStatus.ERROR);
							finalDeploymentStatus = DeploymentStatus.ERROR;
						} else {
							instanceDb.setStatus(DeploymentStatus.ERROR);
							finalDeploymentStatus = DeploymentStatus.ERROR;
						}

						deployStatusRepository.save(instanceDb);
					}
				}

				// Save and Update File store Statuses
				List<JSONObject> filestoreList = filestoreMap.get(deploymentName);
				if (filestoreList != null) {

					for (JSONObject filestore : filestoreList) {
						DeployStatus filestoreDb = deployStatusRepository
								.findOneByDeploymentTypeNameAndTypeAndDeploymentId(filestore.getString("name"),
										DeploymentType.NFS, deployment);
						if (filestoreDb == null) {

							filestoreDb = new DeployStatus();
							filestoreDb.setDeploymentId(deployment);
							filestoreDb.setDeploymentTypeName(filestore.getString("name"));
							LOGGER.info("file store name : " + filestore.getString("name"));
							filestoreDb.setType(DeploymentType.NFS);
						}
						if (filestore.getString("state").equals("READY")) {

							filestoreDb.setStatus(DeploymentStatus.SUCCESS);
							finalDeploymentStatus = DeploymentStatus.SUCCESS;
						} else {
							filestoreDb.setStatus(DeploymentStatus.PENDING);
						}

						deployStatusRepository.save(filestoreDb);
					}
				}

				deployment.setStatus(finalDeploymentStatus);
				deploymentsRepository.save(deployment);
			}
		}
	}

	/**
	 * Calls cloud for all instance group details
	 * 
	 */
	private ListInstanceGroupManagersPagedResponse fetchAllInstanceGroups() throws IOException {
		LOGGER.info("Fetching all instance groups details");
		GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(newProjectKeyFilePath))
				.createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));

		InstanceGroupManagerSettings instanceGroupManagerSettings = InstanceGroupManagerSettings.newBuilder()
				.setCredentialsProvider(FixedCredentialsProvider.create(credentials)).build();

		InstanceGroupManagerClient instanceGroupManagerClient = InstanceGroupManagerClient
				.create(instanceGroupManagerSettings);

		ProjectZoneName projectZoneNameGroup = ProjectZoneName.of(newProjectId, zone);
		return instanceGroupManagerClient.listInstanceGroupManagers(projectZoneNameGroup);
	}

	/**
	 * Calls cloud and fetches all instances
	 * 
	 */
	private ListInstancesPagedResponse fetchAllInstances() throws IOException {
		LOGGER.info("Fetching all instance details");
		GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(newProjectKeyFilePath))
				.createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));
		InstanceSettings instanceSettings = InstanceSettings.newBuilder()
				.setCredentialsProvider(FixedCredentialsProvider.create(credentials)).build();

		InstanceClient instanceClient = InstanceClient.create(instanceSettings);
		ProjectZoneName projectZoneName = ProjectZoneName.of(newProjectId, zone);

		return instanceClient.listInstances(projectZoneName);
	}

	/**
	 * Calls cloud for all filestore instances
	 * 
	 */
	private JSONArray fetchAllFilestores() throws IOException {
		LOGGER.info("Fetching all file store statuses");
		String uri = "https://file.googleapis.com/v1/";
		String requestListUri = uri + "projects/" + newProjectId + "/locations/" + zone + "/instances";

		GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(newProjectKeyFilePath))
				.createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));

		credentials.refresh();

		AccessToken token = credentials.getAccessToken();

		RestTemplate template = new RestTemplate();

		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "Bearer " + token.getTokenValue());
		headers.set("Content-Type", "application/json");

		HttpEntity<Object> entity = new HttpEntity<Object>(null, headers);

		ResponseEntity<String> result = template.exchange(requestListUri, HttpMethod.GET, entity, String.class);

		JSONArray instanceList = null;
		if (result != null && result.getBody() != null && !result.getBody().isEmpty()) {
			LOGGER.info("Response of filestores");
			String resultStr = result.getBody();
			String resultJson = resultStr.replaceAll("=", ":");

			JSONObject resultobj = new JSONObject(resultJson);

			if (!resultobj.isEmpty()) {
				instanceList = resultobj.getJSONArray("instances");
			}
		}
		return instanceList;
	}
}

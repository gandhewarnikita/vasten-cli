package com.vasten.cli.service.impl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
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
import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstanceClient;
import com.google.cloud.compute.v1.InstanceClient.ListInstancesPagedResponse;
import com.google.cloud.compute.v1.InstanceSettings;
import com.google.cloud.compute.v1.ProjectZoneName;
import com.google.cloud.container.v1.ClusterManagerClient;
import com.google.cloud.container.v1.ClusterManagerSettings;
import com.google.common.collect.Lists;
import com.google.container.v1.Cluster;
import com.google.container.v1.ListClustersResponse;
import com.vasten.cli.entity.DeployStatus;
import com.vasten.cli.entity.DeploymentStatus;
import com.vasten.cli.entity.Deployments;
import com.vasten.cli.repository.DeployStatusRepository;
import com.vasten.cli.repository.DeploymentsRepository;

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

	@Value("${NFS_SHELL_PATH}")
	private String nfsShellPath;

	@Autowired
	private DeploymentsRepository deploymentsRepository;

	@Autowired
	private DeployStatusRepository deployStatusRepository;

	private static final String clusterStatusUrl = "https://container.googleapis.com/v1";

	// @Scheduled()
	@Scheduled(cron = "0/10 * * * * *")
	public void statusScheduler() throws IOException, GeneralSecurityException {

		LOGGER.info("in the deployment status update scheduler");

//		Deployments dbDeployment = new Deployments();
		DeployStatus finalobj = new DeployStatus();

		String clusterName = "";
		String status = "";
		String instanceName = "";
		String instanceStatus = "";
		String nfsName = "";
		String nfsStatus = "";

		List<Deployments> deploymentList = new ArrayList<Deployments>();

		deploymentList = deploymentsRepository.findAllByStatus(DeploymentStatus.PENDING);

		Set<DeployStatus> deploySet = new HashSet<DeployStatus>();

		List<String> nameList = new ArrayList<String>();

		for (Deployments dbDeployment : deploymentList) {
			nameList.add(dbDeployment.getName());
		}

//		this.getStatus();

		List<DeployStatus> deployStatusList = new ArrayList<DeployStatus>();

		Map<String, String> deploymentMap = this.getDeploymentStatus(projectId, zone);

		Map<String, String> instanceMap = this.getInstanceStatus(projectId, zone);

		Map<String, String> nfsMap = this.getNfsStatus();

		if (!CollectionUtils.isEmpty(deploymentMap)) {

			for (Map.Entry<String, String> entry : deploymentMap.entrySet()) {
				DeployStatus deployStatus = new DeployStatus();

				LOGGER.info("in deployment map");
				LOGGER.info("deployment name : " + entry.getKey() + " & deployment status : " + entry.getValue());

				
				
				clusterName = entry.getKey();
	//			deployStatus.setName(clusterName);
				
				deployStatus.setDeploymentTypeName(clusterName);
	//			deployStatus.setDeploymentId(deploymentId);

				status = entry.getValue();

				if (status.equals("RUNNING")) {

					deployStatus.setStatus(DeploymentStatus.SUCCESS);

					Deployments dbDeploy = deploymentsRepository.findByNameAndIsDeletedFalse(clusterName);

					if (dbDeploy != null) {
						if (dbDeploy.getStatus().equals(DeploymentStatus.PENDING)) {
							dbDeploy.setStatus(DeploymentStatus.SUCCESS);
							deploymentsRepository.save(dbDeploy);
						}
					}

				} else if (status.equals("PROVISIONING")) {
					deployStatus.setStatus(DeploymentStatus.PROVISIONING);

				} else if ((status.equals("TERMINATED")) || (status.equals("DELETED") || (status.equals("DELETING")))) {

					deployStatus.setStatus(DeploymentStatus.ERROR);
				}

				// LOGGER.info("deploy status obj after deployment : " + deployStatus);

				// deployStatusList.add(deployStatus);

				// deployStatusRepository.save(deployStatus);

			}

		}

//		LOGGER.info("deploy status obj after deployment : " + deployStatus);

		if (!CollectionUtils.isEmpty(instanceMap)) {

			for (Map.Entry<String, String> entry : instanceMap.entrySet()) {
				DeployStatus deployStatus = new DeployStatus();

				LOGGER.info("in instance map");

				instanceName = entry.getKey();
				instanceStatus = entry.getValue();

				LOGGER.info("instance name : " + instanceName + " & instance status : " + instanceStatus);

	//			deployStatus.setInstanceName(instanceName);

				if (instanceStatus.equals("RUNNING")) {
		//			deployStatus.setInstanceStatus(DeploymentStatus.SUCCESS);

				} else if (instanceStatus.equals("PROVISIONING")) {
			//		deployStatus.setInstanceStatus(DeploymentStatus.PROVISIONING);
				} else if ((instanceStatus.equals("TERMINATED")) || (instanceStatus.equals("DELETED"))
						|| (instanceStatus.equals("DELETING"))) {
				//	deployStatus.setInstanceStatus(DeploymentStatus.ERROR);
				}

				// LOGGER.info("deploy status obj after instance : " + deployStatus);

				// deployStatusList.add(deployStatus);

				// deployStatusRepository.save(deployStatus);
			}

		}

//		LOGGER.info("deploy status obj after instance : " + deployStatus);

		if (!CollectionUtils.isEmpty(nfsMap)) {

			for (Map.Entry<String, String> entry : nfsMap.entrySet()) {
				DeployStatus deployStatus = new DeployStatus();

				LOGGER.info("in nfs map");

				nfsName = entry.getKey();
				nfsStatus = entry.getValue();

				LOGGER.info("nfs name : " + nfsName + " & nfs status : " + nfsStatus);

	//			deployStatus.setNfsName(nfsName);

				if (nfsStatus.equals("READY")) {
		//			deployStatus.setNfsStatus(DeploymentStatus.SUCCESS);

				} else if (nfsStatus.equals("PROVISIONING")) {
			//		deployStatus.setNfsStatus(DeploymentStatus.PROVISIONING);

				} else if ((nfsStatus.equals("TERMINATED")) || (nfsStatus.equals("DELETING"))
						|| (nfsStatus.equals("DELETED"))) {
				//	deployStatus.setNfsStatus(DeploymentStatus.ERROR);
				}

				// LOGGER.info("deploy status obj after nfs : " + deployStatus);

				// deployStatusList.add(deployStatus);

				// deployStatusRepository.save(deployStatus);
			}

		}

	}

	private Map<String, String> getDeploymentStatus(String projectId, String zone)
			throws FileNotFoundException, IOException {
		LOGGER.info("Getting sttaus of all deployments");

		Map<String, String> deployMap = new HashMap<String, String>();
		String deploymentName = "";
		String deploymentStatus = "";

		String jsonPath = "/home/scriptuit/Downloads/unique-badge-276520-d6e270a9c112.json";

		GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(jsonPath))
				.createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));

		ClusterManagerSettings clusterManagerSettings = ClusterManagerSettings.newBuilder()
				.setCredentialsProvider(FixedCredentialsProvider.create(credentials)).build();

		ClusterManagerClient clusterManagerClient = ClusterManagerClient.create(clusterManagerSettings);
		ListClustersResponse response = clusterManagerClient.listClusters(projectId, zone);

		for (Cluster cluster : response.getClustersList()) {
			deploymentName = cluster.getName();
			String name[] = deploymentName.split("-");
			String newname = name[0];

			deploymentStatus = cluster.getStatus().toString();

			deployMap.put(newname, deploymentStatus);

			// deployMap.put(deploymentName, deploymentStatus);
		}

		return deployMap;
	}

	private Map<String, String> getInstanceStatus(String projectId, String zone)
			throws FileNotFoundException, IOException {

		LOGGER.info("Getting all instances status");

		String instanceName = "";
		String instanceStatus = "";

		Map<String, String> instanceMap = new HashMap<String, String>();

		String jsonPath = "/home/scriptuit/Downloads/unique-badge-276520-d6e270a9c112.json";

		GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(jsonPath))
				.createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));

		InstanceSettings instanceSettings = InstanceSettings.newBuilder()
				.setCredentialsProvider(FixedCredentialsProvider.create(credentials)).build();

		InstanceClient instanceClient = InstanceClient.create(instanceSettings);
		ProjectZoneName projectZoneName = ProjectZoneName.of(projectId, zone);

		ListInstancesPagedResponse instanceList = instanceClient.listInstances(projectZoneName);

		for (Instance instance : instanceList.iterateAll()) {

			instanceName = instance.getName();
			instanceStatus = instance.getStatus();

			instanceMap.put(instanceName, instanceStatus);

		}

		return instanceMap;
	}

//	@Scheduled(cron = "0/10 * * * * *")
	private Map<String, String> getNfsStatus() throws IOException {
		LOGGER.info("Getting nfs status");

		Map<String, String> nfsMap = new HashMap<String, String>();

		String jsonPath = "/home/scriptuit/Downloads/unique-badge-276520-d6e270a9c112.json";

		String uri = "https://file.googleapis.com/v1/";
		String requestListUri = uri + "projects/" + projectId + "/locations/" + zone + "/instances";

		GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(jsonPath))
				.createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));

		credentials.refresh();

		AccessToken token = credentials.getAccessToken();

		RestTemplate template = new RestTemplate();

		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "Bearer " + token.getTokenValue());
		headers.set("Content-Type", "application/json");

		HttpEntity<Object> entity = new HttpEntity<Object>(null, headers);

		ResponseEntity<String> resultList = template.exchange(requestListUri, HttpMethod.GET, entity, String.class);

		LOGGER.info("response code resultList : " + resultList.getStatusCodeValue());
		LOGGER.info("response resultList : " + resultList.getBody());

		if (resultList.getBody() != null || !resultList.getBody().isEmpty()) {
			String strobj = resultList.getBody();
			String strnew = strobj.replaceAll("=", ":");

			LOGGER.info("strnew : " + strnew);

			JSONObject jobj = new JSONObject(strnew);

			if (!jobj.isEmpty()) {

				String state = "";
				String name = "";

				JSONArray jarr = jobj.getJSONArray("instances");
				LOGGER.info("jarr : " + jarr);
				LOGGER.info("******************************************************");

				for (int i = 0; i < jarr.length(); i++) {
					LOGGER.info("jarr element : " + jarr.get(i));
					LOGGER.info("******************************************************");

//					JSONObject jarr2 = jarr.getJSONObject(0);
					//
//					LOGGER.info("jarr2 : " + jarr2);

					String strobj1 = jarr.get(i).toString();

					String strnew2 = strobj1.substring(1, strobj1.length() - 1);
					LOGGER.info("strnew2 : " + strnew2);

					int totalLength = strobj1.length();
					int stateIndex = 0;
					int fileIndex = 0;
					int tierIndex = 0;
					int networkIndex = 0;

					LOGGER.info("******************************************************");

					if (strnew2.contains("state")) {
						stateIndex = strobj1.indexOf("state", 0);
						LOGGER.info("state index = " + stateIndex);
					}

					if (strnew2.contains("fileShares")) {
						fileIndex = strobj1.indexOf("fileShares", 0);
						LOGGER.info("file index = " + fileIndex);
					}

					if (strnew2.contains("networks")) {
						networkIndex = strobj1.indexOf("networks", 0);
						LOGGER.info("network index = " + networkIndex);
					}

					if (strnew2.contains("tier")) {
						tierIndex = strobj1.indexOf("tier", 0);
						LOGGER.info("tier index = " + tierIndex);
					}

					LOGGER.info("******************************************************");

					String strnew3 = strnew2.substring(fileIndex - 1, tierIndex - 3);
					LOGGER.info("strnew3 : " + strnew3);

					if (strnew3.contains("name")) {
						int index2 = strnew3.indexOf("name");
						LOGGER.info("name index : " + index2);

						String substr = strnew3.substring(index2 - 1, strnew3.length() - 2);
						LOGGER.info("******************************************************");

						LOGGER.info("substr : " + substr);

						String substr1[] = substr.split(":");
						String namestr = substr1[1];

						name = namestr.substring(1, namestr.length() - 1);
						LOGGER.info("name : " + name);

					}

					LOGGER.info("******************************************************");

					String strnew4 = strnew2.substring(stateIndex - 1, networkIndex - 3);
					LOGGER.info("strnew4 : " + strnew4);

					String substr2[] = strnew4.split(":");
					String statestr = substr2[1];

					state = statestr.substring(1, statestr.length() - 1);
					LOGGER.info("state : " + state);

					nfsMap.put(name, state);

				}
				return nfsMap;
			}

		}
		return null;
	}

}

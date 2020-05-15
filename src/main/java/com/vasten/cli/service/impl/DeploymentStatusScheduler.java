package com.vasten.cli.service.impl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
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
import com.vasten.cli.entity.DeploymentType;
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

	@Value("${PROJECT_KEYFILE_PATH}")
	private String projectKeyFilePath;

	@Autowired
	private DeploymentsRepository deploymentsRepository;

	@Autowired
	private DeployStatusRepository deployStatusRepository;

	private static final String clusterStatusUrl = "https://container.googleapis.com/v1";

	List<DeployStatus> deployStatusList = new ArrayList<DeployStatus>();

	// @Scheduled()
	@Scheduled(cron = "0/10 * * * * *")
	public void statusScheduler() throws IOException, GeneralSecurityException {
		LOGGER.info("in the deployment status update scheduler");

		this.truncateDb();

		String clusterName = "";
		String status = "";
		String instanceName = "";
		String instanceStatus = "";
		String nfsName = "";
		String nfsStatus = "";

		List<Deployments> deploymentList = new ArrayList<Deployments>();

		deploymentList = deploymentsRepository.findAllByStatusAndIsDeletedFalse(DeploymentStatus.PENDING);

		List<String> nameList = new ArrayList<String>();

		for (Deployments dbDeployment : deploymentList) {
			nameList.add(dbDeployment.getName());
		}

		Map<String, String> clusterMap = this.getClusterStatus();

		Map<String, String> instanceMap = this.getInstanceStatus();

		Map<String, String> nfsMap = this.getNfsStatus();

		if (!CollectionUtils.isEmpty(clusterMap)) {

			for (Map.Entry<String, String> entry : clusterMap.entrySet()) {
				DeployStatus deployStatus = new DeployStatus();

				LOGGER.info("in cluster map");
				LOGGER.info("cluster name : " + entry.getKey() + " & cluster status : " + entry.getValue());

				clusterName = entry.getKey();
				String name[] = clusterName.split("-");
				String newname = name[0];

				deployStatus.setDeploymentTypeName(clusterName);

				Deployments dbDeploy = deploymentsRepository.findByNameAndIsDeletedFalse(newname);

				deployStatus.setDeploymentId(dbDeploy);
				deployStatus.setType(DeploymentType.CLUSTER);

				status = entry.getValue();

				if (status.equals("RUNNING")) {

					deployStatus.setStatus(DeploymentStatus.SUCCESS);

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

				// deployStatusList.add(deployStatus);
				this.saveclusterdb(deployStatus);

			}

		}

		if (!CollectionUtils.isEmpty(instanceMap)) {

			for (Map.Entry<String, String> entry : instanceMap.entrySet()) {
				DeployStatus deployStatus = new DeployStatus();

				LOGGER.info("in instance map");

				instanceName = entry.getKey();
				instanceStatus = entry.getValue();

				LOGGER.info("instance name : " + instanceName + " & instance status : " + instanceStatus);

				deployStatus.setDeploymentTypeName(instanceName);
				deployStatus.setType(DeploymentType.INSTANCE);

				String name[] = instanceName.split("-");

				Deployments dbDeploy = deploymentsRepository.findByNameAndIsDeletedFalse(name[1]);
				deployStatus.setDeploymentId(dbDeploy);

				if (instanceStatus.equals("RUNNING")) {

					deployStatus.setStatus(DeploymentStatus.SUCCESS);

				} else if (instanceStatus.equals("PROVISIONING")) {

					deployStatus.setStatus(DeploymentStatus.PROVISIONING);

				} else if ((instanceStatus.equals("TERMINATED")) || (instanceStatus.equals("DELETED"))
						|| (instanceStatus.equals("DELETING"))) {

					deployStatus.setStatus(DeploymentStatus.ERROR);
				}

				// deployStatusList.add(deployStatus);
				this.saveinsdb(deployStatus);

			}

		}

		if (!CollectionUtils.isEmpty(nfsMap)) {

			for (Map.Entry<String, String> entry : nfsMap.entrySet()) {
				DeployStatus deployStatus = new DeployStatus();

				LOGGER.info("in nfs map");

				nfsName = entry.getKey();
				nfsStatus = entry.getValue();

				LOGGER.info("nfs name : " + nfsName + " & nfs status : " + nfsStatus);

				deployStatus.setDeploymentTypeName(nfsName);
				deployStatus.setType(DeploymentType.NFS);

				Deployments dbDeploy = deploymentsRepository.findByNameAndIsDeletedFalse(nfsName);
				deployStatus.setDeploymentId(dbDeploy);

				if (nfsStatus.equals("READY")) {

					deployStatus.setStatus(DeploymentStatus.SUCCESS);

				} else if (nfsStatus.equals("PROVISIONING")) {

					deployStatus.setStatus(DeploymentStatus.PROVISIONING);

				} else if ((nfsStatus.equals("TERMINATED")) || (nfsStatus.equals("DELETING"))
						|| (nfsStatus.equals("DELETED"))) {

					deployStatus.setStatus(DeploymentStatus.ERROR);
				}

				// deployStatusList.add(deployStatus);
				this.savenfsdb(deployStatus);

			}

		}

//		for (DeployStatus dobj : deployStatusList) {
//			deployStatusRepository.save(dobj);
//		}

	}

	private void truncateDb() {
		deployStatusRepository.deleteAll();

	}

	private void savenfsdb(DeployStatus deployStatus) {
		deployStatusRepository.save(deployStatus);

	}

	private void saveinsdb(DeployStatus deployStatus) {
		deployStatusRepository.save(deployStatus);

	}

	private void saveclusterdb(DeployStatus deployStatus) {
		deployStatusRepository.save(deployStatus);

	}

	private Map<String, String> getClusterStatus() throws FileNotFoundException, IOException {
		LOGGER.info("Getting status of all deployments");

		Map<String, String> clusterMap = new HashMap<String, String>();
		String clusterName = "";
		String clusterStatus = "";

//		String jsonPath = "/home/scriptuit/Downloads/unique-badge-276520-d6e270a9c112.json";
		LOGGER.info("project key file path in cluster : " + projectKeyFilePath);

		GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(projectKeyFilePath))
				.createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));

		ClusterManagerSettings clusterManagerSettings = ClusterManagerSettings.newBuilder()
				.setCredentialsProvider(FixedCredentialsProvider.create(credentials)).build();

		ClusterManagerClient clusterManagerClient = ClusterManagerClient.create(clusterManagerSettings);
		ListClustersResponse response = clusterManagerClient.listClusters(projectId, zone);

		for (Cluster cluster : response.getClustersList()) {
			clusterName = cluster.getName();

			clusterStatus = cluster.getStatus().toString();

			clusterMap.put(clusterName, clusterStatus);
		}

		return clusterMap;
	}

	private Map<String, String> getInstanceStatus() throws FileNotFoundException, IOException {

		LOGGER.info("Getting all instances status");

		String instanceName = "";
		String instanceStatus = "";

		Map<String, String> instanceMap = new HashMap<String, String>();

//		String jsonPath = "/home/scriptuit/Downloads/unique-badge-276520-d6e270a9c112.json";
		LOGGER.info("project key file path in instance : " + projectKeyFilePath);

		GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(projectKeyFilePath))
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

	private Map<String, String> getNfsStatus() throws IOException {
		LOGGER.info("Getting nfs status");

		Map<String, String> nfsMap = new HashMap<String, String>();

//		String jsonPath = "/home/scriptuit/Downloads/unique-badge-276520-d6e270a9c112.json";
		LOGGER.info("project key file path in nfs : " + projectKeyFilePath);

		String uri = "https://file.googleapis.com/v1/";
		String requestListUri = uri + "projects/" + projectId + "/locations/" + zone + "/instances";

		GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(projectKeyFilePath))
				.createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));

		credentials.refresh();

		AccessToken token = credentials.getAccessToken();

		RestTemplate template = new RestTemplate();

		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "Bearer " + token.getTokenValue());
		headers.set("Content-Type", "application/json");

		HttpEntity<Object> entity = new HttpEntity<Object>(null, headers);

		ResponseEntity<String> resultList = template.exchange(requestListUri, HttpMethod.GET, entity, String.class);

		if (resultList.getBody() != null || !resultList.getBody().isEmpty()) {
			String strobj = resultList.getBody();
			String strnew = strobj.replaceAll("=", ":");

			JSONObject jobj = new JSONObject(strnew);

			if (!jobj.isEmpty()) {

				String state = "";
				String name = "";

				JSONArray jarr = jobj.getJSONArray("instances");

				for (int i = 0; i < jarr.length(); i++) {

					String strobj1 = jarr.get(i).toString();

					String strnew2 = strobj1.substring(1, strobj1.length() - 1);

					int stateIndex = 0;
					int fileIndex = 0;
					int tierIndex = 0;
					int networkIndex = 0;

					if (strnew2.contains("state")) {
						stateIndex = strobj1.indexOf("state", 0);
					}

					if (strnew2.contains("fileShares")) {
						fileIndex = strobj1.indexOf("fileShares", 0);
					}

					if (strnew2.contains("networks")) {
						networkIndex = strobj1.indexOf("networks", 0);
					}

					if (strnew2.contains("tier")) {
						tierIndex = strobj1.indexOf("tier", 0);
					}

					String strnew3 = strnew2.substring(fileIndex - 1, tierIndex - 3);

					if (strnew3.contains("name")) {
						int index2 = strnew3.indexOf("name");

						String substr = strnew3.substring(index2 - 1, strnew3.length() - 2);

						String substr1[] = substr.split(":");
						String namestr = substr1[1];

						name = namestr.substring(1, namestr.length() - 1);

					}

					String strnew4 = strnew2.substring(stateIndex - 1, networkIndex - 3);

					String substr2[] = strnew4.split(":");
					String statestr = substr2[1];

					state = statestr.substring(1, statestr.length() - 1);

					nfsMap.put(name, state);

				}
				return nfsMap;
			}

		}
		return null;
	}

}

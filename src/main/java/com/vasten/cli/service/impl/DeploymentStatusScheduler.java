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
import com.google.cloud.compute.v1.InstanceClient.ListInstancesPagedResponse;
import com.google.cloud.compute.v1.InstanceSettings;
import com.google.cloud.compute.v1.NetworkInterface;
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

/**
 * Scheduler for fetching status of deployment's cluster, instance and nfs from
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

	List<DeployStatus> deployStatusList = new ArrayList<DeployStatus>();

	@Scheduled(cron = "0 0/1 * * * *")
//	@Scheduled(cron = "10 * * * * *")
	public void statusScheduler() throws IOException, GeneralSecurityException {
		LOGGER.info("In the deployment status update scheduler");

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

		ListInstancesPagedResponse instanceList = this.getInstanceStatus();

		Map<String, String> nfsMap = this.getNfsStatus();

		if (!CollectionUtils.isEmpty(clusterMap)) {

			for (Map.Entry<String, String> entry : clusterMap.entrySet()) {
				DeployStatus deployStatus = new DeployStatus();

				LOGGER.info("In cluster map");
				LOGGER.info("cluster name : " + entry.getKey() + " & cluster status : " + entry.getValue());

				clusterName = entry.getKey();
				String name[] = clusterName.split("-");
				String newname = name[0];

				LOGGER.info("is " + clusterName + " pending : " + nameList.contains(newname));

				if (nameList.contains(newname)) {

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

					} else if ((status.equals("TERMINATED"))
							|| (status.equals("DELETED") || (status.equals("DELETING")))) {

						deployStatus.setStatus(DeploymentStatus.ERROR);
					}

					this.saveclusterdb(deployStatus);
				}

			}

		}

		for (Instance instance : instanceList.iterateAll()) {
			DeployStatus deployStatus = new DeployStatus();

			LOGGER.info("In instance status list");

			instanceName = instance.getName();
			instanceStatus = instance.getStatus();

			List<NetworkInterface> networkList = instance.getNetworkInterfacesList();

			String externalIp = "";

			for (NetworkInterface network : networkList) {

				for (AccessConfig accessConfig : network.getAccessConfigsList()) {
					externalIp = accessConfig.getNatIP();
				}
			}

			LOGGER.info("instance name : " + instanceName + " & instance status : " + instanceStatus
					+ " & external ip : " + externalIp);

			String name[] = instanceName.split("-");

			if (instanceName.startsWith("gke")) {
				LOGGER.info("instance name : " + instanceName);

				LOGGER.info("is " + name[1] + " cluster pending : " + nameList.contains(name[1]));

				if (nameList.contains(name[1])) {

					deployStatus.setDeploymentTypeName(instanceName);
					deployStatus.setType(DeploymentType.INSTANCE);

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

					deployStatus.setExternalIp(externalIp);
					this.saveinsdb(deployStatus);
				}
			}

		}

		if (!CollectionUtils.isEmpty(nfsMap)) {

			for (Map.Entry<String, String> entry : nfsMap.entrySet()) {
				DeployStatus deployStatus = new DeployStatus();

				LOGGER.info("in nfs map");

				nfsName = entry.getKey();
				nfsStatus = entry.getValue();

				LOGGER.info("nfs name : " + nfsName + " & nfs status : " + nfsStatus);

				LOGGER.info("is " + nfsName + " cluster pending : " + nameList.contains(nfsName));

				if (nameList.contains(nfsName)) {

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

					this.savenfsdb(deployStatus);
				}

			}

		}
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

	// Get the status of cluster from GCP console
	private Map<String, String> getClusterStatus() throws FileNotFoundException, IOException {
		LOGGER.info("Getting status of all deployments");

		Map<String, String> clusterMap = new HashMap<String, String>();
		String clusterName = "";
		String clusterStatus = "";

		GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(newProjectKeyFilePath))
				.createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));

		ClusterManagerSettings clusterManagerSettings = ClusterManagerSettings.newBuilder()
				.setCredentialsProvider(FixedCredentialsProvider.create(credentials)).build();

		ClusterManagerClient clusterManagerClient = ClusterManagerClient.create(clusterManagerSettings);
		ListClustersResponse response = clusterManagerClient.listClusters(newProjectId, newZone);

		for (Cluster cluster : response.getClustersList()) {
			clusterName = cluster.getName();

			clusterStatus = cluster.getStatus().toString();

			clusterMap.put(clusterName, clusterStatus);
		}

		clusterManagerClient.shutdown();

		return clusterMap;
	}

	// Get the status of instances created by the cluster from GCP console
	private ListInstancesPagedResponse getInstanceStatus() throws IOException {
		LOGGER.info("Getting all instances status");

		GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(newProjectKeyFilePath))
				.createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));

		InstanceSettings instanceSettings = InstanceSettings.newBuilder()
				.setCredentialsProvider(FixedCredentialsProvider.create(credentials)).build();

		InstanceClient instanceClient = InstanceClient.create(instanceSettings);
		ProjectZoneName projectZoneName = ProjectZoneName.of(newProjectId, newZone);

		ListInstancesPagedResponse instanceList = instanceClient.listInstances(projectZoneName);

		return instanceList;
	}

	// Get the status of file store from GCP console
	private Map<String, String> getNfsStatus() throws IOException {
		LOGGER.info("Getting nfs status");

		Map<String, String> nfsMap = new HashMap<String, String>();

		String uri = "https://file.googleapis.com/v1/";
		String requestListUri = uri + "projects/" + newProjectId + "/locations/" + newZone + "/instances";

		GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(newProjectKeyFilePath))
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

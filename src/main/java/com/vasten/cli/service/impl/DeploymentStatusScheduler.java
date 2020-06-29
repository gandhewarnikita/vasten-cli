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
import com.google.cloud.compute.v1.InstanceGroupManager;

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

	@Scheduled(cron = "0 0/1 * * * *")
//	@Scheduled(cron = "10 * * * * *")
	public void statusScheduler() throws IOException, GeneralSecurityException {
		LOGGER.info("In the deployment status update scheduler");

		String nfsName = "";
		String nfsStatus = "";

		List<Deployments> deploymentList = new ArrayList<Deployments>();

		deploymentList = deploymentsRepository.findAllByStatusAndIsDeletedFalse(DeploymentStatus.PENDING);

		List<String> nameList = new ArrayList<String>();

		for (Deployments dbDeployment : deploymentList) {
			nameList.add(dbDeployment.getName());
		}

		List<String> instanceGroupNameList = new ArrayList<String>();

		ListInstanceGroupManagersPagedResponse instanceGroupList = this.getInstanceGroup();

		LOGGER.info("If instance group list contains an instance group : "
				+ instanceGroupList.iterateAll().iterator().hasNext());

		Map<String, String> nfsMap = this.getNfsStatus();

		if (instanceGroupList.iterateAll().iterator().hasNext()) {

			for (InstanceGroupManager element : instanceGroupList.iterateAll()) {

				DeployStatus deployStatus = new DeployStatus();

				String instanceGroupName = "";
				boolean instanceGroupStatus;

				instanceGroupName = element.getName();
				instanceGroupNameList.add(instanceGroupName);

				instanceGroupStatus = element.getStatus().getIsStable();
				LOGGER.info("instance group status : " + instanceGroupStatus);

				if (nameList.contains(instanceGroupName)) {

					deployStatus.setDeploymentTypeName(instanceGroupName);
					deployStatus.setType(DeploymentType.INSTANCE_GROUP);

					Deployments dbDeploy = deploymentsRepository.findByNameAndIsDeletedFalse(instanceGroupName);
					deployStatus.setDeploymentId(dbDeploy);

					if (dbDeploy != null) {
						if (dbDeploy.getStatus().equals(DeploymentStatus.PENDING)) {
							dbDeploy.setStatus(DeploymentStatus.SUCCESS);
							deploymentsRepository.save(dbDeploy);
						}
					}

					if (instanceGroupStatus) {

						deployStatus.setStatus(DeploymentStatus.SUCCESS);

					} else {

						deployStatus.setStatus(DeploymentStatus.ERROR);
					}

					this.saveinsgroupdb(deployStatus);

					this.getInstanceStatus(nameList, instanceGroupNameList);
				}
			}

			if (!CollectionUtils.isEmpty(nfsMap)) {

				for (Map.Entry<String, String> entry : nfsMap.entrySet()) {

					DeployStatus deployStatus = new DeployStatus();

					LOGGER.info("in nfs map");

					nfsName = entry.getKey();
					nfsStatus = entry.getValue();

					LOGGER.info("nfs name : " + nfsName + " & nfs status : " + nfsStatus);

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
	}

	private void getInstanceStatus(List<String> instanceGroupNameList, List<String> nameList)
			throws FileNotFoundException, IOException {
		String name = "";
		String instance = "";
		String status = "";
		String externalIp = "";

		String uri = "https://compute.googleapis.com/compute/v1/";

		GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(newProjectKeyFilePath))
				.createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));

		credentials.refresh();

		AccessToken token = credentials.getAccessToken();

		if (!CollectionUtils.isEmpty(instanceGroupNameList)) {

			for (String instanceGroupName : instanceGroupNameList) {

				String requestListUri = uri + "projects/" + newProjectId + "/zones/" + zone
						+ "/instanceGroupManagers/" + instanceGroupName + "/listManagedInstances";

				RestTemplate template = new RestTemplate();

				HttpHeaders headers = new HttpHeaders();
				headers.set("Authorization", "Bearer " + token.getTokenValue());
				headers.set("Content-Type", "application/json");

				HttpEntity<Object> entity = new HttpEntity<Object>(null, headers);

				ResponseEntity<String> resultList = template.exchange(requestListUri, HttpMethod.POST, entity,
						String.class);

				LOGGER.info("result : " + resultList.getBody());

				JSONObject jobj = new JSONObject(resultList.getBody());

				if (!jobj.isEmpty()) {
					JSONArray jarr = jobj.getJSONArray("managedInstances");

					for (int i = 0; i < jarr.length(); i++) {
						DeployStatus deployStatus = new DeployStatus();

						JSONObject jobj2 = jarr.getJSONObject(i);

						instance = jobj2.getString("instance");
						status = jobj2.getString("instanceStatus");

						LOGGER.info("instance : " + instance + " & status : " + status);

						String instanceArr[] = instance.split("/");

						if (instanceArr.length != 0) {
							int index = instanceArr.length - 1;
							name = instanceArr[index];

							LOGGER.info("name : " + name);

							if (nameList.contains(instanceGroupName)) {
								
								deployStatus.setDeploymentTypeName(name);
								deployStatus.setType(DeploymentType.INSTANCE);

								Deployments dbDeploy = deploymentsRepository
										.findByNameAndIsDeletedFalse(instanceGroupName);
								deployStatus.setDeploymentId(dbDeploy);

								if (status.equals("RUNNING")) {

									deployStatus.setStatus(DeploymentStatus.SUCCESS);

								} else if (status.equals("PROVISIONING")) {

									deployStatus.setStatus(DeploymentStatus.PROVISIONING);

								} else if ((status.equals("TERMINATED")) || (status.equals("DELETING"))
										|| (status.equals("DELETED"))) {

									deployStatus.setStatus(DeploymentStatus.ERROR);
								}

								InstanceSettings instanceSettings = InstanceSettings.newBuilder()
										.setCredentialsProvider(FixedCredentialsProvider.create(credentials)).build();

								InstanceClient instanceClient = InstanceClient.create(instanceSettings);
								ProjectZoneName projectZoneName = ProjectZoneName.of(newProjectId, zone);

								ListInstancesPagedResponse instanceList = instanceClient.listInstances(projectZoneName);

								for (Instance instanceObj : instanceList.iterateAll()) {

									if ((instanceObj.getName().equals(name))
											&& (instanceObj.getStatus().equals(status))) {

										List<NetworkInterface> networkList = instanceObj.getNetworkInterfacesList();

										if (!CollectionUtils.isEmpty(networkList)) {

											for (NetworkInterface network : networkList) {

												if (!CollectionUtils.isEmpty(network.getAccessConfigsList())) {

													for (AccessConfig accessConfig : network.getAccessConfigsList()) {
														externalIp = accessConfig.getNatIP();
													}
												}
											}
										}
									}

									deployStatus.setExternalIp(externalIp);
									this.saveinsdb(deployStatus);

								}
							}
						}

					}
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

	private void saveinsgroupdb(DeployStatus deployStatus) {
		deployStatusRepository.save(deployStatus);

	}

	// Get all instance groups and their status from GCP
	private ListInstanceGroupManagersPagedResponse getInstanceGroup() throws FileNotFoundException, IOException {
		LOGGER.info("Getting all the instance groups");

		GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(newProjectKeyFilePath))
				.createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));

		InstanceGroupManagerSettings instanceGroupManagerSettings = InstanceGroupManagerSettings.newBuilder()
				.setCredentialsProvider(FixedCredentialsProvider.create(credentials)).build();

		InstanceGroupManagerClient instanceGroupManagerClient = InstanceGroupManagerClient
				.create(instanceGroupManagerSettings);

		ProjectZoneName projectZoneNameGroup = ProjectZoneName.of(newProjectId, zone);
		ListInstanceGroupManagersPagedResponse instanceGroupList = instanceGroupManagerClient
				.listInstanceGroupManagers(projectZoneNameGroup);

		return instanceGroupList;

	}

	// Get the status of file store from GCP console
	private Map<String, String> getNfsStatus() throws IOException {
		LOGGER.info("Getting nfs status");

		Map<String, String> nfsMap = new HashMap<String, String>();

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

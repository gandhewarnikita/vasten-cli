package com.vasten.cli.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStore;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.api.gax.paging.Page;
import com.google.api.services.container.Container;
import com.google.api.services.container.Container.Projects.Locations.Clusters;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.compute.v1.GetInstanceGroupManagerHttpRequest;
import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstanceClient;
import com.google.cloud.compute.v1.InstanceClient.ListInstancesPagedResponse;
import com.google.cloud.compute.v1.InstanceGroupManager;
import com.google.cloud.compute.v1.InstanceGroupManagerClient;
import com.google.cloud.compute.v1.InstanceGroupManagerClient.ListInstanceGroupManagersPagedResponse;
import com.google.cloud.compute.v1.InstanceGroupManagerList;
import com.google.cloud.compute.v1.InstanceGroupManagerSettings;
import com.google.cloud.compute.v1.InstanceSettings;
import com.google.cloud.compute.v1.ListInstanceGroupManagersHttpRequest;
import com.google.cloud.compute.v1.ProjectZoneInstanceGroupManagerName;
import com.google.cloud.compute.v1.ProjectZoneName;
import com.google.cloud.container.v1.ClusterManagerClient;
import com.google.cloud.container.v1.ClusterManagerSettings;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
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

	@Autowired
	private DeploymentsRepository deploymentsRepository;

	@Autowired
	private DeployStatusRepository deployStatusRepository;

	private static final String clusterStatusUrl = "https://container.googleapis.com/v1";

	// @Scheduled()
//	@Scheduled(cron = "0/10 * * * * *")
	public void statusScheduler() throws IOException, GeneralSecurityException {

		LOGGER.info("in the scheduler");

//		Deployments dbDeployment = new Deployments();
//		DeployStatus deployStatus = new DeployStatus();

		String clusterName = "";
		String status = "";

		List<Deployments> deploymentList = new ArrayList<Deployments>();

		deploymentList = deploymentsRepository.findAllByStatus(DeploymentStatus.PENDING);

		Set<DeployStatus> deploySet = new HashSet<DeployStatus>();

//		List<String> nameList = new ArrayList<String>();
//
//		for (Deployments dbDeployment : deploymentList) {
//			nameList.add(dbDeployment.getName());
//		}

		List<DeployStatus> deployStatusList = new ArrayList<DeployStatus>();

		// Map<String, String> clusterStatus = new HashMap<String, String>();

		ListClustersResponse result = this.getStatus(projectId, zone);
		// LOGGER.info("result : " + result.getClustersList());

		for (Cluster cluster : result.getClustersList()) {

			DeployStatus deployStatus = new DeployStatus();
			clusterName = cluster.getName();
			status = cluster.getStatus().toString();

			LOGGER.info("clusterName : " + clusterName);
			LOGGER.info("status : " + status);

			// clusterStatus.put(clusterName, status);

			deployStatus.setName(clusterName);

			if (status.equals("RUNNING")) {
				deployStatus.setStatus(DeploymentStatus.SUCCESS);
			} else {
				deployStatus.setStatus(DeploymentStatus.ERROR);
			}

			deploySet.add(deployStatus);
		}

//		List<DeployStatus> dbDeployList = deployStatusRepository.findAll();
//
//		for (DeployStatus deployObj : deployStatusList) {
//			if (!dbDeployList.contains(deployObj)) {
//				LOGGER.info("ans : " + !dbDeployList.contains(deployObj));
//				// deployStatusRepository.save(deployObj);
//				dbDeployList.add(deployStatus);
//			}
//		}
//
//		for (DeployStatus obj : dbDeployList) {
//			deployStatusRepository.save(obj);
//		}

		List<DeployStatus> dbDeployList = deployStatusRepository.findAll();

		Iterator itr = deploySet.iterator();

		while (itr.hasNext()) {
			DeployStatus obj = new DeployStatus();
			obj = (DeployStatus) itr.next();

			if (!dbDeployList.contains(obj)) {
				deployStatusRepository.save(obj);
			}

		}

	}

	private ListClustersResponse getStatus(String projectId, String zone) throws FileNotFoundException, IOException {
		LOGGER.info("Fetching all clusters");

		String jsonPath = "/home/scriptuit/Downloads/gold-braid-268003-fa0b37fc4447.json";

		GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(jsonPath))
				.createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));

		ClusterManagerSettings clusterManagerSettings = ClusterManagerSettings.newBuilder()
				.setCredentialsProvider(FixedCredentialsProvider.create(credentials)).build();

		ClusterManagerClient clusterManagerClient = ClusterManagerClient.create(clusterManagerSettings);
		ListClustersResponse response = clusterManagerClient.listClusters(projectId, zone);

		return response;
	}

//	@Scheduled(cron = "0/10 * * * * *")
	private void getStatusInstance() throws FileNotFoundException, IOException {
		LOGGER.info("in getStatusInstance() method");

//		String jsonPath = "/home/scriptuit/Downloads/academic-torch-248600-c81edA9634bd5.json";
//
//		GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(jsonPath))
//				.createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));
//
//		InstanceGroupManagerSettings instanceGroupManagerSettings = InstanceGroupManagerSettings.newBuilder()
//				.setCredentialsProvider(FixedCredentialsProvider.create(credentials)).build();
//
//		InstanceGroupManagerClient instanceGroupManagerClient = InstanceGroupManagerClient
//				.create(instanceGroupManagerSettings);
//
//		String formattedZone = ProjectZoneName.format(newProjectId, newZone);
//		ListInstanceGroupManagersHttpRequest request = ListInstanceGroupManagersHttpRequest.newBuilder()
//				.setZone(formattedZone).build();
//		ListInstanceGroupManagersPagedResponse response = instanceGroupManagerClient.listInstanceGroupManagers(request);
//
//		for (InstanceGroupManager element : instanceGroupManagerClient.listInstanceGroupManagers(request)
//				.iterateAll()) {
//
//			LOGGER.info("response : " + element);
//		}

		String instanceName = "";
		String instanceStatus = "";
		DeployStatus deployInstance = new DeployStatus();

		String jsonPath = "/home/scriptuit/Downloads/gold-braid-268003-fa0b37fc4447.json";

		GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(jsonPath))
				.createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));

		InstanceSettings instanceSettings = InstanceSettings.newBuilder()
				.setCredentialsProvider(FixedCredentialsProvider.create(credentials)).build();

		InstanceClient instanceClient = InstanceClient.create(instanceSettings);
		ProjectZoneName projectZoneName = ProjectZoneName.of(projectId, newZone);

		ListInstancesPagedResponse instanceList = instanceClient.listInstances(projectZoneName);

//		LOGGER.info("response : " + instanceList);

		for (Instance instance : instanceList.iterateAll()) {
			LOGGER.info("instance response : " + instance);
			LOGGER.info("*********************************************************************");

			instanceName = instance.getName();
			instanceStatus = instance.getStatus();

			LOGGER.info("instanceName : " + instanceName + " & instanceStatus : " + instanceStatus);

			deployInstance.setInstanceName(instanceName);

			if (instanceStatus.equals("RUNNING")) {
				deployInstance.setInstanceStatus(DeploymentStatus.SUCCESS);

			} else if (instanceStatus.equals("TERMINATED")) {
				deployInstance.setInstanceStatus(DeploymentStatus.ERROR);
			}

			deployStatusRepository.save(deployInstance);

		}

	}
	
//	@Scheduled(cron = "0/10 * * * * *")
	private void getNfsStatus() {
		LOGGER.info("Getting nfs status");
		
		
	}

}

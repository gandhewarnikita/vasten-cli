package com.vasten.cli.service.impl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.FieldValue;
import com.google.cloud.bigquery.FieldValueList;
import com.google.cloud.bigquery.JobException;
import com.google.cloud.bigquery.QueryJobConfiguration;
import com.google.cloud.bigquery.QueryParameterValue;
import com.google.cloud.bigquery.TableResult;
import com.vasten.cli.entity.Clients;
import com.vasten.cli.entity.DeploymentCost;
import com.vasten.cli.entity.DeploymentType;
import com.vasten.cli.entity.Deployments;
import com.vasten.cli.entity.User;
import com.vasten.cli.repository.ClientsRepository;
import com.vasten.cli.repository.DeploymentCostRepository;
import com.vasten.cli.repository.DeploymentsRepository;
import com.vasten.cli.repository.UserRepository;

@Component
public class DeploymentCostScheduler {

	private static final Logger LOGGER = LoggerFactory.getLogger(DeploymentCostScheduler.class);

	@Value("${NEW_PROJECT_KEYFILE_PATH}")
	private String newProjectKeyFilePath;

	final String LABEL_KEY_DEPLOYMENT_NAME = "deployment_name";

	@Autowired
	private DeploymentsRepository deploymentsRepository;

	@Autowired
	private DeploymentCostRepository deploymentCostRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ClientsRepository clientsRepository;

	@Scheduled(cron = "0 0/5 * * * *")
//	@Scheduled(cron = "10 * * * * *")
	private void costScheduler() throws JobException, InterruptedException, FileNotFoundException, IOException {
		LOGGER.info("In the deployment cost scheduler");

		List<Deployments> deploymentList = new ArrayList<Deployments>();

		deploymentList = deploymentsRepository.findAll();

		if (deploymentList != null) {
			for (Deployments deployment : deploymentList) {
				this.getTotalCost(deployment);
			}
		}

	}

	private void getTotalCost(Deployments deployment)
			throws JobException, InterruptedException, FileNotFoundException, IOException {
		LOGGER.info("Getting compute cost of deployment : " + deployment.getName());

		String deploymentName = deployment.getName();

		List<Object> objList = new ArrayList<Object>();

		DeploymentCost dbDeploymentCost = null;

		BigQuery bigquery = BigQueryOptions.newBuilder()
				.setCredentials(ServiceAccountCredentials.fromStream(new FileInputStream(newProjectKeyFilePath)))
				.build().getService();

		LocalDate date = LocalDate.now();
		ZonedDateTime startOfDay = date.atStartOfDay(ZoneId.of("UTC"));
		ZonedDateTime endOfDay = ZonedDateTime.of(date, LocalTime.MAX, ZoneId.of("UTC"));

		DateTimeFormatter startformatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");
		String startformatter1 = startOfDay.format(startformatter);

		DateTimeFormatter endformatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");
		String endformatter1 = endOfDay.format(endformatter);

		String query = "SELECT labels.key as key, labels.value as value, \n"
				+ "SUM(cost) AS total, (SUM(CAST(cost * 1000000 AS int64))) / 1000000 AS total_exact\n"
				+ "				FROM `tactile-acolyte-282822.MyFirstProject_Dataset.gcp_billing_export_v1_017421_A19C6D_252A9A`\n"
				+ "	LEFT JOIN UNNEST(labels) as labels\n" + "			WHERE key = \"deployment_name\" AND value = \""
				+ deploymentName + "\" AND usage_start_time >= \"" + startformatter1 + "\" AND usage_end_time <= \""
				+ endformatter1 + "\" GROUP BY key, value";

		LOGGER.info("query : " + query);
		LOGGER.info("\n");

		QueryJobConfiguration computeQueryConfig = QueryJobConfiguration.newBuilder(query).setUseLegacySql(false)
				.build();

		TableResult dataList = bigquery.query(computeQueryConfig);

		if (dataList != null) {
			if (dataList.iterateAll().iterator().hasNext()) {
				for (FieldValueList row : dataList.iterateAll()) {
					for (FieldValue val : row) {
						LOGGER.info("value of query : " + val);
						objList.add(val.getValue());
						LOGGER.info("\n");
					}
					LOGGER.info("\n");
				}
			}
		}

		if (objList != null && !objList.isEmpty()) {

			String cost = objList.get(3).toString();
			Double totalCost = Double.valueOf(cost);
			LOGGER.info("totalCost : " + totalCost);

			User dbUser = null;
			Clients dbClient = null;

			Deployments dbDeployment = deploymentsRepository.findOneByName(deploymentName);
			dbUser = userRepository.findOneById(dbDeployment.getUser().getId());

			if (dbUser != null) {
				dbClient = clientsRepository.findOneById(dbUser.getClients().getId());
			}

			dbDeploymentCost = deploymentCostRepository
					.findOneByDeploymentTypeNameAndDeploymentIdAndUsageDate(deploymentName, deployment, date);

			if (dbDeploymentCost == null) {
				LOGGER.info("dbDeploymentCost is null");
				dbDeploymentCost = new DeploymentCost();

				String name = (String) objList.get(1);

				dbDeploymentCost.setDeploymentId(deployment);
				dbDeploymentCost.setDeploymentTypeName(name);
				dbDeploymentCost.setTotalCost(totalCost);
				dbDeploymentCost.setCostLastUpdated(new Date());
				dbDeploymentCost.setUsageDate(date);

				if (dbClient != null) {
					dbDeploymentCost.setClientName(dbClient);
				}

			} else {
				dbDeploymentCost.setTotalCost(totalCost);
				dbDeploymentCost.setCostLastUpdated(new Date());
			}

			LOGGER.info("total cost of deployment " + deploymentName + " is = " + totalCost);
			deploymentCostRepository.save(dbDeploymentCost);

		}

	}

}
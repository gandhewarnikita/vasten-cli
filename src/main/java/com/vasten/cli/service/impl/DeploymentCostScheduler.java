package com.vasten.cli.service.impl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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
import com.vasten.cli.entity.DeploymentCost;
import com.vasten.cli.entity.DeploymentType;
import com.vasten.cli.entity.Deployments;
import com.vasten.cli.repository.DeploymentCostRepository;
import com.vasten.cli.repository.DeploymentsRepository;

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

//	@Scheduled(cron = "0 0/5 * * * *")
	@Scheduled(cron = "10 * * * * *")
	private void costScheduler() throws JobException, InterruptedException, FileNotFoundException, IOException {
		LOGGER.info("In the deployment cost scheduler");

		List<Deployments> deploymentList = new ArrayList<Deployments>();

		deploymentList = deploymentsRepository.findAll();

		if (deploymentList != null) {
			for (Deployments deployment : deploymentList) {
				this.getCost(deployment);
			}
		}

	}

	private void getCost(Deployments deployment)
			throws JobException, InterruptedException, FileNotFoundException, IOException {
		LOGGER.info("Getting cost of deployment : " + deployment.getName());

		String deploymentName = deployment.getName();

		// BigQuery bigquery = BigQueryOptions.getDefaultInstance().getService();

		BigQuery bigquery = BigQueryOptions.newBuilder()
				.setCredentials(ServiceAccountCredentials.fromStream(new FileInputStream(newProjectKeyFilePath)))
				.build().getService();

		String computeQuery = "SELECT labels.key as key, labels.value as value,\n"
				+ "SUM(cost) + SUM(IFNULL((SELECT SUM(c.amount) FROM   UNNEST(credits) c), 0)) AS total, (SUM(CAST(cost * 1000000 AS int64))+\n"
				+ "SUM(IFNULL((SELECT SUM(CAST(c.amount * 1000000 as int64))\n"
				+ "			 FROM UNNEST(credits) c), 0))) / 1000000 AS total_exact\n"
				+ "				FROM `tactile-acolyte-282822.MyFirstProject_Dataset.gcp_billing_export_v1_017421_A19C6D_252A9A`\n"
				+ "	LEFT JOIN UNNEST(labels) as labels\n"
				+ "			WHERE service.description = \"Compute Engine\" AND key = \"deployment_name\" AND value = \""
				+ deploymentName + "\" GROUP BY key, value";

		LOGGER.info("computeQuery : " + computeQuery);
		LOGGER.info("\n");

		QueryJobConfiguration computeQueryConfig = QueryJobConfiguration.newBuilder(computeQuery).setUseLegacySql(false)
				.build();

//		TableResult results = bigquery.query(queryConfig);
//
//		results.iterateAll().forEach(row -> row.forEach(val -> LOGGER.info("%s,", val.toString())));
//
//		LOGGER.info("Query with named parameters performed successfully.");

		// Print the results.
		for (FieldValueList row : bigquery.query(computeQueryConfig).iterateAll()) {
			for (FieldValue val : row) {
				LOGGER.info("\n");
				LOGGER.info("value : " + val.toString());
				LOGGER.info("val.getValue() : " + val.getValue());
				LOGGER.info("\n");

				Double computeCost = 0.0;

				String var = val.getValue().toString();
				LOGGER.info("var = " + var);

				if (var.matches("[0-9.]*")) {
					computeCost = Double.valueOf(var);
					LOGGER.info("Compute Cost : " + computeCost);
					break;
				}

				if (val.getValue().equals(deploymentName)) {
					// Store the cost of the deployment in Db
					DeploymentCost dbDeploymentCost = deploymentCostRepository
							.findOneByDeploymentTypeNameAndTypeAndDeploymentId(deploymentName, DeploymentType.INSTANCE,
									deployment);

					if (dbDeploymentCost == null) {
						LOGGER.info("dbDeploymentCost is null");
						dbDeploymentCost = new DeploymentCost();
						dbDeploymentCost.setDeploymentId(deployment);
						dbDeploymentCost.setDeploymentTypeName(deploymentName);
						dbDeploymentCost.setType(DeploymentType.INSTANCE);
						dbDeploymentCost.setComputeCost(computeCost);
						dbDeploymentCost.setCostLastUpdated(new Date());
					} else {
						dbDeploymentCost.setComputeCost(computeCost);
						dbDeploymentCost.setCostLastUpdated(new Date());
					}

					deploymentCostRepository.save(dbDeploymentCost);
				}

			}
			LOGGER.info("\n");
		}

		String fileStoreQuery = "SELECT labels.key as key, labels.value as value,\n"
				+ "SUM(cost) + SUM(IFNULL((SELECT SUM(c.amount) FROM   UNNEST(credits) c), 0)) AS total, (SUM(CAST(cost * 1000000 AS int64))+\n"
				+ "SUM(IFNULL((SELECT SUM(CAST(c.amount * 1000000 as int64))\n"
				+ "			 FROM UNNEST(credits) c), 0))) / 1000000 AS total_exact\n"
				+ "				FROM `tactile-acolyte-282822.MyFirstProject_Dataset.gcp_billing_export_v1_017421_A19C6D_252A9A`\n"
				+ "	LEFT JOIN UNNEST(labels) as labels\n"
				+ "			WHERE service.description = \"Cloud Filestore\" AND key = \"deployment_name\" AND value = \""
				+ deploymentName + "\" GROUP BY key, value";

		QueryJobConfiguration filestoreQueryConfig = QueryJobConfiguration.newBuilder(fileStoreQuery)
				.setUseLegacySql(false).build();

		for (FieldValueList row : bigquery.query(filestoreQueryConfig).iterateAll()) {
			for (FieldValue val : row) {
				LOGGER.info("\n");
				LOGGER.info("\n");
				LOGGER.info("value gugu : " + val.toString());
				LOGGER.info("val.getValue() hgyugiu : " + val.getValue());
				LOGGER.info("\n");

				Double networkCost = 0.0;

				String var = val.getValue().toString();
				LOGGER.info("var huhui = " + var);

				if (var.matches("[0-9.]*")) {
					networkCost = Double.valueOf(var);
					LOGGER.info("Network Cost : " + networkCost);
					break;
				}

				if (val.getValue().equals(deploymentName)) {
					// Store the cost of the deployment in Db
					DeploymentCost dbDeploymentCost = deploymentCostRepository
							.findOneByDeploymentTypeNameAndTypeAndDeploymentId(deploymentName, DeploymentType.INSTANCE,
									deployment);

					if (dbDeploymentCost == null) {
						LOGGER.info("dbDeploymentCost is null");
						dbDeploymentCost = new DeploymentCost();
						dbDeploymentCost.setDeploymentId(deployment);
						dbDeploymentCost.setDeploymentTypeName(deploymentName);
						dbDeploymentCost.setType(DeploymentType.INSTANCE);
						dbDeploymentCost.setNetworkCost(networkCost);
						dbDeploymentCost.setCostLastUpdated(new Date());
					} else {
						dbDeploymentCost.setNetworkCost(networkCost);
						dbDeploymentCost.setCostLastUpdated(new Date());
					}

					deploymentCostRepository.save(dbDeploymentCost);
				}

			}
			LOGGER.info("\n");
		}

	}

}
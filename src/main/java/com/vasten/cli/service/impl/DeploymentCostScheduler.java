package com.vasten.cli.service.impl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
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
import com.google.cloud.bigquery.JobException;
import com.google.cloud.bigquery.QueryJobConfiguration;
import com.google.cloud.bigquery.QueryParameterValue;
import com.google.cloud.bigquery.TableResult;
import com.vasten.cli.entity.Deployments;
import com.vasten.cli.repository.DeploymentsRepository;

@Component
public class DeploymentCostScheduler {

	private static final Logger LOGGER = LoggerFactory.getLogger(DeploymentCostScheduler.class);

	@Value("${NEW_PROJECT_KEYFILE_PATH}")
	private String newProjectKeyFilePath;

	final String LABEL_KEY_DEPLOYMENT_NAME = "deployment_name";

	@Autowired
	private DeploymentsRepository deploymentsRepository;

//	@Scheduled(cron = "0 0/5 * * * *")
	@Scheduled(cron = "10 * * * * *")
	private void costScheduler() throws JobException, InterruptedException, FileNotFoundException, IOException {
		LOGGER.info("In the deployment cost scheduler");

		List<Deployments> deploymentList = new ArrayList<Deployments>();

		deploymentList = deploymentsRepository.findAll();

		if (deploymentList != null) {
			for (Deployments deployment : deploymentList) {
				this.getCost(deployment.getName());
			}
		}

	}

	private void getCost(String name) throws JobException, InterruptedException, FileNotFoundException, IOException {
		LOGGER.info("Getting cost of deployment : " + name);

		String deploymentName = name;

		// BigQuery bigquery = BigQueryOptions.getDefaultInstance().getService();

		BigQuery bigquery = BigQueryOptions.newBuilder()
				.setCredentials(ServiceAccountCredentials.fromStream(new FileInputStream(newProjectKeyFilePath)))
				.build().getService();

		String query = "SELECT\n" + "  labels.key as key,\n" + "  labels.value as value,\n" + "    SUM(cost)\n"
				+ "    + SUM(IFNULL((SELECT SUM(c.amount)\n" + "                  FROM   UNNEST(credits) c), 0))\n"
				+ "    AS total,\n" + "  (SUM(CAST(cost * 1000000 AS int64))\n"
				+ "    + SUM(IFNULL((SELECT SUM(CAST(c.amount * 1000000 as int64))\n"
				+ "                  FROM UNNEST(credits) c), 0))) / 1000000\n" + "    AS total_exact\n"
				+ "FROM `bold-vial-279601.vasten_cloud_dataset.gcp_billing_export_v1_0126B4_C5CBF1_516B59` \n"
				+ "LEFT JOIN UNNEST(labels) as labels\n"
				+ "WHERE service.description = \"Compute Engine\" AND sku.description = \"N1 Predefined Instance Core running in Americas\" AND key = \"deployment_name\"\\"
				+ "AND value = \"@deploymentName\" GROUP BY key, value";

		QueryJobConfiguration queryConfig = QueryJobConfiguration.newBuilder(query)
				.addNamedParameter("deploymentName", QueryParameterValue.string(deploymentName)).build();

		TableResult results = bigquery.query(queryConfig);

		results.iterateAll().forEach(row -> row.forEach(val -> LOGGER.info("%s,", val.toString())));

		LOGGER.info("Query with named parameters performed successfully.");

	}

}
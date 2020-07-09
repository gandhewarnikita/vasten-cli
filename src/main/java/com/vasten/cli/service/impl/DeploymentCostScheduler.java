package com.vasten.cli.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.vasten.cli.entity.Deployments;
import com.vasten.cli.repository.DeploymentsRepository;

@Component
public class DeploymentCostScheduler {

	private static final Logger LOGGER = LoggerFactory.getLogger(DeploymentCostScheduler.class);

	final String LABEL_KEY_DEPLOYMENT_NAME = "deployment_name";

	@Autowired
	private DeploymentsRepository deploymentsRepository;

//	@Scheduled(cron = "0 0/10 ")
//	@Scheduled(cron = "10 *")
	private void costScheduler() {
		LOGGER.info("In the deployment cost scheduler");

		List<Deployments> deploymentList = new ArrayList<Deployments>();

		deploymentList = deploymentsRepository.findAll();
		
		this.getCost();

	}

	private void getCost() {
		// TODO Auto-generated method stub
		
	}
}
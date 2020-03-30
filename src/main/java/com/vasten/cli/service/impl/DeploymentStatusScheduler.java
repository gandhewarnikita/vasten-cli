package com.vasten.cli.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.vasten.cli.entity.DeploymentStatus;
import com.vasten.cli.entity.Deployments;
import com.vasten.cli.repository.DeploymentsRepository;

@Component
public class DeploymentStatusScheduler {

	private static final Logger LOGGER = LoggerFactory.getLogger(DeploymentStatusScheduler.class);
	
	@Autowired
	private DeploymentsRepository deploymentsRepository;
	
	//@Scheduled()
	public void statusScheduler() {
		List<Deployments> deploymentList = new ArrayList<Deployments>();
		
		deploymentList = deploymentsRepository.findAllByStatus(DeploymentStatus.PENDING);
	}
}

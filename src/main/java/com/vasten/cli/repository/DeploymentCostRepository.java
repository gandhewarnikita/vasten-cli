package com.vasten.cli.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vasten.cli.entity.DeploymentCost;
import com.vasten.cli.entity.DeploymentType;
import com.vasten.cli.entity.Deployments;

@Repository
public interface DeploymentCostRepository extends JpaRepository<DeploymentCost, Integer> {

	public DeploymentCost findOneByDeploymentTypeNameAndTypeAndDeploymentId(String deploymentName,
			DeploymentType instance, Deployments deployment);

	public List<DeploymentCost> findByDeploymentId(Deployments dbDeployment);

	public DeploymentCost findOneByDeploymentId(Deployments dbDeployment);

}

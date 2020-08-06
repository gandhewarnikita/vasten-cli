package com.vasten.cli.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vasten.cli.entity.Clients;
import com.vasten.cli.entity.DeploymentCost;
import com.vasten.cli.entity.DeploymentType;
import com.vasten.cli.entity.Deployments;

@Repository
public interface DeploymentCostRepository extends JpaRepository<DeploymentCost, Integer> {

//	public DeploymentCost findOneByDeploymentTypeNameAndTypeAndDeploymentId(String deploymentName,
//			DeploymentType instance, Deployments deployment);

	public List<DeploymentCost> findByDeploymentId(Deployments dbDeployment);

	public DeploymentCost findOneByDeploymentId(Deployments dbDeployment);

//	public DeploymentCost findOneByDeploymentTypeNameAndTypeAndDeploymentIdAndUsageDataCost(String deploymentName,
//			DeploymentType instance, Deployments deployment, LocalDate date);

//	public DeploymentCost findOneByDeploymentTypeNameAndDeploymentIdAndUsageDataCost(String deploymentName,
//			Deployments deployment, LocalDate date);
//
//	public List<DeploymentCost> findByDeploymentIdAndUsageDataCostBetween(Deployments deployment,
//			LocalDate localStartDate, LocalDate date);

	public List<DeploymentCost> findByDeploymentIdAndUsageDateBetween(Deployments deployment, LocalDate localStartDate,
			LocalDate date);

	public DeploymentCost findOneByDeploymentTypeNameAndDeploymentIdAndUsageDate(String deploymentName,
			Deployments deployment, LocalDate date);

	public List<DeploymentCost> findByDeploymentIdOrderByUsageDateDesc(Deployments deploymentObj);

	public List<DeploymentCost> findByDeploymentIdOrderByCostLastUpdatedDesc(Deployments deploymentObj);

	public List<DeploymentCost> findAllByClientIdOrderByCostLastUpdatedDesc(Clients dbClient);

	public List<DeploymentCost> findAllByClientIdAndUsageDateBetweenOrderByCostLastUpdatedDesc(Clients dbClient,
			LocalDate localStartDate, LocalDate date);

}

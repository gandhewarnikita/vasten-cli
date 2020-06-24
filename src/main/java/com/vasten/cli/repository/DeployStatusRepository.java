package com.vasten.cli.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vasten.cli.entity.DeployStatus;
import com.vasten.cli.entity.DeploymentType;
import com.vasten.cli.entity.Deployments;

/**
 * Repository for DeployStatus related activity
 * 
 * @author scriptuit
 *
 */
@Repository
public interface DeployStatusRepository extends JpaRepository<DeployStatus, Integer> {

	List<DeployStatus> findAllByDeploymentId(Deployments dbDeployment);

	DeployStatus findOneByDeploymentIdAndStatus(Deployments dbDeploy, DeploymentType nfs);
}

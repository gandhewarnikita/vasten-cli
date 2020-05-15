package com.vasten.cli.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.vasten.cli.entity.DeployStatus;
import com.vasten.cli.entity.DeploymentType;
import com.vasten.cli.entity.Deployments;

@Repository
public interface DeployStatusRepository extends JpaRepository<DeployStatus, Integer> {

	List<DeployStatus> findAllByDeploymentId(Deployments dbDeployment);
}

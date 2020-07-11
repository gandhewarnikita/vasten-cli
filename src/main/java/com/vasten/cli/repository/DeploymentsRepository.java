package com.vasten.cli.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vasten.cli.entity.DeploymentStatus;
import com.vasten.cli.entity.Deployments;
import com.vasten.cli.entity.User;

/**
 * Repository for Deployment related activity
 * 
 * @author scriptuit
 *
 */
@Repository
public interface DeploymentsRepository extends JpaRepository<Deployments, Integer> {

	public Deployments findByName(String name);

	public List<Deployments> findAllByStatus(DeploymentStatus pending);

	public List<Deployments> findAllByUser(User dbUser);

	public Deployments findByUserAndName(User dbUser, String name);

	public Deployments findByNameAndIsDeletedFalse(String name);

	public Deployments findByUserAndNameAndIsDeletedFalse(User dbUser, String name);

	public List<Deployments> findAllByStatusAndIsDeletedFalse(DeploymentStatus pending);

	public Deployments findOneByIdAndIsDeletedFalse(int deploymentId);

	public List<Deployments> findAllByUserAndIsDeletedFalse(User dbUser);

	public Deployments findByUserAndIdAndIsDeletedFalse(User dbUser, Integer deploymentId);

	public Deployments findOneByIdAndUserAndIsDeletedFalse(User dbUser, int deploymentId);

	public Deployments findOneById(int deploymentId);

}

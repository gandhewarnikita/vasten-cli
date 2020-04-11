package com.vasten.cli.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vasten.cli.entity.Clients;
import com.vasten.cli.entity.DeploymentStatus;
import com.vasten.cli.entity.Deployments;
import com.vasten.cli.entity.User;

@Repository
public interface DeploymentsRepository extends JpaRepository<Deployments, Integer> {

	public Deployments findByName(String name);

//	public List<Deployments> findAllByClients(Clients dbClient);
//
//	public Deployments findByClientsAndName(Clients dbClient, String name);

	public List<Deployments> findAllByStatus(DeploymentStatus pending);

	public List<Deployments> findAllByUser(User dbUser);

	public Deployments findByUserAndName(User dbUser, String name);

	public Deployments findByNameAndIsDeletedFalse(String name);

	public Deployments findByUserAndNameAndIsDeletedFalse(User dbUser, String name);

}

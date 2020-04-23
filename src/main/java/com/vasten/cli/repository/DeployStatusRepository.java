package com.vasten.cli.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vasten.cli.entity.DeployStatus;

@Repository
public interface DeployStatusRepository extends JpaRepository<DeployStatus, Integer> {

	public DeployStatus findByName(String name);

}

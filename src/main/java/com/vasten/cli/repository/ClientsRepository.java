package com.vasten.cli.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vasten.cli.entity.Clients;

/**
 * Repository for Client related activity
 * 
 * @author scriptuit
 *
 */
@Repository
public interface ClientsRepository extends JpaRepository<Clients, Integer> {

	public Clients findOneById(int id);

	public Clients findByName(String name);

}

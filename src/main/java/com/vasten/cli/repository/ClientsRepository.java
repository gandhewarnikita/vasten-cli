package com.vasten.cli.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vasten.cli.entity.Clients;

@Repository
public interface ClientsRepository extends JpaRepository<Clients, Integer> {

	public Clients findByEmail(String email);

	public Clients findOneById(int id);

}

package com.vasten.cli.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vasten.cli.entity.Clients;
import com.vasten.cli.entity.User;

/**
 * Repository for User related activity
 * 
 * @author scriptuit
 *
 */
@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

	public User findByEmail(String email);

	public User findOneById(int id);

	public User findOneByEmail(String email);

	public List<User> findAllByClients(Clients dbClient);

}

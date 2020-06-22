package com.vasten.cli.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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

}

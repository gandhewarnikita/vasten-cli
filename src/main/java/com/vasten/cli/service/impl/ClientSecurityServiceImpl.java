package com.vasten.cli.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.vasten.cli.entity.Clients;
import com.vasten.cli.repository.ClientsRepository;

@Service(value = "clientsService")
public class ClientSecurityServiceImpl implements UserDetailsService {

	@Autowired
	private ClientsRepository clientsRepository;
	
	@Override
	public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
		Clients client = clientsRepository.findByEmail(userId);
		if(client == null) {
			throw new UsernameNotFoundException("Invalid username or password.");
		}
		return new org.springframework.security.core.userdetails.User(client.getEmail(), client.getPassword(), getAuthority());
	}

	private List<SimpleGrantedAuthority> getAuthority() {
		return Arrays.asList(new SimpleGrantedAuthority("ROLE_ADMIN"));
	}

	public List<Clients> findAll() {
		List<Clients> list = new ArrayList<>();
		clientsRepository.findAll().iterator().forEachRemaining(list::add);
		return list;
	}
}

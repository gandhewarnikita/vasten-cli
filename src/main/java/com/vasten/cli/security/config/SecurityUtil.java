package com.vasten.cli.security.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import com.vasten.cli.repository.ClientsRepository;

@Component
public class SecurityUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(SecurityUtil.class);

	@Autowired
	private ClientsRepository clientsRepository;

	public static User loggedInUser() throws Exception {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication instanceof AnonymousAuthenticationToken) {
			throw new Exception("No user session available.");
		}

		LOGGER.info("Authentication : "+(User) authentication.getPrincipal());
		return (User) authentication.getPrincipal();
	}

	public com.vasten.cli.entity.Clients getLoggedInUser() {
		try {
			User userFound = loggedInUser();
			String email = userFound.getUsername();
			LOGGER.info("Client Email: " + email);
			LOGGER.info("Client : " + userFound);
			com.vasten.cli.entity.Clients client = clientsRepository.findByEmail(email);
			return client;
		} catch (Exception e) {
			LOGGER.error("Exception occured while getting the logged in user: ", e);

		}

		return null;
	}
}

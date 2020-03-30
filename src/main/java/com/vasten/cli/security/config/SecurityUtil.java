package com.vasten.cli.security.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import com.vasten.cli.repository.UserRepository;

@Component
public class SecurityUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(SecurityUtil.class);

	@Autowired
	private UserRepository userRepository;

	public static User loggedInUser() throws Exception {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication instanceof AnonymousAuthenticationToken) {
			throw new Exception("No user session available.");
		}

		LOGGER.info("Authentication : " + (User) authentication.getPrincipal());
		return (User) authentication.getPrincipal();
	}

	public com.vasten.cli.entity.User getLoggedInUser() {
		try {
			User userFound = loggedInUser();
			String email = userFound.getUsername();
			LOGGER.info("User Email: " + email);
			LOGGER.info("User : " + userFound);
			com.vasten.cli.entity.User user = userRepository.findByEmail(email);
			return user;
		} catch (Exception e) {
			LOGGER.error("Exception occured while getting the logged in user: ", e);

		}

		return null;
	}
}

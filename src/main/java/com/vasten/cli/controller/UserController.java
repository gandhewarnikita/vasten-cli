package com.vasten.cli.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.vasten.cli.entity.User;
import com.vasten.cli.security.config.SecurityUtil;
import com.vasten.cli.service.UserService;

@RestController
public class UserController {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private SecurityUtil securityUtil;
	
	@RequestMapping(value = "/api/user", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public User create(@RequestBody User userData) {
		LOGGER.info("Api received to create user");
		User newUser = userService.create(userData);
		return newUser;
	}
	
	@RequestMapping(value = "/loggedIn", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public User getLoggedInUser() {
		return securityUtil.getLoggedInUser();
	}
}

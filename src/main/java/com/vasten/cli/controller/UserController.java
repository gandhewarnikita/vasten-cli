package com.vasten.cli.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.token.TokenStore;
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

	@Autowired
	private TokenStore tokenStore;

	@RequestMapping(value = "/user", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public User create(@RequestBody User userData) {
		LOGGER.info("Api received to create user");
		User newUser = userService.create(userData);
		return newUser;
	}

	@RequestMapping(value = "/api/loggedIn", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public User getLoggedInUser() {
		return securityUtil.getLoggedInUser();
	}

	@RequestMapping(value = "/api/updatepassword", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
	public void updatePassword(@RequestBody Map<String, String> passwordData) {
		LOGGER.info("Api received to update password");
		User user = securityUtil.getLoggedInUser();
		userService.updatePassword(user.getEmail(), passwordData);
	}

	@RequestMapping(value = "/oauth/revoke-token", method = RequestMethod.GET)
	public void logout(HttpServletRequest request) {
		String authHeader = request.getHeader("Authorization");
		LOGGER.info("authHeader : " + authHeader);
		if (authHeader != null) {
			String tokenValue = authHeader.replace("Bearer", "").trim();
			LOGGER.info("tokenValue : " + tokenValue);
			OAuth2AccessToken accessToken = tokenStore.readAccessToken(tokenValue);
			LOGGER.info("access token : " + accessToken);
			tokenStore.removeAccessToken(accessToken);

			OAuth2RefreshToken refreshToken = accessToken.getRefreshToken();
			LOGGER.info("refresh token : " + refreshToken);
			tokenStore.removeRefreshToken(refreshToken);
		}
	}
}

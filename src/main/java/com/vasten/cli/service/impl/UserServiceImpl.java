package com.vasten.cli.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.stereotype.Service;

import com.vasten.cli.entity.Clients;
import com.vasten.cli.entity.User;
import com.vasten.cli.error.ValidationError;
import com.vasten.cli.exception.CliBadRequestException;
import com.vasten.cli.repository.ClientsRepository;
import com.vasten.cli.repository.UserRepository;
import com.vasten.cli.service.UserService;
import com.vasten.cli.utility.ValidationUtility;

@Service
public class UserServiceImpl implements UserService {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ClientsRepository clientsRepository;

	@Autowired
	private ValidationUtility validationUtility;

	@Autowired
	private HttpServletRequest request;

	@Autowired
	private TokenStore tokenStore;

	@Override
	public User create(User userData) {
		LOGGER.info("Creating user");

		validationUtility.validateUserData(userData);

		User newUser = new User();

		newUser.setCreatedDate(new Date());
		newUser.setUpdatedDate(new Date());
		newUser.setEmail(userData.getEmail());
		newUser.setClients(userData.getClients());
		newUser.setRole("ROLE_USER");

		BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
		String password = bCryptPasswordEncoder.encode(userData.getPassword());

		newUser.setPassword(password);

		return userRepository.save(newUser);
	}

	@Override
	public void updatePassword(String email, Map<String, String> passwordData) {
		LOGGER.info("Updating user password");

		validationUtility.validateUpdateData(email, passwordData);

		User dbUser = userRepository.findOneByEmail(email);

		String password = passwordData.get("newPassword");
		BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
		String newPassword = bCryptPasswordEncoder.encode(password);

		dbUser.setPassword(newPassword);

		userRepository.save(dbUser);

		this.logout(request);
	}

	private void logout(HttpServletRequest request2) {
		LOGGER.info("Deleting old tokens");

		String authHeader = request.getHeader("Authorization");

		if (authHeader != null) {
			String tokenValue = authHeader.replace("Bearer", "").trim();

			OAuth2AccessToken accessToken = tokenStore.readAccessToken(tokenValue);
			tokenStore.removeAccessToken(accessToken);
			LOGGER.info("access token removed");

			OAuth2RefreshToken refreshToken = accessToken.getRefreshToken();
			tokenStore.removeRefreshToken(refreshToken);
			LOGGER.info("refresh token removed");
		}
	}

}

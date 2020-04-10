package com.vasten.cli.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.vasten.cli.entity.Clients;
import com.vasten.cli.entity.User;
import com.vasten.cli.error.ValidationError;
import com.vasten.cli.exception.CliBadRequestException;
import com.vasten.cli.repository.ClientsRepository;
import com.vasten.cli.repository.UserRepository;
import com.vasten.cli.service.UserService;

@Service
public class UserServiceImpl implements UserService {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ClientsRepository clientsRepository;

	@Override
	public User create(User userData) {
		LOGGER.info("Creating user");

		validateUserData(userData);

		User newUser = new User();

		newUser.setCreatedDate(new Date());
		newUser.setUpdatedDate(new Date());
		newUser.setEmail(userData.getEmail());
		newUser.setClients(userData.getClients());

		BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
		String password = bCryptPasswordEncoder.encode(userData.getPassword());

		newUser.setPassword(password);

		return userRepository.save(newUser);
	}

	private void validateUserData(User userData) {
		List<ValidationError> validationErrorList = new ArrayList<ValidationError>();

		if (userData.getEmail() == null || userData.getEmail().isEmpty()) {
			LOGGER.error("Email is mandatory");
			validationErrorList.add(new ValidationError("email", "Email is mandatory"));
		} else {
			User dbUser = userRepository.findByEmail(userData.getEmail());

			if (dbUser != null) {
				LOGGER.error("User already exists");
				validationErrorList.add(new ValidationError("email", "User already exists"));
			}
		}

//		if (userData.getPassword() == null || userData.getPassword().isEmpty()) {
//			LOGGER.error("Password is mandatory");
//			validationErrorList.add(new ValidationError("password", "Password is mandatory"));
//		}

		if (userData.getClients() == null || userData.getClients().getId() == null) {
			LOGGER.error("Client id is mandatory");
			validationErrorList.add(new ValidationError("clientId", "Client id is mandatory"));

		} else {
			Clients dbClient = clientsRepository.findOneById(userData.getClients().getId());

			if (dbClient == null) {
				LOGGER.error("Client does not exist");
				validationErrorList.add(new ValidationError("clientId", "Client does not exist"));
			}
		}

		if (validationErrorList != null && !validationErrorList.isEmpty()) {
			throw new CliBadRequestException("Bad Request", validationErrorList);
		}

	}

}

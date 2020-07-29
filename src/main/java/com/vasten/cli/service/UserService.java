package com.vasten.cli.service;

import java.util.Map;

import com.vasten.cli.entity.User;

public interface UserService {

	public User create(User userData);

	void updatePassword(String email, Map<String, String> passwordData);

}

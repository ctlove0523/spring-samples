package io.github.ctlove0523.jwt.repository;

import java.util.HashMap;
import java.util.Map;

import io.github.ctlove0523.jwt.model.User;
import io.github.ctlove0523.jwt.repository.UserRepository;

public class MemoryUserRepository implements UserRepository {
	private Map<String, User> users = new HashMap<>();

	@Override
	public User findByName(String userName) {
		return users.get(userName);
	}

	@Override
	public void save(User user) {
		users.put(user.getName(), user);
	}
}

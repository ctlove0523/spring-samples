package io.github.ctlove0523.jwt.repository;

import io.github.ctlove0523.jwt.model.User;

public interface UserRepository {

	User findByName(String userName);

	void save(User user);
}

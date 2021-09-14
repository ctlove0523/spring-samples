package io.github.ctlove0523.jwt.controller;

import java.util.Objects;
import java.util.UUID;

import io.github.ctlove0523.jwt.controller.dto.CreateUserRequest;
import io.github.ctlove0523.jwt.model.Role;
import io.github.ctlove0523.jwt.model.User;
import io.github.ctlove0523.jwt.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@Slf4j
public class UserController {

	@Autowired
	private UserRepository userRepository;

	@RequestMapping(value = "/v1/users",method = RequestMethod.POST)
	public ResponseEntity<User> createUser(@RequestBody @Validated CreateUserRequest request) {
		log.info("begin to create user {}", request.getName());
		User user = userRepository.findByName(request.getName());
		if (Objects.isNull(user)) {
			Role role = new Role();
			role.setName(request.getRole());
			user = new User();
			user.setName(request.getName());
			user.setPassword(request.getPassword());
			user.setId(UUID.randomUUID().toString());
			user.setRole(role);
			userRepository.save(user);
		}

		return new ResponseEntity<>(user, HttpStatus.OK);
	}
}

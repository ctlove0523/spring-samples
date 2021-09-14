package io.github.ctlove0523.jwt.controller;

import io.github.ctlove0523.jwt.security.jwt.TokenClient;
import io.github.ctlove0523.jwt.controller.dto.CreateUserRequest;
import io.github.ctlove0523.jwt.controller.dto.LoginResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class TokenController {
	@Autowired
	private TokenClient tokenClient;

	@RequestMapping(value = "/v1/tokens", method = RequestMethod.POST)
	public ResponseEntity<LoginResponse> login(@RequestBody CreateUserRequest request) {
		String token = tokenClient.createToken(request.getName());
		LoginResponse response = new LoginResponse();
		response.setToken(token);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
}

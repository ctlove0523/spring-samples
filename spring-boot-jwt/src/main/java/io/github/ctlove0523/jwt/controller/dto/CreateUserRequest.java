package io.github.ctlove0523.jwt.controller.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @author chentong
 * @since 0.0.1
 */
@Getter
@Setter
public class CreateUserRequest {
	private String name;
	private String password;
	private String role;
}

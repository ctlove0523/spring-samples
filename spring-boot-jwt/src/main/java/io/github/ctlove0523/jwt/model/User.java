package io.github.ctlove0523.jwt.model;

import lombok.Getter;
import lombok.Setter;

/**
 * @author chentong
 * @since 0.0.1
 */
@Getter
@Setter
public class User {
	private String id;
	private String name;
	private String password;
	private Role role;
}

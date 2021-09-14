package io.github.ctlove0523.jwt.security;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * @author chentong
 * @since 0.0.1
 */
@Getter
@Setter
public class AuthException {
	@JsonProperty("error_code")
	private String errorCode;

	@JsonProperty("error_message")
	private String errorMessage;
}

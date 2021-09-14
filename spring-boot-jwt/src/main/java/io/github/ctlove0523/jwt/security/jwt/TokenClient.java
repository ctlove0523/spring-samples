package io.github.ctlove0523.jwt.security.jwt;

import javax.servlet.http.HttpServletRequest;

import io.github.ctlove0523.jwt.model.User;

/**
 * @author chentong
 * @since 0.0.1
 */
public interface TokenClient {
	String createToken(String user);

	User validateToken(String token);

	String resolveToken(HttpServletRequest request);
}

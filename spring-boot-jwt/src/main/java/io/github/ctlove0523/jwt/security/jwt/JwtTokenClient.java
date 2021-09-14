package io.github.ctlove0523.jwt.security.jwt;

import java.security.Key;
import java.util.Date;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;

import io.github.ctlove0523.jwt.model.Role;
import io.github.ctlove0523.jwt.model.User;
import io.github.ctlove0523.jwt.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JwtTokenClient implements TokenClient {
	private static final Logger log = LoggerFactory.getLogger(JwtTokenClient.class);

	private TokenConfigureProvider configure;
	private UserRepository userRepository;

	public JwtTokenClient(TokenConfigureProvider configure, UserRepository userRepository) {
		this.configure = configure;
		this.userRepository = userRepository;
	}

	@Override
	public String createToken(String userName) {
		User user = userRepository.findByName(userName);
		if (Objects.isNull(user)) {
			log.warn("user {} not exist", userName);
			return "";
		}

		Claims claims = Jwts.claims().setSubject(userName);
		claims.put("role", user.getRole().getName());
		claims.put("id", user.getId());

		Date now = new Date();
		Date validity = new Date(now.getTime() + configure.validityInMilliseconds());

		Key secretKey = configure.signingKey();
		return Jwts.builder()
				.setClaims(claims)
				.setIssuedAt(now)
				.setExpiration(validity)
				.signWith(secretKey)
				.compact();
	}


	@Override
	public User validateToken(String token) {
		Key secretKey = configure.signingKey();
		Claims claims = Jwts.parserBuilder()
				.setSigningKey(secretKey)
				.build()
				.parseClaimsJws(token)
				.getBody();
		String userName = claims.getSubject();
		String role = claims.get("role", String.class);
		String userId = claims.get("id", String.class);

		Role authRole = new Role();
		authRole.setName(role);

		User authUser = new User();
		authUser.setId(userId);
		authUser.setName(userName);
		authUser.setRole(authRole);

		return authUser;
	}

	@Override
	public String resolveToken(HttpServletRequest request) {
		return request.getHeader("X-Auth-Token");
	}
}

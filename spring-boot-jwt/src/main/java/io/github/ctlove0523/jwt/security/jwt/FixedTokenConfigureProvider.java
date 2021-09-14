package io.github.ctlove0523.jwt.security.jwt;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;

import io.jsonwebtoken.security.Keys;

public class FixedTokenConfigureProvider implements TokenConfigureProvider {
	private Key key;

	@Override
	public Key signingKey() {
		if (Objects.isNull(key)) {
			String secret = UUID.randomUUID().toString();
			byte[] encodedSecret = Base64.getEncoder().encode(secret.getBytes(StandardCharsets.UTF_8));
			key = Keys.hmacShaKeyFor(encodedSecret);
		}
		return key;
	}

	@Override
	public long validityInMilliseconds() {
		return 10000000;
	}
}

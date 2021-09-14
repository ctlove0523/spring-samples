package io.github.ctlove0523.jwt.security.jwt;

import java.security.Key;

/**
 * @author chentong
 * @since 0.0.1
 */
public interface TokenConfigureProvider {
	/**
	 * BASE64编码的签名密钥
	 * @return 签名密钥，比如初始签名密钥位password,BASE64编码后为：cGFzc3dvcmQ=
	 */
	Key signingKey();

	/**
	 * Token的有效时间
	 * @return token的有效时间
	 */
	long validityInMilliseconds();

}

package io.github.ctlove0523.jwt.security;

import java.util.Arrays;
import java.util.UUID;

import javax.servlet.Filter;

import io.github.ctlove0523.jwt.repository.MemoryUserRepository;
import io.github.ctlove0523.jwt.security.jwt.FixedTokenConfigureProvider;
import io.github.ctlove0523.jwt.security.jwt.JwtTokenClient;
import io.github.ctlove0523.jwt.security.jwt.TokenClient;
import io.github.ctlove0523.jwt.security.jwt.TokenConfigureProvider;
import io.github.ctlove0523.jwt.model.Role;
import io.github.ctlove0523.jwt.model.User;
import io.github.ctlove0523.jwt.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthFilterConfigure {

	@Autowired
	private PolicyManager policyManager;

	@Bean
	public UserRepository userRepository() {
		// 预置admin账号
		Role role = new Role();
		role.setName("admin");
		User admin = new User();
		admin.setName("admin");
		admin.setPassword("admin");
		admin.setRole(role);
		admin.setId(UUID.randomUUID().toString());

		MemoryUserRepository repository = new MemoryUserRepository();
		repository.save(admin);

		return repository;
	}

	@Bean
	public TokenConfigureProvider tokenConfigureProvider() {
		return new FixedTokenConfigureProvider();
	}

	@Bean
	public TokenClient tokenClient(TokenConfigureProvider provider, UserRepository userRepository) {
		return new JwtTokenClient(provider, userRepository);
	}

	@Bean
	public FilterRegistrationBean<Filter> registerAuthenticationFilter(UserRepository repository, TokenClient client) {
		FilterRegistrationBean<Filter> filter = new FilterRegistrationBean<>();
		filter.setFilter(new TokenFilter(repository, client, policyManager));
		filter.setName("authentication filter");
		filter.setEnabled(true);
		filter.setUrlPatterns(Arrays.asList("/v1/users/*", "/v1/applications/*"));
		return filter;
	}

}

package io.github.ctlove0523.jwt.security;

import java.io.IOException;
import java.util.Objects;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.ctlove0523.jwt.security.jwt.TokenClient;
import io.github.ctlove0523.jwt.model.Policy;
import io.github.ctlove0523.jwt.model.User;
import io.github.ctlove0523.jwt.repository.UserRepository;

public class TokenFilter implements Filter {
	private static final ObjectMapper MAPPER = new ObjectMapper();
	private final UserRepository userRepository;
	private final TokenClient tokenClient;
	private final PolicyManager policyManager;

	TokenFilter(UserRepository userRepository, TokenClient tokenClient, PolicyManager policyManager) {
		this.userRepository = userRepository;
		this.tokenClient = tokenClient;
		this.policyManager = policyManager;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;

		String token = tokenClient.resolveToken(httpRequest);
		if (Objects.isNull(token) || token.isEmpty()) {
			httpResponse.setStatus(404);
			httpResponse.setContentType("application/json");
			AuthException e = new AuthException();
			e.setErrorMessage("X-Auth-Token not exist");
			e.setErrorCode("00001");
			writeAuthException(httpResponse, e);
			return;
		}

		User user = tokenClient.validateToken(token);
		if (Objects.isNull(userRepository.findByName(user.getName()))) {
			httpResponse.setStatus(403);
			httpResponse.setContentType("application/json");
			AuthException e = new AuthException();
			e.setErrorMessage("user not exist");
			e.setErrorCode("00002");
			writeAuthException(httpResponse, e);
			return;
		}

		Policy policy = resolvePolicy(httpRequest);
		if (Objects.isNull(policy)) {
			httpResponse.setStatus(403);
			httpResponse.setContentType("application/json");
			AuthException e = new AuthException();
			e.setErrorMessage("no policy allowed");
			e.setErrorCode("00003");
			writeAuthException(httpResponse, e);
			return;
		}

		if (!policy.getAllowedRoles().contains(user.getRole().getName())) {
			httpResponse.setStatus(403);
			httpResponse.setContentType("application/json");
			AuthException e = new AuthException();
			e.setErrorMessage("user not allowed");
			e.setErrorCode("00004");
			writeAuthException(httpResponse, e);
			return;
		}

		chain.doFilter(request, response);
	}

	private void writeAuthException(HttpServletResponse response, AuthException e) {
		try {
			response.getWriter().write(MAPPER.writeValueAsString(e));
		}
		catch (IOException exception) {
			exception.printStackTrace();
		}
	}

	private Policy resolvePolicy(HttpServletRequest request) {
		String method = request.getMethod();
		String action = "";
		switch (method) {
		case "POST":
			action = "create";
			break;
		case "GET":
			action = "show";
			break;
		default:
		}
		String url = request.getServletPath();

		return policyManager.getByUrl(url + "_" + action);
	}
}

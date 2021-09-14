package io.github.ctlove0523.jwt.security;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.annotation.PostConstruct;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.ctlove0523.jwt.model.Policy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component
public class PolicyManager {
	private static final String FILE_NAME = "classpath:api.json";
	private static final ObjectMapper mapper = new ObjectMapper();
	private static final Map<String, Policy> policies = new HashMap<>();

	@Autowired
	private ResourceLoader resourceLoader;

	@PostConstruct
	public void init() throws Exception {
		Resource resource = resourceLoader.getResource(FILE_NAME);
		List<Policy> policyList = mapper.readValue(resource.getInputStream(), new TypeReference<List<Policy>>() { });

		policyList.forEach(new Consumer<Policy>() {
			@Override
			public void accept(Policy policy) {
				policies.put(policy.getUrl() + "_" + policy.getAction(), policy);
			}
		});
	}

	public Policy getByUrl(String url) {
		return policies.get(url);
	}
}

package io.ctlove0523.spring.gateway.config;

import reactor.netty.http.client.HttpClient;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
public class HttpClientConfig implements BeanPostProcessor {

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (bean instanceof HttpClient) {
			HttpClient client = (HttpClient) bean;
			return client.metrics(true, s -> s);

		}
		return bean;
	}
}

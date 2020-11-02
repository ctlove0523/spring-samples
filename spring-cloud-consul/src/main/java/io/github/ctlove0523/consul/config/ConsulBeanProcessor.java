package io.github.ctlove0523.consul.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cloud.consul.ConsulProperties;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@Component
public class ConsulBeanProcessor implements BeanPostProcessor {

	@Nullable
	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (bean instanceof ConsulProperties) {
			ConsulProperties consulProperties = (ConsulProperties) bean;
			consulProperties.setPort(8500);
			consulProperties.setHost("localhost");
			return consulProperties;
		}

		return bean;
	}
}

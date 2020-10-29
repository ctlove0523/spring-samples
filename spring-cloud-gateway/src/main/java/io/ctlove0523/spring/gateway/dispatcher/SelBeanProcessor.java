package io.ctlove0523.spring.gateway.dispatcher;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.DispatcherHandler;

@Component
public class SelBeanProcessor implements BeanPostProcessor {

	@Nullable
	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (bean instanceof DispatcherHandler && "webHandler".equals(beanName)) {
			return new SelfDispatcherHandler((DispatcherHandler) bean);
		}

		return bean;
	}
}

package io.ctlove0523.spring.interceptor.impl;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author chentong
 */
@EnableWebMvc
@Configuration
public class InterceptorConfigure implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new FirstHandlerInterceptor("FIRST"));
        registry.addInterceptor(new SelfAsyncHandlerInterceptor("async interceptor"));
    }
}

package io.ctlove0523.spring.interceptor.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

/**
 * @author chentong
 */
@Slf4j
public class FirstHandlerInterceptor implements HandlerInterceptor {
    private String name;

    FirstHandlerInterceptor(String name) {
        this.name = name == null ? getClass().getName() : name;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        log.info("enter {} interceptor pre handle method at {}", name, System.currentTimeMillis());
        request.setAttribute("Service-Transition-Id", UUID.randomUUID().toString());
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           @Nullable ModelAndView modelAndView) {
        log.info("enter {} interceptor post handle method at {}", name, System.currentTimeMillis());
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                @Nullable Exception ex) {
        log.info("enter {} interceptor after completion method at {}", name, System.currentTimeMillis());
    }
}

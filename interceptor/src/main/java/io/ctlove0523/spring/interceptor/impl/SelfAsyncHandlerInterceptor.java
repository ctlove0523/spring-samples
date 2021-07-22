package io.ctlove0523.spring.interceptor.impl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.springframework.lang.Nullable;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author chentong
 */
@Slf4j
public class SelfAsyncHandlerInterceptor implements AsyncHandlerInterceptor {
    private String name;

    SelfAsyncHandlerInterceptor(String name) {
        this.name = name;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        log.info("enter {} interceptor pre handle method at {}", name, System.currentTimeMillis());
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

    @Override
    public void afterConcurrentHandlingStarted(HttpServletRequest request, HttpServletResponse response,
                                               Object handler) {
        log.info("enter {} interceptor", this.name);
        log.info("Service-Transition-Id = {}", request.getAttribute("Service-Transition-Id"));
    }
}

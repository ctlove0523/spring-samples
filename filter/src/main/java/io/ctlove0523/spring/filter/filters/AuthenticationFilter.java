package io.ctlove0523.spring.filter.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.ctlove0523.spring.filter.authentication.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * @author chentong
 */
@Component
public class AuthenticationFilter implements Filter {
    private String filterName;

    @Autowired
    private UserService userService;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        if (filterConfig != null) {
            this.filterName = filterConfig.getFilterName();
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        // filter request
        System.out.println(filterName + " process request");
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        String userName = httpServletRequest.getHeader("user");
        String password = httpServletRequest.getHeader("password");
        boolean userMatch = userService.userMatch(userName, password);
        if (userName == null || password == null) {
            HttpServletResponse httpServletResponse = (HttpServletResponse) response;
            httpServletResponse.setStatus(HttpStatus.BAD_REQUEST.value());
            return;
        }
        if (!userMatch) {
            HttpServletResponse httpServletResponse = (HttpServletResponse) response;
            httpServletResponse.setStatus(HttpStatus.FORBIDDEN.value());
            return;
        }

        chain.doFilter(request, response);

        // filter response
        System.out.println(filterName + " filter response");
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        if (httpServletResponse.getStatus() == HttpStatus.OK.value()) {
            userService.view(userName);
        }
    }

    @Override
    public void destroy() {

    }
}

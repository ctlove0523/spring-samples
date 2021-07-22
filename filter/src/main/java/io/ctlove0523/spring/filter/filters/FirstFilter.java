package io.ctlove0523.spring.filter.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * @author chentong
 */
public class FirstFilter implements Filter {
    private String filterName;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        if (filterConfig != null) {
            this.filterName = filterConfig.getFilterName();
        }
        System.out.println(filterName + " init finished");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        System.out.println(filterName + " filter request");
        filterChain.doFilter(servletRequest, servletResponse);
        System.out.println(filterName + " filter response");
    }


    @Override
    public void destroy() {
        System.out.println(filterName + " begin to destroy");
    }
}

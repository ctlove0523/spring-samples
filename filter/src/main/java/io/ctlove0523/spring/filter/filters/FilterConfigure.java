package io.ctlove0523.spring.filter.filters;


import javax.servlet.Filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author chentong
 */
@Configuration
public class FilterConfigure {

    @Autowired
    private AuthenticationFilter authenticationFilter;

    @Bean
    public FilterRegistrationBean<Filter> registerAuthenticationFilter() {
        FilterRegistrationBean<Filter> filter = new FilterRegistrationBean<>();
        filter.setFilter(authenticationFilter);
        filter.setName("authentication filter");
        filter.setOrder(Order.FIRST.order);
        return filter;
    }

    @Bean
    public FilterRegistrationBean<Filter> registrationBean() {
        FilterRegistrationBean<Filter> filter = new FilterRegistrationBean<>();
        filter.setFilter(new FirstFilter());
        filter.setName("second filter");
        filter.setOrder(Order.SECOND.order);
        return filter;
    }

    @Bean
    public FilterRegistrationBean<Filter> registerSecondBean() {
        FilterRegistrationBean<Filter> filter = new FilterRegistrationBean<>();
        filter.setFilter(new SecondFilter());
        filter.setName("third filter");
        filter.setOrder(Order.THIRD.order);
        return filter;
    }

    private enum Order {
        FIRST(1),
        SECOND(FIRST.order + 1),
        THIRD(SECOND.order + 1);
        private int order;

        Order(int order) {
            this.order = order;
        }
    }
}

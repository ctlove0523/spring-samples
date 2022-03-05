package io.github.ctlove0523.tls.config;

import io.github.ctlove0523.tls.AllowedFilter;
import io.github.ctlove0523.tls.ConnectorConfigureRepository;
import io.github.ctlove0523.tls.ServerAllowedUrl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.ServletContext;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class FilterConfigure {

    @Autowired
    private ServletContext servletContext;

    @Autowired
    private ConnectorConfigureRepository repository;

    @Bean
    public FilterRegistrationBean<AllowedFilter> allowedFilter() {
        List<ServerAllowedUrl> allowedUrls = repository.load()
                .stream()
                .map(connectorConfigure -> {
                    ServerAllowedUrl serverAllowedUrl = new ServerAllowedUrl();
                    serverAllowedUrl.setPort(connectorConfigure.getPort());
                    serverAllowedUrl.setUrls(connectorConfigure.getAllowedUrls());
                    return serverAllowedUrl;
                }).collect(Collectors.toList());
        AllowedFilter allowedFilter = new AllowedFilter(allowedUrls);
        FilterRegistrationBean<AllowedFilter> filter = new FilterRegistrationBean<>();
        filter.setFilter(allowedFilter);
        filter.setOrder(0);

        return filter;
    }

}

package io.github.ctlove0523.tls;

import org.springframework.util.AntPathMatcher;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class AllowedFilter implements Filter {
    private final Map<Integer, List<String>> patternMap = new HashMap<>();
    private final AntPathMatcher antPathMatcher;

    public AllowedFilter(List<ServerAllowedUrl> urls) {
        this.antPathMatcher = new AntPathMatcher();
        this.antPathMatcher.setCachePatterns(true);
        this.antPathMatcher.setCaseSensitive(true);

        if (Objects.isNull(urls)) {
            urls = new ArrayList<>();
        }

        for (ServerAllowedUrl url : urls) {
            int port = url.getPort();
            if (url.getUrls() == null) {
                patternMap.put(port, new ArrayList<>());
            } else {
                patternMap.put(port, url.getUrls());
            }
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        int serverPort = request.getServerPort();

        if (!patternMap.containsKey(serverPort)) {
            chain.doFilter(httpRequest, httpResponse);
            return;
        }

        List<String> patterns = patternMap.get(serverPort);
        for (String pattern : patterns) {
            String uri = httpRequest.getRequestURI();
            if (antPathMatcher.match(pattern, uri)) {
                chain.doFilter(httpRequest, httpResponse);
                return;
            }

        }

        httpResponse.setStatus(401);

    }
}

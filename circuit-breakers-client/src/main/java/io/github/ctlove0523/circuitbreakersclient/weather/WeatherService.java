package io.github.ctlove0523.circuitbreakersclient.weather;

import java.util.concurrent.Callable;

import javax.annotation.PostConstruct;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Copyright (C), 2018-2018, open source
 * FileName: WeatherService
 *
 * @author: chentong
 * Date:     2018/12/23 13:56
 */
@Service
public class WeatherService {
    private static final Logger log = LoggerFactory.getLogger(WeatherService.class);

    private static final RestTemplate template = new RestTemplate();
    private static final String URI = "http://localhost:8080/api/v1.0/weather";
    private static CircuitBreakerConfig config;
    private static CircuitBreaker circuitBreaker;

    @PostConstruct
    public void circuitBereakerInit() {
        config = CircuitBreakerConfig.custom()
                .failureRateThreshold(0.5F)
                .recordExceptions(Exception.class)
                .ringBufferSizeInClosedState(3)
                .ringBufferSizeInHalfOpenState(2)
                .build();
        circuitBreaker = CircuitBreaker.of("weather", config);
    }

    public WeatherInfo queryWeatherInfo() {
        log.info("begin to query weather info");
        log.info("circuit breaker current state is {}",order2State(circuitBreaker.getState().getOrder()));
        CircuitBreaker.Metrics metrics = circuitBreaker.getMetrics();
        log.info("failure rate = {},failed = {},success = {}",metrics.getFailureRate(),metrics.getNumberOfFailedCalls(),metrics.getNumberOfSuccessfulCalls());
        try {
            return circuitBreaker.executeCallable(new Callable<WeatherInfo>() {
                @Override
                public WeatherInfo call() throws Exception {
                    ResponseEntity<WeatherInfo> response = template.getForEntity(URI, WeatherInfo.class);
                    return response.getBody();
                }
            });
        } catch (Exception e) {
            log.info("exception = {}", e.toString());
        }
        return defaultInfo();
    }

    @HystrixCommand(fallbackMethod = "defaultInfo")
    public WeatherInfo getWeatherInfo() {
        log.info("enter getWeatherInfo method");
        ResponseEntity<WeatherInfo> response = template.getForEntity(URI, WeatherInfo.class);
        return response.getBody();
    }

    public WeatherInfo defaultInfo() {
        log.info("server failed, circuit breakers open");
        WeatherInfo info = new WeatherInfo();
        info.setHumidity(-1.0);
        info.setTemperature(-1.0);
        info.setType("default");
        return info;
    }


    private String order2State(int order) {
        switch (order) {
            case 0:
                return "CLOSED";
            case 1:
                return "OPEN";
            case 2:
                return "HALF_OPEN";

            case 3:
                return "DISABLED";
            case 4:
                return "FORCED_OPEN";
            default:
                return "";
        }
    }
}

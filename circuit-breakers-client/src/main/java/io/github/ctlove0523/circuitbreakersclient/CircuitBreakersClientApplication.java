package io.github.ctlove0523.circuitbreakersclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;

@SpringBootApplication
@EnableCircuitBreaker
public class CircuitBreakersClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(CircuitBreakersClientApplication.class, args);
	}

}


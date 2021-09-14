package io.github.ctlove0523.jwt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAutoConfiguration
public class SpringBootJwtApplication {
	public static void main(String[] args) throws Exception{
		SpringApplication.run(SpringBootJwtApplication.class, args);
	}
}

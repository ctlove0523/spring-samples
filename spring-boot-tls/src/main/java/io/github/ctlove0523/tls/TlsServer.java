package io.github.ctlove0523.tls;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAutoConfiguration
public class TlsServer {
    public static void main(String[] args) {
        SpringApplication.run(TlsServer.class, args);
    }
}

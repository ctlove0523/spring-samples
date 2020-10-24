package io.ctlove0523.spring.gateway.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.ctlove0523.spring.gateway.routes.SelfGatewayControllerEndpoint;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * @author chentong
 */
@Configuration
public class GatewayConfig {
    @Bean
    @ConditionalOnAvailableEndpoint
    public SelfGatewayControllerEndpoint routeController() {
        return new SelfGatewayControllerEndpoint();
    }

    @Bean
    public DataSource mysqlDataSource() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setJdbcUrl("jdbc:mysql://localhost:3306/test?useUnicode=true&characterEncoding=utf-8&serverTimezone=UTC");
        config.setUsername("root");
        config.setPassword("0523");

        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        return new HikariDataSource(config);
    }
}

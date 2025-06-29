package com.pupperfield.backend.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatabaseConfig {
    @Bean("dataSource")
    HikariDataSource dataSource() {
        var config = new HikariConfig();
        config.setJdbcUrl("jdbc:sqlite:src/main/resources/database/dogs.db");
        config.setPoolName("pupperfield");
        return new HikariDataSource(config);
    }
}

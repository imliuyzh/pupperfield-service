package com.pupperfield.backend.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * A configuration class for setting up the data source using HikariCP.
 */
@Configuration
public class DatabaseConfig {
    /**
     * Configures a HikariDataSource to set up a connection pool for a local database.
     *
     * @return a configured data source with information connecting to the database
     */
    @Bean("dataSource")
    public HikariDataSource dataSource() {
        var config = new HikariConfig();
        config.setConnectionTimeout(1000);    // Wait for a second maximum for a connection
        config.setJdbcUrl("jdbc:sqlite:src/main/resources/database/dogs.db");
        config.setPoolName("pupperfield");
        return new HikariDataSource(config);
    }
}

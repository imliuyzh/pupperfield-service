package com.pupperfield.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * This class is the main entry point for the Pupperfield backend application.
 */
@EnableCaching
@SpringBootApplication
public class PupperfieldApplication {
    /**
     * Main entry point for the application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(PupperfieldApplication.class, args);
    }
}

package com.pupperfield.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point of the application.
 */
@SpringBootApplication
public class PupperfieldApplication {
    /**
     * Creates a Spring context to start running the application.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(PupperfieldApplication.class, args);
    }
}

package com.pupperfield.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * This class is the main entry point for the Pupperfield backend application.
 */
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

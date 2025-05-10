package com.pupperfield.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class PupperfieldApplication {
	public static void main(String[] args) {
		SpringApplication.run(PupperfieldApplication.class, args);
	}
}

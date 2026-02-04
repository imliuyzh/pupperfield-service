package com.pupperfield.backend.config;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * A configuration class for HTTP settings such as CORS.
 */
@AllArgsConstructor
@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {
    /**
     * Configures CORS mappings to allow all origins, enables credentials,
     * and supports GET, POST, and OPTIONS methods.
     *
     * @param registry an object to configure allowed origins and methods
     */
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowCredentials(true)
            .allowedMethods(
                HttpMethod.GET.name(),
                HttpMethod.OPTIONS.name(),
                HttpMethod.POST.name())
            .allowedOriginPatterns("*");
    }
}

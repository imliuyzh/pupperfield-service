package com.pupperfield.backend.config;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@AllArgsConstructor
@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        registry.addMapping("/**")
            .allowCredentials(true)
            .allowedMethods(
                HttpMethod.GET.name(),
                HttpMethod.OPTIONS.name(),
                HttpMethod.POST.name())
            .allowedOriginPatterns("*");
    }
}

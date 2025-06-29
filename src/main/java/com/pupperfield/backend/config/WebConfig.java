package com.pupperfield.backend.config;

import com.pupperfield.backend.interceptor.AuthInterceptor;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@AllArgsConstructor
@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {
    private AuthInterceptor authInterceptor;

    public void addCorsMappings(@NonNull CorsRegistry registry) {
        registry.addMapping("/**")
            .allowCredentials(true)
            .allowedMethods(
                HttpMethod.GET.name(),
                HttpMethod.POST.name(),
                HttpMethod.OPTIONS.name()
            )
            .allowedOriginPatterns("*");
    }

    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
            .addPathPatterns("/**")
            .excludePathPatterns(
                "/api-docs/**",
                "/auth/login",
                "/status",
                "/swagger-ui.html",
                "/swagger-ui/**"
            );
    }
}

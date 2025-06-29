package com.pupperfield.backend.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean("swaggerConfig")
    OpenAPI swaggerConfig() {
        return new OpenAPI()
            .externalDocs(new ExternalDocumentation()
                .url("https://frontend-take-home.fetch.com")
            )
            .info(new Info()
                .description("This is a mock API for Fetch Rewards' front " +
                    "end take home assessment. The original problem " +
                    "statement is available at: ")
                .title("Pupperfield API")
            );
    }
}

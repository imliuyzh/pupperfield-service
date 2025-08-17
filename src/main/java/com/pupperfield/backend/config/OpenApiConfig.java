package com.pupperfield.backend.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * A configuration class for setting up the OpenAPI/Swagger documentation.
 */
@Configuration
public class OpenApiConfig {
    /**
     * Configures the title and description for the API document. Plus, it
     * links to the original problem statement from Fetch Rewards.
     *
     * @return an object with metadata of the API documentation
     */
    @Bean("swaggerConfig")
    public OpenAPI swaggerConfig() {
        return new OpenAPI()
            .externalDocs(new ExternalDocumentation().url("https://frontend-take-home.fetch.com"))
            .info(new Info()
                .description("This is a mock API for Fetch Rewards' front end take home "
                    + "assessment. The original problem statement is available at the URL below. "
                    + "For a list of implementation differences between this API and the original,"
                    + " please check out the README file in the repository.")
                .title("Pupperfield API")
            );
    }
}

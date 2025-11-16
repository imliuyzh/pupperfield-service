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
                    + "assessment available at the URL below. Here is a list of high-level "
                    + "implementation differences comparing to the original:"
                    + "<ol>"
                    + "<li>Stricter validation constraints are placed upon the user input.</li>"
                    + "<li>HTTP 401 and HTTP 422 responses include more error information.</li>"
                    + "<li>HTTP 405 is used for unsupported HTTP methods instead of HTTP 404.</li>"
                    + "<li>HTTP 415 is used for unsupported media types instead of returning the "
                        + "response in a different format.</li>"
                    + "<li>A white label error page is returned when the URL has invalid "
                        + "characters.</li>"
                    + "<li>A new `/status` endpoint is introduced to report application status.</li>"
                    + "</ol>")
                .title("Pupperfield API")
            );
    }
}

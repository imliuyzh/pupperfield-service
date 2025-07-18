package com.pupperfield.backend.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.hibernate.validator.constraints.Length;

/**
 * A Data Transfer Object representing the login request.
 */
@AllArgsConstructor
@Schema(
    description = "User email and name for log in.",
    requiredMode = Schema.RequiredMode.REQUIRED
)
@Value
public class LoginRequestDto {
    @Email(message = "email is not valid")
    @NotBlank(message = "email must not be empty")
    @Schema(
        example = "name@email.com",
        requiredMode = Schema.RequiredMode.REQUIRED,
        title = "User's email address"
    )
    String email;

    @Length(max = 800, message = "name should not have more than 800 characters")
    @NotBlank(message = "name must not be empty")
    @Schema(
        example = "First Last",
        requiredMode = Schema.RequiredMode.REQUIRED,
        title = "User's name"
    )
    String name;
}

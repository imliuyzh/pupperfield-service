package com.pupperfield.backend.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Value;

@AllArgsConstructor
@Value
public class LoginRequestDto {
    @Email(message = "email is not valid")
    @NotBlank(message = "email must not be empty")
    private String email;

    @NotBlank(message = "name must not be empty")
    private String name;
}

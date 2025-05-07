package com.pupperfield.backend.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Value;

@AllArgsConstructor
@Value
public class LoginDto {
    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String name;
}

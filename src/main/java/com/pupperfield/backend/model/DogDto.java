package com.pupperfield.backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class DogDto {
    @PositiveOrZero
    private int age;

    @NotBlank
    private String breed, id, name;

    @JsonProperty("img")
    @NotBlank
    private String imageLink;

    @JsonProperty("zip_code")
    @NotBlank
    private String zipCode;
}

package com.pupperfield.backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A Data Transfer Object representing a dog in the database.
 */
@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
@Schema(description = "Dog information")
public class DogDto {
    @PositiveOrZero
    @Schema(title = "A dog's age")
    private long age;

    @NotBlank
    @Schema(title = "A dog's breed")
    private String breed;

    @NotBlank
    @Schema(title = "A dog's ID")
    private String id;

    @NotBlank
    @Schema(title = "A dog's name")
    private String name;

    @JsonProperty("img")
    @NotBlank
    @Schema(title = "Link to a dog's image")
    private String imageLink;

    @JsonProperty("zip_code")
    @NotBlank
    @Schema(title = "A dog's zip code")
    private String zipCode;
}

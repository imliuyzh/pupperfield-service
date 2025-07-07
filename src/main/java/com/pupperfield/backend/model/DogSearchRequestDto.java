package com.pupperfield.backend.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * A Data Transfer Object representing the request parameters for searching dogs.
 */
@AllArgsConstructor
@Data
@NoArgsConstructor
@Schema(description = "Dog search request parameters")
public class DogSearchRequestDto {
    @Schema(example = "\"Affenpinscher\"", title = "A list of dog breeds")
    @Size(message = "breeds should not be empty", min = 1)
    private List<@NotBlank(message = "a breed must not be empty") @Valid String>
        breeds = null;

    @NotNull(message = "from must be a number starting from zero")
    @PositiveOrZero(message = "from must be zero or positive")
    @Schema(example = "0", title = "Index of the first dog in the result")
    private Integer from = 0;

    @PositiveOrZero(message = "ageMax must be zero or positive")
    @Schema(example = "10", title = "Maximum age of dogs")
    private Integer ageMax = null;

    @PositiveOrZero(message = "ageMin must be zero or positive")
    @Schema(example = "5", title = "Minimum age of dogs")
    private Integer ageMin = null;

    @NotNull(message = "size must be a positive number")
    @Positive(message = "size must be positive")
    @Schema(example = "25", title = "Number of dogs in the result")
    private Integer size = 25;

    @Pattern(
        message = "sort must match (age|breed|name):(asc|desc)",
        regexp = "^(age|breed|name):(asc|desc)$"
    )
    @Schema(example = "breed:asc", title = "How to sort the result")
    private String sort = "breed:asc";

    @Schema(example = "\"12345\"", title = "A list of zip codes")
    @Size(message = "zipCodes should not be empty", min = 1)
    private List<@NotBlank(message = "a zip code must not be empty") @Valid String>
        zipCodes = null;
}

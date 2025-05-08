package com.pupperfield.backend.model;

import java.util.List;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder.Default;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class DogSearchRequestDto {
    @PositiveOrZero
    private int ageMin, ageMax;

    private List<String> breeds, zipCodes;

    @Default
    @PositiveOrZero
    private int from = 0;

    @Default
    @Positive
    private int size = 25;

    @Default
    @Pattern(regexp = "^(age|breed|name):(asc|desc)$")
    private String sort = "breed:asc";
}

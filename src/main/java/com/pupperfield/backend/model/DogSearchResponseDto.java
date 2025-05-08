package com.pupperfield.backend.model;

import java.util.List;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Builder
@Data
public class DogSearchResponseDto {
    private String next, prev;

    private List<String> resultIds;

    @PositiveOrZero
    private int total;
}

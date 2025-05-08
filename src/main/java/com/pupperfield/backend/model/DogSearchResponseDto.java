package com.pupperfield.backend.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Builder
@Data
public class DogSearchResponseDto {
    private String next, prev;
    private List<String> resultIds;
    private int total;
}

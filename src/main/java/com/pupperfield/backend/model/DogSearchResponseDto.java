package com.pupperfield.backend.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Builder
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DogSearchResponseDto {
    private String next;
    
    @JsonProperty("prev")
    private String previous;

    private List<String> resultIds;
    private Long total;
}

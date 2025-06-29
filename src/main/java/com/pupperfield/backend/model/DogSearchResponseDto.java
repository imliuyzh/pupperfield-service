package com.pupperfield.backend.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@AllArgsConstructor
@Builder
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Dog search results")
public class DogSearchResponseDto {
    @Schema(title = "A link to the next page of results")
    private String next;

    @JsonProperty("prev")
    @Schema(title = "A link to the previous page of results")
    private String previous;

    @Schema(title = "A list of dog IDs")
    private List<String> resultIds;

    @Schema(title = "Total number of results")
    private Long total;
}

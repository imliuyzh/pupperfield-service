package com.pupperfield.backend.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * A Data Transfer Object representing the results of a dog search query.
 */
@AllArgsConstructor
@Builder
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Dog search results")
public class DogSearchResponseDto {
    @Schema(example = "/dogs/search?size=1&from=3", title = "Next page link")
    private String next;

    @JsonProperty("prev")
    @Schema(example = "/dogs/search?size=1&from=1", title = "Previous page link")
    private String previous;

    @Schema(example = "[\"rcD-OZUBBPFf4ZNZzDCC\"]", title = "A list of dog IDs")
    private List<String> resultIds;

    @Schema(example = "150", title = "Total number of results")
    private long total;
}

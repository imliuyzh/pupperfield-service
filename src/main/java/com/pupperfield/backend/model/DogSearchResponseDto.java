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
@Schema(
    accessMode = Schema.AccessMode.WRITE_ONLY,
    description = "Dog search results"
)
public class DogSearchResponseDto {
    @Schema(
        accessMode = Schema.AccessMode.WRITE_ONLY,
        title = "A link to the next page of results"
    )
    private String next;

    @JsonProperty("prev")
    @Schema(
        accessMode = Schema.AccessMode.WRITE_ONLY,
        title = "A link to the previous page of results"
    )
    private String previous;

    @Schema(
        accessMode = Schema.AccessMode.WRITE_ONLY,
        title = "A list of dog IDs"
    )
    private List<String> resultIds;

    @Schema(
        accessMode = Schema.AccessMode.WRITE_ONLY,
        title = "Total number of results"
    )
    private Long total;
}

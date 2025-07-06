package com.pupperfield.backend.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Collection;

/**
 * A Data Transfer Object representing the response body for invalid requests.
 *
 * @param error an HTTP reason phrase
 * @param details one or more reasons about the error
 */
@Schema(description = "Error message when an invalid request is sent")
public record InvalidRequestResponseDto(
    @Schema(title = "HTTP reason phrase")
    String error,

    @Schema(title = "Error details")
    Collection<String> details
) {
}

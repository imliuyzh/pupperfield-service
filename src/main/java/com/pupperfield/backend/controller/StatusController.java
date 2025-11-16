package com.pupperfield.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.pupperfield.backend.constant.StatusConstants.STATUS_PATH;

@RestController
@Tag(
    description = "Report the current status of the application. It is not included in"
        + " the original implementation.",
    name = "Status")
public class StatusController {
    @GetMapping(STATUS_PATH)
    @Operation(
        description = "Returns HTTP 200 along with its reason phrase. If the application is not "
            + "working, then other unexpected behaviors like HTTP 5xx errors will happen.",
        method = "GET",
        responses = {
            @ApiResponse(
                content = {@Content(
                    examples = {@ExampleObject(value = "OK")},
                    mediaType = "text/plain"
                )},
                description = "OK",
                responseCode = "200"
            )
        },
        summary = "Display \"ok\" when the application is running properly."
    )
    public ResponseEntity<String> report() {
        return ResponseEntity.ok(HttpStatus.OK.getReasonPhrase());
    }
}

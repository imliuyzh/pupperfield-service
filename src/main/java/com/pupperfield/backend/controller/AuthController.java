package com.pupperfield.backend.controller;

import com.pupperfield.backend.model.LoginRequestDto;
import com.pupperfield.backend.service.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

import static org.apache.commons.lang3.BooleanUtils.toInteger;

@AllArgsConstructor
@RequestMapping("/auth")
@RestController
@Tag(description = "Perform log in and log out operations.", name = "Authentication")
public class AuthController {
    /**
     * Name of the cookie that has the token.
     */
    public static final String COOKIE_NAME = "fetch-access-token";

    private TokenService tokenService;

    @Operation(
        description = "Takes an email address and name wrapped in a JSON body and returns a "
            + "cookie that will be sent with every request (the browser handles it automatically"
            + "). Remember to call this endpoint again before the token expires in an hour.",
        method = "POST",
        responses = {
            @ApiResponse(
                content = {@Content(
                    examples = {@ExampleObject(value = "OK")},
                    mediaType = "text/plain"
                )},
                description = "OK",
                responseCode = "200"
            ),
            @ApiResponse(
                content = {@Content(
                    examples = {@ExampleObject(value = "{\"error\":" +
                        "\"Unprocessable Entity\",\"detail\":[\"email is " +
                        "not valid\"]}")},
                    mediaType = "application/json"
                )},
                description = "Invalid request",
                responseCode = "422"
            )
        },
        summary = "Create a cookie called \"fetch-access-token.\""
    )
    @PostMapping("/login")
    public ResponseEntity<String> login(
        @Parameter(description = "User email and name", required = true)
        @RequestBody
        @Validated
        LoginRequestDto user
    ) {
        var cookie = createCookie(tokenService.generate(user.getEmail(), user.getName()));
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, cookie.toString())
            .body("OK");
    }

    @Operation(
        description = "Takes the authentication cookie, nullifies it by emptying the token, "
            + "and sets it to expire immediately.",
        method = "POST",
        responses = {
            @ApiResponse(
                content = {@Content(
                    examples = {@ExampleObject(value = "OK")},
                    mediaType = "text/plain"
                )},
                description = "OK",
                responseCode = "200"
            ),
            @ApiResponse(
                content = {@Content(
                    examples = {@ExampleObject(value = "Unauthorized")},
                    mediaType = "text/plain"
                )},
                description = "Unauthorized",
                responseCode = "401"
            )
        },
        summary = "Invalidate the cookie \"fetch-access-token.\""
    )
    @PostMapping("/logout")
    public ResponseEntity<String> logOut() {
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, createCookie(null).toString())
            .body("OK");
    }

    /**
     * Create or replace a cookie with a token.
     *
     * @param value a JWT token set to be expired within an hour. A null value
     * indicates the cookie should be invalidated.
     * @return a new cookie
     */
    private ResponseCookie createCookie(String value) {
        return ResponseCookie.from(COOKIE_NAME)
            .httpOnly(true)
            .maxAge(Duration.ofHours(toInteger(value != null)))
            .path("/")
            .sameSite("none")
            .secure(true)
            .value(value)
            .build();
    }
}

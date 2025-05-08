package com.pupperfield.backend.controller;

import java.time.Duration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;

import com.pupperfield.backend.model.LoginRequestDto;
import com.pupperfield.backend.service.TokenService;

@AllArgsConstructor
@RequestMapping("/auth")
@RestController
public class AuthenticationController {
    public static final String COOKIE_NAME = "fetch-access-token";

    private TokenService tokenService;

    @PostMapping("/login")
    public ResponseEntity<String> login
            (@RequestBody @Validated LoginRequestDto user) {
        var cookie = createCookie(tokenService.generate(
            user.getEmail(), user.getName()
        ));
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, cookie.toString())
            .body("OK");
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logOut() {
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, createCookie(null).toString())
            .body("OK");
    }

    private ResponseCookie createCookie(String value) {
        return ResponseCookie.from(COOKIE_NAME)
            .httpOnly(true)
            .maxAge(Duration.ofHours(value != null ? 1 : 0))
            .path("/")
            .sameSite("none")
            .secure(true)
            .value(value)
            .build();
    }
}

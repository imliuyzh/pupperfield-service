package com.pupperfield.backend.controller;

import java.security.SecureRandom;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@RequestMapping("/dogs")
@RestController
public class DogController {
    private static final SecureRandom RANDOM = new SecureRandom();

    @PostMapping("/match")
    public Map<String, String> match(
        @NotNull
        @RequestBody
        @Size(min = 1)
        List<String> matches
    ) {
        return Map.of("match", matches.get(RANDOM.nextInt(matches.size())));
    }
}

package com.pupperfield.backend.controller;

import java.security.SecureRandom;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pupperfield.backend.entity.Dog;
import com.pupperfield.backend.repository.DogRepository;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@RequestMapping("/dogs")
@RestController
public class DogController {
    private DogRepository dogRepository;
    private static final SecureRandom RANDOM = new SecureRandom();

    @GetMapping("/breeds")
    public Collection<String> getBreeds() {
        return dogRepository.getBreeds();
    }

    @PostMapping()
    public Collection<Dog> getDogInfo(@RequestBody List<String> ids) {
        return dogRepository.findByIdIn(ids);
    }

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

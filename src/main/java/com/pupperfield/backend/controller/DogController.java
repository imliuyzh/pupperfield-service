package com.pupperfield.backend.controller;

import java.security.SecureRandom;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pupperfield.backend.entity.Dog;
import com.pupperfield.backend.model.DogSearchRequestDto;
import com.pupperfield.backend.model.DogSearchResponseDto;
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

    @PostMapping("")
    public Collection<Dog> list(
        @NotNull
        @RequestBody
        @Size(min = 1, max = 100)
        List<String> idList
    ) {
        return dogRepository.findByIdIn(idList);
    }

    @GetMapping("/search")
    public DogSearchResponseDto search
            (@RequestParam DogSearchRequestDto parameters) {
        var response = DogSearchResponseDto.builder();
        return response.build();
    }

    @PostMapping("/match")
    public Map<String, String> match(
        @NotNull
        @RequestBody
        @Size(min = 1)
        List<String> idList
    ) {
        return Map.of("match", idList.get(RANDOM.nextInt(idList.size())));
    }
}

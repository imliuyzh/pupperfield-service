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

import com.pupperfield.backend.model.DogDto;
import com.pupperfield.backend.model.DogSearchResponseDto;
import com.pupperfield.backend.service.DogService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@RequestMapping("/dogs")
@RestController
public class DogController {
    private static final SecureRandom RANDOM = new SecureRandom();

    private DogService dogService;

    @GetMapping("/breeds")
    public Collection<String> getBreeds() {
        return dogService.getBreeds();
    }

    @PostMapping
    public List<DogDto> list(
        @NotNull(message = "body must not be null")
        @RequestBody
        @Size(
            max = 100,
            message = "body must have 1 to 100 dog IDs",
            min = 1
        )
        List<
            @NotBlank(message = "a dog ID must not be empty")
            @Valid 
            String
        > idList
    ) {
        return dogService.listDogs(idList);
    }

    @GetMapping("/search")
    public DogSearchResponseDto search(
        @RequestParam(required = false)
        List<
            @NotBlank(message = "a breed must not be empty")
            @Valid
            String
        > breeds,

        @PositiveOrZero(message = "from must be positive or zero")
        @RequestParam(
            defaultValue = "0",
            required = false
        )
        Integer from,

        @PositiveOrZero(message = "ageMax must be positive or zero")
        @RequestParam(name = "ageMax", required = false)
        Integer maxAge,

        @PositiveOrZero(message = "ageMin must be positive or zero")
        @RequestParam(name = "ageMin", required = false)
        Integer minAge,

        HttpServletRequest request,

        @Positive(message = "size must be positive")
        @RequestParam(
            defaultValue = "25",
            required = false
        )
        Integer size,

        @Pattern(
            message = "sort must be age:asc, age:desc, breed:asc, breed:desc,"
                + " name:asc, or name:desc",
            regexp = "^(age|breed|name):(asc|desc)$"
        )
        @RequestParam(
            defaultValue = "breed:asc",
            required = false
        )
        String sort,

        @RequestParam(required = false)
        List<
            @NotBlank(message = "a zip code must not be empty")
            @Valid
            String
        > zipCodes
    ) {
        var outcome = dogService.searchDogs(
            breeds, from, maxAge, minAge, size, sort, zipCodes
        );
        var response = DogSearchResponseDto.builder()
            .resultIds(outcome.getFirst())
            .total(outcome.getSecond());
        if (from - size >= 0) {
            response.previous(buildNavigation(from - size, request));
        }
        if (from + size < outcome.getSecond()) {
            response.next(buildNavigation(from + size, request));
        }
        return response.build();
    }

    @PostMapping("/match")
    public Map<String, String> match(
        @NotNull(message = "body must not be null")
        @RequestBody
        @Size(message = "body must not be empty", min = 1)
        List<
            @NotBlank(message = "a dog ID must not be empty")
            @Valid
            String
        > idList
    ) {
        return Map.of("match", idList.get(RANDOM.nextInt(idList.size())));
    }

    private String buildNavigation(
        Integer from,
        HttpServletRequest request
    ) {
        var tokens = request.getQueryString().split("&");
        var fromExists = false;
        for (var index = 0; index < tokens.length; index++) {
            if (tokens[index].startsWith("from=")) {
                tokens[index] = String.format("from=%d", from);
                fromExists = true;
                break;
            }
        }
        return String.format(
            fromExists ? "/dogs/search?%s" : "/dogs/search?%s&from=" + from,
            String.join("&", tokens)
        );
    }
}

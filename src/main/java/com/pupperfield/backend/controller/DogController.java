package com.pupperfield.backend.controller;

import java.security.SecureRandom;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.cache.annotation.Cacheable;
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
        @NotNull
        @RequestBody
        @Size(min = 1, max = 100)
        List<@NotBlank @Valid String> idList
    ) {
        return dogService.listDogs(idList);
    }

    @Cacheable(
        key = "#breeds + '_' + #from + '_' + #maxAge + '_' + #minAge + '_' "
            + "+ #size + '_' + #sort + '_' + #zipCodes",
        value = "searches"
    )
    @GetMapping("/search")
    public DogSearchResponseDto search(
        @RequestParam(required = false)
        List<@NotBlank @Valid String> breeds,

        @PositiveOrZero
        @RequestParam(
            defaultValue = "0",
            required = false
        )
        Integer from,

        @PositiveOrZero
        @RequestParam(name = "ageMax", required = false)
        Integer maxAge,

        @PositiveOrZero
        @RequestParam(name = "ageMin", required = false)
        Integer minAge,

        HttpServletRequest request,

        @Positive
        @RequestParam(
            defaultValue = "25",
            required = false
        )
        Integer size,

        @Pattern(regexp = "^(age|breed|name):(asc|desc)$")
        @RequestParam(
            defaultValue = "breed:asc",
            required = false
        )
        String sort,

        @RequestParam(required = false)
        List<@NotBlank @Valid String> zipCodes
    ) {
        var outcome = dogService.findDogs(
            breeds, from, maxAge, minAge, size, sort, zipCodes
        );
        var response = DogSearchResponseDto.builder()
            .resultIds(outcome.getFirst().toList())
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
        @NotNull
        @RequestBody
        @Size(min = 1)
        List<@NotBlank @Valid String> idList
    ) {
        return Map.of("match", idList.get(RANDOM.nextInt(idList.size())));
    }

    private String buildNavigation(
        Integer from,
        HttpServletRequest request
    ) {
        var tokens = request.getQueryString().split("&");
        for (int index = 0; index < tokens.length; index++) {
            if (tokens[index].startsWith("from=")) {
                tokens[index] = String.format("from=%d", from);
                break;
            }
        }
        return String.format("/dogs/search?%s", String.join("&", tokens));
    }
}

package com.pupperfield.backend.controller;

import com.pupperfield.backend.model.DogDto;
import com.pupperfield.backend.model.DogSearchResponseDto;
import com.pupperfield.backend.service.DogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@RequestMapping("/dogs")
@RestController
@Tag(
    description = "Execute operations upon dogs stored in the database.",
    name = "Dogs"
)
public class DogController {
    private static final SecureRandom RANDOM = new SecureRandom();

    private DogService dogService;

    @Operation(
        description = "Extracts all dog breeds from the database. " +
            "The list is sorted in non-descending order.",
        method = "GET",
        responses = {
            @ApiResponse(
                content = {@Content(
                    examples = {
                        @ExampleObject(value = "[\"Affenpinscher\"]")
                    },
                    mediaType = "application/json"
                )},
                description = "OK",
                responseCode = "200"
            ),
            @ApiResponse(
                content = {@Content(
                    examples = {
                        @ExampleObject(value = "Unauthorized")
                    },
                    mediaType = "text/plain"
                )},
                description = "Unauthorized",
                responseCode = "401"
            )
        },
        summary = "Retrieve a list of all dog breeds in the database."
    )
    @GetMapping("/breeds")
    public Collection<String> getBreeds() {
        return dogService.getBreeds();
    }

    @Operation(
        description = "Receives a list of dog IDs and fetches data from the " +
            "database about the dogs. The size of the list is restricted " +
            "from 1 to 100 inclusive.",
        method = "POST",
        responses = {
            @ApiResponse(
                content = {@Content(
                    examples = {
                        @ExampleObject(value = "[{\"img\":\"https://frontend" +
                            "-take-home.fetch.com/dog-images/n02110627-" +
                            "affenpinscher/n02110627_10225.jpg\",\"name\":" +
                            "\"Brionna\",\"age\":14,\"breed\":" +
                            "\"Affenpinscher\",\"zip_code\":\"06519\"," +
                            "\"id\":\"qcD-OZUBBPFf4ZNZzDCC\"}]")
                    },
                    mediaType = "application/json"
                )},
                description = "OK",
                responseCode = "200"
            ),
            @ApiResponse(
                content = {@Content(
                    examples = {
                        @ExampleObject(value = "Unauthorized")
                    },
                    mediaType = "text/plain"
                )},
                description = "Unauthorized",
                responseCode = "401"
            )
        },
        summary = "Get a list of dog details based on the array passed in."
    )
    @PostMapping
    public List<DogDto> list(
        @NotNull(message = "body must not be null")
        @Parameter(
            description = "Dog IDs",
            required = true
        )
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

    @Operation(
        description = "Takes search conditions from the request parameters " +
            "to search for dogs in the database. All parameters can be " +
            "empty. By default, size is set to 25, from is set to 0, and " +
            "sort is set to breed:asc. Note that only dog IDs are returned, " +
            "use POST /dogs to retrieve information about them.",
        method = "GET",
        responses = {
            @ApiResponse(
                content = {@Content(
                    examples = {
                        @ExampleObject(value = "{\"next\":\"/dogs/search?" +
                            "breeds=Affenpinscher&size=1&sort=name%3Aasc" +
                            "&from=1\",\"resultIds\":[" +
                            "\"rcD-OZUBBPFf4ZNZzDCC\"],\"total\":150}")
                    },
                    mediaType = "application/json"
                )},
                description = "OK",
                responseCode = "200"
            ),
            @ApiResponse(
                content = {@Content(
                    examples = {
                        @ExampleObject(value = "Unauthorized")
                    },
                    mediaType = "text/plain"
                )},
                description = "Unauthorized",
                responseCode = "401"
            )
        },
        summary = "Find dogs that matches the search criteria."
    )
    @GetMapping("/search")
    public DogSearchResponseDto search(
        @RequestParam(required = false)
        List<
            @NotBlank(message = "a breed must not be empty")
            @Valid
                String
            > breeds,

        @PositiveOrZero(message = "from must be positive or zero")
        @RequestParam(defaultValue = "0", required = false)
        Integer from,

        @PositiveOrZero(message = "ageMax must be positive or zero")
        @RequestParam(name = "ageMax", required = false)
        Integer maxAge,

        @PositiveOrZero(message = "ageMin must be positive or zero")
        @RequestParam(name = "ageMin", required = false)
        Integer minAge,

        HttpServletRequest request,

        @Positive(message = "size must be positive")
        @RequestParam(defaultValue = "25", required = false)
        Integer size,

        @Pattern(
            message = "sort must match (age|breed|name):(asc|desc)",
            regexp = "^(age|breed|name):(asc|desc)$"
        )
        @RequestParam(defaultValue = "breed:asc", required = false)
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

    @Operation(
        description = "Picks and returns a random dog ID from the input list.",
        method = "POST",
        responses = {
            @ApiResponse(
                content = {@Content(
                    examples = {
                        @ExampleObject(value =
                            "{\"match\": \"-7_-OZUBBPFf4ZNZzPJP\"}"
                        )
                    },
                    mediaType = "application/json"
                )},
                description = "OK",
                responseCode = "200"
            ),
            @ApiResponse(
                content = {@Content(
                    examples = {
                        @ExampleObject(value = "Unauthorized")
                    },
                    mediaType = "text/plain"
                )},
                description = "Unauthorized",
                responseCode = "401"
            )
        },
        summary = "Randomly select a dog from the list provided."
    )
    @PostMapping("/match")
    public Map<String, String> match(
        @NotNull(message = "body must not be null")
        @Parameter(
            description = "Dog IDs",
            required = true
        )
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

    /**
     * Build the navigation URL for "prev" and "next" fields in the
     * search result.
     *
     * @param from a new value for the "from" parameter to be set.
     * @param request a HttpServletRequest object that has the current
     * query string.
     * @return a string for the navigation path including the new
     * "from" parameter.
     */
    private String buildNavigation(
        Integer from,
        HttpServletRequest request
    ) {
        var tokens = request.getQueryString().split("&");
        var fromExists = false;
        for (var index = 0; index < tokens.length; index++) {
            if (tokens[index].startsWith("from=")) {
                tokens[index] = "from=" + from;
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

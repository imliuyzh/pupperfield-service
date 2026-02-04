package com.pupperfield.backend.controller;

import com.pupperfield.backend.model.DogDto;
import com.pupperfield.backend.model.DogSearchRequestDto;
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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.pupperfield.backend.constant.DogConstants.DOGS_PATH;
import static com.pupperfield.backend.constant.DogConstants.DOG_BREEDS_PATH;
import static com.pupperfield.backend.constant.DogConstants.DOG_MATCH_PATH;
import static com.pupperfield.backend.constant.DogConstants.DOG_SEARCH_PATH;

@AllArgsConstructor
@RestController
@Tag(description = "Execute operations upon dogs stored in the database.", name = "Dogs")
public class DogController {
    private DogService dogService;

    @GetMapping(DOG_BREEDS_PATH)
    @Operation(
        description = "Extracts all dog breeds from the database. The result is sorted in "
            + "non-descending order.",
        method = "GET",
        responses = {
            @ApiResponse(
                content = {@Content(
                    examples = {@ExampleObject(value = "[\"Affenpinscher\"]")},
                    mediaType = "application/json"
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
        summary = "Retrieve a list of all dog breeds in the database."
    )
    public Collection<String> getBreeds() {
        return dogService.getBreeds();
    }

    @Operation(
        description = "Receives a list of dog IDs (100 IDs max) and fetches their data. "
            + "Unlike the original implementation:"
            + "<ul>"
            + "<li>Fields are sorted by their names in alphabetical order.</li>"
            + "<li>A nonexistent dog ID is omitted from the response.</li>"
            + "<li>Duplicated dog information is listed only once.</li>"
            + "</ul>",
        method = "POST",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                examples = {@ExampleObject(value = "[\"qcD-OZUBBPFf4ZNZzDCC\"]")},
                mediaType = "application/json"
            ),
            required = true
        ),
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
                    examples = {@ExampleObject(value = "Unauthorized")},
                    mediaType = "text/plain"
                )},
                description = "Unauthorized",
                responseCode = "401"
            ),
            @ApiResponse(
                content = {@Content(
                    examples = {@ExampleObject(value = "{\"error\":" +
                        "\"Unprocessable Entity\",\"detail\":[" +
                        "\"a dog ID must not be empty\"]}")},
                    mediaType = "application/json"
                )},
                description = "Invalid request",
                responseCode = "422"
            )
        },
        summary = "Get a list of dog details based on the array passed in."
    )
    @PostMapping(DOGS_PATH)
    public List<DogDto> list(
        @NotNull(message = "body must not be null")
        @Parameter(
            description = "Dog IDs",
            example = "[\"qcD-OZUBBPFf4ZNZzDCC\"]",
            required = true
        )
        @RequestBody
        @Size(max = 100, message = "body must have at most 100 dog IDs")
        List<@NotBlank(message = "a dog ID must not be empty") @Size(max = 20, message =
            "a dog ID should have at most 20 characters") @Valid String> idList
    ) {
        return dogService.listDogs(idList);
    }

    @Operation(
        description = "Picks and returns a random dog ID from the input list.",
        method = "POST",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                examples = {
                    @ExampleObject(value = "[\"rcD-OZUBBPFf4ZNZzDCC\", \"-7_-OZUBBPFf4ZNZzPJP\"]")
                },
                mediaType = "application/json"
            ),
            required = true
        ),
        responses = {
            @ApiResponse(
                content = {@Content(
                    examples = {@ExampleObject(value = "{\"match\": \"-7_-OZUBBPFf4ZNZzPJP\"}")},
                    mediaType = "application/json"
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
            ),
            @ApiResponse(
                content = {@Content(
                    examples = {@ExampleObject(value = "{\"error\":" +
                        "\"Unprocessable Entity\",\"detail\":[\"a dog ID " +
                        "must not be empty\"]}")},
                    mediaType = "application/json"
                )},
                description = "Invalid request",
                responseCode = "422"
            )
        },
        summary = "Randomly select a dog from the list provided."
    )
    @PostMapping(DOG_MATCH_PATH)
    public Map<String, String> match(
        @NotNull(message = "body must not be null")
        @RequestBody
        @Size(message = "body must not be empty", min = 1)
        List<@NotBlank(message = "a dog ID must not be empty") @Size(max = 20, message =
            "a dog ID should have at most 20 characters") @Valid String> idList
    ) {
        return Map.of("match", dogService.matchDogs(idList));
    }

    @GetMapping(DOG_SEARCH_PATH)
    @Operation(
        description = "Searches for dogs in the database using filter conditions from request "
            + "parameters. All parameters are optional; by default, size is 25, from is 0, and "
            + "sort is breed:asc. Note only dog IDs are returned, please use POST /dogs to "
            + "retrieve full dog information. Unlike the original implementation:"
            + "<ul>"
            + "<li>Maximum number of dogs can be as much as the total number of rows in "
                + "database.</li>"
            + "<li>Parameter order in \"next\" and \"prev\" fields may differ from the original "
                + "sometimes.</li>"
            + "<li>Dog IDs can be listed differently due to the spider (`populator`)'s "
                + "processing order.</li>"
            + "<li>"
            + "`,` can be used to pass in multiple values for a query string parameter."
            + "<ul><li>Each parameter value must be listed out individually in the original "
                + "implementation.</li></ul>"
            + "</li>"
            + "<li>"
            + "\"prev\" and/or \"next\" fields are not included in the response when the \"from\""
                + "parameter value is out of bounds."
            + "<ul><li>For \"prev,\" it is included when \"from\" is equal or greater than "
                + "0.</li></ul>"
            + "<ul><li>For \"next,\" it is included when \"from\" is greater than zero but "
                + "less than the total number of result.</li></ul>"
            + "</li>"
            + "<li>HTTP 422 instead of HTTP 400/500 is used when an out of range value is "
                + "provided for some query string parameters.</li>"
            + "<li>HTTP 422 instead of HTTP 400 is used when some query string parameters "
                + "are repeated.</li>"
            + "</ul>",
        method = "GET",
        responses = {
            @ApiResponse(
                content = {@Content(
                    examples = {
                        @ExampleObject(value = "{\"next\":\"/dogs/search?" +
                            "breeds=Affenpinscher&size=1&from=26\",\"prev\":" +
                            "\"/dogs/search?breeds=Affenpinscher&size=1&" +
                            "from=24\",\"resultIds\":[\"5MD-OZUBBPFf4ZNZzDCC" +
                            "\"],\"total\":150}")
                    },
                    mediaType = "application/json"
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
            ),
            @ApiResponse(
                content = {@Content(
                    examples = {@ExampleObject(value = "{\"error\":" +
                        "\"Unprocessable Entity\",\"detail\":[" +
                        "\"from should be Integer\"]}")},
                    mediaType = "application/json"
                )},
                description = "Invalid request",
                responseCode = "422"
            )
        },
        summary = "Find dogs that matches the search criteria."
    )
    public DogSearchResponseDto search(
        @Valid DogSearchRequestDto parameters,
        HttpServletRequest request
    ) {
        Pair<List<String>, Long> outcome = dogService.searchDogs(parameters);
        var queryString = request.getQueryString();
        int size = parameters.getSize(),
            nextFrom = parameters.getFrom() + size,
            previousFrom = parameters.getFrom() - size;
        return DogSearchResponseDto.builder()
            .resultIds(outcome.getFirst())
            .total(outcome.getSecond())
            .next(nextFrom < outcome.getSecond() && nextFrom > 0
                ? dogService.buildNavigation(queryString, nextFrom, size)
                : null)
            .previous(previousFrom >= 0
                ? dogService.buildNavigation(queryString, previousFrom, size)
                : null)
            .build();
    }
}

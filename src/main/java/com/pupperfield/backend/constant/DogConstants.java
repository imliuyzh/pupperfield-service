package com.pupperfield.backend.constant;

import lombok.NoArgsConstructor;

/**
 * Constants for the dog endpoints.
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class DogConstants {
    /**
     * The base path for all dog operations.
     */
    public static final String DOGS_PATH = "/dogs";

    /**
     * The path for retrieving all dog breeds.
     */
    public static final String DOG_BREEDS_PATH = DOGS_PATH + "/breeds";

    /**
     * The path for matching a dog.
     */
    public static final String DOG_MATCH_PATH = DOGS_PATH + "/match";

    /**
     * The path for searching dogs.
     */
    public static final String DOG_SEARCH_PATH = DOGS_PATH + "/search";
}

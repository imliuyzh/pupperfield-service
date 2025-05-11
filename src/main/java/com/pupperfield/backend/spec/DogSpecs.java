package com.pupperfield.backend.spec;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.pupperfield.backend.entity.Dog;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access=lombok.AccessLevel.PRIVATE)
public class DogSpecs {
    public static Specification<Dog> withBreeds(List<String> breeds) {
        return (root, query, builder) ->
            builder.in(root.get("breed")).value(breeds);
    }

    public static Specification<Dog> withMaxAge(Integer ageMax) {
        return (root, query, builder) ->
            builder.lessThanOrEqualTo(root.get("age"), ageMax);
    }

    public static Specification<Dog> withMinAge(Integer ageMin) {
        return (root, query, builder) ->
            builder.greaterThanOrEqualTo(root.get("age"), ageMin);
    }

    public static Specification<Dog> withZipCodes(List<String> zipCodes) {
        return (root, query, builder) ->
            builder.in(root.get("zipCode")).value(zipCodes);
    }
}

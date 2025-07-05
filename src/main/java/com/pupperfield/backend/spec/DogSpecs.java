package com.pupperfield.backend.spec;

import com.pupperfield.backend.entity.Dog;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class DogSpecs {
    public static Specification<Dog> withBreeds(List<String> breeds) {
        return (root, query, builder) -> builder.in(root.get("breed")).value(breeds);
    }

    public static Specification<Dog> withAgeMax(Integer ageMax) {
        return (root, query, builder) -> builder.lessThanOrEqualTo(root.get("age"), ageMax);
    }

    public static Specification<Dog> withAgeMin(Integer ageMin) {
        return (root, query, builder) -> builder.greaterThanOrEqualTo(root.get("age"), ageMin);
    }

    public static Specification<Dog> withZipCodes(List<String> zipCodes) {
        return (root, query, builder) -> builder.in(root.get("zipCode")).value(zipCodes);
    }
}

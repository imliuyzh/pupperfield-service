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

    public static Specification<Dog> withMaxAge(Integer maxAge) {
        return (root, query, builder) -> builder.lessThanOrEqualTo(root.get("age"), maxAge);
    }

    public static Specification<Dog> withMinAge(Integer minAge) {
        return (root, query, builder) -> builder.greaterThanOrEqualTo(root.get("age"), minAge);
    }

    public static Specification<Dog> withZipCodes(List<String> zipCodes) {
        return (root, query, builder) -> builder.in(root.get("zipCode")).value(zipCodes);
    }
}

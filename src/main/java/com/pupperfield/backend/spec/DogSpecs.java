package com.pupperfield.backend.spec;

import com.pupperfield.backend.entity.Dog;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

/**
 * A utility class providing static methods for filtering
 * {@link com.pupperfield.backend.entity.Dog Dog} entities.
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class DogSpecs {
    /**
     * Returns a Specification that filters dogs by a list of breeds.
     *
     * @param breeds a list of dog breeds to filter by
     * @return a Specification to filter dogs matching any breed in {@code breeds}
     */
    public static Specification<Dog> withBreeds(List<String> breeds) {
        return (root, query, builder) -> builder.in(root.get("breed")).value(breeds);
    }

    /**
     * Returns a Specification that filters dogs with age less than or equal to the specified age.
     *
     * @param ageMax the maximum age (inclusive) to filter dogs by
     * @return a Specification to filter dogs with age less than or equal to {@code ageMax}
     */
    public static Specification<Dog> withAgeMax(Integer ageMax) {
        return (root, query, builder) -> builder.lessThanOrEqualTo(root.get("age"), ageMax);
    }

    /**
     * Returns a Specification that filters dogs with age greater than or equal to the
     * specified age.
     *
     * @param ageMin the minimum age (inclusive) to filter dogs by
     * @return a Specification to filter dogs with age greater than or equal to {@code ageMin}
     */
    public static Specification<Dog> withAgeMin(Integer ageMin) {
        return (root, query, builder) -> builder.greaterThanOrEqualTo(root.get("age"), ageMin);
    }

    /**
     * Returns a Specification that filters dogs by a list of zip codes.
     *
     * @param zipCodes a list of zip codes to filter dogs by
     * @return a Specification to filter dogs located in any zip code in {@code zipCodes}
     */
    public static Specification<Dog> withZipCodes(List<String> zipCodes) {
        return (root, query, builder) -> builder.in(root.get("zipCode")).value(zipCodes);
    }
}

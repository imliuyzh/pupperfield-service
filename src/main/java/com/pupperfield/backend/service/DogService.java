package com.pupperfield.backend.service;

import com.pupperfield.backend.config.CacheConfig;
import com.pupperfield.backend.entity.Dog;
import com.pupperfield.backend.mapper.DogMapper;
import com.pupperfield.backend.model.DogDto;
import com.pupperfield.backend.model.DogSearchRequestDto;
import com.pupperfield.backend.pagination.DogSearchPagination;
import com.pupperfield.backend.repository.DogRepository;
import com.pupperfield.backend.spec.DogSpecs;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;

/**
 * A service for dog-related operations.
 */
@AllArgsConstructor
@Service
public class DogService {
    private DogMapper dogMapper;
    private DogRepository dogRepository;

    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Retrieves a cached list of all dog breeds.
     *
     * @return a collection of dog breed names
     */
    @Cacheable(cacheNames = {CacheConfig.BREED_CACHE})
    public Collection<String> getBreeds() {
        return dogRepository.getBreeds();
    }

    /**
     * Returns a list of dog information in the original input order. Results are cached
     * unless the result is empty.
     *
     * @param idList a list of dog IDs
     * @return a list of {@link com.pupperfield.backend.model.DogDto DogDto} in input order
     */
    @Cacheable(
        cacheNames = {CacheConfig.LIST_CACHE},
        unless = "#result?.isEmpty()"
    )
    public List<DogDto> listDogs(List<String> idList) {
        var indexMap = new HashMap<String, Integer>();
        for (var index = 0; index < idList.size(); index++) {
            indexMap.put(idList.get(index), index);
        }
        return dogRepository.findAllById(idList)
            .stream()
            .sorted(Comparator.comparingInt(dog -> indexMap.get(dog.getId())))
            .map(dogMapper::dogToDogDto)
            .toList();
    }

    /**
     * Randomly selects a dog ID from the input.
     *
     * @param idList a list of dog IDs to choose from
     * @return a random dog ID
     */
    public String matchDogs(List<String> idList) {
        return idList.get(RANDOM.nextInt(idList.size()));
    }

    /**
     * Searches for dogs based on various filter and sort parameters. Results are cached
     * unless the result is empty.
     *
     * @param parameters search parameters
     * @return an object containing a list of dog IDs and the total count
     */
    @Cacheable(
        cacheNames = {CacheConfig.SEARCH_CACHE},
        key = "#parameters.getBreeds() + '_' + #parameters.getFrom() + '_' + "
            + "#parameters.getAgeMax() + '_' + #parameters.getAgeMin() + '_' + "
            + "#parameters.getSize() + '_' + #parameters.getSort() + '_' + "
            + "#parameters.getZipCodes()",
        unless = "#result?.getSecond() <= 0"
    )
    public Pair<List<String>, Long> searchDogs(DogSearchRequestDto parameters) {
        // Use Specification.unrestricted() for Spring Data JPA v4.0+.
        Specification<Dog> conditions = (root, query, builder) -> null;
        if (parameters.getZipCodes() != null && parameters.getZipCodes().isEmpty() == false) {
            conditions = conditions.and(DogSpecs.withZipCodes(parameters.getZipCodes()));
        }
        if (parameters.getAgeMax() != null) {
            conditions = conditions.and(DogSpecs.withAgeMax(parameters.getAgeMax()));
        }
        if (parameters.getAgeMin() != null) {
            conditions = conditions.and(DogSpecs.withAgeMin(parameters.getAgeMin()));
        }
        if (parameters.getBreeds() != null && parameters.getBreeds().isEmpty() == false) {
            conditions = conditions.and(DogSpecs.withBreeds(parameters.getBreeds()));
        }

        var sortInfo = parameters.getSort().split(":");
        var pageRequest = dogRepository.findAll(conditions, new DogSearchPagination(
            parameters.getSize(),
            parameters.getFrom(),
            Sort.by(new Order(sortInfo[1].equals("asc") ? ASC : DESC, sortInfo[0]))
        ));
        return Pair.of(
            pageRequest.getContent()
                .stream()
                .map(Dog::getId)
                .toList(),
            pageRequest.getTotalElements()
        );
    }

    /**
     * Builds a navigation URL for search pagination. If the "size" field is present in the
     * query string, then it will be kept and the parameter is ignored. Otherwise, the value
     * in the argument will be used in the query string.
     *
     * @param query current query string
     * @param from a value for the "from" field
     * @param size a value for the "size" field
     * @return a full query for pagination
     */
    public String buildNavigation(String query, Integer from, Integer size) {
        var pairs = new ArrayList<String>();
        if (isNotBlank(query)) {
            Collections.addAll(pairs, query.split("&"));
        }

        boolean fromExists = false, sizeExists = false;
        for (int index = 0; index < pairs.size(); index++) {
            if (pairs.get(index).startsWith("from=")) {
                pairs.set(index, "from=%d".formatted(from));
                fromExists = true;
            }
            if (pairs.get(index).startsWith("size=")) {
                sizeExists = true;
            }
        }
        if (sizeExists == false) {
            pairs.add("size=%d".formatted(size));
        }
        if (fromExists == false) {
            pairs.add("from=%d".formatted(from));
        }

        return "/dogs/search?%s".formatted(String.join("&", pairs));
    }
}

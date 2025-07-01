package com.pupperfield.backend.service;

import com.pupperfield.backend.config.CacheConfig;
import com.pupperfield.backend.entity.Dog;
import com.pupperfield.backend.mapper.DogMapper;
import com.pupperfield.backend.model.DogDto;
import com.pupperfield.backend.model.DogSearchResponseDto;
import com.pupperfield.backend.repository.DogRepository;
import com.pupperfield.backend.spec.DogSpecs;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;

@AllArgsConstructor
@Service
public class DogService {
    private DogMapper dogMapper;
    private DogRepository dogRepository;

    private static final SecureRandom RANDOM = new SecureRandom();

    public DogSearchResponseDto buildSearchResponse(
        Pair<List<String>, Long> queryResult,
        Integer from,
        Integer size,
        HttpServletRequest request
    ) {
        var response = DogSearchResponseDto.builder()
            .resultIds(queryResult.getFirst())
            .total(queryResult.getSecond());
        if (from - size >= 0) {
            response = response.previous(
                buildNavigation(from - size, request));
        }
        if (from + size < queryResult.getSecond()) {
            response = response.next(
                buildNavigation(from + size, request));
        }
        return response.build();
    }

    @Cacheable(cacheNames = {CacheConfig.BREED_CACHE})
    public Collection<String> getBreeds() {
        return dogRepository.getBreeds();
    }

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

    public String matchDogs(List<String> idList) {
        return idList.get(RANDOM.nextInt(idList.size()));
    }

    @Cacheable(
        cacheNames = {CacheConfig.SEARCH_CACHE},
        key = "#breeds + '_' + #from + '_' + #maxAge + '_' + #minAge + '_' + "
            + "#size + '_' + #sort + '_' + #zipCodes",
        unless = "#result?.getSecond() <= 0"
    )
    public Pair<List<String>, Long> searchDogs(
        List<String> breeds,
        Integer from,
        Integer maxAge,
        Integer minAge,
        Integer size,
        String sort,
        List<String> zipCodes
    ) {
        // Use Specification.unrestricted() for Spring Data JPA v4.0+.
        Specification<Dog> conditions = (root, query, builder) -> null;
        if (zipCodes != null && zipCodes.isEmpty() == false) {
            conditions = conditions.and(DogSpecs.withZipCodes(zipCodes));
        }
        if (maxAge != null) {
            conditions = conditions.and(DogSpecs.withMaxAge(maxAge));
        }
        if (minAge != null) {
            conditions = conditions.and(DogSpecs.withMinAge(minAge));
        }
        if (breeds != null && breeds.isEmpty() == false) {
            conditions = conditions.and(DogSpecs.withBreeds(breeds));
        }

        var sortInfo = sort.split(":");
        var sortOrder = Sort.by(new Order(
            sortInfo[1].equals("asc") ? ASC : DESC, sortInfo[0]
        ));

        return Pair.of(
            dogRepository.findAll(conditions, sortOrder)
                .stream()
                .skip(from)
                .limit(size)
                .map(Dog::getId)
                .toList(),
            dogRepository.count(conditions)
        );
    }

    /**
     * Build the value for "prev" and "next" fields in the search result.
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

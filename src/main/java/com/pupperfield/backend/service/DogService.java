package com.pupperfield.backend.service;

import com.pupperfield.backend.config.CacheConfig;
import com.pupperfield.backend.entity.Dog;
import com.pupperfield.backend.mapper.DogMapper;
import com.pupperfield.backend.model.DogDto;
import com.pupperfield.backend.repository.DogRepository;
import com.pupperfield.backend.spec.DogSpecs;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.util.Pair;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
        var pageRequest = dogRepository.findAll(conditions, new DogSearchPageRequest(
            size, from, Sort.by(new Order(sortInfo[1].equals("asc") ? ASC : DESC, sortInfo[0]))));
        return Pair.of(
            pageRequest.getContent()
                .stream()
                .map(Dog::getId)
                .toList(),
            pageRequest.getTotalElements()
        );
    }

    /**
     * Build the value for "prev" and "next" fields in the search result.
     *
     * @param query current query string
     * @param from a value for the "from" field
     * @param size a value for the "size" field
     * @return a string for the navigation path including the new "from" parameter
     */
    public String buildNavigation(String query, Integer from, Integer size) {
        var pairs = new ArrayList<String>();
        if (query != null && query.isEmpty() == false) {
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
        if (fromExists == false) {
            pairs.add("from=%d".formatted(from));
        }
        if (sizeExists == false) {
            pairs.add("size=%d".formatted(size));
        }
        return "/dogs/search?%s".formatted(String.join("&", pairs));
    }

    @Value
    private static class DogSearchPageRequest implements Pageable {
        int limit;

        @Getter
        long offset;

        @Getter
        Sort sort;

        @NonNull
        public Pageable first() {
            return new DogSearchPageRequest(this.limit, this.offset, this.sort);
        }

        public int getPageNumber() {
            return 0;
        }

        public int getPageSize() {
            return this.limit;
        }

        public boolean hasPrevious() {
            return false;
        }

        @NonNull
        public Pageable next() {
            throw new UnsupportedOperationException("Not implemented");
        }

        @NonNull
        public Pageable previousOrFirst() {
            return this.first();
        }

        @NonNull
        public Pageable withPage(int pageNumber) {
            throw new UnsupportedOperationException("Not implemented");
        }
    }
}

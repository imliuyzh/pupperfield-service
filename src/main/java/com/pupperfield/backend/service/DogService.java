package com.pupperfield.backend.service;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import com.pupperfield.backend.entity.Dog;
import com.pupperfield.backend.mapper.DogMapper;
import com.pupperfield.backend.model.DogDto;
import com.pupperfield.backend.repository.DogRepository;
import com.pupperfield.backend.spec.DogSpecs;

import lombok.AllArgsConstructor;

import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;

@AllArgsConstructor
@Service
public class DogService {
    private DogMapper dogMapper;
    private DogRepository dogRepository;

    @Cacheable("breeds")
    public Collection<String> getBreeds() {
        return dogRepository.getBreeds();
    }

    @Cacheable("lists")
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

    @Cacheable(
        key = "#breeds + '_' + #from + '_' + #maxAge + '_' + #minAge + '_' + "
            + "#size + '_' + #sort + '_' + #zipCodes",
        value = "searches"
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
        Specification<Dog> conditions = Specification.where(null);
        if (zipCodes != null && zipCodes.isEmpty() == false) {
            conditions = conditions.and(DogSpecs.withZipCodes(zipCodes));
        }
        if (minAge != null) {
            conditions = conditions.and(DogSpecs.withMinAge(minAge));
        }
        if (maxAge != null) {
            conditions = conditions.and(DogSpecs.withMaxAge(maxAge));
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
}

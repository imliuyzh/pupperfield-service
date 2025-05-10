package com.pupperfield.backend.service;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.pupperfield.backend.mapper.DogMapper;
import com.pupperfield.backend.model.DogDto;
import com.pupperfield.backend.model.DogSearchRequestDto;
import com.pupperfield.backend.model.DogSearchResponseDto;
import com.pupperfield.backend.repository.DogRepository;

import lombok.AllArgsConstructor;

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
    public Collection<DogDto> listDogs(List<String> idList) {
        var indexMap = new HashMap<String, Integer>();
        for (int i = 0; i < idList.size(); i++) {
            indexMap.put(idList.get(i), i);
        }
        return dogRepository.findAllById(idList)
            .stream()
            .sorted(Comparator.comparingInt(dog -> indexMap.get(dog.getId())))
            .map(dogMapper::dogToDogDto)
            .toList();
    }

    public DogSearchResponseDto searchDogs(DogSearchRequestDto parameters) {
        var response = DogSearchResponseDto.builder();
        return response.build();
    }
}

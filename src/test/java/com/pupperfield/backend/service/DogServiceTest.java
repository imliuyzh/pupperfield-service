package com.pupperfield.backend.service;

import com.pupperfield.backend.entity.Dog;
import com.pupperfield.backend.mapper.DogMapper;
import com.pupperfield.backend.model.DogDto;
import com.pupperfield.backend.model.DogSearchRequestDto;
import com.pupperfield.backend.pagination.DogSearchPagination;
import com.pupperfield.backend.repository.DogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.data.domain.Sort.Direction.ASC;

@ExtendWith(MockitoExtension.class)
public class DogServiceTest {
    @Mock
    private DogMapper dogMapper;

    @Mock
    private DogRepository dogRepository;

    @InjectMocks
    private DogService dogService;

    @Test
    public void testGetBreeds() {
        given(dogRepository.getBreeds()).willReturn(List.of("Breed1", "Breed2", "Breed3"));
        var breeds = dogService.getBreeds();

        verify(dogRepository, times(1)).getBreeds();
        assertThat(breeds).isNotEmpty();
        for (var breed : breeds) {
            assertThat(breed).isNotEmpty();
        }
    }

    @Test
    public void testListDogs() {
        var idList = List.of(
            "s8D-OZUBBPFf4ZNZzA1l",
            "tMD-OZUBBPFf4ZNZzA1l",
            "tcD-OZUBBPFf4ZNZzA1l",
            "ucD-OZUBBPFf4ZNZzA1l",
            "vMD-OZUBBPFf4ZNZzA1l"
        );
        var dogList = idList.stream()
            .map(id -> Dog.builder().id(id).build())
            .toList();

        given(dogRepository.findAllById(any())).willReturn(dogList);
        for (var index = 0; index < idList.size(); index++) {
            given(dogMapper.dogToDogDto(dogList.get(index))).willReturn(
                DogDto.builder().id(idList.get(index)).build());
        }

        var dogs = dogService.listDogs(idList);
        verify(dogRepository, times(1)).findAllById(ArgumentMatchers.eq(idList));
        verify(dogMapper, times(idList.size())).dogToDogDto(any());
        assertThat(dogs).isNotEmpty();
        assertThat(dogs.size()).isEqualTo(idList.size());
        dogs.forEach(dog -> assertThat(idList.contains(dog.getId())).isTrue());
    }

    @Test
    public void testMatchDogs() {
        var idList = List.of(
            "n8D-OZUBBPFf4ZNZzCt-",
            "gcD-OZUBBPFf4ZNZzAli",
            "G8D-OZUBBPFf4ZNZzBxx",
            "Wr_-OZUBBPFf4ZNZzPJO",
            "xMD-OZUBBPFf4ZNZzCp9",
            "lsD-OZUBBPFf4ZNZzDKD",
            "k8D-OZUBBPFf4ZNZzDaG",
            "Ar_-OZUBBPFf4ZNZzPVS",
            "87_-OZUBBPFf4ZNZzPlX",
            "lMD-OZUBBPFf4ZNZzCh8",
            "w8D-OZUBBPFf4ZNZzCh8"
        );
        assertThat(idList.contains(dogService.matchDogs(idList))).isTrue();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSearchDogs() {
        given(dogRepository.findAll(any(Specification.class), any(DogSearchPagination.class)))
            .willReturn(new PageImpl<>(
                List.of(Dog.builder().id("qcD-OZUBBPFf4ZNZzDCC").build()),
                new DogSearchPagination(1, 0, Sort.by(ASC, "breed")), 1));

        var result = dogService.searchDogs(
            DogSearchRequestDto.builder().from(0).size(1).sort("breed:asc").build());
        verify(dogRepository, times(1)).findAll(
            any(Specification.class), any(DogSearchPagination.class));
        assertThat(result.getFirst()).isNotEmpty();
        assertThat(result.getSecond()).isGreaterThan(0);
    }
}

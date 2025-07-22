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
public class DogServiceTests {
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
        breeds.forEach(breed -> assertThat(breed).isNotEmpty());
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
        for (var dog : dogList) {
            given(dogMapper.dogToDogDto(dog)).willReturn(DogDto.builder().id(dog.getId()).build());
        }

        var dogs = dogService.listDogs(idList);
        verify(dogRepository, times(1)).findAllById(ArgumentMatchers.eq(idList));
        verify(dogMapper, times(idList.size())).dogToDogDto(any(Dog.class));
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

        var result = dogService.searchDogs(DogSearchRequestDto.builder()
            .from(0)
            .size(1)
            .sort("breed:asc")
            .build()
        );
        verify(dogRepository, times(1)).findAll(
            any(Specification.class), any(DogSearchPagination.class));
        assertThat(result.getFirst()).isNotEmpty();
        assertThat(result.getSecond()).isGreaterThan(0);
    }

    @Test
    public void testBuildNavigationWithAllFieldsPresent() {
        var link = dogService.buildNavigation(
            "from=0&size=50&sort=breed:asc&ageMax=0&ageMin=15&breeds=Doberman&zipCodes=10001",
            50, 25
        );
        assertThat(link.contains("from=50")).isTrue();
        assertThat(link.contains("size=25")).isFalse();
        assertThat(link.contains("size=50")).isTrue();
        assertThat(link.contains("sort=breed:asc")).isTrue();
        assertThat(link.contains("ageMax=0")).isTrue();
        assertThat(link.contains("ageMin=15")).isTrue();
        assertThat(link.contains("breeds=Doberman")).isTrue();
        assertThat(link.contains("zipCodes=10001")).isTrue();
    }

    @Test
    public void testBuildNavigationWithEmptyQueryString() {
        var link = dogService.buildNavigation("", 0, 5);
        assertThat(link.contains("from=0")).isTrue();
        assertThat(link.contains("size=5")).isTrue();
    }

    @Test
    public void testBuildNavigationWithFromMissing() {
        var link = dogService.buildNavigation("size=100&ageMin=6", 10, 100);
        assertThat(link.contains("from=10")).isTrue();
        assertThat(link.contains("size=100")).isTrue();
        assertThat(link.contains("ageMin=6")).isTrue();
    }

    @Test
    public void testBuildNavigationWithSizeMissing() {
        var link = dogService.buildNavigation("from=0&zipCodes=12345,54321", 10, 5);
        assertThat(link.contains("from=10")).isTrue();
        assertThat(link.contains("size=5")).isTrue();
        assertThat(link.contains("zipCodes=12345,54321")).isTrue();
    }

    @Test
    public void testBuildNavigationWithFromAndSizeMissing() {
        var link = dogService.buildNavigation(
            "breeds=Doberman,Affenpinscher&zipCodes=12345,54321", 234, 9);
        assertThat(link.contains("from=234")).isTrue();
        assertThat(link.contains("size=9")).isTrue();
        assertThat(link.contains("breeds=Doberman,Affenpinscher")).isTrue();
        assertThat(link.contains("zipCodes=12345,54321")).isTrue();
    }

    @Test
    public void testBuildNavigationWithRepeatedParameters1() {
        var link = dogService.buildNavigation("from=0&from=5&size=100&size=200", 2, 1);
        assertThat(link.contains("from=2")).isTrue();
        assertThat(link.contains("size=1")).isTrue();
        assertThat(link.split("&").length).isEqualTo(2);
    }

    @Test
    public void testBuildNavigationWithRepeatedParameters2() {
        var link = dogService.buildNavigation(
            "from=0&ageMax=5&from=5&size=100&ageMax=0&size=200", 2, 1);
        assertThat(link.contains("from=2")).isTrue();
        assertThat(link.contains("size=1")).isTrue();
        assertThat(link.split("&").length).isEqualTo(4);
    }
}

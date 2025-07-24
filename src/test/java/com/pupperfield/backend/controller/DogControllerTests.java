package com.pupperfield.backend.controller;

import com.pupperfield.backend.model.DogDto;
import com.pupperfield.backend.model.DogSearchRequestDto;
import com.pupperfield.backend.service.DogService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.util.Pair;

import java.security.SecureRandom;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class DogControllerTests {
    @InjectMocks
    private DogController dogController;

    @Mock
    private DogService dogService;

    @Test
    public void testGetBreeds() {
        given(dogService.getBreeds()).willReturn(List.of("Breed1", "Breed2", "Breed3"));
        var breeds = dogController.getBreeds();

        verify(dogService, times(1)).getBreeds();
        assertThat(breeds).isNotEmpty();
        breeds.forEach(breed -> assertThat(breed).isNotEmpty());
    }

    @Test
    public void testList() {
        var idList = List.of(
            "CcD-OZUBBPFf4ZNZzCh7",
            "zsD-OZUBBPFf4ZNZzCd7",
            "E8D-OZUBBPFf4ZNZzCh7",
            "88D-OZUBBPFf4ZNZzCd7",
            "w8D-OZUBBPFf4ZNZzCd7",
            "lsD-OZUBBPFf4ZNZzCd7",
            "lMD-OZUBBPFf4ZNZzCd7",
            "_cD-OZUBBPFf4ZNZzCd7",
            "0MD-OZUBBPFf4ZNZzCd7",
            "9MD-OZUBBPFf4ZNZzCd7"
        );
        given(dogService.listDogs(idList)).willReturn(idList.stream()
            .map(id -> DogDto.builder().id(id).build())
            .toList());
        var dogs = dogController.list(idList);
        verify(dogService, times(1)).listDogs(idList);
        assertThat(dogs).isNotEmpty();
        dogs.forEach(dog -> assertThat(dog).isNotNull());
    }

    @Test
    public void testMatch() {
        var idList = List.of("kMD-OZUBBPFf4ZNZzCd7", "icD-OZUBBPFf4ZNZzCd7");
        given(dogService.matchDogs(idList))
            .willReturn(idList.get(new SecureRandom().nextInt(idList.size())));
        var result = dogController.match(idList);
        verify(dogService, times(1)).matchDogs(idList);
        assertThat(idList.contains(result.get("match"))).isTrue();
    }

    @Test
    public void testSearch() {
        given(dogService.searchDogs(any(DogSearchRequestDto.class)))
            .willReturn(Pair.of(List.of("rr_-OZUBBPFf4ZNZzPlX"), 1L));
        var result = dogController.search(
            DogSearchRequestDto.builder()
                .sort("breed:asc")
                .zipCodes(List.of("12345"))
                .build(),
            mock(HttpServletRequest.class)
        );
        assertThat(result).isNotNull();
        assertThat(result.getResultIds()).isNotEmpty();
        assertThat(result.getResultIds().getFirst()).isEqualTo("rr_-OZUBBPFf4ZNZzPlX");
        assertThat(result.getTotal()).isEqualTo(1);
        verify(dogService, times(1)).searchDogs(any(DogSearchRequestDto.class));
    }
}

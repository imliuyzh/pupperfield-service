package com.pupperfield.backend.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.pupperfield.backend.config.CacheConfig;
import com.pupperfield.backend.model.DogSearchRequestDto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class DogServiceCachingTests {
    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private DogService dogService;

    @BeforeEach
    public void setUp() {
        for (var cacheName : cacheManager.getCacheNames()) {
            cacheManager.getCache(cacheName).clear();
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testAllCachesAreEmptyAtFirst() {
        for (var cacheName : cacheManager.getCacheNames()) {
            var cache = (Cache<Object, Object>) cacheManager.getCache(cacheName).getNativeCache();
            assertThat(cache.asMap().size()).isZero();
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testBreedCacheFilledOncePopulated() {
        var cache = (Cache<Object, Object>) cacheManager.getCache(CacheConfig.BREED_CACHE)
            .getNativeCache();
        assertThat(cache.asMap().size()).isZero();
        dogService.getBreeds();
        assertThat(cache.asMap().size()).isEqualTo(1);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testListCacheFilledOncePopulated() {
        var cache = (Cache<Object, Object>) cacheManager.getCache(CacheConfig.LIST_CACHE)
            .getNativeCache();
        assertThat(cache.asMap().size()).isZero();
        dogService.listDogs(List.of("Tb_-OZUBBPFf4ZNZzPFL", "UL_-OZUBBPFf4ZNZzPFL"));
        assertThat(cache.asMap().size()).isEqualTo(1);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testListCacheNotFilledIfResultIsEmpty() {
        var cache = (Cache<Object, Object>) cacheManager.getCache(CacheConfig.LIST_CACHE)
            .getNativeCache();
        assertThat(cache.asMap().size()).isZero();
        dogService.listDogs(List.of("1", "2", "3"));
        assertThat(cache.asMap().size()).isZero();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSearchCacheFilledOncePopulated() {
        var cache = (Cache<Object, Object>) cacheManager.getCache(CacheConfig.SEARCH_CACHE)
            .getNativeCache();
        assertThat(cache.asMap().size()).isZero();
        dogService.searchDogs(DogSearchRequestDto.builder()
            .from(10)
            .size(1)
            .build());
        assertThat(cache.asMap().size()).isGreaterThan(0);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSearchCacheNotFilledIfResultIsEmpty() {
        var cache = (Cache<Object, Object>) cacheManager.getCache(CacheConfig.SEARCH_CACHE)
            .getNativeCache();
        assertThat(cache.asMap().size()).isZero();
        dogService.searchDogs(DogSearchRequestDto.builder()
            .size(1)
            .zipCodes(List.of("fwfwef"))
            .build());
        assertThat(cache.asMap().size()).isZero();
    }
}

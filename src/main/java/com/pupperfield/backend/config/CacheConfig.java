package com.pupperfield.backend.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {
    public static final String
        BREED_CACHE = "breeds",
        LIST_CACHE = "lists",
        SEARCH_CACHE = "searches";

    @Bean("cacheManager")
    public CacheManager cacheManager() {
        var cacheManager = new CaffeineCacheManager();
        cacheManager.registerCustomCache(
            BREED_CACHE, Caffeine.newBuilder().build()
        );
        cacheManager.registerCustomCache(
            LIST_CACHE, Caffeine.newBuilder()
                .expireAfterAccess(15, TimeUnit.MINUTES)
                .build()
        );
        cacheManager.registerCustomCache(
            SEARCH_CACHE, Caffeine.newBuilder()
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .build()
        );
        return cacheManager;
    }
}

package com.pupperfield.backend.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * A configuration class for setting up caching using Caffeine.
 */
@Configuration
@EnableCaching
public class CacheConfig {
    /**
     * Cache name for /dogs/breeds.
     */
    public static final String BREED_CACHE = "breeds";

    /**
     * Cache name for /dogs.
     */
    public static final String LIST_CACHE = "lists";

    /**
     * Cache name for /dogs/search.
     */
    public static final String SEARCH_CACHE = "searches";

    /**
     * Creates a CacheManager using Caffeine as the provider. It contains three caches:
     *
     * <ul>
     *     <li>{@code breeds} — no expiration</li>
     *     <li>{@code lists} — expires 15 minutes after last access</li>
     *     <li>{@code searches} — expires 10 minutes after last access</li>
     * </ul>
     *
     * @return a configured CacheManager instance
     */
    @Bean("cacheManager")
    public CacheManager cacheManager() {
        var cacheManager = new CaffeineCacheManager();
        cacheManager.registerCustomCache(BREED_CACHE, Caffeine.newBuilder().build());
        cacheManager.registerCustomCache(
            LIST_CACHE, Caffeine.newBuilder()
                .expireAfterAccess(1, TimeUnit.MINUTES)
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

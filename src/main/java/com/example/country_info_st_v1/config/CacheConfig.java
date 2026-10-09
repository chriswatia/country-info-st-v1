package com.example.country_info_st_v1.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String CACHE_COUNTRIES = "countries";
    public static final String CACHE_COUNTRY_BY_ID = "countryById";
    public static final String CACHE_COUNTRY_INFO = "countryInfo";

    @Bean
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        cacheManager.setCacheNames(List.of(CACHE_COUNTRIES, CACHE_COUNTRY_BY_ID, CACHE_COUNTRY_INFO));
        return cacheManager;
    }
}

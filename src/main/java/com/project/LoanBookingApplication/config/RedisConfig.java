package com.project.LoanBookingApplication.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class RedisConfig {

    private static RedisCacheConfiguration createCacheConfig(Duration ttl) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(ttl)
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
                )
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer())
                );
    }

    
    
    @Bean
    public RedisCacheManager cacheManager(
            RedisConnectionFactory connectionFactory,
            @Value("${cache.redis.default-ttl-minutes}") long defaultTtlMinutes,
            @Value("${cache.redis.offers-ttl-minutes}") long offersTtlMinutes,
            @Value("${cache.redis.lenders-ttl-minutes}") long lendersTtlMinutes) {

        Duration defaultTtl = Duration.ofMinutes(defaultTtlMinutes);
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        cacheConfigurations.put(CacheNames.OFFERS, createCacheConfig(Duration.ofMinutes(offersTtlMinutes)));
        cacheConfigurations.put(CacheNames.LENDERS, createCacheConfig(Duration.ofMinutes(lendersTtlMinutes)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(createCacheConfig(defaultTtl))
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}

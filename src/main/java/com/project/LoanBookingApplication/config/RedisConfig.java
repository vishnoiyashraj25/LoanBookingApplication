package com.project.LoanBookingApplication.config;

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

    /** Default TTL for any cache not explicitly configured. */
    private static final Duration DEFAULT_TTL = Duration.ofMinutes(10);

    /** TTL for Offer cache. */
    private static final Duration OFFERS_TTL = Duration.ofMinutes(5);

    /** TTL for Lender cache. */
    private static final Duration LENDERS_TTL = Duration.ofMinutes(15);

    /** TTL for User list cache. */
    private static final Duration USERS_LIST_TTL = Duration.ofMinutes(10);

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
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        cacheConfigurations.put("offers", createCacheConfig(OFFERS_TTL));
        cacheConfigurations.put("lenders", createCacheConfig(LENDERS_TTL));
        cacheConfigurations.put("users_list", createCacheConfig(USERS_LIST_TTL));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(createCacheConfig(DEFAULT_TTL))
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}

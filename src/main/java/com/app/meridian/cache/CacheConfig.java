package com.app.meridian.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.app.meridian.config.RedisAvailability;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.benmanes.caffeine.cache.Caffeine;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Picks the cache backend once, at startup:
 * <ul>
 * <li>{@code provider=auto} (default) — Redis if a server answers the probe, otherwise Caffeine</li>
 * <li>{@code provider=redis} — always Redis</li>
 * <li>{@code provider=memory} — always Caffeine (tests, single small instance)</li>
 * </ul>
 * Whichever wins, the cache names and TTLs are identical, and a cache failure never fails a request.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class CacheConfig {

    private final CacheProperties properties;
    private final RedisAvailability redisAvailability;
    private final RedisConnectionFactory redisConnectionFactory;

    @Bean
    public CacheManager cacheManager() {
        boolean useRedis = properties.isRedis()
                || (properties.isAuto() && redisAvailability.isAvailable());
        if (useRedis) {
            return redisCacheManager();
        }
        log.info("Cache provider: in-memory (Caffeine)");
        return caffeineCacheManager();
    }

    private CacheManager caffeineCacheManager() {
        List<CaffeineCache> caches = new ArrayList<>();
        for (String name : CacheNames.ALL) {
            caches.add(new CaffeineCache(name, Caffeine.newBuilder()
                    .expireAfterWrite(properties.ttlFor(name))
                    .maximumSize(10_000)
                    .build()));
        }
        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(caches);
        return manager;
    }

    private CacheManager redisCacheManager() {
        log.info("Cache provider: Redis (prefix '{}')", properties.keyPrefix());
        RedisCacheConfiguration defaults = RedisCacheConfiguration.defaultCacheConfig()
                .prefixCacheNameWith(properties.keyPrefix())
                .entryTtl(properties.defaultTtl())
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(valueSerializer()));

        Map<String, RedisCacheConfiguration> perCache = new HashMap<>();
        CacheNames.ALL.forEach(name -> perCache.put(name, defaults.entryTtl(properties.ttlFor(name))));

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaults)
                .withInitialCacheConfigurations(perCache)
                .transactionAware()
                .build();
    }

    /** Entities carry LocalDate/LocalDateTime, so the Redis mapper needs the JSR-310 module. */
    private RedisSerializer<Object> valueSerializer() {
        ObjectMapper mapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();
        mapper.activateDefaultTyping(mapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.EVERYTHING, JsonTypeInfo.As.PROPERTY);
        return new GenericJackson2JsonRedisSerializer(mapper);
    }
}

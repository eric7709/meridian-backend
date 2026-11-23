package com.app.meridian.auth.tokens;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.app.meridian.config.RedisAvailability;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Same choice the cache and the rate limiter already make: shared Redis state when a server answers
 * the startup probe, per-JVM state otherwise.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class TokenStoreConfig {

    private final RedisAvailability redisAvailability;
    private final StringRedisTemplate stringRedisTemplate;

    @Bean
    public TokenStore tokenStore() {
        if (redisAvailability.isAvailable()) {
            log.info("Refresh and reset tokens: Redis");
            return new RedisTokenStore(stringRedisTemplate);
        }
        log.info("Refresh and reset tokens: in-memory");
        return new InMemoryTokenStore();
    }
}

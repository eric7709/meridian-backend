package com.app.meridian.rateLimit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.app.meridian.config.RedisAvailability;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * One budget table, two possible stores: Redis (shared by every instance) when it is reachable, or
 * per-JVM counters otherwise. Selected once at startup with the same probe the cache uses.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class RateLimitConfig {

    private final RateLimitProperties properties;
    private final RedisAvailability redisAvailability;
    private final StringRedisTemplate stringRedisTemplate;

    @Bean
    public RateLimiter rateLimiter() {
        boolean useRedis = properties.isRedisBackend()
                || (properties.isAutoBackend() && redisAvailability.isAvailable());
        if (useRedis) {
            log.info("Rate limiter: Redis ({} requests/min, {} on auth)",
                    properties.requestsPerMinute(), properties.authRequestsPerMinute());
            return new RedisRateLimiter(stringRedisTemplate);
        }
        log.info("Rate limiter: in-memory ({} requests/min, {} on auth)",
                properties.requestsPerMinute(), properties.authRequestsPerMinute());
        return new InMemoryRateLimiter();
    }
}

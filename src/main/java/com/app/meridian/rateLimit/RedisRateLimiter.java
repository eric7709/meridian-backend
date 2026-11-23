package com.app.meridian.rateLimit;

import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Shared counters for a multi-instance deployment: INCR on a windowed key. If Redis is down the
 * limiter fails open (the request proceeds) rather than taking the whole API down with it.
 */
@Slf4j
@RequiredArgsConstructor
public class RedisRateLimiter implements RateLimiter {

    private static final String PREFIX = "ratelimit:";

    private final StringRedisTemplate redisTemplate;

    @Override
    public RateLimitDecision tryConsume(String clientKey, String scope, int limit, Duration window) {
        String key = PREFIX + scope + ':' + clientKey;
        long windowSeconds = Math.max(1, window.toSeconds());
        try {
            Long hits = redisTemplate.opsForValue().increment(key);
            if (hits != null && hits == 1L) {
                redisTemplate.expire(key, Duration.ofSeconds(windowSeconds));
            }
            Long ttl = redisTemplate.getExpire(key);
            long retryAfter = ttl == null || ttl <= 0 ? windowSeconds : ttl;
            long used = hits == null ? 0 : hits;
            return new RateLimitDecision(used <= limit, limit, Math.max(0, limit - used), retryAfter);
        } catch (RuntimeException ex) {
            log.warn("Rate limiter unavailable ({}): allowing the request", ex.getMessage());
            return new RateLimitDecision(true, limit, limit, 0);
        }
    }
}

package com.app.meridian.rateLimit;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Component;

/**
 * Per-JVM counters — correct for a single instance, which is how the dev and test profiles run.
 * With more than one instance use the Redis backend so the budget is shared.
 */
public class InMemoryRateLimiter implements RateLimiter {

    private static final int PURGE_THRESHOLD = 20_000;

    private final ConcurrentHashMap<String, Counter> counters = new ConcurrentHashMap<>();

    private static final class Counter {
        private final long windowStart;
        private final AtomicLong hits = new AtomicLong();

        Counter(long windowStart) {
            this.windowStart = windowStart;
        }
    }

    @Override
    public RateLimitDecision tryConsume(String clientKey, String scope, int limit, Duration window) {
        long now = System.currentTimeMillis();
        long windowMillis = Math.max(1_000, window.toMillis());
        String key = scope + ':' + clientKey;
        Counter counter = counters.compute(key, (k, existing) -> {
            if (existing == null || now - existing.windowStart >= windowMillis) {
                Counter fresh = new Counter(now);
                fresh.hits.set(1);
                return fresh;
            }
            existing.hits.incrementAndGet();
            return existing;
        });
        long hits = counter.hits.get();
        long elapsed = now - counter.windowStart;
        long retryAfter = Math.max(1, (windowMillis - elapsed + 999) / 1000);
        if (counters.size() > PURGE_THRESHOLD) {
            counters.entrySet().removeIf(entry -> now - entry.getValue().windowStart >= windowMillis);
        }
        return new RateLimitDecision(hits <= limit, limit, Math.max(0, limit - hits), retryAfter);
    }
}

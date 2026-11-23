package com.app.meridian.cache;

import java.time.Duration;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @param provider   "auto" (default: Redis when reachable, else memory), "redis" or "memory"
 * @param defaultTtl fallback TTL for caches without an explicit entry
 * @param keyPrefix  Redis key prefix, so several apps can share one instance
 * @param ttls       per-cache TTL overrides
 */
@ConfigurationProperties(prefix = "meridian.cache")
public record CacheProperties(String provider, Duration defaultTtl, String keyPrefix,
        Map<String, Duration> ttls) {

    public CacheProperties {
        provider = provider == null ? "auto" : provider.toLowerCase();
        defaultTtl = defaultTtl == null ? Duration.ofMinutes(10) : defaultTtl;
        keyPrefix = keyPrefix == null ? "meridian:" : keyPrefix;
        ttls = ttls == null ? Map.of() : Map.copyOf(ttls);
    }

    public Duration ttlFor(String cacheName) {
        return ttls.getOrDefault(cacheName, defaultTtl);
    }

    public boolean isRedis() {
        return "redis".equals(provider);
    }

    public boolean isAuto() {
        return "auto".equals(provider);
    }
}

package com.app.meridian.auth.tokens;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.StringRedisTemplate;

import lombok.extern.slf4j.Slf4j;

/**
 * Refresh tokens shared by every instance. Redis owns the TTL, so expired tokens disappear on their
 * own; a small set per user is the index "sign out everywhere" needs.
 *
 * <p>If Redis stops answering mid-flight every operation falls back to an in-process store instead of
 * throwing: a token endpoint is not the place to return 500s. The trade-off is that tokens issued
 * while Redis is down are only visible to this instance, which is logged once.
 */
@Slf4j
public class RedisTokenStore implements TokenStore {

    private static final String PREFIX = "meridian:auth:";
    private static final String SEPARATOR = "|";

    private final StringRedisTemplate redis;
    private final InMemoryTokenStore fallback = new InMemoryTokenStore();
    private final AtomicBoolean degraded = new AtomicBoolean();

    public RedisTokenStore(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public void save(String namespace, String tokenHash, Long userId, Instant expiresAt) {
        Duration ttl = Duration.between(Instant.now(), expiresAt);
        try {
            redis.opsForValue().set(key(namespace, tokenHash), serialize(userId, expiresAt, false), ttl);
            String userKey = userKey(namespace, userId);
            redis.opsForSet().add(userKey, tokenHash);
            redis.expire(userKey, ttl);
        } catch (RuntimeException ex) {
            degrade(ex);
            fallback.save(namespace, tokenHash, userId, expiresAt);
        }
    }

    @Override
    public Optional<StoredToken> find(String namespace, String tokenHash) {
        try {
            String value = redis.opsForValue().get(key(namespace, tokenHash));
            if (value == null) {
                return fallback.find(namespace, tokenHash);
            }
            StoredToken stored = deserialize(value);
            return stored.isExpired(Instant.now()) ? Optional.empty() : Optional.of(stored);
        } catch (RuntimeException ex) {
            degrade(ex);
            return fallback.find(namespace, tokenHash);
        }
    }

    @Override
    public void markRevoked(String namespace, String tokenHash) {
        try {
            String key = key(namespace, tokenHash);
            String value = redis.opsForValue().get(key);
            if (value == null) {
                fallback.markRevoked(namespace, tokenHash);
                return;
            }
            StoredToken stored = deserialize(value);
            Long remaining = redis.getExpire(key);
            redis.opsForValue().set(key, serialize(stored.userId(), stored.expiresAt(), true),
                    remaining == null || remaining <= 0 ? Duration.ofMinutes(1) : Duration.ofSeconds(remaining));
        } catch (RuntimeException ex) {
            degrade(ex);
            fallback.markRevoked(namespace, tokenHash);
        }
    }

    @Override
    public void delete(String namespace, String tokenHash) {
        try {
            redis.delete(key(namespace, tokenHash));
        } catch (RuntimeException ex) {
            degrade(ex);
        }
        fallback.delete(namespace, tokenHash);
    }

    @Override
    public void deleteAllForUser(String namespace, Long userId) {
        try {
            String userKey = userKey(namespace, userId);
            Set<String> held = redis.opsForSet().members(userKey);
            if (held != null && !held.isEmpty()) {
                redis.delete(held.stream().map(hash -> key(namespace, hash)).collect(Collectors.toSet()));
            }
            redis.delete(userKey);
        } catch (RuntimeException ex) {
            degrade(ex);
        }
        fallback.deleteAllForUser(namespace, userId);
    }

    private void degrade(RuntimeException ex) {
        if (degraded.compareAndSet(false, true)) {
            log.warn("Refresh token store fell back to in-memory: {} — tokens issued now are not "
                    + "visible to other instances", ex.getClass().getSimpleName());
        }
    }

    private static String key(String namespace, String tokenHash) {
        return PREFIX + namespace + ":" + tokenHash;
    }

    private static String userKey(String namespace, Long userId) {
        return PREFIX + namespace + ":user:" + userId;
    }

    private static String serialize(Long userId, Instant expiresAt, boolean revoked) {
        return userId + SEPARATOR + expiresAt.toEpochMilli() + SEPARATOR + (revoked ? "1" : "0");
    }

    private static StoredToken deserialize(String value) {
        String[] parts = value.split("\\" + SEPARATOR);
        return new StoredToken(Long.valueOf(parts[0]),
                Instant.ofEpochMilli(Long.parseLong(parts[1])),
                "1".equals(parts[2]));
    }
}

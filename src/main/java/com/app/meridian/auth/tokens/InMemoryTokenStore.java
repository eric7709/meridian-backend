package com.app.meridian.auth.tokens;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Fallback store for a single JVM: entries expire on read, and the per-user index is what makes
 * "sign out everywhere" possible without scanning the whole map.
 */
public class InMemoryTokenStore implements TokenStore {

    private final Map<String, StoredToken> tokens = new ConcurrentHashMap<>();
    private final Map<Long, Set<String>> index = new ConcurrentHashMap<>();

    @Override
    public void save(String namespace, String tokenHash, Long userId, Instant expiresAt) {
        String key = key(namespace, tokenHash);
        tokens.put(key, new StoredToken(userId, expiresAt, false));
        index.computeIfAbsent(userId, id -> ConcurrentHashMap.newKeySet()).add(userKey(namespace, key));
    }

    @Override
    public Optional<StoredToken> find(String namespace, String tokenHash) {
        String key = key(namespace, tokenHash);
        StoredToken stored = tokens.get(key);
        if (stored == null) {
            return Optional.empty();
        }
        if (stored.isExpired(Instant.now())) {
            tokens.remove(key);
            return Optional.empty();
        }
        return Optional.of(stored);
    }

    @Override
    public void markRevoked(String namespace, String tokenHash) {
        String key = key(namespace, tokenHash);
        tokens.computeIfPresent(key, (k, stored) -> new StoredToken(stored.userId(), stored.expiresAt(), true));
    }

    @Override
    public void delete(String namespace, String tokenHash) {
        tokens.remove(key(namespace, tokenHash));
    }

    @Override
    public void deleteAllForUser(String namespace, Long userId) {
        Set<String> held = index.get(userId);
        if (held == null) {
            return;
        }
        // only this namespace: a password reset must not sign the user out of other stores
        String prefix = namespace + "|";
        Set<String> remaining = ConcurrentHashMap.newKeySet();
        held.forEach(marker -> {
            if (marker.startsWith(prefix)) {
                tokens.remove(unmark(marker));
            } else {
                remaining.add(marker);
            }
        });
        if (remaining.isEmpty()) {
            index.remove(userId, held);
        } else {
            held.clear();
            held.addAll(remaining);
        }
    }

    private static String key(String namespace, String tokenHash) {
        return namespace + ":" + tokenHash;
    }

    /** The index stores namespace-qualified markers, so one user can hold tokens in two namespaces. */
    private static String userKey(String namespace, String tokenKey) {
        return namespace + "|" + tokenKey;
    }

    private static String unmark(String marker) {
        return marker.substring(marker.indexOf('|') + 1);
    }
}

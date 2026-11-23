package com.app.meridian.auth.tokens;

import java.time.Instant;
import java.util.Optional;

/**
 * Short-lived, revocable tokens: refresh tokens and password-reset tokens. Keys are token hashes,
 * never the raw token. Two implementations exist (Redis when reachable, otherwise this JVM's heap)
 * because the app has to work on a single laptop as well as behind a load balancer.
 */
public interface TokenStore {

    void save(String namespace, String tokenHash, Long userId, Instant expiresAt);

    Optional<StoredToken> find(String namespace, String tokenHash);

    /** Keeps the entry until it would have expired, so a replay can be told apart from a bad token. */
    void markRevoked(String namespace, String tokenHash);

    void delete(String namespace, String tokenHash);

    /** Signs out every token a user holds in that namespace (logout everywhere, password change). */
    void deleteAllForUser(String namespace, Long userId);
}

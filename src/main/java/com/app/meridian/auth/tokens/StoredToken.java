package com.app.meridian.auth.tokens;

import java.time.Instant;

/**
 * One entry in the {@link TokenStore}. {@code revoked} is the interesting flag for refresh tokens:
 * a token that was already exchanged is kept around so a replay can be recognised as theft.
 */
public record StoredToken(Long userId, Instant expiresAt, boolean revoked) {

    public boolean isExpired(Instant now) {
        return !expiresAt.isAfter(now);
    }

    public boolean isUsable(Instant now) {
        return !revoked && !isExpired(now);
    }
}

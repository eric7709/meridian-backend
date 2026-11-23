package com.app.meridian.auth.services;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.app.meridian.auth.tokens.StoredToken;
import com.app.meridian.auth.tokens.TokenStore;
import com.app.meridian.auth.tokens.Tokens;
import com.app.meridian.exceptions.ForbiddenException;
import com.app.meridian.security.JwtProperties;
import com.app.meridian.security.JwtService;
import com.app.meridian.user.User;
import com.app.meridian.user.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Hands out the pair the client holds: a short-lived access JWT it sends on every request, and an
 * opaque refresh token it exchanges for a new pair when the access token expires.
 *
 * <p>Refresh tokens rotate on every use. Presenting one that was already exchanged is treated as a
 * stolen token: every session of that user is revoked instead of quietly issuing another pair.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenIssuer {

    /** Namespace inside the shared token store. */
    static final String REFRESH_NAMESPACE = "refresh";

    private final JwtService jwtService;
    private final TokenStore tokenStore;
    private final JwtProperties jwtProperties;
    private final UserRepository userRepository;

    public IssuedTokens issue(User user) {
        Instant refreshExpiresAt = Instant.now().plus(jwtProperties.refreshExpirationMinutes(), ChronoUnit.MINUTES);
        String refreshToken = createRefreshToken(user.getId(), refreshExpiresAt);
        return new IssuedTokens(jwtService.issueAccessToken(user), refreshToken, "Bearer",
                jwtService.expirationMinutes(), jwtProperties.refreshExpirationMinutes());
    }

    /** Validates the refresh token, spends it, and returns a brand new pair. */
    public IssuedTokens rotate(String rawRefreshToken) {
        requireToken(rawRefreshToken);
        String hash = Tokens.hash(rawRefreshToken.trim());
        StoredToken stored = tokenStore.find(REFRESH_NAMESPACE, hash)
                .orElseThrow(() -> new ForbiddenException("This refresh token is invalid or has expired"));

        if (stored.revoked()) {
            tokenStore.deleteAllForUser(REFRESH_NAMESPACE, stored.userId());
            log.warn("Refresh token reuse detected for user {} — all sessions revoked", stored.userId());
            throw new ForbiddenException(
                    "This refresh token has already been used; every session has been signed out");
        }

        User user = userRepository.findById(stored.userId())
                .orElseThrow(() -> new ForbiddenException("This refresh token is invalid or has expired"));
        if (Boolean.FALSE.equals(user.getActive())) {
            tokenStore.deleteAllForUser(REFRESH_NAMESPACE, user.getId());
            throw new ForbiddenException("This account has been deactivated");
        }

        tokenStore.markRevoked(REFRESH_NAMESPACE, hash);
        Instant refreshExpiresAt = Instant.now().plus(jwtProperties.refreshExpirationMinutes(), ChronoUnit.MINUTES);
        String nextRefreshToken = createRefreshToken(user.getId(), refreshExpiresAt);
        return new IssuedTokens(jwtService.issueAccessToken(user), nextRefreshToken, "Bearer",
                jwtService.expirationMinutes(), jwtProperties.refreshExpirationMinutes());
    }

    /** Called on logout: the access token dies when it expires, the refresh token dies now. */
    public void revoke(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            return;
        }
        tokenStore.delete(REFRESH_NAMESPACE, Tokens.hash(rawRefreshToken.trim()));
    }

    public void revokeAllForUser(Long userId) {
        if (userId != null) {
            tokenStore.deleteAllForUser(REFRESH_NAMESPACE, userId);
        }
    }

    /** Owner of a refresh token, used by "sign out everywhere" when the access token already expired. */
    public Optional<Long> ownerOf(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            return Optional.empty();
        }
        return tokenStore.find(REFRESH_NAMESPACE, Tokens.hash(rawRefreshToken.trim()))
                .map(StoredToken::userId);
    }

    private String createRefreshToken(Long userId, Instant expiresAt) {
        String rawToken = Tokens.newToken();
        tokenStore.save(REFRESH_NAMESPACE, Tokens.hash(rawToken), userId, expiresAt);
        return rawToken;
    }

    private static void requireToken(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new ForbiddenException("Refresh token is required");
        }
    }
}

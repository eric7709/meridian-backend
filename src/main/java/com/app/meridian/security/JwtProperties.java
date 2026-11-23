package com.app.meridian.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @param secret                 HMAC key; must be at least 32 bytes
 * @param expirationMinutes      access token lifetime (short — the client refreshes silently)
 * @param refreshExpirationMinutes refresh token lifetime, in minutes (default 14 days)
 */
@ConfigurationProperties(prefix = "meridian.jwt")
public record JwtProperties(String secret, long expirationMinutes, long refreshExpirationMinutes) {

    public static final long DEFAULT_ACCESS_MINUTES = 30;
    public static final long DEFAULT_REFRESH_MINUTES = 14L * 24 * 60;

    public JwtProperties {
        expirationMinutes = expirationMinutes <= 0 ? DEFAULT_ACCESS_MINUTES : expirationMinutes;
        refreshExpirationMinutes = refreshExpirationMinutes <= 0
                ? DEFAULT_REFRESH_MINUTES
                : refreshExpirationMinutes;
    }
}

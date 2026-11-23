package com.app.meridian.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import com.app.meridian.user.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Access tokens only. They are short-lived and stateless; anything revocable (refresh tokens,
 * password-reset tokens) is an opaque token kept in the {@code TokenStore} instead.
 */
@Service
public class JwtService {

    /** Marks the token as an access token, so another kind of JWT can never be replayed here. */
    private static final String TYPE_CLAIM = "typ";
    private static final String TYPE_ACCESS = "access";

    private final SecretKey key;
    private final long expirationMinutes;

    public JwtService(JwtProperties properties) {
        this.key = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
        this.expirationMinutes = properties.expirationMinutes();
    }

    public String issueAccessToken(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim(TYPE_CLAIM, TYPE_ACCESS)
                .claim("identifier", user.getIdentifier())
                .claim("role", user.getRole().name())
                .claim("fullName", user.getFullName())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(expirationMinutes, ChronoUnit.MINUTES)))
                .signWith(key)
                .compact();
    }

    public long expirationMinutes() {
        return expirationMinutes;
    }

    /** Returns the verified claims, or null when the token is missing, tampered with or expired. */
    public Claims parse(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
            return TYPE_ACCESS.equals(claims.get(TYPE_CLAIM, String.class)) ? claims : null;
        } catch (RuntimeException ex) {
            return null;
        }
    }
}

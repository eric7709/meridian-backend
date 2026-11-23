package com.app.meridian.auth.services;

/** What login and refresh both hand back: the pair plus the lifetimes the client needs to plan around. */
public record IssuedTokens(String accessToken, String refreshToken, String tokenType,
        long expiresInMinutes, long refreshExpiresInMinutes) {
}

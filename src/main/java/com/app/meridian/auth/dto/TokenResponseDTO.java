package com.app.meridian.auth.dto;

/** The rotated pair returned by POST /auth/refresh (no user payload — the caller already knows who it is). */
public record TokenResponseDTO(String accessToken, String refreshToken, String tokenType,
        long expiresInMinutes, long refreshExpiresInMinutes) {
}

package com.app.meridian.auth.dto;

import com.app.meridian.user.dto.UserResponseDTO;

/**
 * @param accessToken            short-lived JWT to send as {@code Authorization: Bearer ...}
 * @param refreshToken           opaque token to POST to /auth/refresh when the access token expires
 * @param tokenType              always "Bearer"
 * @param expiresInMinutes       access token lifetime
 * @param refreshExpiresInMinutes refresh token lifetime
 */
public record LoginResponseDTO(String accessToken, String refreshToken, String tokenType,
        long expiresInMinutes, long refreshExpiresInMinutes, UserResponseDTO user) {
}

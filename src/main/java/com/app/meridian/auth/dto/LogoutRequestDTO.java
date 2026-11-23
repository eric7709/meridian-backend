package com.app.meridian.auth.dto;

/**
 * @param refreshToken the token to revoke; omit it when signing out everywhere
 * @param allDevices   revoke every refresh token this account holds
 */
public record LogoutRequestDTO(String refreshToken, Boolean allDevices) {
}

package com.app.meridian.auth.dto;

/**
 * @param message    always the same text, so it never reveals whether an address is registered
 * @param resetToken only filled when {@code meridian.auth.expose-reset-token} is on (no mail server)
 */
public record ForgotPasswordResponseDTO(String message, String resetToken) {
}

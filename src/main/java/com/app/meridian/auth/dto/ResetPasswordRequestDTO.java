package com.app.meridian.auth.dto;

public record ResetPasswordRequestDTO(String token, String newPassword) {
}

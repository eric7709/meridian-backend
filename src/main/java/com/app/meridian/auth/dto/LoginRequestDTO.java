package com.app.meridian.auth.dto;

import com.app.meridian.user.enums.Role;

/** Sign in with identifier OR email; role is optional but rejected when it does not match. */
public record LoginRequestDTO(String identifier, String password, Role role) {
}

package com.app.meridian.user.dto;

import com.app.meridian.user.enums.Role;

/** Creating an account needs a password; updating one only if the password changes. */
public record UserAccountRequestDTO(String identifier, String email, String password, String fullName, Role role, Boolean active) {
}

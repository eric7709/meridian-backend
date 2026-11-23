package com.app.meridian.user.dto;

import java.time.LocalDateTime;
import com.app.meridian.user.enums.Role;
/** Payload accepted by create and update. Update only applies non-null fields. */
public record UserRequestDTO(String identifier, String email, String fullName, Role role, Boolean active, LocalDateTime lastLoginAt) {
}

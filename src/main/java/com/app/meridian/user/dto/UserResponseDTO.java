package com.app.meridian.user.dto;

import java.time.LocalDateTime;
import com.app.meridian.user.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDTO {

    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String identifier;
    private String email;
    private String fullName;
    private Role role;
    private Boolean active;
    private LocalDateTime lastLoginAt;
}

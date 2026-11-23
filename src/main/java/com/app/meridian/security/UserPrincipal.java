package com.app.meridian.security;

import java.security.Principal;

import com.app.meridian.user.enums.Role;

/** What a verified JWT puts into the security context. */
public record UserPrincipal(Long id, String identifier, String fullName, Role role) implements Principal {

    @Override
    public String getName() {
        return identifier;
    }

    public boolean isAdmin() {
        return role == Role.ADMIN || role == Role.IT_SUPPORT;
    }

    public boolean isTeacher() {
        return role == Role.TEACHER;
    }

    public boolean isStudent() {
        return role == Role.STUDENT;
    }
}

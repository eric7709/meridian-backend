package com.app.meridian.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.app.meridian.exceptions.ForbiddenException;
import com.app.meridian.user.enums.Role;

/** Reads the caller out of the security context — the single source of ownership rules. */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static UserPrincipal principal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new ForbiddenException("You must be signed in to perform this action");
        }
        return principal;
    }

    public static Long userId() {
        return principal().id();
    }

    public static Role role() {
        return principal().role();
    }

    public static boolean isAdmin() {
        return principal().isAdmin();
    }
}

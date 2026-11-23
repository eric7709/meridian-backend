package com.app.meridian.auth.services;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.app.meridian.auth.dto.LogoutRequestDTO;
import com.app.meridian.security.UserPrincipal;

import lombok.RequiredArgsConstructor;

/**
 * Signing out is server-side now: the refresh token is revoked so it cannot be exchanged again.
 * With {@code allDevices} every refresh token of the account is dropped — that also works when the
 * access token has already expired, because the owner is read from the refresh token itself.
 */
@Service
@RequiredArgsConstructor
public class LogoutService {

    private final TokenIssuer tokenIssuer;

    public void logout(LogoutRequestDTO request) {
        String refreshToken = request == null ? null : request.refreshToken();
        boolean allDevices = request != null && Boolean.TRUE.equals(request.allDevices());

        if (allDevices) {
            Long userId = signedInUserId().orElseGet(() -> tokenIssuer.ownerOf(refreshToken).orElse(null));
            tokenIssuer.revokeAllForUser(userId);
        }
        tokenIssuer.revoke(refreshToken);
    }

    private static java.util.Optional<Long> signedInUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            return java.util.Optional.of(principal.id());
        }
        return java.util.Optional.empty();
    }
}

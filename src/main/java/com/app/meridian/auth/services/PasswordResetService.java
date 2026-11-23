package com.app.meridian.auth.services;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.meridian.auth.AuthProperties;
import com.app.meridian.auth.dto.ForgotPasswordResponseDTO;
import com.app.meridian.auth.tokens.StoredToken;
import com.app.meridian.auth.tokens.TokenStore;
import com.app.meridian.auth.tokens.Tokens;
import com.app.meridian.exceptions.BadRequestException;
import com.app.meridian.user.User;
import com.app.meridian.user.UserRepository;
import com.app.meridian.user.utils.PasswordPolicy;
import com.app.meridian.utils.ValidationUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Forgot / reset password with single-use, hashed, expiring tokens.
 *
 * <p>{@code request} answers identically whether or not the address exists — the only difference is
 * that a token is stored and mailed for a real account. {@code reset} is what actually changes the
 * password: it spends the token, refuses weak passwords, and signs the account out everywhere.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    static final String RESET_NAMESPACE = "password-reset";
    static final Duration TOKEN_TTL = Duration.ofMinutes(30);

    private static final String NEUTRAL_MESSAGE =
            "If that email belongs to an account, a reset link has been sent";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenStore tokenStore;
    private final TokenIssuer tokenIssuer;
    private final ResetMailer resetMailer;
    private final AuthProperties authProperties;

    @Transactional(readOnly = true)
    public ForgotPasswordResponseDTO request(String email) {
        ValidationUtils.requireNonBlank(email, "Email");
        String address = email.trim();
        Optional<User> user = userRepository.findByEmailIgnoreCase(address);

        if (user.isEmpty()) {
            log.info("Password reset requested for unknown address {}", address);
            return new ForgotPasswordResponseDTO(NEUTRAL_MESSAGE, null);
        }

        String rawToken = Tokens.newToken();
        // one live token per account: asking again invalidates the previous link
        tokenStore.deleteAllForUser(RESET_NAMESPACE, user.get().getId());
        tokenStore.save(RESET_NAMESPACE, Tokens.hash(rawToken), user.get().getId(),
                Instant.now().plus(TOKEN_TTL));
        resetMailer.sendResetToken(address, rawToken, TOKEN_TTL);

        return new ForgotPasswordResponseDTO(NEUTRAL_MESSAGE,
                authProperties.exposeResetToken() ? rawToken : null);
    }

    @Transactional
    public void reset(String token, String newPassword) {
        ValidationUtils.requireNonBlank(token, "Reset token");
        PasswordPolicy.validate(newPassword);

        String hash = Tokens.hash(token.trim());
        StoredToken stored = tokenStore.find(RESET_NAMESPACE, hash)
                .filter(candidate -> candidate.isUsable(Instant.now()))
                .orElseThrow(() -> new BadRequestException("This reset token is invalid or has expired"));

        User user = userRepository.findById(stored.userId())
                .orElseThrow(() -> new BadRequestException("This reset token is invalid or has expired"));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        tokenStore.delete(RESET_NAMESPACE, hash);
        tokenStore.deleteAllForUser(RESET_NAMESPACE, user.getId());
        // a reset means the old sessions are not trusted any more
        tokenIssuer.revokeAllForUser(user.getId());
        log.info("Password reset completed for user {}", user.getId());
    }
}

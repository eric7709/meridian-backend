package com.app.meridian.auth.services;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.meridian.auth.dto.LoginRequestDTO;
import com.app.meridian.auth.dto.LoginResponseDTO;
import com.app.meridian.exceptions.ForbiddenException;
import com.app.meridian.user.User;
import com.app.meridian.user.UserRepository;
import com.app.meridian.user.utils.UserMapper;
import com.app.meridian.utils.ValidationUtils;

import lombok.RequiredArgsConstructor;

/** Identifier-or-email + password + optional role check; returns an access token and a refresh token. */
@Service
@RequiredArgsConstructor
public class LoginService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenIssuer tokenIssuer;
    private final UserMapper userMapper;

    @Transactional
    public LoginResponseDTO login(LoginRequestDTO request) {
        ValidationUtils.requireNonNull(request, "Request body");
        ValidationUtils.requireNonBlank(request.identifier(), "Identifier or email");
        ValidationUtils.requireNonBlank(request.password(), "Password");
        String key = request.identifier().trim();
        User user = userRepository.findByEmailIgnoreCase(key)
                .or(() -> userRepository.findByIdentifierIgnoreCase(key))
                .orElseThrow(() -> new ForbiddenException("Invalid credentials"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ForbiddenException("Invalid credentials");
        }
        if (Boolean.FALSE.equals(user.getActive())) {
            throw new ForbiddenException("This account has been deactivated");
        }
        if (request.role() != null && request.role() != user.getRole()) {
            throw new ForbiddenException("This account cannot sign in with the selected role");
        }
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        IssuedTokens tokens = tokenIssuer.issue(user);
        return new LoginResponseDTO(tokens.accessToken(), tokens.refreshToken(), tokens.tokenType(),
                tokens.expiresInMinutes(), tokens.refreshExpiresInMinutes(), userMapper.toResponse(user));
    }
}

package com.app.meridian.user.services;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.meridian.exceptions.ConflictException;
import com.app.meridian.exceptions.ValidationFailure;
import com.app.meridian.user.User;
import com.app.meridian.user.UserEvents;
import com.app.meridian.user.UserRepository;
import com.app.meridian.user.dto.UserAccountRequestDTO;
import com.app.meridian.user.enums.Role;
import com.app.meridian.user.utils.UserMapper;
import com.app.meridian.utils.ValidationUtils;

import lombok.RequiredArgsConstructor;

/** Creates a login account with a BCrypt-hashed password. */
@Service
@RequiredArgsConstructor
public class CreateUserAccount {

    private final UserRepository userRepository;
    private final UserEvents userEvents;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User create(UserAccountRequestDTO request) {
        ValidationUtils.requireNonNull(request, "Request body");
        ValidationUtils.requireNonBlank(request.identifier(), "Identifier");
        ValidationUtils.requireNonBlank(request.email(), "Email");
        ValidationUtils.requireNonBlank(request.password(), "Password");
        ValidationUtils.requireNonBlank(request.fullName(), "Full name");
        ValidationUtils.requireNonNull(request.role(), "Role");
        if (request.password().length() < 6) {
            throw new ValidationFailure("Password must be at least 6 characters");
        }
        if (userRepository.existsByIdentifierIgnoreCase(request.identifier().trim())) {
            throw new ConflictException("An account with this identifier already exists");
        }
        if (userRepository.existsByEmailIgnoreCase(request.email().trim())) {
            throw new ConflictException("An account with this email already exists");
        }
        User user = new User();
        user.setIdentifier(request.identifier().trim());
        user.setEmail(request.email().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName().trim());
        user.setRole(request.role());
        user.setActive(request.active() == null ? Boolean.TRUE : request.active());
        User saved = userRepository.save(user);
        userEvents.broadcastCreated(userMapper.toResponse(saved));
        return saved;
    }
}

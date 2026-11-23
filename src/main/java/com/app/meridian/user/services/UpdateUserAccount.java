package com.app.meridian.user.services;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.meridian.exceptions.ConflictException;
import com.app.meridian.exceptions.NotFoundException;
import com.app.meridian.user.User;
import com.app.meridian.user.UserEvents;
import com.app.meridian.user.UserRepository;
import com.app.meridian.user.dto.UserAccountRequestDTO;
import com.app.meridian.user.utils.UserMapper;
import com.app.meridian.utils.ValidationUtils;

import lombok.RequiredArgsConstructor;

/** Partial account update; a blank password means "keep the current one". */
@Service
@RequiredArgsConstructor
public class UpdateUserAccount {

    private final UserRepository userRepository;
    private final UserEvents userEvents;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User update(Long id, UserAccountRequestDTO request) {
        ValidationUtils.requireNonNull(id, "User ID");
        ValidationUtils.requireNonNull(request, "Request body");
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
        if (request.identifier() != null && !request.identifier().isBlank()) {
            if (userRepository.existsByIdentifierIgnoreCaseAndIdNot(request.identifier().trim(), id)) {
                throw new ConflictException("Another account already uses this identifier");
            }
            user.setIdentifier(request.identifier().trim());
        }
        if (request.email() != null && !request.email().isBlank()) {
            if (userRepository.existsByEmailIgnoreCaseAndIdNot(request.email().trim(), id)) {
                throw new ConflictException("Another account already uses this email");
            }
            user.setEmail(request.email().trim().toLowerCase());
        }
        if (request.fullName() != null && !request.fullName().isBlank()) {
            user.setFullName(request.fullName().trim());
        }
        if (request.role() != null) {
            user.setRole(request.role());
        }
        if (request.active() != null) {
            user.setActive(request.active());
        }
        if (request.password() != null && !request.password().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.password()));
        }
        User saved = userRepository.save(user);
        userEvents.broadcastUpdated(userMapper.toResponse(saved));
        return saved;
    }
}

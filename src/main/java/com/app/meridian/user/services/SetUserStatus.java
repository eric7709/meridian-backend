package com.app.meridian.user.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.meridian.exceptions.NotFoundException;
import com.app.meridian.user.User;
import com.app.meridian.user.UserEvents;
import com.app.meridian.user.UserRepository;
import com.app.meridian.user.utils.UserMapper;
import com.app.meridian.utils.ValidationUtils;

import lombok.RequiredArgsConstructor;

/** Activate or deactivate an account without deleting its history. */
@Service
@RequiredArgsConstructor
public class SetUserStatus {

    private final UserRepository userRepository;
    private final UserEvents userEvents;
    private final UserMapper userMapper;

    @Transactional
    public User set(Long id, boolean active) {
        ValidationUtils.requireNonNull(id, "User ID");
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
        user.setActive(active);
        User saved = userRepository.save(user);
        userEvents.broadcastUpdated(userMapper.toResponse(saved));
        return saved;
    }
}

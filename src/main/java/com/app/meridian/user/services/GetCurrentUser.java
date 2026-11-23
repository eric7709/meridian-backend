package com.app.meridian.user.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.meridian.exceptions.NotFoundException;
import com.app.meridian.user.User;
import com.app.meridian.user.UserRepository;
import com.app.meridian.security.SecurityUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetCurrentUser {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public User get() {
        return userRepository.findById(SecurityUtils.userId())
                .orElseThrow(() -> new NotFoundException("Signed-in account no longer exists"));
    }
}

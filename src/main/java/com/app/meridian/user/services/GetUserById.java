package com.app.meridian.user.services;

import com.app.meridian.exceptions.NotFoundException;
import com.app.meridian.user.User;
import com.app.meridian.user.UserRepository;
import com.app.meridian.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetUserById {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public User get(Long id) {
        ValidationUtils.requireNonNull(id, "User ID");
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }
}

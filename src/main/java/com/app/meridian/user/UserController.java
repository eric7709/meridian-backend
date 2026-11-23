package com.app.meridian.user;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.app.meridian.common.PageResponse;
import com.app.meridian.user.dto.UserAccountRequestDTO;
import com.app.meridian.user.dto.UserResponseDTO;
import com.app.meridian.user.dto.UserSearchCriteria;
import com.app.meridian.user.services.CreateUserAccount;
import com.app.meridian.user.services.GetUserById;
import com.app.meridian.user.services.SearchUsers;
import com.app.meridian.user.services.SetUserStatus;
import com.app.meridian.user.services.UpdateUserAccount;
import com.app.meridian.user.utils.UserMapper;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/meridian/users")
public class UserController {

    private final CreateUserAccount createUserAccount;
    private final UpdateUserAccount updateUserAccount;
    private final SetUserStatus setUserStatus;
    private final GetUserById getUserById;
    private final SearchUsers searchUsers;
    private final UserMapper userMapper;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','IT_SUPPORT')")
    public PageResponse<UserResponseDTO> search(UserSearchCriteria criteria) {
        Page<User> page = searchUsers.search(criteria);
        return PageResponse.of(page, userMapper::toResponse);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','IT_SUPPORT')")
    public UserResponseDTO get(@PathVariable Long id) {
        return userMapper.toResponse(getUserById.get(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','IT_SUPPORT')")
    public UserResponseDTO create(@RequestBody UserAccountRequestDTO request) {
        return userMapper.toResponse(createUserAccount.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','IT_SUPPORT')")
    public UserResponseDTO update(@PathVariable Long id, @RequestBody UserAccountRequestDTO request) {
        return userMapper.toResponse(updateUserAccount.update(id, request));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponseDTO setStatus(@PathVariable Long id, @RequestParam boolean active) {
        return userMapper.toResponse(setUserStatus.set(id, active));
    }
}

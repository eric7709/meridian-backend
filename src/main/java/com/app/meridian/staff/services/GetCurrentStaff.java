package com.app.meridian.staff.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.meridian.exceptions.ForbiddenException;
import com.app.meridian.staff.Staff;
import com.app.meridian.staff.StaffRepository;
import com.app.meridian.security.SecurityUtils;

import lombok.RequiredArgsConstructor;

/** The staff profile behind the signed-in account (teachers and admins both have one). */
@Service
@RequiredArgsConstructor
public class GetCurrentStaff {

    private final StaffRepository staffRepository;

    @Transactional(readOnly = true)
    public Staff get() {
        return staffRepository.findByUserId(SecurityUtils.userId())
                .orElseThrow(() -> new ForbiddenException("This action requires a staff profile"));
    }
}

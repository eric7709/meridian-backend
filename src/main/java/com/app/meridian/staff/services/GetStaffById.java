package com.app.meridian.staff.services;

import com.app.meridian.exceptions.NotFoundException;
import com.app.meridian.staff.Staff;
import com.app.meridian.staff.StaffRepository;
import com.app.meridian.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetStaffById {

    private final StaffRepository staffRepository;

    @Transactional(readOnly = true)
    public Staff get(Long id) {
        ValidationUtils.requireNonNull(id, "Staff ID");
        return staffRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Staff not found"));
    }
}

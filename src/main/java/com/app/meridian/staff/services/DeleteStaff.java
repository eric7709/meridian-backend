package com.app.meridian.staff.services;

import com.app.meridian.exceptions.NotFoundException;
import com.app.meridian.staff.StaffEvents;
import com.app.meridian.staff.StaffRepository;
import com.app.meridian.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteStaff {

    private final StaffRepository staffRepository;
    private final StaffEvents staffEvents;

    @Transactional
    public void delete(Long id) {
        ValidationUtils.requireNonNull(id, "Staff ID");
        if (!staffRepository.existsById(id)) {
            throw new NotFoundException("Staff not found");
        }
        staffRepository.deleteById(id);
        staffEvents.broadcastDeleted(id);
    }
}

package com.app.meridian.staff.services;

import com.app.meridian.department.Department;
import com.app.meridian.exceptions.ConflictException;
import com.app.meridian.exceptions.NotFoundException;
import com.app.meridian.staff.Staff;
import com.app.meridian.staff.StaffEvents;
import com.app.meridian.staff.StaffRepository;
import com.app.meridian.staff.dto.StaffRequestDTO;
import com.app.meridian.staff.enums.EmploymentType;
import com.app.meridian.staff.enums.StaffRole;
import com.app.meridian.staff.utils.StaffMapper;
import com.app.meridian.user.User;
import com.app.meridian.utils.EntityResolver;
import com.app.meridian.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Applies only the fields present in the request; everything else is left alone. */
@Service
@RequiredArgsConstructor
public class UpdateStaff {

    private final EntityResolver entityResolver;
    private final StaffRepository staffRepository;
    private final StaffEvents staffEvents;
    private final StaffMapper staffMapper;

    @Transactional
    public Staff update(Long id, StaffRequestDTO request) {
        ValidationUtils.requireNonNull(id, "Staff ID");
        ValidationUtils.requireNonNull(request, "Request body");
        Staff entity = staffRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Staff not found"));
        if (request.userId() != null) {
            entity.setUser(entityResolver.resolveUser(request.userId()));
        }
        if (request.departmentId() != null) {
            entity.setDepartment(entityResolver.resolveDepartment(request.departmentId()));
        }

        if (request.staffNo() != null && !request.staffNo().isBlank()) {
            if (staffRepository.existsByStaffNoIgnoreCaseAndIdNot(request.staffNo().trim(), id)) {
                throw new ConflictException("Staff with this staffNo already exists");
            }
            entity.setStaffNo(request.staffNo().trim());
        }
        if (request.staffRole() != null) {
            entity.setStaffRole(request.staffRole());
        }
        if (request.employmentType() != null) {
            entity.setEmploymentType(request.employmentType());
        }
        if (request.phone() != null && !request.phone().isBlank()) {
            entity.setPhone(request.phone().trim());
        }

        Staff saved = staffRepository.save(entity);
        staffEvents.broadcastUpdated(staffMapper.toResponse(saved));
        return saved;
    }
}

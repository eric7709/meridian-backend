package com.app.meridian.staff.services;

import java.time.Year;
import com.app.meridian.department.Department;
import com.app.meridian.exceptions.ConflictException;
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

@Service
@RequiredArgsConstructor
public class CreateStaff {

    private final EntityResolver entityResolver;
    private final StaffRepository staffRepository;
    private final StaffEvents staffEvents;
    private final StaffMapper staffMapper;

    @Transactional
    public Staff create(StaffRequestDTO request) {
        ValidationUtils.requireNonNull(request, "Request body");
        ValidationUtils.requireNonNull(request.staffRole(), "staffRole");
        ValidationUtils.requireNonNull(request.userId(), "user");

        User user = entityResolver.resolveUser(request.userId());
        Department department = entityResolver.resolveDepartment(request.departmentId());

        if (request.staffNo() != null && staffRepository.existsByStaffNoIgnoreCase(request.staffNo().trim())) {
            throw new ConflictException("Staff with this staffNo already exists");
        }

        Staff entity = new Staff();
        String staffNo = request.staffNo() == null || request.staffNo().isBlank()
                ? nextStaffNo()
                : request.staffNo().trim();
        entity.setStaffNo(staffNo);
        entity.setStaffRole(request.staffRole());
        entity.setEmploymentType(request.employmentType() == null ? EmploymentType.FULL_TIME : request.employmentType());
        entity.setPhone(ValidationUtils.trimOrNull(request.phone()));
        entity.setUser(user);
        entity.setDepartment(department);

        Staff saved = staffRepository.save(entity);
        staffEvents.broadcastCreated(staffMapper.toResponse(saved));
        return saved;
    }

    private String nextStaffNo() {
        long sequence = staffRepository.count() + 1;
        String candidate;
        do {
            candidate = "STF/%d/%03d".formatted(Year.now().getValue(), sequence++);
        } while (staffRepository.existsByStaffNoIgnoreCase(candidate));
        return candidate;
    }
}

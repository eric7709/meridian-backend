package com.app.meridian.department.services;

import com.app.meridian.department.Department;
import com.app.meridian.department.DepartmentEvents;
import com.app.meridian.department.DepartmentRepository;
import com.app.meridian.department.dto.DepartmentRequestDTO;
import com.app.meridian.department.utils.DepartmentMapper;
import com.app.meridian.exceptions.ConflictException;
import com.app.meridian.exceptions.NotFoundException;
import com.app.meridian.staff.Staff;
import com.app.meridian.utils.EntityResolver;
import com.app.meridian.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Applies only the fields present in the request; everything else is left alone. */
@Service
@RequiredArgsConstructor
public class UpdateDepartment {

    private final EntityResolver entityResolver;
    private final DepartmentRepository departmentRepository;
    private final DepartmentEvents departmentEvents;
    private final DepartmentMapper departmentMapper;

    @Transactional
    public Department update(Long id, DepartmentRequestDTO request) {
        ValidationUtils.requireNonNull(id, "Department ID");
        ValidationUtils.requireNonNull(request, "Request body");
        Department entity = departmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Department not found"));
        if (request.headStaffId() != null) {
            entity.setHeadStaff(entityResolver.resolveStaff(request.headStaffId()));
        }

        if (request.name() != null && !request.name().isBlank()) {
            if (departmentRepository.existsByNameIgnoreCaseAndIdNot(request.name().trim(), id)) {
                throw new ConflictException("Department with this name already exists");
            }
            entity.setName(request.name().trim());
        }

        Department saved = departmentRepository.save(entity);
        departmentEvents.broadcastUpdated(departmentMapper.toResponse(saved));
        return saved;
    }
}

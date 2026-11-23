package com.app.meridian.department.services;

import com.app.meridian.department.Department;
import com.app.meridian.department.DepartmentEvents;
import com.app.meridian.department.DepartmentRepository;
import com.app.meridian.department.dto.DepartmentRequestDTO;
import com.app.meridian.department.utils.DepartmentMapper;
import com.app.meridian.exceptions.ConflictException;
import com.app.meridian.staff.Staff;
import com.app.meridian.utils.EntityResolver;
import com.app.meridian.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateDepartment {

    private final EntityResolver entityResolver;
    private final DepartmentRepository departmentRepository;
    private final DepartmentEvents departmentEvents;
    private final DepartmentMapper departmentMapper;

    @Transactional
    public Department create(DepartmentRequestDTO request) {
        ValidationUtils.requireNonNull(request, "Request body");
        ValidationUtils.requireNonBlank(request.name(), "name");

        Staff headStaff = entityResolver.resolveStaff(request.headStaffId());

        if (request.name() != null && departmentRepository.existsByNameIgnoreCase(request.name().trim())) {
            throw new ConflictException("Department with this name already exists");
        }

        Department entity = new Department();
        entity.setName(request.name().trim());
        entity.setHeadStaff(headStaff);

        Department saved = departmentRepository.save(entity);
        departmentEvents.broadcastCreated(departmentMapper.toResponse(saved));
        return saved;
    }
}

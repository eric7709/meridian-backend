package com.app.meridian.department.services;

import com.app.meridian.department.DepartmentEvents;
import com.app.meridian.department.DepartmentRepository;
import com.app.meridian.exceptions.NotFoundException;
import com.app.meridian.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteDepartment {

    private final DepartmentRepository departmentRepository;
    private final DepartmentEvents departmentEvents;

    @Transactional
    public void delete(Long id) {
        ValidationUtils.requireNonNull(id, "Department ID");
        if (!departmentRepository.existsById(id)) {
            throw new NotFoundException("Department not found");
        }
        departmentRepository.deleteById(id);
        departmentEvents.broadcastDeleted(id);
    }
}

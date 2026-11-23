package com.app.meridian.department.services;

import com.app.meridian.department.Department;
import com.app.meridian.department.DepartmentRepository;
import com.app.meridian.exceptions.NotFoundException;
import com.app.meridian.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetDepartmentById {

    private final DepartmentRepository departmentRepository;

    @Transactional(readOnly = true)
    public Department get(Long id) {
        ValidationUtils.requireNonNull(id, "Department ID");
        return departmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Department not found"));
    }
}

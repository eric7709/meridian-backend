package com.app.meridian.enrollment.services;

import com.app.meridian.enrollment.Enrollment;
import com.app.meridian.enrollment.EnrollmentRepository;
import com.app.meridian.exceptions.NotFoundException;
import com.app.meridian.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetEnrollmentById {

    private final EnrollmentRepository enrollmentRepository;

    @Transactional(readOnly = true)
    public Enrollment get(Long id) {
        ValidationUtils.requireNonNull(id, "Enrollment ID");
        return enrollmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Enrollment not found"));
    }
}

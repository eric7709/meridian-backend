package com.app.meridian.enrollment.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.meridian.enrollment.Enrollment;
import com.app.meridian.enrollment.EnrollmentRepository;
import com.app.meridian.student.services.GetCurrentStudent;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetStudentEnrollments {

    private final EnrollmentRepository enrollmentRepository;
    private final GetCurrentStudent getCurrentStudent;

    @Transactional(readOnly = true)
    public List<Enrollment> forStudent(Long studentId) {
        Long target = studentId == null ? getCurrentStudent.get().getId() : studentId;
        return enrollmentRepository.findByStudentIdOrderByEnrolledOnDesc(target);
    }
}

package com.app.meridian.enrollment.services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.meridian.academicSession.AcademicSession;
import com.app.meridian.classroom.ClassRoom;
import com.app.meridian.enrollment.Enrollment;
import com.app.meridian.enrollment.EnrollmentRepository;
import com.app.meridian.enrollment.dto.PromoteRequestDTO;
import com.app.meridian.auditLog.services.AuditTrail;
import com.app.meridian.exceptions.BadRequestException;
import com.app.meridian.student.Student;
import com.app.meridian.student.StudentRepository;
import com.app.meridian.utils.EntityResolver;
import com.app.meridian.utils.ValidationUtils;

import lombok.RequiredArgsConstructor;

/** Bulk end-of-session promotion. Students already enrolled in the target session are skipped. */
@Service
@RequiredArgsConstructor
public class PromoteStudents {

    private final EntityResolver entityResolver;
    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final AuditTrail auditTrail;

    @Transactional
    public List<Long> promote(PromoteRequestDTO request) {
        ValidationUtils.requireNonNull(request, "Request body");
        ValidationUtils.requireNonNull(request.toSessionId(), "Target session");
        ValidationUtils.requireNonNull(request.toClassId(), "Target class");
        ValidationUtils.requireNotEmpty(request.studentIds(), "Student ids");
        AcademicSession toSession = entityResolver.resolveAcademicSession(request.toSessionId());
        ClassRoom toClass = entityResolver.resolveClassRoom(request.toClassId());
        List<Long> promoted = new ArrayList<>();
        for (Long studentId : request.studentIds()) {
            Student student = entityResolver.resolveStudent(studentId);
            if (enrollmentRepository.findByStudentIdAndSessionId(student.getId(), toSession.getId()).isPresent()) {
                continue;
            }
            Enrollment enrollment = new Enrollment();
            enrollment.setStudent(student);
            enrollment.setClassRoom(toClass);
            enrollment.setSession(toSession);
            enrollment.setEnrolledOn(LocalDate.now());
            enrollmentRepository.save(enrollment);
            if (Boolean.TRUE.equals(toSession.getIsCurrent())) {
                student.setCurrentClass(toClass);
                studentRepository.save(student);
            }
            promoted.add(student.getId());
        }
        if (promoted.isEmpty()) {
            throw new BadRequestException("None of those students needed promoting");
        }
        auditTrail.record("STUDENTS_PROMOTED", "Enrollment", null,
                promoted.size() + " student(s) promoted to " + toClass.getName());
        return promoted;
    }
}

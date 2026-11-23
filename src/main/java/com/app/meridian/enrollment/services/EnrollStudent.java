package com.app.meridian.enrollment.services;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.meridian.academicSession.AcademicSession;
import com.app.meridian.classroom.ClassRoom;
import com.app.meridian.enrollment.Enrollment;
import com.app.meridian.enrollment.EnrollmentEvents;
import com.app.meridian.enrollment.EnrollmentRepository;
import com.app.meridian.enrollment.dto.EnrollmentRequestDTO;
import com.app.meridian.enrollment.utils.EnrollmentMapper;
import com.app.meridian.auditLog.services.AuditTrail;
import com.app.meridian.exceptions.ConflictException;
import com.app.meridian.student.Student;
import com.app.meridian.utils.EntityResolver;
import com.app.meridian.utils.ValidationUtils;

import lombok.RequiredArgsConstructor;

/** Places a student in a class for a session; one enrollment per student per session. */
@Service
@RequiredArgsConstructor
public class EnrollStudent {

    private final EntityResolver entityResolver;
    private final EnrollmentRepository enrollmentRepository;
    private final EnrollmentEvents enrollmentEvents;
    private final EnrollmentMapper enrollmentMapper;
    private final AuditTrail auditTrail;

    @Transactional
    public Enrollment enroll(EnrollmentRequestDTO request) {
        ValidationUtils.requireNonNull(request, "Request body");
        ValidationUtils.requireNonNull(request.studentId(), "Student");
        ValidationUtils.requireNonNull(request.classId(), "Class");
        ValidationUtils.requireNonNull(request.sessionId(), "Session");
        Student student = entityResolver.resolveStudent(request.studentId());
        ClassRoom classRoom = entityResolver.resolveClassRoom(request.classId());
        AcademicSession session = entityResolver.resolveAcademicSession(request.sessionId());
        if (enrollmentRepository.findByStudentIdAndSessionId(student.getId(), session.getId()).isPresent()) {
            throw new ConflictException("This student is already enrolled for that session");
        }
        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setClassRoom(classRoom);
        enrollment.setSession(session);
        enrollment.setEnrolledOn(request.enrolledOn() == null ? LocalDate.now() : request.enrolledOn());
        Enrollment saved = enrollmentRepository.save(enrollment);
        if (Boolean.TRUE.equals(session.getIsCurrent())) {
            student.setCurrentClass(classRoom);
        }
        enrollmentEvents.broadcastCreated(enrollmentMapper.toResponse(saved));
        auditTrail.record("STUDENT_ENROLLED", "Enrollment", saved.getId(),
                student.getUser().getFullName() + " enrolled in " + classRoom.getName());
        return saved;
    }
}

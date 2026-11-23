package com.app.meridian.attendance.services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.meridian.enrollment.Enrollment;
import com.app.meridian.enrollment.EnrollmentRepository;
import com.app.meridian.term.Term;
import com.app.meridian.attendance.Attendance;
import com.app.meridian.attendance.AttendanceEvents;
import com.app.meridian.attendance.AttendanceRepository;
import com.app.meridian.attendance.dto.AttendanceEntryDTO;
import com.app.meridian.attendance.dto.AttendanceRegisterRequestDTO;
import com.app.meridian.attendance.utils.AttendanceMapper;
import com.app.meridian.exceptions.BadRequestException;
import com.app.meridian.student.Student;
import com.app.meridian.utils.EntityResolver;
import com.app.meridian.utils.ValidationUtils;

import lombok.RequiredArgsConstructor;

/** Marks (or re-marks) a class register; one attendance row per student per day. */
@Service
@RequiredArgsConstructor
public class MarkAttendanceRegister {

    private final EntityResolver entityResolver;
    private final AttendanceRepository attendanceRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AttendanceEvents attendanceEvents;
    private final AttendanceMapper attendanceMapper;

    @Transactional
    public List<Attendance> mark(AttendanceRegisterRequestDTO request) {
        ValidationUtils.requireNonNull(request, "Request body");
        ValidationUtils.requireNonNull(request.termId(), "Term");
        ValidationUtils.requireNonNull(request.classId(), "Class");
        ValidationUtils.requireNotEmpty(request.entries(), "Attendance entries");
        Term term = entityResolver.resolveTerm(request.termId());
        LocalDate date = request.date() == null ? LocalDate.now() : request.date();
        List<Enrollment> roster = enrollmentRepository.findByClassRoomIdAndSessionId(
                request.classId(), term.getSession().getId());
        if (roster.isEmpty()) {
            throw new BadRequestException("That class has no enrolled students for the session");
        }
        List<Long> rosterIds = roster.stream().map(enrollment -> enrollment.getStudent().getId()).toList();
        List<Attendance> saved = new ArrayList<>();
        for (AttendanceEntryDTO entry : request.entries()) {
            ValidationUtils.requireNonNull(entry, "Attendance entry");
            ValidationUtils.requireNonNull(entry.studentId(), "Student");
            ValidationUtils.requireNonNull(entry.status(), "Attendance status");
            if (!rosterIds.contains(entry.studentId())) {
                throw new BadRequestException("Student " + entry.studentId() + " is not in this class");
            }
            Student student = entityResolver.resolveStudent(entry.studentId());
            Attendance attendance = attendanceRepository
                    .findByStudentIdAndDate(student.getId(), date)
                    .orElseGet(() -> {
                        Attendance created = new Attendance();
                        created.setStudent(student);
                        created.setDate(date);
                        return created;
                    });
            attendance.setTerm(term);
            attendance.setStatus(entry.status());
            Attendance row = attendanceRepository.save(attendance);
            attendanceEvents.broadcastUpdated(attendanceMapper.toResponse(row));
            saved.add(row);
        }
        return saved;
    }
}

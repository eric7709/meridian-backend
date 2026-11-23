package com.app.meridian.attendance.services;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.meridian.enrollment.Enrollment;
import com.app.meridian.enrollment.EnrollmentRepository;
import com.app.meridian.term.Term;
import com.app.meridian.attendance.Attendance;
import com.app.meridian.attendance.AttendanceRepository;
import com.app.meridian.attendance.dto.AttendanceSummaryDTO;
import com.app.meridian.attendance.dto.AttendanceSummaryRowDTO;
import com.app.meridian.attendance.enums.AttendanceStatus;
import com.app.meridian.exceptions.BadRequestException;
import com.app.meridian.student.Student;
import com.app.meridian.student.services.GetCurrentStudent;
import com.app.meridian.utils.EntityResolver;
import com.app.meridian.utils.ValidationUtils;

import lombok.RequiredArgsConstructor;
import com.app.meridian.term.services.GetActiveTerm;

/** Attendance percentage per student for a term (the dashboard "attendance %"). */
@Service
@RequiredArgsConstructor
public class GetAttendanceSummary {

    private final EntityResolver entityResolver;
    private final AttendanceRepository attendanceRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final GetCurrentStudent getCurrentStudent;
    private final GetActiveTerm getActiveTerm;

    @Transactional(readOnly = true)
    public AttendanceSummaryDTO forClass(Long termId, Long classId) {
        ValidationUtils.requireNonNull(termId, "Term");
        ValidationUtils.requireNonNull(classId, "Class");
        Term term = entityResolver.resolveTerm(termId);
        List<Enrollment> roster = enrollmentRepository.findByClassRoomIdAndSessionId(classId,
                term.getSession().getId());
        if (roster.isEmpty()) {
            throw new BadRequestException("That class has no enrolled students for the session");
        }
        List<Long> studentIds = roster.stream().map(enrollment -> enrollment.getStudent().getId()).toList();
        List<Attendance> records = attendanceRepository.findByTermIdAndStudentIdIn(term.getId(), studentIds);
        Map<Long, List<Attendance>> byStudent = records.stream()
                .collect(Collectors.groupingBy(record -> record.getStudent().getId()));
        List<AttendanceSummaryRowDTO> rows = new ArrayList<>();
        for (Enrollment enrollment : roster) {
            Student student = enrollment.getStudent();
            List<Attendance> mine = byStudent.getOrDefault(student.getId(), List.of());
            long present = mine.stream().filter(r -> r.getStatus() == AttendanceStatus.PRESENT).count();
            long late = mine.stream().filter(r -> r.getStatus() == AttendanceStatus.LATE).count();
            long absent = mine.stream().filter(r -> r.getStatus() == AttendanceStatus.ABSENT).count();
            long total = mine.size();
            Double percentage = total == 0 ? null
                    : Math.round(((present + late) * 10000.0) / total) / 100.0;
            rows.add(new AttendanceSummaryRowDTO(student.getId(), student.getStudentNo(),
                    student.getUser().getFullName(), present, absent, late, total, percentage));
        }
        rows.sort(Comparator.comparing(AttendanceSummaryRowDTO::studentName, String.CASE_INSENSITIVE_ORDER));
        Set<java.time.LocalDate> days = new HashSet<>();
        records.forEach(record -> days.add(record.getDate()));
        return new AttendanceSummaryDTO(term.getId(), days.size(), rows);
    }

    @Transactional(readOnly = true)
    public AttendanceSummaryDTO forCurrentStudent(Long termId) {
        Term term = termId != null ? entityResolver.resolveTerm(termId) : getActiveTerm.get();
        Student student = getCurrentStudent.get();
        List<Attendance> mine = academicTermRecords(term, student);
        long present = mine.stream().filter(r -> r.getStatus() == AttendanceStatus.PRESENT).count();
        long late = mine.stream().filter(r -> r.getStatus() == AttendanceStatus.LATE).count();
        long absent = mine.stream().filter(r -> r.getStatus() == AttendanceStatus.ABSENT).count();
        long total = mine.size();
        Double percentage = total == 0 ? null : Math.round(((present + late) * 10000.0) / total) / 100.0;
        return new AttendanceSummaryDTO(term == null ? null : term.getId(), total,
                List.of(new AttendanceSummaryRowDTO(student.getId(), student.getStudentNo(),
                        student.getUser().getFullName(), present, absent, late, total, percentage)));
    }

    private List<Attendance> academicTermRecords(Term term, Student student) {
        if (term == null) {
            return List.of();
        }
        return attendanceRepository.findByStudentIdAndTermId(student.getId(), term.getId());
    }
}

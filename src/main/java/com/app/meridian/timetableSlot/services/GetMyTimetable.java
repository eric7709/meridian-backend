package com.app.meridian.timetableSlot.services;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.meridian.enrollment.Enrollment;
import com.app.meridian.enrollment.EnrollmentRepository;
import com.app.meridian.term.Term;
import com.app.meridian.term.services.GetActiveTerm;
import com.app.meridian.timetableSlot.TimetableSlot;
import com.app.meridian.timetableSlot.TimetableSlotRepository;
import com.app.meridian.timetableSlot.enums.DayOfWeek;
import com.app.meridian.exceptions.BadRequestException;
import com.app.meridian.staff.services.GetCurrentStaff;
import com.app.meridian.student.services.GetCurrentStudent;
import com.app.meridian.security.SecurityUtils;

import lombok.RequiredArgsConstructor;

/** "Today" and "this week" grids: teachers see their lessons, students see their class's. */
@Service
@RequiredArgsConstructor
public class GetMyTimetable {

    private final TimetableSlotRepository timetableSlotRepository;
    private final GetActiveTerm getActiveTerm;
    private final EnrollmentRepository enrollmentRepository;
    private final GetCurrentStaff getCurrentStaff;
    private final GetCurrentStudent getCurrentStudent;

    @Transactional(readOnly = true)
    public List<TimetableSlot> get(String day, boolean week) {
        Term term = getActiveTerm.get();
        List<TimetableSlot> slots = timetableSlotRepository.findByTermIdOrderByStartTimeAsc(term.getId());
        Long teacherId = null;
        Long classId = null;
        if (SecurityUtils.principal().isTeacher()) {
            teacherId = getCurrentStaff.get().getId();
        } else if (SecurityUtils.principal().isStudent()) {
            var student = getCurrentStudent.get();
            classId = enrollmentRepository
                    .findByStudentIdAndSessionId(student.getId(), term.getSession().getId())
                    .map(Enrollment::getClassRoom)
                    .map(c -> c.getId())
                    .orElse(null);
            if (classId == null) {
                return List.of();
            }
        }
        Long scopedTeacherId = teacherId;
        Long scopedClassId = classId;
        DayOfWeek targetDay = resolveDay(day);
        return slots.stream()
                .filter(slot -> scopedTeacherId == null
                        || slot.getAssignment().getTeacher().getId().equals(scopedTeacherId))
                .filter(slot -> scopedClassId == null
                        || slot.getAssignment().getClassRoom().getId().equals(scopedClassId))
                .filter(slot -> targetDay == null || slot.getDayOfWeek() == targetDay)
                .filter(slot -> !week || true)
                .toList();
    }

    private DayOfWeek resolveDay(String day) {
        if (day == null || day.isBlank() || "today".equalsIgnoreCase(day)) {
            java.time.DayOfWeek today = LocalDate.now().getDayOfWeek();
            return switch (today) {
                case MONDAY -> DayOfWeek.MONDAY;
                case TUESDAY -> DayOfWeek.TUESDAY;
                case WEDNESDAY -> DayOfWeek.WEDNESDAY;
                case THURSDAY -> DayOfWeek.THURSDAY;
                case FRIDAY -> DayOfWeek.FRIDAY;
                default -> null;
            };
        }
        try {
            return DayOfWeek.valueOf(day.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Unknown day of week: " + day);
        }
    }
}

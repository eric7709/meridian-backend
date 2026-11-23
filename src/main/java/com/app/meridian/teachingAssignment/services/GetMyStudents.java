package com.app.meridian.teachingAssignment.services;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.meridian.enrollment.Enrollment;
import com.app.meridian.enrollment.EnrollmentRepository;
import com.app.meridian.teachingAssignment.TeachingAssignment;
import com.app.meridian.teachingAssignment.TeachingAssignmentRepository;
import com.app.meridian.term.Term;
import com.app.meridian.term.services.GetActiveTerm;
import com.app.meridian.exceptions.BadRequestException;
import com.app.meridian.staff.Staff;
import com.app.meridian.staff.services.GetCurrentStaff;
import com.app.meridian.teachingAssignment.dto.MyStudentRowDTO;

import lombok.RequiredArgsConstructor;

/** Every student in the classes this teacher teaches — never anyone else's. */
@Service
@RequiredArgsConstructor
public class GetMyStudents {

    private final TeachingAssignmentRepository teachingAssignmentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final GetActiveTerm getActiveTerm;
    private final GetCurrentStaff getCurrentStaff;

    @Transactional(readOnly = true)
    public List<MyStudentRowDTO> get(String search, Long classId, Long subjectId) {
        Staff me = getCurrentStaff.get();
        Term active = getActiveTerm.get();
        Long sessionId = active.getSession().getId();
        List<TeachingAssignment> assignments = teachingAssignmentRepository
                .findByTeacherIdAndSessionId(me.getId(), sessionId).stream()
                .filter(assignment -> classId == null || assignment.getClassRoom().getId().equals(classId))
                .filter(assignment -> subjectId == null || assignment.getSubject().getId().equals(subjectId))
                .toList();
        Map<String, MyStudentRowDTO> rows = new LinkedHashMap<>();
        for (TeachingAssignment assignment : assignments) {
            List<Enrollment> roster = enrollmentRepository
                    .findByClassRoomIdAndSessionId(assignment.getClassRoom().getId(), sessionId);
            for (Enrollment enrollment : roster) {
                if (!matches(enrollment, search)) {
                    continue;
                }
                String key = enrollment.getStudent().getId() + "|" + assignment.getSubject().getId();
                rows.putIfAbsent(key, new MyStudentRowDTO(
                        enrollment.getStudent().getId(),
                        enrollment.getStudent().getStudentNo(),
                        enrollment.getStudent().getUser().getFullName(),
                        assignment.getClassRoom().getId(),
                        assignment.getClassRoom().getName(),
                        assignment.getSubject().getName()));
            }
        }
        List<MyStudentRowDTO> result = new ArrayList<>(rows.values());
        result.sort(Comparator.comparing(MyStudentRowDTO::studentName, String.CASE_INSENSITIVE_ORDER));
        return result;
    }

    private boolean matches(Enrollment enrollment, String search) {
        if (search == null || search.isBlank()) {
            return true;
        }
        String needle = search.trim().toLowerCase();
        String haystack = (enrollment.getStudent().getUser().getFullName() + " "
                + enrollment.getStudent().getStudentNo()).toLowerCase();
        return haystack.contains(needle);
    }
}

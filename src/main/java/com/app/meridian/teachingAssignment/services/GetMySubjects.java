package com.app.meridian.teachingAssignment.services;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.meridian.enrollment.EnrollmentRepository;
import com.app.meridian.teachingAssignment.TeachingAssignment;
import com.app.meridian.teachingAssignment.TeachingAssignmentRepository;
import com.app.meridian.term.Term;
import com.app.meridian.term.services.GetActiveTerm;
import com.app.meridian.exceptions.BadRequestException;
import com.app.meridian.staff.Staff;
import com.app.meridian.staff.services.GetCurrentStaff;
import com.app.meridian.teachingAssignment.dto.MySubjectDTO;

import lombok.RequiredArgsConstructor;

/** "My subjects" cards: always scoped to the active term's session. */
@Service
@RequiredArgsConstructor
public class GetMySubjects {

    private final TeachingAssignmentRepository teachingAssignmentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final GetActiveTerm getActiveTerm;
    private final GetCurrentStaff getCurrentStaff;

    @Transactional(readOnly = true)
    public List<MySubjectDTO> get(String sessionLabelFilter) {
        Staff me = getCurrentStaff.get();
        Term active = getActiveTerm.get();
        Long sessionId = active.getSession().getId();
        List<TeachingAssignment> assignments = teachingAssignmentRepository
                .findByTeacherIdAndSessionId(me.getId(), sessionId);
        return assignments.stream()
                .filter(assignment -> sessionLabelFilter == null || sessionLabelFilter.isBlank()
                        || sessionLabelFilter.equalsIgnoreCase(assignment.getSession().getLabel()))
                .map(assignment -> new MySubjectDTO(
                        assignment.getId(),
                        assignment.getSubject().getId(),
                        assignment.getSubject().getName(),
                        assignment.getSubject().getCode(),
                        assignment.getClassRoom().getId(),
                        assignment.getClassRoom().getName(),
                        assignment.getSession().getLabel(),
                        enrollmentRepository.findByClassRoomIdAndSessionId(
                                assignment.getClassRoom().getId(), sessionId).size()))
                .sorted(Comparator.comparing(MySubjectDTO::subjectName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }
}

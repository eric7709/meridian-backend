package com.app.meridian.teachingAssignment.services;

import com.app.meridian.academicSession.AcademicSession;
import com.app.meridian.classroom.ClassRoom;
import com.app.meridian.exceptions.ConflictException;
import com.app.meridian.exceptions.NotFoundException;
import com.app.meridian.staff.Staff;
import com.app.meridian.subject.Subject;
import com.app.meridian.teachingAssignment.TeachingAssignment;
import com.app.meridian.teachingAssignment.TeachingAssignmentEvents;
import com.app.meridian.teachingAssignment.TeachingAssignmentRepository;
import com.app.meridian.teachingAssignment.dto.TeachingAssignmentRequestDTO;
import com.app.meridian.teachingAssignment.utils.TeachingAssignmentMapper;
import com.app.meridian.utils.EntityResolver;
import com.app.meridian.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Applies only the fields present in the request; everything else is left alone. */
@Service
@RequiredArgsConstructor
public class UpdateTeachingAssignment {

    private final EntityResolver entityResolver;
    private final TeachingAssignmentRepository teachingAssignmentRepository;
    private final TeachingAssignmentEvents teachingAssignmentEvents;
    private final TeachingAssignmentMapper teachingAssignmentMapper;

    @Transactional
    public TeachingAssignment update(Long id, TeachingAssignmentRequestDTO request) {
        ValidationUtils.requireNonNull(id, "TeachingAssignment ID");
        ValidationUtils.requireNonNull(request, "Request body");
        TeachingAssignment entity = teachingAssignmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("TeachingAssignment not found"));
        if (request.teacherId() != null) {
            entity.setTeacher(entityResolver.resolveStaff(request.teacherId()));
        }
        if (request.subjectId() != null) {
            entity.setSubject(entityResolver.resolveSubject(request.subjectId()));
        }
        if (request.classId() != null) {
            entity.setClassRoom(entityResolver.resolveClassRoom(request.classId()));
        }
        if (request.sessionId() != null) {
            entity.setSession(entityResolver.resolveAcademicSession(request.sessionId()));
        }


        TeachingAssignment saved = teachingAssignmentRepository.save(entity);
        teachingAssignmentEvents.broadcastUpdated(teachingAssignmentMapper.toResponse(saved));
        return saved;
    }
}

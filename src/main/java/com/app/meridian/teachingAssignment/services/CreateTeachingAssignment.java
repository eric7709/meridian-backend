package com.app.meridian.teachingAssignment.services;

import com.app.meridian.academicSession.AcademicSession;
import com.app.meridian.classroom.ClassRoom;
import com.app.meridian.exceptions.ConflictException;
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

@Service
@RequiredArgsConstructor
public class CreateTeachingAssignment {

    private final EntityResolver entityResolver;
    private final TeachingAssignmentRepository teachingAssignmentRepository;
    private final TeachingAssignmentEvents teachingAssignmentEvents;
    private final TeachingAssignmentMapper teachingAssignmentMapper;

    @Transactional
    public TeachingAssignment create(TeachingAssignmentRequestDTO request) {
        ValidationUtils.requireNonNull(request, "Request body");
        ValidationUtils.requireNonNull(request.teacherId(), "teacher");
        ValidationUtils.requireNonNull(request.subjectId(), "subject");
        ValidationUtils.requireNonNull(request.classId(), "classRoom");
        ValidationUtils.requireNonNull(request.sessionId(), "session");

        Staff teacher = entityResolver.resolveStaff(request.teacherId());
        Subject subject = entityResolver.resolveSubject(request.subjectId());
        ClassRoom classRoom = entityResolver.resolveClassRoom(request.classId());
        AcademicSession session = entityResolver.resolveAcademicSession(request.sessionId());

        if (teachingAssignmentRepository.existsByTeacherIdAndSubjectIdAndClassRoomIdAndSessionId(request.teacherId(), request.subjectId(), request.classId(), request.sessionId())) {
            throw new ConflictException("TeachingAssignment already exists for that combination");
        }

        TeachingAssignment entity = new TeachingAssignment();
        entity.setTeacher(teacher);
        entity.setSubject(subject);
        entity.setClassRoom(classRoom);
        entity.setSession(session);

        TeachingAssignment saved = teachingAssignmentRepository.save(entity);
        teachingAssignmentEvents.broadcastCreated(teachingAssignmentMapper.toResponse(saved));
        return saved;
    }
}

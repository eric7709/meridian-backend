package com.app.meridian.classroom.services;

import com.app.meridian.academicSession.AcademicSession;
import com.app.meridian.classroom.ClassRoom;
import com.app.meridian.classroom.ClassRoomEvents;
import com.app.meridian.classroom.ClassRoomRepository;
import com.app.meridian.classroom.dto.ClassRoomRequestDTO;
import com.app.meridian.classroom.enums.Level;
import com.app.meridian.classroom.utils.ClassRoomMapper;
import com.app.meridian.exceptions.ConflictException;
import com.app.meridian.exceptions.NotFoundException;
import com.app.meridian.staff.Staff;
import com.app.meridian.utils.EntityResolver;
import com.app.meridian.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Applies only the fields present in the request; everything else is left alone. */
@Service
@RequiredArgsConstructor
public class UpdateClassRoom {

    private final EntityResolver entityResolver;
    private final ClassRoomRepository classRoomRepository;
    private final ClassRoomEvents classRoomEvents;
    private final ClassRoomMapper classRoomMapper;

    @Transactional
    public ClassRoom update(Long id, ClassRoomRequestDTO request) {
        ValidationUtils.requireNonNull(id, "Class ID");
        ValidationUtils.requireNonNull(request, "Request body");
        ClassRoom entity = classRoomRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Class not found"));
        if (request.formTeacherId() != null) {
            entity.setFormTeacher(entityResolver.resolveStaff(request.formTeacherId()));
        }
        if (request.sessionId() != null) {
            entity.setSession(entityResolver.resolveAcademicSession(request.sessionId()));
        }

        if (request.name() != null && !request.name().isBlank()) {
            if (classRoomRepository.existsByNameIgnoreCaseAndIdNot(request.name().trim(), id)) {
                throw new ConflictException("Class with this name already exists");
            }
            entity.setName(request.name().trim());
        }
        if (request.level() != null) {
            entity.setLevel(request.level());
        }
        if (request.arm() != null && !request.arm().isBlank()) {
            entity.setArm(request.arm().trim());
        }

        ClassRoom saved = classRoomRepository.save(entity);
        classRoomEvents.broadcastUpdated(classRoomMapper.toResponse(saved));
        return saved;
    }
}

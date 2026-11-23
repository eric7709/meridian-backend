package com.app.meridian.classroom.services;

import com.app.meridian.academicSession.AcademicSession;
import com.app.meridian.classroom.ClassRoom;
import com.app.meridian.classroom.ClassRoomEvents;
import com.app.meridian.classroom.ClassRoomRepository;
import com.app.meridian.classroom.dto.ClassRoomRequestDTO;
import com.app.meridian.classroom.enums.Level;
import com.app.meridian.classroom.utils.ClassRoomMapper;
import com.app.meridian.exceptions.ConflictException;
import com.app.meridian.staff.Staff;
import com.app.meridian.utils.EntityResolver;
import com.app.meridian.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateClassRoom {

    private final EntityResolver entityResolver;
    private final ClassRoomRepository classRoomRepository;
    private final ClassRoomEvents classRoomEvents;
    private final ClassRoomMapper classRoomMapper;

    @Transactional
    public ClassRoom create(ClassRoomRequestDTO request) {
        ValidationUtils.requireNonNull(request, "Request body");
        ValidationUtils.requireNonBlank(request.name(), "name");
        ValidationUtils.requireNonNull(request.level(), "level");

        Staff formTeacher = entityResolver.resolveStaff(request.formTeacherId());
        AcademicSession session = entityResolver.resolveAcademicSession(request.sessionId());

        if (request.name() != null && classRoomRepository.existsByNameIgnoreCase(request.name().trim())) {
            throw new ConflictException("Class with this name already exists");
        }

        ClassRoom entity = new ClassRoom();
        entity.setName(request.name().trim());
        entity.setLevel(request.level());
        entity.setArm(ValidationUtils.trimOrNull(request.arm()));
        entity.setFormTeacher(formTeacher);
        entity.setSession(session);

        ClassRoom saved = classRoomRepository.save(entity);
        classRoomEvents.broadcastCreated(classRoomMapper.toResponse(saved));
        return saved;
    }
}

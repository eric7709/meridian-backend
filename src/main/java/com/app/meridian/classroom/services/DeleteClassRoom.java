package com.app.meridian.classroom.services;

import com.app.meridian.classroom.ClassRoomEvents;
import com.app.meridian.classroom.ClassRoomRepository;
import com.app.meridian.exceptions.NotFoundException;
import com.app.meridian.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteClassRoom {

    private final ClassRoomRepository classRoomRepository;
    private final ClassRoomEvents classRoomEvents;

    @Transactional
    public void delete(Long id) {
        ValidationUtils.requireNonNull(id, "Class ID");
        if (!classRoomRepository.existsById(id)) {
            throw new NotFoundException("Class not found");
        }
        classRoomRepository.deleteById(id);
        classRoomEvents.broadcastDeleted(id);
    }
}

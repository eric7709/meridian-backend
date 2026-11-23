package com.app.meridian.classroom.services;

import com.app.meridian.classroom.ClassRoom;
import com.app.meridian.classroom.ClassRoomRepository;
import com.app.meridian.exceptions.NotFoundException;
import com.app.meridian.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetClassRoomById {

    private final ClassRoomRepository classRoomRepository;

    @Transactional(readOnly = true)
    public ClassRoom get(Long id) {
        ValidationUtils.requireNonNull(id, "Class ID");
        return classRoomRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Class not found"));
    }
}

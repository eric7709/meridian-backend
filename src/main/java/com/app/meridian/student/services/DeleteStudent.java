package com.app.meridian.student.services;

import com.app.meridian.exceptions.NotFoundException;
import com.app.meridian.student.StudentEvents;
import com.app.meridian.student.StudentRepository;
import com.app.meridian.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteStudent {

    private final StudentRepository studentRepository;
    private final StudentEvents studentEvents;

    @Transactional
    public void delete(Long id) {
        ValidationUtils.requireNonNull(id, "Student ID");
        if (!studentRepository.existsById(id)) {
            throw new NotFoundException("Student not found");
        }
        studentRepository.deleteById(id);
        studentEvents.broadcastDeleted(id);
    }
}

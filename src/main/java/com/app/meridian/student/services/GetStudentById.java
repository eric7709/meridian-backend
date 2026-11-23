package com.app.meridian.student.services;

import com.app.meridian.exceptions.NotFoundException;
import com.app.meridian.student.Student;
import com.app.meridian.student.StudentRepository;
import com.app.meridian.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetStudentById {

    private final StudentRepository studentRepository;

    @Transactional(readOnly = true)
    public Student get(Long id) {
        ValidationUtils.requireNonNull(id, "Student ID");
        return studentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Student not found"));
    }
}

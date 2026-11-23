package com.app.meridian.student.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.meridian.exceptions.ForbiddenException;
import com.app.meridian.student.Student;
import com.app.meridian.student.StudentRepository;
import com.app.meridian.security.SecurityUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetCurrentStudent {

    private final StudentRepository studentRepository;

    @Transactional(readOnly = true)
    public Student get() {
        return studentRepository.findByUserId(SecurityUtils.userId())
                .orElseThrow(() -> new ForbiddenException("This action requires a student profile"));
    }
}

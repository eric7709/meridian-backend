package com.app.meridian.teachingAssignment.services;

import com.app.meridian.exceptions.NotFoundException;
import com.app.meridian.teachingAssignment.TeachingAssignment;
import com.app.meridian.teachingAssignment.TeachingAssignmentRepository;
import com.app.meridian.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetTeachingAssignmentById {

    private final TeachingAssignmentRepository teachingAssignmentRepository;

    @Transactional(readOnly = true)
    public TeachingAssignment get(Long id) {
        ValidationUtils.requireNonNull(id, "TeachingAssignment ID");
        return teachingAssignmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("TeachingAssignment not found"));
    }
}

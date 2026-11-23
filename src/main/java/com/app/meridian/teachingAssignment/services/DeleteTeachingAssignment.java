package com.app.meridian.teachingAssignment.services;

import com.app.meridian.exceptions.NotFoundException;
import com.app.meridian.teachingAssignment.TeachingAssignmentEvents;
import com.app.meridian.teachingAssignment.TeachingAssignmentRepository;
import com.app.meridian.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteTeachingAssignment {

    private final TeachingAssignmentRepository teachingAssignmentRepository;
    private final TeachingAssignmentEvents teachingAssignmentEvents;

    @Transactional
    public void delete(Long id) {
        ValidationUtils.requireNonNull(id, "TeachingAssignment ID");
        if (!teachingAssignmentRepository.existsById(id)) {
            throw new NotFoundException("TeachingAssignment not found");
        }
        teachingAssignmentRepository.deleteById(id);
        teachingAssignmentEvents.broadcastDeleted(id);
    }
}

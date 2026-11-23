package com.app.meridian.subject.services;

import com.app.meridian.exceptions.NotFoundException;
import com.app.meridian.subject.SubjectEvents;
import com.app.meridian.subject.SubjectRepository;
import com.app.meridian.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteSubject {

    private final SubjectRepository subjectRepository;
    private final SubjectEvents subjectEvents;

    @Transactional
    public void delete(Long id) {
        ValidationUtils.requireNonNull(id, "Subject ID");
        if (!subjectRepository.existsById(id)) {
            throw new NotFoundException("Subject not found");
        }
        subjectRepository.deleteById(id);
        subjectEvents.broadcastDeleted(id);
    }
}

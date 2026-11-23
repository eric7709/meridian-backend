package com.app.meridian.subject.services;

import com.app.meridian.exceptions.NotFoundException;
import com.app.meridian.subject.Subject;
import com.app.meridian.subject.SubjectRepository;
import com.app.meridian.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetSubjectById {

    private final SubjectRepository subjectRepository;

    @Transactional(readOnly = true)
    public Subject get(Long id) {
        ValidationUtils.requireNonNull(id, "Subject ID");
        return subjectRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Subject not found"));
    }
}

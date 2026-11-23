package com.app.meridian.academicSession.services;

import com.app.meridian.academicSession.AcademicSessionEvents;
import com.app.meridian.academicSession.AcademicSessionRepository;
import com.app.meridian.exceptions.NotFoundException;
import com.app.meridian.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteAcademicSession {

    private final AcademicSessionRepository academicSessionRepository;
    private final AcademicSessionEvents academicSessionEvents;

    @Transactional
    public void delete(Long id) {
        ValidationUtils.requireNonNull(id, "AcademicSession ID");
        if (!academicSessionRepository.existsById(id)) {
            throw new NotFoundException("AcademicSession not found");
        }
        academicSessionRepository.deleteById(id);
        academicSessionEvents.broadcastDeleted(id);
    }
}

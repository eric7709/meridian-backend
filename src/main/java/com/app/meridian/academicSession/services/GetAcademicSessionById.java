package com.app.meridian.academicSession.services;

import com.app.meridian.academicSession.AcademicSession;
import com.app.meridian.academicSession.AcademicSessionRepository;
import com.app.meridian.exceptions.NotFoundException;
import com.app.meridian.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetAcademicSessionById {

    private final AcademicSessionRepository academicSessionRepository;

    @Transactional(readOnly = true)
    public AcademicSession get(Long id) {
        ValidationUtils.requireNonNull(id, "AcademicSession ID");
        return academicSessionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("AcademicSession not found"));
    }
}

package com.app.meridian.academicSession.services;

import java.time.LocalDate;
import com.app.meridian.academicSession.AcademicSession;
import com.app.meridian.academicSession.AcademicSessionEvents;
import com.app.meridian.academicSession.AcademicSessionRepository;
import com.app.meridian.academicSession.dto.AcademicSessionRequestDTO;
import com.app.meridian.academicSession.utils.AcademicSessionMapper;
import com.app.meridian.exceptions.ConflictException;
import com.app.meridian.exceptions.NotFoundException;
import com.app.meridian.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Applies only the fields present in the request; everything else is left alone. */
@Service
@RequiredArgsConstructor
public class UpdateAcademicSession {

    private final AcademicSessionRepository academicSessionRepository;
    private final AcademicSessionEvents academicSessionEvents;
    private final AcademicSessionMapper academicSessionMapper;

    @Transactional
    public AcademicSession update(Long id, AcademicSessionRequestDTO request) {
        ValidationUtils.requireNonNull(id, "AcademicSession ID");
        ValidationUtils.requireNonNull(request, "Request body");
        AcademicSession entity = academicSessionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("AcademicSession not found"));
        if (request.label() != null && !request.label().isBlank()) {
            if (academicSessionRepository.existsByLabelIgnoreCaseAndIdNot(request.label().trim(), id)) {
                throw new ConflictException("AcademicSession with this label already exists");
            }
            entity.setLabel(request.label().trim());
        }
        if (request.startDate() != null) {
            entity.setStartDate(request.startDate());
        }
        if (request.endDate() != null) {
            entity.setEndDate(request.endDate());
        }
        if (request.isCurrent() != null) {
            entity.setIsCurrent(request.isCurrent());
        }

        AcademicSession saved = academicSessionRepository.save(entity);
        academicSessionEvents.broadcastUpdated(academicSessionMapper.toResponse(saved));
        return saved;
    }
}

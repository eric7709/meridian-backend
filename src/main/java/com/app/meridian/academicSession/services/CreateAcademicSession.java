package com.app.meridian.academicSession.services;

import java.time.LocalDate;
import com.app.meridian.academicSession.AcademicSession;
import com.app.meridian.academicSession.AcademicSessionEvents;
import com.app.meridian.academicSession.AcademicSessionRepository;
import com.app.meridian.academicSession.dto.AcademicSessionRequestDTO;
import com.app.meridian.academicSession.utils.AcademicSessionMapper;
import com.app.meridian.exceptions.ConflictException;
import com.app.meridian.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateAcademicSession {

    private final AcademicSessionRepository academicSessionRepository;
    private final AcademicSessionEvents academicSessionEvents;
    private final AcademicSessionMapper academicSessionMapper;

    @Transactional
    public AcademicSession create(AcademicSessionRequestDTO request) {
        ValidationUtils.requireNonNull(request, "Request body");
        ValidationUtils.requireNonBlank(request.label(), "label");
        ValidationUtils.requireNonNull(request.startDate(), "startDate");
        ValidationUtils.requireNonNull(request.endDate(), "endDate");

        if (request.label() != null && academicSessionRepository.existsByLabelIgnoreCase(request.label().trim())) {
            throw new ConflictException("AcademicSession with this label already exists");
        }

        AcademicSession entity = new AcademicSession();
        entity.setLabel(request.label().trim());
        entity.setStartDate(request.startDate());
        entity.setEndDate(request.endDate());
        entity.setIsCurrent(request.isCurrent() == null ? false : request.isCurrent());

        AcademicSession saved = academicSessionRepository.save(entity);
        academicSessionEvents.broadcastCreated(academicSessionMapper.toResponse(saved));
        return saved;
    }
}

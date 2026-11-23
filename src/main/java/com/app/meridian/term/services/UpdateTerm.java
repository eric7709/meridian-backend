package com.app.meridian.term.services;

import com.app.meridian.academicSession.AcademicSession;
import com.app.meridian.exceptions.ConflictException;
import com.app.meridian.exceptions.NotFoundException;
import com.app.meridian.term.Term;
import com.app.meridian.term.TermEvents;
import com.app.meridian.term.TermRepository;
import com.app.meridian.term.dto.TermRequestDTO;
import com.app.meridian.term.enums.TermName;
import com.app.meridian.term.utils.TermMapper;
import com.app.meridian.utils.EntityResolver;
import com.app.meridian.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Applies only the fields present in the request; everything else is left alone. */
@Service
@RequiredArgsConstructor
public class UpdateTerm {

    private final EntityResolver entityResolver;
    private final TermRepository termRepository;
    private final TermEvents termEvents;
    private final TermMapper termMapper;

    @Transactional
    @CacheEvict(cacheNames = {"activeTerm"}, allEntries = true)
    public Term update(Long id, TermRequestDTO request) {
        ValidationUtils.requireNonNull(id, "Term ID");
        ValidationUtils.requireNonNull(request, "Request body");
        Term entity = termRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Term not found"));
        if (request.sessionId() != null) {
            entity.setSession(entityResolver.resolveAcademicSession(request.sessionId()));
        }

        if (request.name() != null) {
            entity.setName(request.name());
        }
        if (request.isActive() != null) {
            entity.setIsActive(request.isActive());
        }
        if (request.allowTeacherSubmission() != null) {
            entity.setAllowTeacherSubmission(request.allowTeacherSubmission());
        }
        if (request.allowStudentView() != null) {
            entity.setAllowStudentView(request.allowStudentView());
        }
        if (request.isLocked() != null) {
            entity.setIsLocked(request.isLocked());
        }

        Term saved = termRepository.save(entity);
        termEvents.broadcastUpdated(termMapper.toResponse(saved));
        return saved;
    }
}

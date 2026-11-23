package com.app.meridian.term.services;

import com.app.meridian.academicSession.AcademicSession;
import com.app.meridian.exceptions.ConflictException;
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

@Service
@RequiredArgsConstructor
public class CreateTerm {

    private final EntityResolver entityResolver;
    private final TermRepository termRepository;
    private final TermEvents termEvents;
    private final TermMapper termMapper;

    @Transactional
    @CacheEvict(cacheNames = {"activeTerm"}, allEntries = true)
    public Term create(TermRequestDTO request) {
        ValidationUtils.requireNonNull(request, "Request body");
        ValidationUtils.requireNonNull(request.name(), "name");
        ValidationUtils.requireNonNull(request.sessionId(), "session");

        AcademicSession session = entityResolver.resolveAcademicSession(request.sessionId());

        if (termRepository.existsBySessionIdAndName(request.sessionId(), request.name())) {
            throw new ConflictException("Term already exists for that combination");
        }

        Term entity = new Term();
        entity.setName(request.name());
        entity.setIsActive(request.isActive() == null ? false : request.isActive());
        entity.setAllowTeacherSubmission(request.allowTeacherSubmission() == null ? true : request.allowTeacherSubmission());
        entity.setAllowStudentView(request.allowStudentView() == null ? false : request.allowStudentView());
        entity.setIsLocked(request.isLocked() == null ? false : request.isLocked());
        entity.setSession(session);

        Term saved = termRepository.save(entity);
        termEvents.broadcastCreated(termMapper.toResponse(saved));
        return saved;
    }
}

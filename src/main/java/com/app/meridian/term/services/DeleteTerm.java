package com.app.meridian.term.services;

import com.app.meridian.exceptions.NotFoundException;
import com.app.meridian.term.TermEvents;
import com.app.meridian.term.TermRepository;
import com.app.meridian.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteTerm {

    private final TermRepository termRepository;
    private final TermEvents termEvents;

    @Transactional
    @CacheEvict(cacheNames = {"activeTerm"}, allEntries = true)
    public void delete(Long id) {
        ValidationUtils.requireNonNull(id, "Term ID");
        if (!termRepository.existsById(id)) {
            throw new NotFoundException("Term not found");
        }
        termRepository.deleteById(id);
        termEvents.broadcastDeleted(id);
    }
}

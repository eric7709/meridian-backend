package com.app.meridian.term.services;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.meridian.cache.CacheNames;
import com.app.meridian.exceptions.BadRequestException;
import com.app.meridian.term.Term;
import com.app.meridian.term.TermRepository;

import lombok.RequiredArgsConstructor;

/**
 * Every teacher and student screen starts from the active term, so it is cached. Any create,
 * update or delete of a term evicts the entry (see CreateTerm/UpdateTerm/DeleteTerm).
 */
@Service
@RequiredArgsConstructor
public class GetActiveTerm {

    private final TermRepository termRepository;

    @Cacheable(cacheNames = CacheNames.ACTIVE_TERM, key = "'active'")
    @Transactional(readOnly = true)
    public Term get() {
        return termRepository.findByIsActiveTrue()
                .orElseThrow(() -> new BadRequestException("No term is currently active"));
    }
}

package com.app.meridian.term.services;

import com.app.meridian.exceptions.NotFoundException;
import com.app.meridian.term.Term;
import com.app.meridian.term.TermRepository;
import com.app.meridian.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetTermById {

    private final TermRepository termRepository;

    @Transactional(readOnly = true)
    public Term get(Long id) {
        ValidationUtils.requireNonNull(id, "Term ID");
        return termRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Term not found"));
    }
}

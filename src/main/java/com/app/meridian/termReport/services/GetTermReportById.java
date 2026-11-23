package com.app.meridian.termReport.services;

import com.app.meridian.exceptions.NotFoundException;
import com.app.meridian.termReport.TermReport;
import com.app.meridian.termReport.TermReportRepository;
import com.app.meridian.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetTermReportById {

    private final TermReportRepository termReportRepository;

    @Transactional(readOnly = true)
    public TermReport get(Long id) {
        ValidationUtils.requireNonNull(id, "TermReport ID");
        return termReportRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("TermReport not found"));
    }
}

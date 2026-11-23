package com.app.meridian.termReport.services;

import java.util.Comparator;
import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.meridian.cache.CacheNames;
import com.app.meridian.termReport.TermReport;
import com.app.meridian.termReport.TermReportRepository;

import lombok.RequiredArgsConstructor;

/**
 * Cacheable read of one student's term reports. It is a separate bean from GetMyReports on purpose:
 * a @Cacheable method called from inside the same class would bypass the proxy and never cache.
 */
@Service
@RequiredArgsConstructor
public class LoadStudentReports {

    private final TermReportRepository termReportRepository;

    @Cacheable(cacheNames = CacheNames.TERM_REPORTS_BY_STUDENT, key = "#studentId")
    @Transactional(readOnly = true)
    public List<TermReport> load(Long studentId) {
        return termReportRepository.findByStudentId(studentId).stream()
                .sorted(Comparator.comparing((TermReport report) -> report.getTerm().getSession().getLabel())
                        .thenComparing(report -> report.getTerm().getName()))
                .toList();
    }
}

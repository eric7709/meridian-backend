package com.app.meridian.termReport.services;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.meridian.exceptions.ForbiddenException;
import com.app.meridian.exceptions.NotFoundException;
import com.app.meridian.staff.services.GetCurrentStaff;
import com.app.meridian.termReport.TermReport;
import com.app.meridian.termReport.TermReportEvents;
import com.app.meridian.cache.CacheNames;
import com.app.meridian.termReport.TermReportRepository;
import com.app.meridian.termReport.utils.TermReportMapper;
import com.app.meridian.security.SecurityUtils;
import com.app.meridian.utils.ValidationUtils;

import lombok.RequiredArgsConstructor;
import com.app.meridian.term.Term;

/** Only the form teacher of the student's class (or an admin) may write the remark. */
@Service
@RequiredArgsConstructor
public class SetFormTeacherRemark {

    private final TermReportRepository termReportRepository;
    private final TermReportEvents termReportEvents;
    private final TermReportMapper termReportMapper;
    private final GetCurrentStaff getCurrentStaff;

    @Transactional
    @CacheEvict(cacheNames = CacheNames.TERM_REPORTS_BY_STUDENT, allEntries = true)
    public TermReport set(Long reportId, String remark) {
        ValidationUtils.requireNonNull(reportId, "Term report ID");
        ValidationUtils.requireNonBlank(remark, "Remark");
        TermReport report = termReportRepository.findById(reportId)
                .orElseThrow(() -> new NotFoundException("Term report not found"));
        if (!SecurityUtils.isAdmin()) {
            var staff = getCurrentStaff.get();
            var currentClass = report.getStudent().getCurrentClass();
            boolean isFormTeacher = currentClass != null && currentClass.getFormTeacher() != null
                    && currentClass.getFormTeacher().getId().equals(staff.getId());
            if (!isFormTeacher) {
                throw new ForbiddenException("Only the form teacher can write this remark");
            }
        }
        report.setFormTeacherRemark(remark.trim());
        report.setRemarkBy(getCurrentStaff.get());
        TermReport saved = termReportRepository.save(report);
        termReportEvents.broadcastUpdated(termReportMapper.toResponse(saved));
        return saved;
    }
}

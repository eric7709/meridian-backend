package com.app.meridian.termReport;

import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.meridian.common.PageResponse;
import com.app.meridian.termReport.dto.TermReportResponseDTO;
import com.app.meridian.termReport.dto.TermReportSearchCriteria;
import com.app.meridian.termReport.services.SearchTermReports;
import com.app.meridian.termReport.services.SetFormTeacherRemark;
import com.app.meridian.termReport.utils.TermReportMapper;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/meridian/term-reports")
public class TermReportController {

    private final SearchTermReports searchTermReports;
    private final SetFormTeacherRemark setFormTeacherRemark;
    private final TermReportMapper termReportMapper;

    @GetMapping
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN','IT_SUPPORT')")
    public PageResponse<TermReportResponseDTO> search(TermReportSearchCriteria criteria) {
        Page<TermReport> page = searchTermReports.search(criteria);
        return PageResponse.of(page, termReportMapper::toResponse);
    }

    @PutMapping("/{id}/remark")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN','IT_SUPPORT')")
    public TermReportResponseDTO remark(@PathVariable Long id, @RequestBody RemarkRequest request) {
        return termReportMapper.toResponse(setFormTeacherRemark.set(id, request.remark()));
    }

    public record RemarkRequest(String remark) {
    }
}

package com.app.meridian.auditLog;

import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.meridian.auditLog.dto.AuditLogResponseDTO;
import com.app.meridian.auditLog.dto.AuditLogSearchCriteria;
import com.app.meridian.auditLog.services.SearchAuditLogs;
import com.app.meridian.auditLog.utils.AuditLogMapper;
import com.app.meridian.common.PageResponse;

import lombok.RequiredArgsConstructor;

/** The full audit history, paged — /activity/recent remains the dashboard's short timeline. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/meridian/audit-logs")
@PreAuthorize("hasAnyRole('ADMIN','IT_SUPPORT')")
public class AuditLogController {

    private final SearchAuditLogs searchAuditLogs;
    private final AuditLogMapper auditLogMapper;

    @GetMapping
    public PageResponse<AuditLogResponseDTO> search(AuditLogSearchCriteria criteria) {
        Page<AuditLog> page = searchAuditLogs.search(criteria);
        return PageResponse.of(page, auditLogMapper::toResponse);
    }
}

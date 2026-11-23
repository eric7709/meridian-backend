package com.app.meridian.auditLog;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.meridian.auditLog.dto.AuditLogResponseDTO;
import com.app.meridian.auditLog.services.GetRecentActivity;
import com.app.meridian.auditLog.utils.AuditLogMapper;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/meridian/activity")
public class ActivityController {

    private final GetRecentActivity getRecentActivity;
    private final AuditLogMapper auditLogMapper;

    @GetMapping("/recent")
    @PreAuthorize("hasAnyRole('ADMIN','IT_SUPPORT')")
    public List<AuditLogResponseDTO> recent() {
        return getRecentActivity.recent().stream().map(auditLogMapper::toResponse).toList();
    }
}

package com.app.meridian.auditLog.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.meridian.auditLog.AuditLog;
import com.app.meridian.auditLog.AuditLogRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetRecentActivity {

    private final AuditLogRepository auditLogRepository;

    @Transactional(readOnly = true)
    public List<AuditLog> recent() {
        return auditLogRepository.findTop15ByOrderByCreatedAtDesc();
    }
}

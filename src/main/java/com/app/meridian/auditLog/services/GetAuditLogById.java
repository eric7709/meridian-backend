package com.app.meridian.auditLog.services;

import com.app.meridian.auditLog.AuditLog;
import com.app.meridian.auditLog.AuditLogRepository;
import com.app.meridian.exceptions.NotFoundException;
import com.app.meridian.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetAuditLogById {

    private final AuditLogRepository auditLogRepository;

    @Transactional(readOnly = true)
    public AuditLog get(Long id) {
        ValidationUtils.requireNonNull(id, "AuditLog ID");
        return auditLogRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("AuditLog not found"));
    }
}

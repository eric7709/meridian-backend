package com.app.meridian.auditLog.services;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.app.meridian.user.UserRepository;
import com.app.meridian.security.SecurityUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.app.meridian.auditLog.AuditLog;
import com.app.meridian.auditLog.AuditLogRepository;

/**
 * Writes the admin "Recent Activity" timeline. Called from the services that change state in a
 * way an auditor would care about: approvals, enrolments, payments, settings changes.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuditTrail {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    @Transactional
    public void record(String action, String entityType, Long entityId, String summary) {
        try {
            AuditLog log = new AuditLog();
            log.setAction(action);
            log.setEntityType(entityType);
            log.setEntityId(entityId);
            log.setSummary(summary);
            try {
                userRepository.findById(SecurityUtils.userId()).ifPresent(log::setActor);
            } catch (RuntimeException anonymous) {
                log.setActor(null);
            }
            auditLogRepository.save(log);
        } catch (RuntimeException ex) {
            // Auditing must never break the business operation it is describing.
            AuditTrail.log.warn("Could not write audit entry {} {}: {}", action, entityType, ex.getMessage());
        }
    }
}

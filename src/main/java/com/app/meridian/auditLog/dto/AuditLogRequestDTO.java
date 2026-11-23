package com.app.meridian.auditLog.dto;

/** Payload accepted by create and update. Update only applies non-null fields. */
public record AuditLogRequestDTO(String action, String entityType, Long entityId, String summary, Long actorId) {
}

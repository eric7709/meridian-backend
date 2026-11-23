package com.app.meridian.auditLog;

import com.app.meridian.auditLog.dto.AuditLogResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/** Pushes created / updated / deleted events to STOMP subscribers. */
@Component
@RequiredArgsConstructor
public class AuditLogEvents {

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcastCreated(AuditLogResponseDTO payload) {
        if (payload == null) {
            return;
        }
        messagingTemplate.convertAndSend("/topic/audit-logs/created", payload);
    }

    public void broadcastUpdated(AuditLogResponseDTO payload) {
        if (payload == null) {
            return;
        }
        messagingTemplate.convertAndSend("/topic/audit-logs/updated", payload);
    }

    public void broadcastDeleted(Long id) {
        if (id == null) {
            return;
        }
        messagingTemplate.convertAndSend("/topic/audit-logs/deleted", id);
    }
}


package com.app.meridian.department;

import com.app.meridian.department.dto.DepartmentResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/** Pushes created / updated / deleted events to STOMP subscribers. */
@Component
@RequiredArgsConstructor
public class DepartmentEvents {

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcastCreated(DepartmentResponseDTO payload) {
        if (payload == null) {
            return;
        }
        messagingTemplate.convertAndSend("/topic/departments/created", payload);
    }

    public void broadcastUpdated(DepartmentResponseDTO payload) {
        if (payload == null) {
            return;
        }
        messagingTemplate.convertAndSend("/topic/departments/updated", payload);
    }

    public void broadcastDeleted(Long id) {
        if (id == null) {
            return;
        }
        messagingTemplate.convertAndSend("/topic/departments/deleted", id);
    }
}


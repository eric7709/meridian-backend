package com.app.meridian.staff;

import com.app.meridian.staff.dto.StaffResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/** Pushes created / updated / deleted events to STOMP subscribers. */
@Component
@RequiredArgsConstructor
public class StaffEvents {

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcastCreated(StaffResponseDTO payload) {
        if (payload == null) {
            return;
        }
        messagingTemplate.convertAndSend("/topic/staff/created", payload);
    }

    public void broadcastUpdated(StaffResponseDTO payload) {
        if (payload == null) {
            return;
        }
        messagingTemplate.convertAndSend("/topic/staff/updated", payload);
    }

    public void broadcastDeleted(Long id) {
        if (id == null) {
            return;
        }
        messagingTemplate.convertAndSend("/topic/staff/deleted", id);
    }
}


package com.app.meridian.academicSession;

import com.app.meridian.academicSession.dto.AcademicSessionResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/** Pushes created / updated / deleted events to STOMP subscribers. */
@Component
@RequiredArgsConstructor
public class AcademicSessionEvents {

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcastCreated(AcademicSessionResponseDTO payload) {
        if (payload == null) {
            return;
        }
        messagingTemplate.convertAndSend("/topic/sessions/created", payload);
    }

    public void broadcastUpdated(AcademicSessionResponseDTO payload) {
        if (payload == null) {
            return;
        }
        messagingTemplate.convertAndSend("/topic/sessions/updated", payload);
    }

    public void broadcastDeleted(Long id) {
        if (id == null) {
            return;
        }
        messagingTemplate.convertAndSend("/topic/sessions/deleted", id);
    }
}


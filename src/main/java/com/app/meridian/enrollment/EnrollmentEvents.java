package com.app.meridian.enrollment;

import com.app.meridian.enrollment.dto.EnrollmentResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/** Pushes created / updated / deleted events to STOMP subscribers. */
@Component
@RequiredArgsConstructor
public class EnrollmentEvents {

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcastCreated(EnrollmentResponseDTO payload) {
        if (payload == null) {
            return;
        }
        messagingTemplate.convertAndSend("/topic/enrollments/created", payload);
    }

    public void broadcastUpdated(EnrollmentResponseDTO payload) {
        if (payload == null) {
            return;
        }
        messagingTemplate.convertAndSend("/topic/enrollments/updated", payload);
    }

    public void broadcastDeleted(Long id) {
        if (id == null) {
            return;
        }
        messagingTemplate.convertAndSend("/topic/enrollments/deleted", id);
    }
}


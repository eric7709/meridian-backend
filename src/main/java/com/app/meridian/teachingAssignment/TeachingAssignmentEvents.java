package com.app.meridian.teachingAssignment;

import com.app.meridian.teachingAssignment.dto.TeachingAssignmentResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/** Pushes created / updated / deleted events to STOMP subscribers. */
@Component
@RequiredArgsConstructor
public class TeachingAssignmentEvents {

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcastCreated(TeachingAssignmentResponseDTO payload) {
        if (payload == null) {
            return;
        }
        messagingTemplate.convertAndSend("/topic/assignments/created", payload);
    }

    public void broadcastUpdated(TeachingAssignmentResponseDTO payload) {
        if (payload == null) {
            return;
        }
        messagingTemplate.convertAndSend("/topic/assignments/updated", payload);
    }

    public void broadcastDeleted(Long id) {
        if (id == null) {
            return;
        }
        messagingTemplate.convertAndSend("/topic/assignments/deleted", id);
    }
}


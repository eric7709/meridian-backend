package com.app.meridian.subject;

import com.app.meridian.subject.dto.SubjectResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/** Pushes created / updated / deleted events to STOMP subscribers. */
@Component
@RequiredArgsConstructor
public class SubjectEvents {

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcastCreated(SubjectResponseDTO payload) {
        if (payload == null) {
            return;
        }
        messagingTemplate.convertAndSend("/topic/subjects/created", payload);
    }

    public void broadcastUpdated(SubjectResponseDTO payload) {
        if (payload == null) {
            return;
        }
        messagingTemplate.convertAndSend("/topic/subjects/updated", payload);
    }

    public void broadcastDeleted(Long id) {
        if (id == null) {
            return;
        }
        messagingTemplate.convertAndSend("/topic/subjects/deleted", id);
    }
}


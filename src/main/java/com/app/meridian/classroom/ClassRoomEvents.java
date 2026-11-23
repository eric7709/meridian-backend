package com.app.meridian.classroom;

import com.app.meridian.classroom.dto.ClassRoomResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/** Pushes created / updated / deleted events to STOMP subscribers. */
@Component
@RequiredArgsConstructor
public class ClassRoomEvents {

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcastCreated(ClassRoomResponseDTO payload) {
        if (payload == null) {
            return;
        }
        messagingTemplate.convertAndSend("/topic/classes/created", payload);
    }

    public void broadcastUpdated(ClassRoomResponseDTO payload) {
        if (payload == null) {
            return;
        }
        messagingTemplate.convertAndSend("/topic/classes/updated", payload);
    }

    public void broadcastDeleted(Long id) {
        if (id == null) {
            return;
        }
        messagingTemplate.convertAndSend("/topic/classes/deleted", id);
    }
}


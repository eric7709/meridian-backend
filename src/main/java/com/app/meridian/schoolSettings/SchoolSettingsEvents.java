package com.app.meridian.schoolSettings;

import com.app.meridian.schoolSettings.dto.SchoolSettingsResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/** Pushes created / updated / deleted events to STOMP subscribers. */
@Component
@RequiredArgsConstructor
public class SchoolSettingsEvents {

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcastCreated(SchoolSettingsResponseDTO payload) {
        if (payload == null) {
            return;
        }
        messagingTemplate.convertAndSend("/topic/settings/created", payload);
    }

    public void broadcastUpdated(SchoolSettingsResponseDTO payload) {
        if (payload == null) {
            return;
        }
        messagingTemplate.convertAndSend("/topic/settings/updated", payload);
    }

    public void broadcastDeleted(Long id) {
        if (id == null) {
            return;
        }
        messagingTemplate.convertAndSend("/topic/settings/deleted", id);
    }
}


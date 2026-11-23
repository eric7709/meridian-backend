package com.app.meridian.announcement;

import com.app.meridian.announcement.dto.AnnouncementResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/** Pushes created / updated / deleted events to STOMP subscribers. */
@Component
@RequiredArgsConstructor
public class AnnouncementEvents {

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcastCreated(AnnouncementResponseDTO payload) {
        if (payload == null) {
            return;
        }
        messagingTemplate.convertAndSend("/topic/announcements/created", payload);
    }

    public void broadcastUpdated(AnnouncementResponseDTO payload) {
        if (payload == null) {
            return;
        }
        messagingTemplate.convertAndSend("/topic/announcements/updated", payload);
    }

    public void broadcastDeleted(Long id) {
        if (id == null) {
            return;
        }
        messagingTemplate.convertAndSend("/topic/announcements/deleted", id);
    }
}


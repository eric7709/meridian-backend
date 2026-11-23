package com.app.meridian.timetableSlot;

import com.app.meridian.timetableSlot.dto.TimetableSlotResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/** Pushes created / updated / deleted events to STOMP subscribers. */
@Component
@RequiredArgsConstructor
public class TimetableSlotEvents {

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcastCreated(TimetableSlotResponseDTO payload) {
        if (payload == null) {
            return;
        }
        messagingTemplate.convertAndSend("/topic/timetable/created", payload);
    }

    public void broadcastUpdated(TimetableSlotResponseDTO payload) {
        if (payload == null) {
            return;
        }
        messagingTemplate.convertAndSend("/topic/timetable/updated", payload);
    }

    public void broadcastDeleted(Long id) {
        if (id == null) {
            return;
        }
        messagingTemplate.convertAndSend("/topic/timetable/deleted", id);
    }
}


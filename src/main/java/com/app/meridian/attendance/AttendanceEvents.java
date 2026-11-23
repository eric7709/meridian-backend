package com.app.meridian.attendance;

import com.app.meridian.attendance.dto.AttendanceResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/** Pushes created / updated / deleted events to STOMP subscribers. */
@Component
@RequiredArgsConstructor
public class AttendanceEvents {

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcastCreated(AttendanceResponseDTO payload) {
        if (payload == null) {
            return;
        }
        messagingTemplate.convertAndSend("/topic/attendance/created", payload);
    }

    public void broadcastUpdated(AttendanceResponseDTO payload) {
        if (payload == null) {
            return;
        }
        messagingTemplate.convertAndSend("/topic/attendance/updated", payload);
    }

    public void broadcastDeleted(Long id) {
        if (id == null) {
            return;
        }
        messagingTemplate.convertAndSend("/topic/attendance/deleted", id);
    }
}


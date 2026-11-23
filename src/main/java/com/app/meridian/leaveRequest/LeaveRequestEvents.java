package com.app.meridian.leaveRequest;

import com.app.meridian.leaveRequest.dto.LeaveRequestResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/** Pushes created / updated / deleted events to STOMP subscribers. */
@Component
@RequiredArgsConstructor
public class LeaveRequestEvents {

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcastCreated(LeaveRequestResponseDTO payload) {
        if (payload == null) {
            return;
        }
        messagingTemplate.convertAndSend("/topic/leave/created", payload);
    }

    public void broadcastUpdated(LeaveRequestResponseDTO payload) {
        if (payload == null) {
            return;
        }
        messagingTemplate.convertAndSend("/topic/leave/updated", payload);
    }

    public void broadcastDeleted(Long id) {
        if (id == null) {
            return;
        }
        messagingTemplate.convertAndSend("/topic/leave/deleted", id);
    }
}


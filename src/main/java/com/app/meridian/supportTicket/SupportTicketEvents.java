package com.app.meridian.supportTicket;

import com.app.meridian.supportTicket.dto.SupportTicketResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/** Pushes created / updated / deleted events to STOMP subscribers. */
@Component
@RequiredArgsConstructor
public class SupportTicketEvents {

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcastCreated(SupportTicketResponseDTO payload) {
        if (payload == null) {
            return;
        }
        messagingTemplate.convertAndSend("/topic/support/created", payload);
    }

    public void broadcastUpdated(SupportTicketResponseDTO payload) {
        if (payload == null) {
            return;
        }
        messagingTemplate.convertAndSend("/topic/support/updated", payload);
    }

    public void broadcastDeleted(Long id) {
        if (id == null) {
            return;
        }
        messagingTemplate.convertAndSend("/topic/support/deleted", id);
    }
}


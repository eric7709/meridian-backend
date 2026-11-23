package com.app.meridian.termReport;

import com.app.meridian.termReport.dto.TermReportResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/** Pushes created / updated / deleted events to STOMP subscribers. */
@Component
@RequiredArgsConstructor
public class TermReportEvents {

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcastCreated(TermReportResponseDTO payload) {
        if (payload == null) {
            return;
        }
        messagingTemplate.convertAndSend("/topic/term-reports/created", payload);
    }

    public void broadcastUpdated(TermReportResponseDTO payload) {
        if (payload == null) {
            return;
        }
        messagingTemplate.convertAndSend("/topic/term-reports/updated", payload);
    }

    public void broadcastDeleted(Long id) {
        if (id == null) {
            return;
        }
        messagingTemplate.convertAndSend("/topic/term-reports/deleted", id);
    }
}


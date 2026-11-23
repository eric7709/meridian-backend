package com.app.meridian.resultSheet;

import com.app.meridian.resultSheet.dto.ResultSheetResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/** Pushes created / updated / deleted events to STOMP subscribers. */
@Component
@RequiredArgsConstructor
public class ResultSheetEvents {

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcastCreated(ResultSheetResponseDTO payload) {
        if (payload == null) {
            return;
        }
        messagingTemplate.convertAndSend("/topic/result-sheets/created", payload);
    }

    public void broadcastUpdated(ResultSheetResponseDTO payload) {
        if (payload == null) {
            return;
        }
        messagingTemplate.convertAndSend("/topic/result-sheets/updated", payload);
    }

    public void broadcastDeleted(Long id) {
        if (id == null) {
            return;
        }
        messagingTemplate.convertAndSend("/topic/result-sheets/deleted", id);
    }
}


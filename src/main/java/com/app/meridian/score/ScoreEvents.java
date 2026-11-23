package com.app.meridian.score;

import com.app.meridian.score.dto.ScoreResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/** Pushes created / updated / deleted events to STOMP subscribers. */
@Component
@RequiredArgsConstructor
public class ScoreEvents {

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcastCreated(ScoreResponseDTO payload) {
        if (payload == null) {
            return;
        }
        messagingTemplate.convertAndSend("/topic/scores/created", payload);
    }

    public void broadcastUpdated(ScoreResponseDTO payload) {
        if (payload == null) {
            return;
        }
        messagingTemplate.convertAndSend("/topic/scores/updated", payload);
    }

    public void broadcastDeleted(Long id) {
        if (id == null) {
            return;
        }
        messagingTemplate.convertAndSend("/topic/scores/deleted", id);
    }
}


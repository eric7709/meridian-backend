package com.app.meridian.gradeScale;

import com.app.meridian.gradeScale.dto.GradeScaleResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/** Pushes created / updated / deleted events to STOMP subscribers. */
@Component
@RequiredArgsConstructor
public class GradeScaleEvents {

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcastCreated(GradeScaleResponseDTO payload) {
        if (payload == null) {
            return;
        }
        messagingTemplate.convertAndSend("/topic/grading-scale/created", payload);
    }

    public void broadcastUpdated(GradeScaleResponseDTO payload) {
        if (payload == null) {
            return;
        }
        messagingTemplate.convertAndSend("/topic/grading-scale/updated", payload);
    }

    public void broadcastDeleted(Long id) {
        if (id == null) {
            return;
        }
        messagingTemplate.convertAndSend("/topic/grading-scale/deleted", id);
    }
}


package com.app.meridian.feePayment;

import com.app.meridian.feePayment.dto.FeePaymentResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/** Pushes created / updated / deleted events to STOMP subscribers. */
@Component
@RequiredArgsConstructor
public class FeePaymentEvents {

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcastCreated(FeePaymentResponseDTO payload) {
        if (payload == null) {
            return;
        }
        messagingTemplate.convertAndSend("/topic/fees/created", payload);
    }

    public void broadcastUpdated(FeePaymentResponseDTO payload) {
        if (payload == null) {
            return;
        }
        messagingTemplate.convertAndSend("/topic/fees/updated", payload);
    }

    public void broadcastDeleted(Long id) {
        if (id == null) {
            return;
        }
        messagingTemplate.convertAndSend("/topic/fees/deleted", id);
    }
}


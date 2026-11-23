package com.app.meridian.supportTicket.dto;

import com.app.meridian.supportTicket.enums.TicketStatus;
/** Payload accepted by create and update. Update only applies non-null fields. */
public record SupportTicketRequestDTO(String issueType, String message, TicketStatus status, Long userId) {
}

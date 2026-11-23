package com.app.meridian.supportTicket.dto;

import java.time.LocalDateTime;
import com.app.meridian.supportTicket.enums.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SupportTicketResponseDTO {

    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String issueType;
    private String message;
    private TicketStatus status;
    private Long userId;
    private String userName;
    private String userEmail;
}

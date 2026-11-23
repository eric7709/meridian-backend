package com.app.meridian.supportTicket.services;

import com.app.meridian.exceptions.ConflictException;
import com.app.meridian.exceptions.NotFoundException;
import com.app.meridian.supportTicket.SupportTicket;
import com.app.meridian.supportTicket.SupportTicketEvents;
import com.app.meridian.supportTicket.SupportTicketRepository;
import com.app.meridian.supportTicket.dto.SupportTicketRequestDTO;
import com.app.meridian.supportTicket.enums.TicketStatus;
import com.app.meridian.supportTicket.utils.SupportTicketMapper;
import com.app.meridian.user.User;
import com.app.meridian.utils.EntityResolver;
import com.app.meridian.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Applies only the fields present in the request; everything else is left alone. */
@Service
@RequiredArgsConstructor
public class UpdateSupportTicket {

    private final EntityResolver entityResolver;
    private final SupportTicketRepository supportTicketRepository;
    private final SupportTicketEvents supportTicketEvents;
    private final SupportTicketMapper supportTicketMapper;

    @Transactional
    public SupportTicket update(Long id, SupportTicketRequestDTO request) {
        ValidationUtils.requireNonNull(id, "SupportTicket ID");
        ValidationUtils.requireNonNull(request, "Request body");
        SupportTicket entity = supportTicketRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("SupportTicket not found"));
        if (request.userId() != null) {
            entity.setUser(entityResolver.resolveUser(request.userId()));
        }

        if (request.issueType() != null && !request.issueType().isBlank()) {
            entity.setIssueType(request.issueType().trim());
        }
        if (request.message() != null && !request.message().isBlank()) {
            entity.setMessage(request.message().trim());
        }
        if (request.status() != null) {
            entity.setStatus(request.status());
        }

        SupportTicket saved = supportTicketRepository.save(entity);
        supportTicketEvents.broadcastUpdated(supportTicketMapper.toResponse(saved));
        return saved;
    }
}

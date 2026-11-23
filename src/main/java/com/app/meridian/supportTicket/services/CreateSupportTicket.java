package com.app.meridian.supportTicket.services;

import com.app.meridian.exceptions.ConflictException;
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

@Service
@RequiredArgsConstructor
public class CreateSupportTicket {

    private final EntityResolver entityResolver;
    private final SupportTicketRepository supportTicketRepository;
    private final SupportTicketEvents supportTicketEvents;
    private final SupportTicketMapper supportTicketMapper;

    @Transactional
    public SupportTicket create(SupportTicketRequestDTO request) {
        ValidationUtils.requireNonNull(request, "Request body");
        ValidationUtils.requireNonBlank(request.issueType(), "issueType");
        ValidationUtils.requireNonBlank(request.message(), "message");
        ValidationUtils.requireNonNull(request.userId(), "user");

        User user = entityResolver.resolveUser(request.userId());


        SupportTicket entity = new SupportTicket();
        entity.setIssueType(request.issueType().trim());
        entity.setMessage(request.message().trim());
        entity.setStatus(request.status() == null ? TicketStatus.OPEN : request.status());
        entity.setUser(user);

        SupportTicket saved = supportTicketRepository.save(entity);
        supportTicketEvents.broadcastCreated(supportTicketMapper.toResponse(saved));
        return saved;
    }
}

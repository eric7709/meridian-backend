package com.app.meridian.supportTicket.services;

import com.app.meridian.exceptions.NotFoundException;
import com.app.meridian.supportTicket.SupportTicketEvents;
import com.app.meridian.supportTicket.SupportTicketRepository;
import com.app.meridian.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteSupportTicket {

    private final SupportTicketRepository supportTicketRepository;
    private final SupportTicketEvents supportTicketEvents;

    @Transactional
    public void delete(Long id) {
        ValidationUtils.requireNonNull(id, "SupportTicket ID");
        if (!supportTicketRepository.existsById(id)) {
            throw new NotFoundException("SupportTicket not found");
        }
        supportTicketRepository.deleteById(id);
        supportTicketEvents.broadcastDeleted(id);
    }
}

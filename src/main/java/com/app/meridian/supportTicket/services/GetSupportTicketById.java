package com.app.meridian.supportTicket.services;

import com.app.meridian.exceptions.NotFoundException;
import com.app.meridian.supportTicket.SupportTicket;
import com.app.meridian.supportTicket.SupportTicketRepository;
import com.app.meridian.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetSupportTicketById {

    private final SupportTicketRepository supportTicketRepository;

    @Transactional(readOnly = true)
    public SupportTicket get(Long id) {
        ValidationUtils.requireNonNull(id, "SupportTicket ID");
        return supportTicketRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("SupportTicket not found"));
    }
}

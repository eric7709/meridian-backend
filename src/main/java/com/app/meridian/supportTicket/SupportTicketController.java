package com.app.meridian.supportTicket;

import com.app.meridian.common.PageResponse;
import com.app.meridian.supportTicket.dto.SupportTicketRequestDTO;
import com.app.meridian.supportTicket.dto.SupportTicketResponseDTO;
import com.app.meridian.supportTicket.dto.SupportTicketSearchCriteria;
import com.app.meridian.supportTicket.services.CreateSupportTicket;
import com.app.meridian.supportTicket.services.DeleteSupportTicket;
import com.app.meridian.supportTicket.services.GetSupportTicketById;
import com.app.meridian.supportTicket.services.SearchSupportTickets;
import com.app.meridian.supportTicket.services.UpdateSupportTicket;
import com.app.meridian.supportTicket.utils.SupportTicketMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/meridian/support")
public class SupportTicketController {

    private final CreateSupportTicket createSupportTicket;
    private final UpdateSupportTicket updateSupportTicket;
    private final DeleteSupportTicket deleteSupportTicket;
    private final GetSupportTicketById getSupportTicketById;
    private final SearchSupportTickets searchSupportTickets;
    private final SupportTicketMapper supportTicketMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SupportTicketResponseDTO create(@RequestBody SupportTicketRequestDTO request) {
        return supportTicketMapper.toResponse(createSupportTicket.create(request));
    }

    @GetMapping
    public PageResponse<SupportTicketResponseDTO> search(SupportTicketSearchCriteria criteria) {
        Page<SupportTicket> page = searchSupportTickets.search(criteria);
        return PageResponse.of(page, supportTicketMapper::toResponse);
    }

    @GetMapping("/{id}")
    public SupportTicketResponseDTO get(@PathVariable Long id) {
        return supportTicketMapper.toResponse(getSupportTicketById.get(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','IT_SUPPORT')")
    public SupportTicketResponseDTO update(@PathVariable Long id, @RequestBody SupportTicketRequestDTO request) {
        return supportTicketMapper.toResponse(updateSupportTicket.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        deleteSupportTicket.delete(id);
    }
}

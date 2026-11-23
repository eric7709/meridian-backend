package com.app.meridian.supportTicket;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import com.app.meridian.supportTicket.enums.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long>, JpaSpecificationExecutor<SupportTicket> {

    long countByStatus(TicketStatus status);

}

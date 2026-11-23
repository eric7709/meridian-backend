package com.app.meridian.supportTicket.utils;

import java.util.List;
import com.app.meridian.supportTicket.SupportTicket;
import com.app.meridian.supportTicket.dto.SupportTicketResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SupportTicketMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userName", source = "user.fullName")
    @Mapping(target = "userEmail", source = "user.email")
    SupportTicketResponseDTO toResponse(SupportTicket entity);

    List<SupportTicketResponseDTO> mapAllToResponse(List<SupportTicket> entities);
}

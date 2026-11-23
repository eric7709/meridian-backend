package com.app.meridian.auditLog.utils;

import java.util.List;
import com.app.meridian.auditLog.AuditLog;
import com.app.meridian.auditLog.dto.AuditLogResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuditLogMapper {

    @Mapping(target = "actorId", source = "actor.id")
    @Mapping(target = "actorName", source = "actor.fullName")
    AuditLogResponseDTO toResponse(AuditLog entity);

    List<AuditLogResponseDTO> mapAllToResponse(List<AuditLog> entities);
}

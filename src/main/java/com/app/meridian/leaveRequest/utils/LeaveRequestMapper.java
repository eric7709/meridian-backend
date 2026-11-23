package com.app.meridian.leaveRequest.utils;

import java.util.List;
import com.app.meridian.leaveRequest.LeaveRequest;
import com.app.meridian.leaveRequest.dto.LeaveRequestResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LeaveRequestMapper {

    @Mapping(target = "staffId", source = "staff.id")
    @Mapping(target = "staffName", source = "staff.user.fullName")
    @Mapping(target = "staffNo", source = "staff.staffNo")
    LeaveRequestResponseDTO toResponse(LeaveRequest entity);

    List<LeaveRequestResponseDTO> mapAllToResponse(List<LeaveRequest> entities);
}

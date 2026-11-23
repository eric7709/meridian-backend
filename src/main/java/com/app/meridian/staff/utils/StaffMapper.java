package com.app.meridian.staff.utils;

import java.util.List;
import com.app.meridian.staff.Staff;
import com.app.meridian.staff.dto.StaffResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StaffMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "departmentId", source = "department.id")
    @Mapping(target = "userFullName", source = "user.fullName")
    @Mapping(target = "departmentName", source = "department.name")
    @Mapping(target = "userEmail", source = "user.email")
    StaffResponseDTO toResponse(Staff entity);

    List<StaffResponseDTO> mapAllToResponse(List<Staff> entities);
}

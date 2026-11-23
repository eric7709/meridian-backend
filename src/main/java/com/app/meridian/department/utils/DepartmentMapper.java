package com.app.meridian.department.utils;

import java.util.List;
import com.app.meridian.department.Department;
import com.app.meridian.department.dto.DepartmentResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DepartmentMapper {

    @Mapping(target = "headStaffId", source = "headStaff.id")
    @Mapping(target = "headStaffName", source = "headStaff.user.fullName")
    DepartmentResponseDTO toResponse(Department entity);

    List<DepartmentResponseDTO> mapAllToResponse(List<Department> entities);
}

package com.app.meridian.subject.utils;

import java.util.List;
import com.app.meridian.subject.Subject;
import com.app.meridian.subject.dto.SubjectResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SubjectMapper {

    @Mapping(target = "departmentId", source = "department.id")
    SubjectResponseDTO toResponse(Subject entity);

    List<SubjectResponseDTO> mapAllToResponse(List<Subject> entities);
}

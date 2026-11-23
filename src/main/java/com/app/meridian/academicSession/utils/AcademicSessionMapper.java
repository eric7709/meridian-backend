package com.app.meridian.academicSession.utils;

import java.util.List;
import com.app.meridian.academicSession.AcademicSession;
import com.app.meridian.academicSession.dto.AcademicSessionResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AcademicSessionMapper {

    AcademicSessionResponseDTO toResponse(AcademicSession entity);

    List<AcademicSessionResponseDTO> mapAllToResponse(List<AcademicSession> entities);
}

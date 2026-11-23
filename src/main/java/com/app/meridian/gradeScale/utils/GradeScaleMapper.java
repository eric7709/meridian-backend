package com.app.meridian.gradeScale.utils;

import java.util.List;
import com.app.meridian.gradeScale.GradeScale;
import com.app.meridian.gradeScale.dto.GradeScaleResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GradeScaleMapper {

    GradeScaleResponseDTO toResponse(GradeScale entity);

    List<GradeScaleResponseDTO> mapAllToResponse(List<GradeScale> entities);
}

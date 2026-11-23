package com.app.meridian.schoolSettings.utils;

import java.util.List;
import com.app.meridian.schoolSettings.SchoolSettings;
import com.app.meridian.schoolSettings.dto.SchoolSettingsResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SchoolSettingsMapper {

    @Mapping(target = "currentTermId", source = "currentTerm.id")
    @Mapping(target = "currentTermName", source = "currentTerm.name")
    @Mapping(target = "currentSessionLabel", source = "currentTerm.session.label")
    SchoolSettingsResponseDTO toResponse(SchoolSettings entity);

    List<SchoolSettingsResponseDTO> mapAllToResponse(List<SchoolSettings> entities);
}

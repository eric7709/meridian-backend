package com.app.meridian.announcement.utils;

import java.util.List;
import com.app.meridian.announcement.Announcement;
import com.app.meridian.announcement.dto.AnnouncementResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AnnouncementMapper {

    @Mapping(target = "createdById", source = "createdBy.id")
    @Mapping(target = "createdByName", source = "createdBy.fullName")
    AnnouncementResponseDTO toResponse(Announcement entity);

    List<AnnouncementResponseDTO> mapAllToResponse(List<Announcement> entities);
}

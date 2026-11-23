package com.app.meridian.classroom.utils;

import java.util.List;
import com.app.meridian.classroom.ClassRoom;
import com.app.meridian.classroom.dto.ClassRoomResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ClassRoomMapper {

    @Mapping(target = "formTeacherId", source = "formTeacher.id")
    @Mapping(target = "sessionId", source = "session.id")
    @Mapping(target = "sessionLabel", source = "session.label")
    @Mapping(target = "formTeacherName", source = "formTeacher.user.fullName")
    ClassRoomResponseDTO toResponse(ClassRoom entity);

    List<ClassRoomResponseDTO> mapAllToResponse(List<ClassRoom> entities);
}

package com.app.meridian.timetableSlot.utils;

import java.util.List;
import com.app.meridian.timetableSlot.TimetableSlot;
import com.app.meridian.timetableSlot.dto.TimetableSlotResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TimetableSlotMapper {

    @Mapping(target = "assignmentId", source = "assignment.id")
    @Mapping(target = "termId", source = "term.id")
    @Mapping(target = "subjectName", source = "assignment.subject.name")
    @Mapping(target = "className", source = "assignment.classRoom.name")
    @Mapping(target = "teacherName", source = "assignment.teacher.user.fullName")
    TimetableSlotResponseDTO toResponse(TimetableSlot entity);

    List<TimetableSlotResponseDTO> mapAllToResponse(List<TimetableSlot> entities);
}

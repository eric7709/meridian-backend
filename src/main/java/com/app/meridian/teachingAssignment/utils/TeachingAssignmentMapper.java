package com.app.meridian.teachingAssignment.utils;

import java.util.List;
import com.app.meridian.teachingAssignment.TeachingAssignment;
import com.app.meridian.teachingAssignment.dto.TeachingAssignmentResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TeachingAssignmentMapper {

    @Mapping(target = "teacherId", source = "teacher.id")
    @Mapping(target = "subjectId", source = "subject.id")
    @Mapping(target = "classId", source = "classRoom.id")
    @Mapping(target = "sessionId", source = "session.id")
    @Mapping(target = "teacherName", source = "teacher.user.fullName")
    @Mapping(target = "subjectName", source = "subject.name")
    @Mapping(target = "subjectCode", source = "subject.code")
    @Mapping(target = "className", source = "classRoom.name")
    TeachingAssignmentResponseDTO toResponse(TeachingAssignment entity);

    List<TeachingAssignmentResponseDTO> mapAllToResponse(List<TeachingAssignment> entities);
}

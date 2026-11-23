package com.app.meridian.enrollment.utils;

import java.util.List;
import com.app.meridian.enrollment.Enrollment;
import com.app.meridian.enrollment.dto.EnrollmentResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EnrollmentMapper {

    @Mapping(target = "studentId", source = "student.id")
    @Mapping(target = "classId", source = "classRoom.id")
    @Mapping(target = "sessionId", source = "session.id")
    @Mapping(target = "classRoomName", source = "classRoom.name")
    @Mapping(target = "sessionLabel", source = "session.label")
    @Mapping(target = "studentName", source = "student.user.fullName")
    @Mapping(target = "studentNo", source = "student.studentNo")
    @Mapping(target = "className", source = "classRoom.name")
    EnrollmentResponseDTO toResponse(Enrollment entity);

    List<EnrollmentResponseDTO> mapAllToResponse(List<Enrollment> entities);
}

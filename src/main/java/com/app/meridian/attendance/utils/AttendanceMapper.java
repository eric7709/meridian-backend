package com.app.meridian.attendance.utils;

import java.util.List;
import com.app.meridian.attendance.Attendance;
import com.app.meridian.attendance.dto.AttendanceResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AttendanceMapper {

    @Mapping(target = "studentId", source = "student.id")
    @Mapping(target = "termId", source = "term.id")
    @Mapping(target = "studentName", source = "student.user.fullName")
    @Mapping(target = "studentNo", source = "student.studentNo")
    AttendanceResponseDTO toResponse(Attendance entity);

    List<AttendanceResponseDTO> mapAllToResponse(List<Attendance> entities);
}

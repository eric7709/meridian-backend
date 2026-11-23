package com.app.meridian.student.utils;

import java.util.List;
import com.app.meridian.student.Student;
import com.app.meridian.student.dto.StudentResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StudentMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "currentClassId", source = "currentClass.id")
    @Mapping(target = "userFullName", source = "user.fullName")
    @Mapping(target = "currentClassName", source = "currentClass.name")
    @Mapping(target = "userEmail", source = "user.email")
    StudentResponseDTO toResponse(Student entity);

    List<StudentResponseDTO> mapAllToResponse(List<Student> entities);
}

package com.app.meridian.resultSheet.utils;

import java.util.List;
import com.app.meridian.resultSheet.ResultSheet;
import com.app.meridian.resultSheet.dto.ResultSheetResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ResultSheetMapper {

    @Mapping(target = "assignmentId", source = "assignment.id")
    @Mapping(target = "termId", source = "term.id")
    @Mapping(target = "submittedById", source = "submittedBy.id")
    @Mapping(target = "reviewedById", source = "reviewedBy.id")
    @Mapping(target = "subjectName", source = "assignment.subject.name")
    @Mapping(target = "className", source = "assignment.classRoom.name")
    @Mapping(target = "teacherName", source = "assignment.teacher.user.fullName")
    @Mapping(target = "termName", source = "term.name")
    @Mapping(target = "sessionLabel", source = "term.session.label")
    @Mapping(target = "submittedByName", source = "submittedBy.user.fullName")
    @Mapping(target = "reviewedByName", source = "reviewedBy.user.fullName")
    ResultSheetResponseDTO toResponse(ResultSheet entity);

    List<ResultSheetResponseDTO> mapAllToResponse(List<ResultSheet> entities);
}

package com.app.meridian.termReport.utils;

import java.util.List;
import com.app.meridian.termReport.TermReport;
import com.app.meridian.termReport.dto.TermReportResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TermReportMapper {

    @Mapping(target = "studentId", source = "student.id")
    @Mapping(target = "termId", source = "term.id")
    @Mapping(target = "remarkById", source = "remarkBy.id")
    @Mapping(target = "studentName", source = "student.user.fullName")
    @Mapping(target = "studentNo", source = "student.studentNo")
    @Mapping(target = "termName", source = "term.name")
    @Mapping(target = "sessionLabel", source = "term.session.label")
    @Mapping(target = "remarkByName", source = "remarkBy.user.fullName")
    TermReportResponseDTO toResponse(TermReport entity);

    List<TermReportResponseDTO> mapAllToResponse(List<TermReport> entities);
}

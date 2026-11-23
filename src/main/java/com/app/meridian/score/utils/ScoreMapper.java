package com.app.meridian.score.utils;

import java.util.List;
import com.app.meridian.score.Score;
import com.app.meridian.score.dto.ScoreResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ScoreMapper {

    @Mapping(target = "sheetId", source = "sheet.id")
    @Mapping(target = "studentId", source = "student.id")
    @Mapping(target = "gradeId", source = "grade.id")
    @Mapping(target = "studentName", source = "student.user.fullName")
    @Mapping(target = "studentNo", source = "student.studentNo")
    @Mapping(target = "gradeCode", source = "grade.grade")
    ScoreResponseDTO toResponse(Score entity);

    List<ScoreResponseDTO> mapAllToResponse(List<Score> entities);
}

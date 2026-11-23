package com.app.meridian.student.dto;

import java.util.List;

public record StudentTermResultDTO(String term, Double average, Integer classPosition, Integer classSize,
        String formTeacherRemark, List<StudentSubjectScoreDTO> subjects) {
}

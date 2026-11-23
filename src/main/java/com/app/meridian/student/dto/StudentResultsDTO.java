package com.app.meridian.student.dto;

import java.util.List;

public record StudentResultsDTO(String studentNo, String studentName, String sessionLabel, String term,
        Double average, List<StudentTermResultDTO> terms) {
}

package com.app.meridian.student.dto;

public record StudentSubjectScoreDTO(String subjectName, String subjectCode, Double ca, Double exam,
        Double total, String grade, String remark, Double classAverage, Integer position) {
}

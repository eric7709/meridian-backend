package com.app.meridian.student.dto;

public record SubjectHistoryRowDTO(String sessionLabel, String term, Double ca, Double exam,
        Double total, String grade, String remark) {
}

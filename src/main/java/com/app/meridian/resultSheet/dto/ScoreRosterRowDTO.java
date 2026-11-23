package com.app.meridian.resultSheet.dto;

public record ScoreRosterRowDTO(Long studentId, String studentNo, String studentName,
        Double ca, Double exam, Double total, String grade, String remark, boolean marked) {
}

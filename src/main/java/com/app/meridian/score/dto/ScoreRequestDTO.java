package com.app.meridian.score.dto;

/** Payload accepted by create and update. Update only applies non-null fields. */
public record ScoreRequestDTO(Double ca, Double exam, String remark, Long sheetId, Long studentId, Long gradeId) {
}

package com.app.meridian.gradeScale.dto;

/** Payload accepted by create and update. Update only applies non-null fields. */
public record GradeScaleRequestDTO(String grade, Double minScore, Double maxScore, String remark) {
}

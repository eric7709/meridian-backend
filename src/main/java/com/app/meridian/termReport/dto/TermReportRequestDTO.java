package com.app.meridian.termReport.dto;

/** Payload accepted by create and update. Update only applies non-null fields. */
public record TermReportRequestDTO(Double average, Integer classPosition, Integer classSize, String formTeacherRemark, Long studentId, Long termId, Long remarkById) {
}

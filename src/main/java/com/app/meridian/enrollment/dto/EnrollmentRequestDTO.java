package com.app.meridian.enrollment.dto;

import java.time.LocalDate;
/** Payload accepted by create and update. Update only applies non-null fields. */
public record EnrollmentRequestDTO(LocalDate enrolledOn, Long studentId, Long classId, Long sessionId) {
}

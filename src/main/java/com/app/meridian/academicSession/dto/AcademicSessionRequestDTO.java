package com.app.meridian.academicSession.dto;

import java.time.LocalDate;
/** Payload accepted by create and update. Update only applies non-null fields. */
public record AcademicSessionRequestDTO(String label, LocalDate startDate, LocalDate endDate, Boolean isCurrent) {
}

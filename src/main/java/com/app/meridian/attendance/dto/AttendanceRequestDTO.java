package com.app.meridian.attendance.dto;

import java.time.LocalDate;
import com.app.meridian.attendance.enums.AttendanceStatus;
/** Payload accepted by create and update. Update only applies non-null fields. */
public record AttendanceRequestDTO(LocalDate date, AttendanceStatus status, Long studentId, Long termId) {
}

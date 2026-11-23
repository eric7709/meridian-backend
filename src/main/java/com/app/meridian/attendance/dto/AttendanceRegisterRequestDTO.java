package com.app.meridian.attendance.dto;

import java.time.LocalDate;
import java.util.List;

/** The class register for one day. */
public record AttendanceRegisterRequestDTO(Long termId, Long classId, LocalDate date,
        List<AttendanceEntryDTO> entries) {
}

package com.app.meridian.attendance.dto;

import com.app.meridian.attendance.enums.AttendanceStatus;

public record AttendanceEntryDTO(Long studentId, AttendanceStatus status) {
}

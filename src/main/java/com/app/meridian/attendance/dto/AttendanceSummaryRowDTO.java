package com.app.meridian.attendance.dto;

public record AttendanceSummaryRowDTO(Long studentId, String studentNo, String studentName,
        long present, long absent, long late, long total, Double percentage) {
}

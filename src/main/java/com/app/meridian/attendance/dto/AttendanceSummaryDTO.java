package com.app.meridian.attendance.dto;

import java.util.List;

public record AttendanceSummaryDTO(Long termId, long daysMarked, List<AttendanceSummaryRowDTO> rows) {
}

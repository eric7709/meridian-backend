package com.app.meridian.leaveRequest.dto;

import java.time.LocalDate;

import com.app.meridian.leaveRequest.enums.LeaveStatus;

public record LeaveRequestDTO(Long staffId, LocalDate fromDate, LocalDate toDate, String reason,
        LeaveStatus status) {
}

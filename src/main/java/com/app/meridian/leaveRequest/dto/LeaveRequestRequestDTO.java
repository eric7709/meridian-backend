package com.app.meridian.leaveRequest.dto;

import java.time.LocalDate;
import com.app.meridian.leaveRequest.enums.LeaveStatus;
/** Payload accepted by create and update. Update only applies non-null fields. */
public record LeaveRequestRequestDTO(LocalDate fromDate, LocalDate toDate, String reason, LeaveStatus status, Long staffId) {
}

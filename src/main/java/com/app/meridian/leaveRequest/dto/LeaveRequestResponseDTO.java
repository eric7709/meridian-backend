package com.app.meridian.leaveRequest.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.app.meridian.leaveRequest.enums.LeaveStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LeaveRequestResponseDTO {

    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDate fromDate;
    private LocalDate toDate;
    private String reason;
    private LeaveStatus status;
    private Long staffId;
    private String staffName;
    private String staffNo;
}

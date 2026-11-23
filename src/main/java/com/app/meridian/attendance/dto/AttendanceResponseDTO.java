package com.app.meridian.attendance.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.app.meridian.attendance.enums.AttendanceStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceResponseDTO {

    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDate date;
    private AttendanceStatus status;
    private Long studentId;
    private Long termId;
    private String studentName;
    private String studentNo;
}

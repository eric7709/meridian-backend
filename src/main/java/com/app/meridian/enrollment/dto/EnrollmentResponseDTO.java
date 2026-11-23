package com.app.meridian.enrollment.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EnrollmentResponseDTO {

    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDate enrolledOn;
    private Long studentId;
    private Long classId;
    private String classRoomName;
    private Long sessionId;
    private String sessionLabel;
    private String studentName;
    private String studentNo;
    private String className;
}

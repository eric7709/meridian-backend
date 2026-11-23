package com.app.meridian.teachingAssignment.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TeachingAssignmentResponseDTO {

    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long teacherId;
    private Long subjectId;
    private Long classId;
    private Long sessionId;
    private String teacherName;
    private String subjectName;
    private String subjectCode;
    private String className;
}

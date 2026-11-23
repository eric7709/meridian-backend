package com.app.meridian.resultSheet.dto;

import java.time.LocalDateTime;
import com.app.meridian.resultSheet.enums.ResultSheetStatus;
import com.app.meridian.term.enums.TermName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResultSheetResponseDTO {

    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String code;
    private ResultSheetStatus status;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
    private String rejectionReason;
    private Long assignmentId;
    private Long termId;
    private Long submittedById;
    private Long reviewedById;
    private String subjectName;
    private String className;
    private String teacherName;
    private TermName termName;
    private String sessionLabel;
    private String submittedByName;
    private String reviewedByName;
}

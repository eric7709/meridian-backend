package com.app.meridian.term.dto;

import java.time.LocalDateTime;
import com.app.meridian.term.enums.TermName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TermResponseDTO {

    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private TermName name;
    private Boolean isActive;
    private Boolean allowTeacherSubmission;
    private Boolean allowStudentView;
    private Boolean isLocked;
    private Long sessionId;
    private String sessionLabel;
}

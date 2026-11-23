package com.app.meridian.termReport.dto;

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
public class TermReportResponseDTO {

    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Double average;
    private Integer classPosition;
    private Integer classSize;
    private String formTeacherRemark;
    private Long studentId;
    private Long termId;
    private Long remarkById;
    private String studentName;
    private String studentNo;
    private TermName termName;
    private String sessionLabel;
    private String remarkByName;
}

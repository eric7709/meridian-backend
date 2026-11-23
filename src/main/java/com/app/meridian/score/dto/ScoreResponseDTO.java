package com.app.meridian.score.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ScoreResponseDTO {

    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Double ca;
    private Double exam;
    private Double total;
    private String remark;
    private Long sheetId;
    private Long studentId;
    private Long gradeId;
    private String studentName;
    private String studentNo;
    private String gradeCode;
}

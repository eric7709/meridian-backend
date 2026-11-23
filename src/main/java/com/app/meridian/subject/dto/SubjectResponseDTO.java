package com.app.meridian.subject.dto;

import java.time.LocalDateTime;
import com.app.meridian.subject.enums.SubjectLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SubjectResponseDTO {

    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String name;
    private String code;
    private SubjectLevel level;
    private Long departmentId;
}

package com.app.meridian.classroom.dto;

import java.time.LocalDateTime;
import com.app.meridian.classroom.enums.Level;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ClassRoomResponseDTO {

    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String name;
    private Level level;
    private String arm;
    private Long formTeacherId;
    private Long sessionId;
    private String sessionLabel;
    private String formTeacherName;
}

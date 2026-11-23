package com.app.meridian.timetableSlot.dto;

import java.time.LocalDateTime;
import java.time.LocalTime;
import com.app.meridian.timetableSlot.enums.DayOfWeek;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TimetableSlotResponseDTO {

    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private String room;
    private Long assignmentId;
    private Long termId;
    private String subjectName;
    private String className;
    private String teacherName;
}

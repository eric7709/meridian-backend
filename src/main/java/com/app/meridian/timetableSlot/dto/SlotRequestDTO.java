package com.app.meridian.timetableSlot.dto;

import java.time.LocalTime;

import com.app.meridian.timetableSlot.enums.DayOfWeek;

public record SlotRequestDTO(Long assignmentId, Long termId, DayOfWeek dayOfWeek,
        LocalTime startTime, LocalTime endTime, String room) {
}

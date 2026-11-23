package com.app.meridian.timetableSlot.dto;

import java.time.LocalTime;
import com.app.meridian.timetableSlot.enums.DayOfWeek;
/** Payload accepted by create and update. Update only applies non-null fields. */
public record TimetableSlotRequestDTO(DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime, String room, Long assignmentId, Long termId) {
}

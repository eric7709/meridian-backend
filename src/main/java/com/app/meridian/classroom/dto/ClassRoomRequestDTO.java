package com.app.meridian.classroom.dto;

import com.app.meridian.classroom.enums.Level;
/** Payload accepted by create and update. Update only applies non-null fields. */
public record ClassRoomRequestDTO(String name, Level level, String arm, Long formTeacherId, Long sessionId) {
}

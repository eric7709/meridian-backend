package com.app.meridian.teachingAssignment.dto;

/** Payload accepted by create and update. Update only applies non-null fields. */
public record TeachingAssignmentRequestDTO(Long teacherId, Long subjectId, Long classId, Long sessionId) {
}

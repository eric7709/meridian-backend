package com.app.meridian.term.dto;

import com.app.meridian.term.enums.TermName;
/** Payload accepted by create and update. Update only applies non-null fields. */
public record TermRequestDTO(TermName name, Boolean isActive, Boolean allowTeacherSubmission, Boolean allowStudentView, Boolean isLocked, Long sessionId) {
}

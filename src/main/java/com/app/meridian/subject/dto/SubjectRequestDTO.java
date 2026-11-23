package com.app.meridian.subject.dto;

import com.app.meridian.subject.enums.SubjectLevel;
/** Payload accepted by create and update. Update only applies non-null fields. */
public record SubjectRequestDTO(String name, String code, SubjectLevel level, Long departmentId) {
}

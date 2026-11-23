package com.app.meridian.teachingAssignment.dto;

public record MySubjectDTO(Long assignmentId, Long subjectId, String subjectName, String subjectCode,
        Long classId, String className, String sessionLabel, long studentCount) {
}

package com.app.meridian.student.dto;

import java.time.LocalDate;
import com.app.meridian.student.enums.Gender;
import com.app.meridian.student.enums.StudentStatus;
/** Payload accepted by create and update. Update only applies non-null fields. */
public record StudentRequestDTO(String studentNo, Gender gender, LocalDate dob, String phone, String guardianName, String guardianPhone, StudentStatus status, Long userId, Long currentClassId) {
}

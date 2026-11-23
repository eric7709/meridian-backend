package com.app.meridian.student.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.app.meridian.student.enums.Gender;
import com.app.meridian.student.enums.StudentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StudentResponseDTO {

    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String studentNo;
    private Gender gender;
    private LocalDate dob;
    private String phone;
    private String guardianName;
    private String guardianPhone;
    private StudentStatus status;
    private Long userId;
    private String userFullName;
    private Long currentClassId;
    private String currentClassName;
    private String userEmail;
}

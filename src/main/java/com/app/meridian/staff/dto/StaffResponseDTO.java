package com.app.meridian.staff.dto;

import java.time.LocalDateTime;
import com.app.meridian.staff.enums.EmploymentType;
import com.app.meridian.staff.enums.StaffRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StaffResponseDTO {

    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String staffNo;
    private StaffRole staffRole;
    private EmploymentType employmentType;
    private String phone;
    private Long userId;
    private String userFullName;
    private Long departmentId;
    private String departmentName;
    private String userEmail;
}

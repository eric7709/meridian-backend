package com.app.meridian.staff.dto;

import com.app.meridian.staff.enums.EmploymentType;
import com.app.meridian.staff.enums.StaffRole;
/** Payload accepted by create and update. Update only applies non-null fields. */
public record StaffRequestDTO(String staffNo, StaffRole staffRole, EmploymentType employmentType, String phone, Long userId, Long departmentId) {
}

package com.app.meridian.department.dto;

/** Payload accepted by create and update. Update only applies non-null fields. */
public record DepartmentRequestDTO(String name, Long headStaffId) {
}

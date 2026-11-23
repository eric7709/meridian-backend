package com.app.meridian.schoolSettings.dto;

/** Payload accepted by create and update. Update only applies non-null fields. */
public record SchoolSettingsRequestDTO(String name, String email, String phone, String website, String address, String logoUrl, Boolean departmentsEnabled, Boolean feesEnabled, String notificationPrefs, Long currentTermId) {
}

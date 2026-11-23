package com.app.meridian.schoolSettings.dto;

import java.time.LocalDateTime;
import com.app.meridian.term.enums.TermName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SchoolSettingsResponseDTO {

    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String name;
    private String email;
    private String phone;
    private String website;
    private String address;
    private String logoUrl;
    private Boolean departmentsEnabled;
    private Boolean feesEnabled;
    private String notificationPrefs;
    private Long currentTermId;
    private TermName currentTermName;
    private String currentSessionLabel;
}

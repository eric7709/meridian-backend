package com.app.meridian.announcement.dto;

import java.time.LocalDateTime;
import com.app.meridian.announcement.enums.AnnouncementAudience;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AnnouncementResponseDTO {

    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String title;
    private String content;
    private AnnouncementAudience audience;
    private LocalDateTime eventAt;
    private Long createdById;
    private String createdByName;
}

package com.app.meridian.announcement.dto;

import java.time.LocalDateTime;
import com.app.meridian.announcement.enums.AnnouncementAudience;
/** Payload accepted by create and update. Update only applies non-null fields. */
public record AnnouncementRequestDTO(String title, String content, AnnouncementAudience audience, LocalDateTime eventAt, Long createdById) {
}

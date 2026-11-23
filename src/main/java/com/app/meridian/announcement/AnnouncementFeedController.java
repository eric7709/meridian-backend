package com.app.meridian.announcement;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.meridian.announcement.dto.AnnouncementResponseDTO;
import com.app.meridian.announcement.services.GetAnnouncementFeed;
import com.app.meridian.announcement.utils.AnnouncementMapper;
import com.app.meridian.security.SecurityUtils;

import lombok.RequiredArgsConstructor;

/** Role-filtered announcements for the dashboards. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/meridian/announcements")
public class AnnouncementFeedController {

    private final GetAnnouncementFeed getAnnouncementFeed;
    private final AnnouncementMapper announcementMapper;

    @GetMapping("/feed")
    public List<AnnouncementResponseDTO> feed() {
        return getAnnouncementFeed.get(SecurityUtils.role()).stream().map(announcementMapper::toResponse).toList();
    }
}

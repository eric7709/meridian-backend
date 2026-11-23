package com.app.meridian.announcement.services;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.meridian.announcement.Announcement;
import com.app.meridian.announcement.AnnouncementRepository;
import com.app.meridian.cache.CacheNames;
import com.app.meridian.user.enums.Role;
import com.app.meridian.announcement.enums.AnnouncementAudience;

import lombok.RequiredArgsConstructor;

/**
 * Students see ALL + STUDENT announcements, teachers see ALL + TEACHER, admins see everything.
 */
@Service
@RequiredArgsConstructor
public class GetAnnouncementFeed {

    private final AnnouncementRepository announcementRepository;

    @Cacheable(cacheNames = CacheNames.ANNOUNCEMENT_FEED, key = "#role")
    @Transactional(readOnly = true)
    public List<Announcement> get(Role role) {
        if (role == Role.ADMIN || role == Role.IT_SUPPORT) {
            List<AnnouncementAudience> all = List.of(AnnouncementAudience.values());
            return announcementRepository.findByAudienceInOrderByCreatedAtDesc(all);
        }
        List<AnnouncementAudience> audiences = role == Role.TEACHER
                ? List.of(AnnouncementAudience.ALL, AnnouncementAudience.TEACHER)
                : List.of(AnnouncementAudience.ALL, AnnouncementAudience.STUDENT);
        return announcementRepository.findByAudienceInOrderByCreatedAtDesc(audiences);
    }
}

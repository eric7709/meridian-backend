package com.app.meridian.announcement.services;

import com.app.meridian.announcement.AnnouncementEvents;
import com.app.meridian.announcement.AnnouncementRepository;
import com.app.meridian.exceptions.NotFoundException;
import com.app.meridian.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteAnnouncement {

    private final AnnouncementRepository announcementRepository;
    private final AnnouncementEvents announcementEvents;

    @Transactional
    @CacheEvict(cacheNames = {"announcementFeed"}, allEntries = true)
    public void delete(Long id) {
        ValidationUtils.requireNonNull(id, "Announcement ID");
        if (!announcementRepository.existsById(id)) {
            throw new NotFoundException("Announcement not found");
        }
        announcementRepository.deleteById(id);
        announcementEvents.broadcastDeleted(id);
    }
}

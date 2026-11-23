package com.app.meridian.announcement.services;

import java.time.LocalDateTime;
import com.app.meridian.announcement.Announcement;
import com.app.meridian.announcement.AnnouncementEvents;
import com.app.meridian.announcement.AnnouncementRepository;
import com.app.meridian.announcement.dto.AnnouncementRequestDTO;
import com.app.meridian.announcement.enums.AnnouncementAudience;
import com.app.meridian.announcement.utils.AnnouncementMapper;
import com.app.meridian.exceptions.ConflictException;
import com.app.meridian.user.User;
import com.app.meridian.utils.EntityResolver;
import com.app.meridian.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateAnnouncement {

    private final EntityResolver entityResolver;
    private final AnnouncementRepository announcementRepository;
    private final AnnouncementEvents announcementEvents;
    private final AnnouncementMapper announcementMapper;

    @Transactional
    @CacheEvict(cacheNames = {"announcementFeed"}, allEntries = true)
    public Announcement create(AnnouncementRequestDTO request) {
        ValidationUtils.requireNonNull(request, "Request body");
        ValidationUtils.requireNonBlank(request.title(), "title");
        ValidationUtils.requireNonBlank(request.content(), "content");

        User createdBy = entityResolver.resolveUser(request.createdById());


        Announcement entity = new Announcement();
        entity.setTitle(request.title().trim());
        entity.setContent(request.content().trim());
        entity.setAudience(request.audience() == null ? AnnouncementAudience.ALL : request.audience());
        if (request.eventAt() != null) {
            entity.setEventAt(request.eventAt());
        }
        entity.setCreatedBy(createdBy);

        Announcement saved = announcementRepository.save(entity);
        announcementEvents.broadcastCreated(announcementMapper.toResponse(saved));
        return saved;
    }
}

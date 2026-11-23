package com.app.meridian.announcement.services;

import java.time.LocalDateTime;
import com.app.meridian.announcement.Announcement;
import com.app.meridian.announcement.AnnouncementEvents;
import com.app.meridian.announcement.AnnouncementRepository;
import com.app.meridian.announcement.dto.AnnouncementRequestDTO;
import com.app.meridian.announcement.enums.AnnouncementAudience;
import com.app.meridian.announcement.utils.AnnouncementMapper;
import com.app.meridian.exceptions.ConflictException;
import com.app.meridian.exceptions.NotFoundException;
import com.app.meridian.user.User;
import com.app.meridian.utils.EntityResolver;
import com.app.meridian.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Applies only the fields present in the request; everything else is left alone. */
@Service
@RequiredArgsConstructor
public class UpdateAnnouncement {

    private final EntityResolver entityResolver;
    private final AnnouncementRepository announcementRepository;
    private final AnnouncementEvents announcementEvents;
    private final AnnouncementMapper announcementMapper;

    @Transactional
    @CacheEvict(cacheNames = {"announcementFeed"}, allEntries = true)
    public Announcement update(Long id, AnnouncementRequestDTO request) {
        ValidationUtils.requireNonNull(id, "Announcement ID");
        ValidationUtils.requireNonNull(request, "Request body");
        Announcement entity = announcementRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Announcement not found"));
        if (request.createdById() != null) {
            entity.setCreatedBy(entityResolver.resolveUser(request.createdById()));
        }

        if (request.title() != null && !request.title().isBlank()) {
            entity.setTitle(request.title().trim());
        }
        if (request.content() != null && !request.content().isBlank()) {
            entity.setContent(request.content().trim());
        }
        if (request.audience() != null) {
            entity.setAudience(request.audience());
        }
        if (request.eventAt() != null) {
            entity.setEventAt(request.eventAt());
        }

        Announcement saved = announcementRepository.save(entity);
        announcementEvents.broadcastUpdated(announcementMapper.toResponse(saved));
        return saved;
    }
}

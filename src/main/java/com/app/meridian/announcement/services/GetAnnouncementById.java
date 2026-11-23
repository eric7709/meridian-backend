package com.app.meridian.announcement.services;

import com.app.meridian.announcement.Announcement;
import com.app.meridian.announcement.AnnouncementRepository;
import com.app.meridian.exceptions.NotFoundException;
import com.app.meridian.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetAnnouncementById {

    private final AnnouncementRepository announcementRepository;

    @Transactional(readOnly = true)
    public Announcement get(Long id) {
        ValidationUtils.requireNonNull(id, "Announcement ID");
        return announcementRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Announcement not found"));
    }
}

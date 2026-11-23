package com.app.meridian.announcement;

import com.app.meridian.announcement.dto.AnnouncementRequestDTO;
import com.app.meridian.announcement.dto.AnnouncementResponseDTO;
import com.app.meridian.announcement.dto.AnnouncementSearchCriteria;
import com.app.meridian.announcement.services.CreateAnnouncement;
import com.app.meridian.announcement.services.DeleteAnnouncement;
import com.app.meridian.announcement.services.GetAnnouncementById;
import com.app.meridian.announcement.services.SearchAnnouncements;
import com.app.meridian.announcement.services.UpdateAnnouncement;
import com.app.meridian.announcement.utils.AnnouncementMapper;
import com.app.meridian.common.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/meridian/announcements")
public class AnnouncementController {

    private final CreateAnnouncement createAnnouncement;
    private final UpdateAnnouncement updateAnnouncement;
    private final DeleteAnnouncement deleteAnnouncement;
    private final GetAnnouncementById getAnnouncementById;
    private final SearchAnnouncements searchAnnouncements;
    private final AnnouncementMapper announcementMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','IT_SUPPORT')")
    public AnnouncementResponseDTO create(@RequestBody AnnouncementRequestDTO request) {
        return announcementMapper.toResponse(createAnnouncement.create(request));
    }

    @GetMapping
    public PageResponse<AnnouncementResponseDTO> search(AnnouncementSearchCriteria criteria) {
        Page<Announcement> page = searchAnnouncements.search(criteria);
        return PageResponse.of(page, announcementMapper::toResponse);
    }

    @GetMapping("/{id}")
    public AnnouncementResponseDTO get(@PathVariable Long id) {
        return announcementMapper.toResponse(getAnnouncementById.get(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','IT_SUPPORT')")
    public AnnouncementResponseDTO update(@PathVariable Long id, @RequestBody AnnouncementRequestDTO request) {
        return announcementMapper.toResponse(updateAnnouncement.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        deleteAnnouncement.delete(id);
    }
}

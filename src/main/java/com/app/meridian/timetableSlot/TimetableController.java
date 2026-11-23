package com.app.meridian.timetableSlot;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.app.meridian.timetableSlot.dto.SlotRequestDTO;
import com.app.meridian.timetableSlot.dto.TimetableSlotResponseDTO;
import com.app.meridian.timetableSlot.services.DeleteTimetableSlot;
import com.app.meridian.timetableSlot.services.GetClassTimetable;
import com.app.meridian.timetableSlot.services.GetMyTimetable;
import com.app.meridian.timetableSlot.services.ScheduleSlot;
import com.app.meridian.common.PageResponse;
import com.app.meridian.timetableSlot.dto.TimetableSlotResponseDTO;
import com.app.meridian.timetableSlot.dto.TimetableSlotSearchCriteria;
import com.app.meridian.timetableSlot.services.SearchTimetableSlots;
import com.app.meridian.timetableSlot.utils.TimetableSlotMapper;
import org.springframework.data.domain.Page;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/meridian")
public class TimetableController {

    private final GetMyTimetable getMyTimetable;
    private final GetClassTimetable getClassTimetable;
    private final ScheduleSlot scheduleSlot;
    private final DeleteTimetableSlot deleteTimetableSlot;
    private final SearchTimetableSlots searchTimetableSlots;
    private final TimetableSlotMapper timetableSlotMapper;

    /** Admin board: every slot, paged, filterable by term, assignment, day and room. */
    @GetMapping("/timetable")
    public PageResponse<TimetableSlotResponseDTO> search(TimetableSlotSearchCriteria criteria) {
        Page<TimetableSlot> page = searchTimetableSlots.search(criteria);
        return PageResponse.of(page, timetableSlotMapper::toResponse);
    }

    @GetMapping("/timetable/me/today")
    public List<TimetableSlotResponseDTO> today() {
        return getMyTimetable.get("today", false).stream().map(timetableSlotMapper::toResponse).toList();
    }

    @GetMapping("/timetable/me")
    public List<TimetableSlotResponseDTO> day(@RequestParam(required = false) String day) {
        return getMyTimetable.get(day, false).stream().map(timetableSlotMapper::toResponse).toList();
    }

    @GetMapping("/timetable/me/week")
    public List<TimetableSlotResponseDTO> week() {
        return getMyTimetable.get(null, true).stream().map(timetableSlotMapper::toResponse).toList();
    }

    @GetMapping("/classes/{classId}/timetable")
    public List<TimetableSlotResponseDTO> forClass(@PathVariable Long classId,
            @RequestParam(required = false) Long termId) {
        return getClassTimetable.get(classId, termId).stream().map(timetableSlotMapper::toResponse).toList();
    }

    @PostMapping("/timetable")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','IT_SUPPORT')")
    public TimetableSlotResponseDTO create(@RequestBody SlotRequestDTO request) {
        return timetableSlotMapper.toResponse(scheduleSlot.create(request));
    }

    @PutMapping("/timetable/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','IT_SUPPORT')")
    public TimetableSlotResponseDTO update(@PathVariable Long id, @RequestBody SlotRequestDTO request) {
        return timetableSlotMapper.toResponse(scheduleSlot.update(id, request));
    }

    @DeleteMapping("/timetable/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        deleteTimetableSlot.delete(id);
    }
}

package com.app.meridian.timetableSlot.services;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.meridian.cache.CacheNames;
import com.app.meridian.timetableSlot.TimetableSlotEvents;
import com.app.meridian.timetableSlot.TimetableSlotRepository;
import com.app.meridian.exceptions.NotFoundException;
import com.app.meridian.utils.ValidationUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeleteTimetableSlot {

    private final TimetableSlotRepository timetableSlotRepository;
    private final TimetableSlotEvents timetableSlotEvents;

    @Transactional
    @CacheEvict(cacheNames = CacheNames.CLASS_TIMETABLE, allEntries = true)
    public void delete(Long id) {
        ValidationUtils.requireNonNull(id, "Timetable slot ID");
        if (!timetableSlotRepository.existsById(id)) {
            throw new NotFoundException("Timetable slot not found");
        }
        timetableSlotRepository.deleteById(id);
        timetableSlotEvents.broadcastDeleted(id);
    }
}

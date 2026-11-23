package com.app.meridian.timetableSlot.services;

import com.app.meridian.exceptions.NotFoundException;
import com.app.meridian.timetableSlot.TimetableSlot;
import com.app.meridian.timetableSlot.TimetableSlotRepository;
import com.app.meridian.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetTimetableSlotById {

    private final TimetableSlotRepository timetableSlotRepository;

    @Transactional(readOnly = true)
    public TimetableSlot get(Long id) {
        ValidationUtils.requireNonNull(id, "TimetableSlot ID");
        return timetableSlotRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("TimetableSlot not found"));
    }
}

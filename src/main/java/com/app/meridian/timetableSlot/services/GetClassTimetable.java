package com.app.meridian.timetableSlot.services;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.meridian.term.Term;
import com.app.meridian.term.TermRepository;
import com.app.meridian.term.services.GetActiveTerm;
import com.app.meridian.cache.CacheNames;
import com.app.meridian.timetableSlot.TimetableSlot;
import com.app.meridian.timetableSlot.TimetableSlotRepository;
import com.app.meridian.exceptions.BadRequestException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetClassTimetable {

    private final TimetableSlotRepository timetableSlotRepository;
    private final TermRepository termRepository;
    private final GetActiveTerm getActiveTerm;

    @Cacheable(cacheNames = CacheNames.CLASS_TIMETABLE,
            key = "#classId + ':' + (#termId == null ? 'active' : #termId)")
    @Transactional(readOnly = true)
    public List<TimetableSlot> get(Long classId, Long termId) {
        Term term = termId != null
                ? termRepository.findById(termId).orElseThrow(() -> new BadRequestException("Term not found"))
                : getActiveTerm.get();
        return timetableSlotRepository.findByTermIdAndAssignmentClassRoomId(term.getId(), classId);
    }
}

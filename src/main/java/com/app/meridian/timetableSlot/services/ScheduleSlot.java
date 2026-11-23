package com.app.meridian.timetableSlot.services;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.meridian.teachingAssignment.TeachingAssignment;
import com.app.meridian.term.Term;
import com.app.meridian.timetableSlot.TimetableSlot;
import com.app.meridian.cache.CacheNames;
import com.app.meridian.timetableSlot.TimetableSlotEvents;
import com.app.meridian.timetableSlot.TimetableSlotRepository;
import com.app.meridian.timetableSlot.dto.SlotRequestDTO;
import com.app.meridian.timetableSlot.utils.TimetableSlotMapper;
import com.app.meridian.exceptions.BadRequestException;
import com.app.meridian.exceptions.ConflictException;
import com.app.meridian.exceptions.NotFoundException;
import com.app.meridian.utils.EntityResolver;
import com.app.meridian.utils.ValidationUtils;

import lombok.RequiredArgsConstructor;

/** Schedules a lesson, refusing clashes for the same teacher, class or room. */
@Service
@RequiredArgsConstructor
public class ScheduleSlot {

    private final EntityResolver entityResolver;
    private final TimetableSlotRepository timetableSlotRepository;
    private final TimetableSlotEvents timetableSlotEvents;
    private final TimetableSlotMapper timetableSlotMapper;

    @Transactional
    @CacheEvict(cacheNames = CacheNames.CLASS_TIMETABLE, allEntries = true)
    public TimetableSlot create(SlotRequestDTO request) {
        ValidationUtils.requireNonNull(request, "Request body");
        ValidationUtils.requireNonNull(request.assignmentId(), "Teaching assignment");
        ValidationUtils.requireNonNull(request.termId(), "Term");
        ValidationUtils.requireNonNull(request.dayOfWeek(), "Day of week");
        ValidationUtils.requireNonNull(request.startTime(), "Start time");
        ValidationUtils.requireNonNull(request.endTime(), "End time");
        if (!request.startTime().isBefore(request.endTime())) {
            throw new BadRequestException("Start time must be before end time");
        }
        TeachingAssignment assignment = entityResolver.resolveTeachingAssignment(request.assignmentId());
        Term term = entityResolver.resolveTerm(request.termId());
        ensureNoClash(request, term.getId(), null, assignment);
        TimetableSlot slot = new TimetableSlot();
        slot.setAssignment(assignment);
        slot.setTerm(term);
        slot.setDayOfWeek(request.dayOfWeek());
        slot.setStartTime(request.startTime());
        slot.setEndTime(request.endTime());
        slot.setRoom(ValidationUtils.trimOrNull(request.room()));
        TimetableSlot saved = timetableSlotRepository.save(slot);
        timetableSlotEvents.broadcastCreated(timetableSlotMapper.toResponse(saved));
        return saved;
    }

    @Transactional
    @CacheEvict(cacheNames = CacheNames.CLASS_TIMETABLE, allEntries = true)
    public TimetableSlot update(Long id, SlotRequestDTO request) {
        ValidationUtils.requireNonNull(id, "Timetable slot ID");
        TimetableSlot slot = timetableSlotRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Timetable slot not found"));
        TeachingAssignment assignment = request.assignmentId() == null
                ? slot.getAssignment()
                : entityResolver.resolveTeachingAssignment(request.assignmentId());
        Term term = request.termId() == null ? slot.getTerm() : entityResolver.resolveTerm(request.termId());
        var day = request.dayOfWeek() == null ? slot.getDayOfWeek() : request.dayOfWeek();
        var start = request.startTime() == null ? slot.getStartTime() : request.startTime();
        var end = request.endTime() == null ? slot.getEndTime() : request.endTime();
        if (!start.isBefore(end)) {
            throw new BadRequestException("Start time must be before end time");
        }
        ensureNoClash(new SlotRequestDTO(assignment.getId(), term.getId(), day, start, end,
                request.room() == null ? slot.getRoom() : request.room()), term.getId(), id, assignment);
        slot.setAssignment(assignment);
        slot.setTerm(term);
        slot.setDayOfWeek(day);
        slot.setStartTime(start);
        slot.setEndTime(end);
        if (request.room() != null) {
            slot.setRoom(ValidationUtils.trimOrNull(request.room()));
        }
        TimetableSlot saved = timetableSlotRepository.save(slot);
        timetableSlotEvents.broadcastUpdated(timetableSlotMapper.toResponse(saved));
        return saved;
    }

    private void ensureNoClash(SlotRequestDTO request, Long termId, Long ignoreSlotId, TeachingAssignment assignment) {
        for (TimetableSlot existing : timetableSlotRepository.findByTermIdOrderByStartTimeAsc(termId)) {
            if (existing.getId().equals(ignoreSlotId) || existing.getDayOfWeek() != request.dayOfWeek()) {
                continue;
            }
            boolean overlaps = request.startTime().isBefore(existing.getEndTime())
                    && request.endTime().isAfter(existing.getStartTime());
            if (!overlaps) {
                continue;
            }
            TeachingAssignment other = existing.getAssignment();
            if (other.getTeacher().getId().equals(assignment.getTeacher().getId())) {
                throw new ConflictException("This teacher already has a lesson at that time");
            }
            if (other.getClassRoom().getId().equals(assignment.getClassRoom().getId())) {
                throw new ConflictException("This class already has a lesson at that time");
            }
            if (request.room() != null && request.room().equalsIgnoreCase(existing.getRoom())) {
                throw new ConflictException(existing.getRoom() + " is already booked at that time");
            }
        }
    }
}

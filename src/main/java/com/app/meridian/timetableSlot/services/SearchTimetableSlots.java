package com.app.meridian.timetableSlot.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import com.app.meridian.exceptions.BadRequestException;
import com.app.meridian.timetableSlot.TimetableSlot;
import com.app.meridian.timetableSlot.TimetableSlotRepository;
import com.app.meridian.timetableSlot.dto.TimetableSlotSearchCriteria;
import com.app.meridian.timetableSlot.enums.DayOfWeek;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Page-and-filter listing shared by every list screen. */
@Service
@RequiredArgsConstructor
public class SearchTimetableSlots {

    private static final Set<String> SORTABLE = Set.of("id", "createdAt", "updatedAt", "dayOfWeek", "startTime", "endTime", "room");
    /** Sortable paths that live on a related row, ordered inside the query. */
    private static final Set<String> RELATED_SORTABLE = Set.of();
    /** The frontend sorts by the leaf name (fullName), which maps to user.fullName. */
    private static final Map<String, String> RELATED_SORT_ALIASES = Map.of();

    private final TimetableSlotRepository timetableSlotRepository;

    @Transactional(readOnly = true)
    public Page<TimetableSlot> search(TimetableSlotSearchCriteria criteria) {
        Sort.Direction direction = "asc".equalsIgnoreCase(criteria.getSortDirection())
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        String sortBy = resolveSort(criteria.getSortBy());
        int size = Math.min(Math.max(criteria.getLimit(), 1), 200);
        int index = Math.max(criteria.getPage() - 1, 0);
        Pageable pageable = RELATED_SORTABLE.contains(sortBy)
                ? PageRequest.of(index, size)
                : PageRequest.of(index, size, Sort.by(direction, sortBy));
        return timetableSlotRepository.findAll(matching(criteria, sortBy, direction), pageable);
    }

    /** A typo in sortBy is a client bug: say so instead of silently sorting by id. */
    private String resolveSort(String requested) {
        if (requested == null || requested.isBlank()) {
            return "id";
        }
        if (!SORTABLE.contains(requested) && !RELATED_SORTABLE.contains(requested)
                && !RELATED_SORT_ALIASES.containsKey(requested)) {
            throw new BadRequestException("Unknown sort field '" + requested
                    + "'. Sortable: " + new TreeSet<>(SORTABLE) + " or "
                    + new TreeSet<>(RELATED_SORT_ALIASES.keySet()));
        }
        return RELATED_SORT_ALIASES.getOrDefault(requested, requested);
    }

    private Specification<TimetableSlot> matching(TimetableSlotSearchCriteria criteria, String sortBy,
            Sort.Direction direction) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (criteria.getSearch() != null && !criteria.getSearch().isBlank()) {
                String needle = "%" + criteria.getSearch().trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("room")), needle)));
            }
            if (criteria.getAssignmentId() != null) {
                predicates.add(cb.equal(root.get("assignment").get("id"), criteria.getAssignmentId()));
            }
            if (criteria.getTermId() != null) {
                predicates.add(cb.equal(root.get("term").get("id"), criteria.getTermId()));
            }
            if (criteria.getDayOfWeek() != null) {
                predicates.add(cb.equal(root.get("dayOfWeek"), criteria.getDayOfWeek()));
            }
            if (criteria.getRoom() != null && !criteria.getRoom().isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("room")), criteria.getRoom().trim().toLowerCase()));
            }
            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

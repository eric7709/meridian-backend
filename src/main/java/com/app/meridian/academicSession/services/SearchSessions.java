package com.app.meridian.academicSession.services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import com.app.meridian.academicSession.AcademicSession;
import com.app.meridian.academicSession.AcademicSessionRepository;
import com.app.meridian.academicSession.dto.AcademicSessionSearchCriteria;
import com.app.meridian.exceptions.BadRequestException;
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
public class SearchSessions {

    private static final Set<String> SORTABLE = Set.of("id", "createdAt", "updatedAt", "label", "startDate", "endDate", "isCurrent");
    /** Sortable paths that live on a related row, ordered inside the query. */
    private static final Set<String> RELATED_SORTABLE = Set.of();
    /** The frontend sorts by the leaf name (fullName), which maps to user.fullName. */
    private static final Map<String, String> RELATED_SORT_ALIASES = Map.of();

    private final AcademicSessionRepository academicSessionRepository;

    @Transactional(readOnly = true)
    public Page<AcademicSession> search(AcademicSessionSearchCriteria criteria) {
        Sort.Direction direction = "asc".equalsIgnoreCase(criteria.getSortDirection())
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        String sortBy = resolveSort(criteria.getSortBy());
        int size = Math.min(Math.max(criteria.getLimit(), 1), 200);
        int index = Math.max(criteria.getPage() - 1, 0);
        Pageable pageable = RELATED_SORTABLE.contains(sortBy)
                ? PageRequest.of(index, size)
                : PageRequest.of(index, size, Sort.by(direction, sortBy));
        return academicSessionRepository.findAll(matching(criteria, sortBy, direction), pageable);
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

    private Specification<AcademicSession> matching(AcademicSessionSearchCriteria criteria, String sortBy,
            Sort.Direction direction) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (criteria.getSearch() != null && !criteria.getSearch().isBlank()) {
                String needle = "%" + criteria.getSearch().trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("label")), needle)));
            }
            if (criteria.getIsCurrent() != null) {
                predicates.add(cb.equal(root.get("isCurrent"), criteria.getIsCurrent()));
            }
            if (criteria.getMinStartDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("startDate"), criteria.getMinStartDate()));
            }
            if (criteria.getMaxStartDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("startDate"), criteria.getMaxStartDate()));
            }
            if (criteria.getMinEndDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("endDate"), criteria.getMinEndDate()));
            }
            if (criteria.getMaxEndDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("endDate"), criteria.getMaxEndDate()));
            }
            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

package com.app.meridian.gradeScale.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import com.app.meridian.exceptions.BadRequestException;
import com.app.meridian.gradeScale.GradeScale;
import com.app.meridian.gradeScale.GradeScaleRepository;
import com.app.meridian.gradeScale.dto.GradeScaleSearchCriteria;
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
public class SearchGradeScales {

    private static final Set<String> SORTABLE = Set.of("id", "createdAt", "updatedAt", "grade", "minScore", "maxScore", "remark");
    /** Sortable paths that live on a related row, ordered inside the query. */
    private static final Set<String> RELATED_SORTABLE = Set.of();
    /** The frontend sorts by the leaf name (fullName), which maps to user.fullName. */
    private static final Map<String, String> RELATED_SORT_ALIASES = Map.of();

    private final GradeScaleRepository gradeScaleRepository;

    @Transactional(readOnly = true)
    public Page<GradeScale> search(GradeScaleSearchCriteria criteria) {
        Sort.Direction direction = "asc".equalsIgnoreCase(criteria.getSortDirection())
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        String sortBy = resolveSort(criteria.getSortBy());
        int size = Math.min(Math.max(criteria.getLimit(), 1), 200);
        int index = Math.max(criteria.getPage() - 1, 0);
        Pageable pageable = RELATED_SORTABLE.contains(sortBy)
                ? PageRequest.of(index, size)
                : PageRequest.of(index, size, Sort.by(direction, sortBy));
        return gradeScaleRepository.findAll(matching(criteria, sortBy, direction), pageable);
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

    private Specification<GradeScale> matching(GradeScaleSearchCriteria criteria, String sortBy,
            Sort.Direction direction) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (criteria.getSearch() != null && !criteria.getSearch().isBlank()) {
                String needle = "%" + criteria.getSearch().trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("grade")), needle),
                        cb.like(cb.lower(root.get("remark")), needle)));
            }
            if (criteria.getMinMinScore() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("minScore"), criteria.getMinMinScore()));
            }
            if (criteria.getMaxMinScore() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("minScore"), criteria.getMaxMinScore()));
            }
            if (criteria.getMinMaxScore() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("maxScore"), criteria.getMinMaxScore()));
            }
            if (criteria.getMaxMaxScore() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("maxScore"), criteria.getMaxMaxScore()));
            }
            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

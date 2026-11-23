package com.app.meridian.score.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import com.app.meridian.exceptions.BadRequestException;
import com.app.meridian.score.Score;
import com.app.meridian.score.ScoreRepository;
import com.app.meridian.score.dto.ScoreSearchCriteria;
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
public class SearchScores {

    private static final Set<String> SORTABLE = Set.of("id", "createdAt", "updatedAt", "ca", "exam", "total", "remark");
    /** Sortable paths that live on a related row, ordered inside the query. */
    private static final Set<String> RELATED_SORTABLE = Set.of();
    /** The frontend sorts by the leaf name (fullName), which maps to user.fullName. */
    private static final Map<String, String> RELATED_SORT_ALIASES = Map.of();

    private final ScoreRepository scoreRepository;

    @Transactional(readOnly = true)
    public Page<Score> search(ScoreSearchCriteria criteria) {
        Sort.Direction direction = "asc".equalsIgnoreCase(criteria.getSortDirection())
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        String sortBy = resolveSort(criteria.getSortBy());
        int size = Math.min(Math.max(criteria.getLimit(), 1), 200);
        int index = Math.max(criteria.getPage() - 1, 0);
        Pageable pageable = RELATED_SORTABLE.contains(sortBy)
                ? PageRequest.of(index, size)
                : PageRequest.of(index, size, Sort.by(direction, sortBy));
        return scoreRepository.findAll(matching(criteria, sortBy, direction), pageable);
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

    private Specification<Score> matching(ScoreSearchCriteria criteria, String sortBy,
            Sort.Direction direction) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (criteria.getSearch() != null && !criteria.getSearch().isBlank()) {
                String needle = "%" + criteria.getSearch().trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("remark")), needle)));
            }
            if (criteria.getSheetId() != null) {
                predicates.add(cb.equal(root.get("sheet").get("id"), criteria.getSheetId()));
            }
            if (criteria.getStudentId() != null) {
                predicates.add(cb.equal(root.get("student").get("id"), criteria.getStudentId()));
            }
            if (criteria.getMinCa() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("ca"), criteria.getMinCa()));
            }
            if (criteria.getMaxCa() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("ca"), criteria.getMaxCa()));
            }
            if (criteria.getMinExam() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("exam"), criteria.getMinExam()));
            }
            if (criteria.getMaxExam() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("exam"), criteria.getMaxExam()));
            }
            if (criteria.getMinTotal() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("total"), criteria.getMinTotal()));
            }
            if (criteria.getMaxTotal() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("total"), criteria.getMaxTotal()));
            }
            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

package com.app.meridian.leaveRequest.services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import com.app.meridian.exceptions.BadRequestException;
import com.app.meridian.leaveRequest.LeaveRequest;
import com.app.meridian.leaveRequest.LeaveRequestRepository;
import com.app.meridian.leaveRequest.dto.LeaveRequestSearchCriteria;
import com.app.meridian.leaveRequest.enums.LeaveStatus;
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
public class SearchLeaveRequests {

    private static final Set<String> SORTABLE = Set.of("id", "createdAt", "updatedAt", "fromDate", "toDate", "reason", "status");
    /** Sortable paths that live on a related row, ordered inside the query. */
    private static final Set<String> RELATED_SORTABLE = Set.of();
    /** The frontend sorts by the leaf name (fullName), which maps to user.fullName. */
    private static final Map<String, String> RELATED_SORT_ALIASES = Map.of();

    private final LeaveRequestRepository leaveRequestRepository;

    @Transactional(readOnly = true)
    public Page<LeaveRequest> search(LeaveRequestSearchCriteria criteria) {
        Sort.Direction direction = "asc".equalsIgnoreCase(criteria.getSortDirection())
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        String sortBy = resolveSort(criteria.getSortBy());
        int size = Math.min(Math.max(criteria.getLimit(), 1), 200);
        int index = Math.max(criteria.getPage() - 1, 0);
        Pageable pageable = RELATED_SORTABLE.contains(sortBy)
                ? PageRequest.of(index, size)
                : PageRequest.of(index, size, Sort.by(direction, sortBy));
        return leaveRequestRepository.findAll(matching(criteria, sortBy, direction), pageable);
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

    private Specification<LeaveRequest> matching(LeaveRequestSearchCriteria criteria, String sortBy,
            Sort.Direction direction) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (criteria.getSearch() != null && !criteria.getSearch().isBlank()) {
                String needle = "%" + criteria.getSearch().trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("reason")), needle)));
            }
            if (criteria.getStaffId() != null) {
                predicates.add(cb.equal(root.get("staff").get("id"), criteria.getStaffId()));
            }
            if (criteria.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), criteria.getStatus()));
            }
            if (criteria.getMinFromDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("fromDate"), criteria.getMinFromDate()));
            }
            if (criteria.getMaxFromDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("fromDate"), criteria.getMaxFromDate()));
            }
            if (criteria.getMinToDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("toDate"), criteria.getMinToDate()));
            }
            if (criteria.getMaxToDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("toDate"), criteria.getMaxToDate()));
            }
            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

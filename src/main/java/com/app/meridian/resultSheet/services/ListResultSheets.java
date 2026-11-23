package com.app.meridian.resultSheet.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.meridian.resultSheet.ResultSheet;
import com.app.meridian.resultSheet.ResultSheetRepository;
import com.app.meridian.resultSheet.dto.ResultSheetSearchCriteria;
import com.app.meridian.resultSheet.enums.ResultSheetStatus;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;

/**
 * The admin "All Results" and "Pending Approvals" lists. Drafts are never returned here — they
 * belong to the teacher who owns them.
 */
@Service
@RequiredArgsConstructor
public class ListResultSheets {

    private final ResultSheetRepository resultSheetRepository;

    @Transactional(readOnly = true)
    public Page<ResultSheet> search(ResultSheetSearchCriteria criteria) {
        Sort.Direction direction = "asc".equalsIgnoreCase(criteria.getSortDirection())
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        String sortBy = switch (criteria.getSortBy() == null ? "" : criteria.getSortBy()) {
            case "subject" -> "assignment.subject.name";
            case "teacher" -> "assignment.teacher.user.fullName";
            case "submittedOn" -> "submittedAt";
            default -> "submittedAt";
        };
        int size = Math.min(Math.max(criteria.getLimit(), 1), 200);
        Pageable pageable = PageRequest.of(Math.max(criteria.getPage() - 1, 0), size, Sort.by(direction, sortBy));
        return resultSheetRepository.findAll(matching(criteria), pageable);
    }

    private Specification<ResultSheet> matching(ResultSheetSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.notEqual(root.get("status"), ResultSheetStatus.DRAFT));
            if (criteria.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), criteria.getStatus()));
            }
            if (criteria.getClassId() != null) {
                predicates.add(cb.equal(root.get("assignment").get("classRoom").get("id"), criteria.getClassId()));
            }
            if (criteria.getSessionId() != null) {
                predicates.add(cb.equal(root.get("term").get("session").get("id"), criteria.getSessionId()));
            }
            if (criteria.getTeacherId() != null) {
                predicates.add(cb.equal(root.get("assignment").get("teacher").get("id"), criteria.getTeacherId()));
            }
            if (criteria.getTermId() != null) {
                predicates.add(cb.equal(root.get("term").get("id"), criteria.getTermId()));
            }
            if (criteria.getSearch() != null && !criteria.getSearch().isBlank()) {
                String needle = "%" + criteria.getSearch().trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("code")), needle),
                        cb.like(cb.lower(root.get("assignment").get("subject").get("name")), needle),
                        cb.like(cb.lower(root.get("assignment").get("classRoom").get("name")), needle),
                        cb.like(cb.lower(root.get("assignment").get("teacher").get("user").get("fullName")), needle)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

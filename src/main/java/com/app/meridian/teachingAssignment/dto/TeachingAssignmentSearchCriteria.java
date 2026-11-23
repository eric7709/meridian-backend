package com.app.meridian.teachingAssignment.dto;

import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Query parameters for listing: free text, filters, ranges, paging, sorting. */
@Getter
@Setter
@NoArgsConstructor
public class TeachingAssignmentSearchCriteria {

    private String search;
    private Long teacherId;
    private Long subjectId;
    private Long classRoomId;
    private Long sessionId;

    private int page = 1;
    private int limit = 20;
    private String sortBy = "id";
    private String sortDirection = "desc";

    /** Aliases so the frontend can send q / sort / order exactly as the screens do. */
    public void setQ(String q) {
        this.search = q;
    }

    public void setSort(String sort) {
        this.sortBy = sort;
    }

    public void setOrder(String order) {
        this.sortDirection = order;
    }
}

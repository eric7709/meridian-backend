package com.app.meridian.subject.dto;

import java.util.Set;
import com.app.meridian.subject.enums.SubjectLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Query parameters for listing: free text, filters, ranges, paging, sorting. */
@Getter
@Setter
@NoArgsConstructor
public class SubjectSearchCriteria {

    private String search;
    private SubjectLevel level;

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

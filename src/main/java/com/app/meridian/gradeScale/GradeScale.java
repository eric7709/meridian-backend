package com.app.meridian.gradeScale;

import com.app.meridian.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "grade_scales")
public class GradeScale extends BaseEntity {

    @NotNull
    @Column(name = "grade", nullable = false, unique = true)
    private String grade;

    @NotNull
    @Column(name = "min_score", nullable = false)
    private Double minScore;

    @NotNull
    @Column(name = "max_score", nullable = false)
    private Double maxScore;

    @NotNull
    @Column(name = "remark", nullable = false)
    private String remark;

}

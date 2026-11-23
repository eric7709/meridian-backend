package com.app.meridian.score;

import com.app.meridian.common.BaseEntity;
import com.app.meridian.gradeScale.GradeScale;
import com.app.meridian.resultSheet.ResultSheet;
import com.app.meridian.student.Student;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "scores", uniqueConstraints = {
        @UniqueConstraint(name = "uk_scores_sheet_student", columnNames = {"sheet_id", "student_id"})})
public class Score extends BaseEntity {

    @Column(name = "ca", nullable = true)
    private Double ca;

    @Column(name = "exam", nullable = true)
    private Double exam;

    @Column(name = "total", nullable = true)
    private Double total;

    @Column(name = "remark", nullable = true)
    private String remark;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sheet_id", nullable = false, foreignKey = @ForeignKey(name = "fk_scores_sheet"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    @NotNull
    private ResultSheet sheet;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id", nullable = false, foreignKey = @ForeignKey(name = "fk_scores_student"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    @NotNull
    private Student student;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "grade_id", nullable = true, foreignKey = @ForeignKey(name = "fk_scores_grade"))
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private GradeScale grade;

}

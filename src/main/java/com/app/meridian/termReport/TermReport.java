package com.app.meridian.termReport;

import com.app.meridian.common.BaseEntity;
import com.app.meridian.staff.Staff;
import com.app.meridian.student.Student;
import com.app.meridian.term.Term;
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
@Table(name = "term_reports", uniqueConstraints = {
        @UniqueConstraint(name = "uk_term_reports_student_term", columnNames = {"student_id", "term_id"})})
public class TermReport extends BaseEntity {

    @Column(name = "average", nullable = true)
    private Double average;

    @Column(name = "class_position", nullable = true)
    private Integer classPosition;

    @Column(name = "class_size", nullable = true)
    private Integer classSize;

    @Column(name = "form_teacher_remark", nullable = true, columnDefinition = "TEXT")
    private String formTeacherRemark;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id", nullable = false, foreignKey = @ForeignKey(name = "fk_term_reports_student"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    @NotNull
    private Student student;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "term_id", nullable = false, foreignKey = @ForeignKey(name = "fk_term_reports_term"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    @NotNull
    private Term term;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "remarkBy_id", nullable = true, foreignKey = @ForeignKey(name = "fk_term_reports_remarkBy"))
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Staff remarkBy;

}

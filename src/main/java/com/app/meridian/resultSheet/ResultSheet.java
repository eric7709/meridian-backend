package com.app.meridian.resultSheet;

import java.time.LocalDateTime;
import com.app.meridian.common.BaseEntity;
import com.app.meridian.resultSheet.enums.ResultSheetStatus;
import com.app.meridian.staff.Staff;
import com.app.meridian.teachingAssignment.TeachingAssignment;
import com.app.meridian.term.Term;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "result_sheets", uniqueConstraints = {
        @UniqueConstraint(name = "uk_result_sheets_assignment_term", columnNames = {"assignment_id", "term_id"})})
public class ResultSheet extends BaseEntity {

    @NotNull
    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ResultSheetStatus status;

    @Column(name = "submitted_at", nullable = true)
    private LocalDateTime submittedAt;

    @Column(name = "reviewed_at", nullable = true)
    private LocalDateTime reviewedAt;

    @Column(name = "rejection_reason", nullable = true, columnDefinition = "TEXT")
    private String rejectionReason;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assignment_id", nullable = false, foreignKey = @ForeignKey(name = "fk_result_sheets_assignment"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    @NotNull
    private TeachingAssignment assignment;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "term_id", nullable = false, foreignKey = @ForeignKey(name = "fk_result_sheets_term"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    @NotNull
    private Term term;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "submittedBy_id", nullable = true, foreignKey = @ForeignKey(name = "fk_result_sheets_submittedBy"))
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Staff submittedBy;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reviewedBy_id", nullable = true, foreignKey = @ForeignKey(name = "fk_result_sheets_reviewedBy"))
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Staff reviewedBy;

}

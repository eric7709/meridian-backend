package com.app.meridian.term;

import com.app.meridian.academicSession.AcademicSession;
import com.app.meridian.common.BaseEntity;
import com.app.meridian.term.enums.TermName;
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
@Table(name = "terms", uniqueConstraints = {
        @UniqueConstraint(name = "uk_terms_session_name", columnNames = {"session_id", "name"})})
public class Term extends BaseEntity {

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false)
    private TermName name;

    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @NotNull
    @Column(name = "allow_teacher_submission", nullable = false)
    private Boolean allowTeacherSubmission;

    @NotNull
    @Column(name = "allow_student_view", nullable = false)
    private Boolean allowStudentView;

    @NotNull
    @Column(name = "is_locked", nullable = false)
    private Boolean isLocked;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "session_id", nullable = false, foreignKey = @ForeignKey(name = "fk_terms_session"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    @NotNull
    private AcademicSession session;

}

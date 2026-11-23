package com.app.meridian.teachingAssignment;

import com.app.meridian.academicSession.AcademicSession;
import com.app.meridian.classroom.ClassRoom;
import com.app.meridian.common.BaseEntity;
import com.app.meridian.staff.Staff;
import com.app.meridian.subject.Subject;
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
@Table(name = "teaching_assignments", uniqueConstraints = {
        @UniqueConstraint(name = "uk_teaching_assignments_teacher_subject_classRoom_session", columnNames = {"teacher_id", "subject_id", "class_id", "session_id"})})
public class TeachingAssignment extends BaseEntity {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "teacher_id", nullable = false, foreignKey = @ForeignKey(name = "fk_teaching_assignments_teacher"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    @NotNull
    private Staff teacher;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "subject_id", nullable = false, foreignKey = @ForeignKey(name = "fk_teaching_assignments_subject"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    @NotNull
    private Subject subject;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "class_id", nullable = false, foreignKey = @ForeignKey(name = "fk_teaching_assignments_classRoom"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    @NotNull
    private ClassRoom classRoom;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "session_id", nullable = false, foreignKey = @ForeignKey(name = "fk_teaching_assignments_session"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    @NotNull
    private AcademicSession session;

}

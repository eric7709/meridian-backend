package com.app.meridian.enrollment;

import java.time.LocalDate;
import com.app.meridian.academicSession.AcademicSession;
import com.app.meridian.classroom.ClassRoom;
import com.app.meridian.common.BaseEntity;
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
@Table(name = "enrollments", uniqueConstraints = {
        @UniqueConstraint(name = "uk_enrollments_student_session", columnNames = {"student_id", "session_id"})})
public class Enrollment extends BaseEntity {

    @NotNull
    @Column(name = "enrolled_on", nullable = false)
    private LocalDate enrolledOn;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id", nullable = false, foreignKey = @ForeignKey(name = "fk_enrollments_student"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    @NotNull
    private Student student;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "class_id", nullable = false, foreignKey = @ForeignKey(name = "fk_enrollments_classRoom"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    @NotNull
    private ClassRoom classRoom;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "session_id", nullable = false, foreignKey = @ForeignKey(name = "fk_enrollments_session"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    @NotNull
    private AcademicSession session;

}

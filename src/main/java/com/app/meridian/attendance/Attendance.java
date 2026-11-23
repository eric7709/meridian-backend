package com.app.meridian.attendance;

import java.time.LocalDate;
import com.app.meridian.attendance.enums.AttendanceStatus;
import com.app.meridian.common.BaseEntity;
import com.app.meridian.student.Student;
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
@Table(name = "attendance", uniqueConstraints = {
        @UniqueConstraint(name = "uk_attendance_student_date", columnNames = {"student_id", "date"})})
public class Attendance extends BaseEntity {

    @NotNull
    @Column(name = "date", nullable = false)
    private LocalDate date;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AttendanceStatus status;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id", nullable = false, foreignKey = @ForeignKey(name = "fk_attendance_student"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    @NotNull
    private Student student;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "term_id", nullable = false, foreignKey = @ForeignKey(name = "fk_attendance_term"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    @NotNull
    private Term term;

}

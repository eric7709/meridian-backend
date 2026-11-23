package com.app.meridian.student;

import java.time.LocalDate;
import com.app.meridian.classroom.ClassRoom;
import com.app.meridian.common.BaseEntity;
import com.app.meridian.student.enums.Gender;
import com.app.meridian.student.enums.StudentStatus;
import com.app.meridian.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
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
@Table(name = "students")
public class Student extends BaseEntity {

    @Column(name = "student_no", nullable = true, unique = true)
    private String studentNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = true)
    private Gender gender;

    @Column(name = "dob", nullable = true)
    private LocalDate dob;

    @Column(name = "phone", nullable = true)
    private String phone;

    @Column(name = "guardian_name", nullable = true)
    private String guardianName;

    @Column(name = "guardian_phone", nullable = true)
    private String guardianPhone;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StudentStatus status;

    @OneToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_students_user"), unique = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @NotNull
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "currentClass_id", nullable = true, foreignKey = @ForeignKey(name = "fk_students_currentClass"))
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private ClassRoom currentClass;

}

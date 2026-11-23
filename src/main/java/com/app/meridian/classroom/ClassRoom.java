package com.app.meridian.classroom;

import com.app.meridian.academicSession.AcademicSession;
import com.app.meridian.classroom.enums.Level;
import com.app.meridian.common.BaseEntity;
import com.app.meridian.staff.Staff;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "classrooms")
public class ClassRoom extends BaseEntity {

    @NotNull
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false)
    private Level level;

    @Column(name = "arm", nullable = true)
    private String arm;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "formTeacher_id", nullable = true, foreignKey = @ForeignKey(name = "fk_classrooms_formTeacher"))
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Staff formTeacher;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "session_id", nullable = true, foreignKey = @ForeignKey(name = "fk_classrooms_session"))
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private AcademicSession session;

}
